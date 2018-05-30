package com.location.mvp.mvproutelibrary.adapter;

import android.support.annotation.CallSuper;
import android.support.annotation.IdRes;
import android.support.annotation.IntRange;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;


import com.location.mvp.mvproutelibrary.R;
import com.location.mvp.mvproutelibrary.utils.LogUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import retrofit2.http.POST;

/**
 * 项目名称: MvpRoute
 * 类描述:  Recycler的基础适配器
 * 创建人: 田晓龙
 * 创建时间: 2018/5/25 0025 23:26
 * 修改人:
 * 修改内容:
 * 修改时间:
 */


public abstract class BaseAdapter<T> extends RecyclerView.Adapter<ViewHolder> {
	protected List<T> data;

	private View emptyView;
	private ArrayList<DataBean> headerList = new ArrayList<>();


	private final int TYPE_EMPTY = -999999999;
	private final int TYPE_HEADER = 2000000000;
	private final int TYPE_FOOTER = 1000000000;
	private final int TYPE_NOMAL = 0;
	/**
	 * 存储子View的点击事件
	 */
	private SparseArray<OnChildListener> listenerSparseArray;

	protected @LayoutRes
	int[] layouts;

	public BaseAdapter(int[] layouts) {
		this.layouts = layouts;
		data = new ArrayList<>();
	}

	public BaseAdapter(Collection<T> data, int[] layouts) {
		this.data = new ArrayList<>(data);
		this.layouts = layouts;
	}

	public BaseAdapter(int layout) {
		this.layouts = new int[]{layout};
		data = new ArrayList<>();
	}

	public BaseAdapter(Collection<T> data, int layout) {
		this.data = new ArrayList<>(data);
		this.layouts = new int[]{layout};
	}


	/**
	 * 添加字view的点击事件
	 *
	 * @param ids
	 * @param listener
	 */

	public void setChildOnClickListener(@IdRes int ids, @NonNull OnChildListener listener) {
		if (listenerSparseArray == null) {
			listenerSparseArray = new SparseArray<>();
		}
		listenerSparseArray.put(ids, listener);
	}

	public BaseAdapter(Collection<T> data, int[] layouts, AbsListView.OnItemClickListener listener) {
		this.data = new ArrayList<>(data);
		this.layouts = layouts;
		this.listener = listener;
	}

	public BaseAdapter(Collection<T> data, int layout, AbsListView.OnItemClickListener listener) {
		this.data = new ArrayList<>(data);
		this.layouts = new int[]{layout};
		this.listener = listener;
	}

	protected AbsListView.OnItemClickListener listener;

	public void setListener(AbsListView.OnItemClickListener listener) {
		this.listener = listener;
	}

	public void setData(Collection<T> data) {
		this.data = new ArrayList<>(data);
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View view = null;
		if (viewType == TYPE_EMPTY) {
			return new ViewHolder(emptyView);
		}
		if (layouts.length > 1) {
			view = LayoutInflater.from(parent.getContext()).inflate(layouts[viewType], parent, false);
		} else {
			view = LayoutInflater.from(parent.getContext()).inflate(layouts[0], parent, false);
		}
		ViewHolder holder = new ViewHolder(view, listener, listenerSparseArray);

		return holder;
	}

	@Override
	public void onBindViewHolder(ViewHolder holder, int position) {
		if (position > data.size() - 1) {
			conver(holder, null, getItemViewType(position));
		} else {
			conver(holder, data.get(position), getItemViewType(position));
		}
	}


	public abstract void conver(ViewHolder holder, T data, int viewType);

	@CallSuper
	@Override
	public int getItemCount() {
		if (data.isEmpty() && data.size() == 0 && emptyView != null) {
			return 1;
		}
		return data.size();
	}

	public <T> void addHeaderView(T t, @LayoutRes int layout) {
		headerList.add(new DataBean<T>(t, TYPE_HEADER, layout));
	}

	/**
	 * 当没有数据时会显示此View
	 *
	 * @param view
	 */
	public void setEmptyView(@NonNull View view) {
		this.emptyView = view;
		RecyclerView.LayoutParams params = new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.MATCH_PARENT);
		this.emptyView.setLayoutParams(params);
	}

	@Override
	public final int getItemViewType(int position) {
		if (data.isEmpty() && data.size() == 0 && emptyView != null) return TYPE_EMPTY;

		if (isHeaderPos(position)) {
			return headerList.get(position).getType();
		}
		return getItemType(position);
	}

	protected int getItemType(int position) {
		return TYPE_NOMAL;
	}

	public final void loadItem(Collection<T> items) {
		int size = data.size();
		data.addAll(items);
		notifyItemRangeInserted(size, items.size());
		compatibilityDataSizeChanged(items.size());
	}


	public final void loadItem(T t) {
		data.add(t);
		notifyItemInserted(data.size() - 1);
		compatibilityDataSizeChanged(1);
	}


	public final void refresh() {
		notifyDataSetChanged();
	}

	public final void refresh(Collection<T> response) {
		data.clear();
		data.addAll(response);
		notifyDataSetChanged();
	}

	public final void remove(@IntRange(from = 0) int position) {
		if (position >= data.size()) return;
		int size = data.size();
		data.remove(position);
		notifyItemRemoved(position);
		compatibilityDataSizeChanged(0);
	}


	private void compatibilityDataSizeChanged(int newDataSize) {
		int size = data.isEmpty() ? 0 : data.size();
		if (size == newDataSize) notifyDataSetChanged();
	}

	public final void clear() {
		data.clear();
		notifyDataSetChanged();
	}


	private boolean isHeaderPos(int position) {
		return getHeaderCount() > position;
	}

	private int getHeaderCount() {
		return headerList.size();
	}

	private boolean isHeaderType(int type) {
		for (DataBean dataBean : headerList) {
			if (dataBean.getType() == type) {
				return true;
			}

		}
		return false;
	}

}
