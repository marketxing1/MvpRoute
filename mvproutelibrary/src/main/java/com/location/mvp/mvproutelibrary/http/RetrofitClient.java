package com.location.mvp.mvproutelibrary.http;

import android.annotation.SuppressLint;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;

import com.location.mvp.mvproutelibrary.error.IResponseErrorMsg;
import com.location.mvp.mvproutelibrary.service.ApiService;
import com.location.mvp.mvproutelibrary.utils.LogUtils;

import java.io.IOException;
import java.lang.reflect.Proxy;
import java.util.List;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * 项目名称: MvpRoute
 * 类描述:
 * 创建人: location
 * 创建时间: 2018/5/13 0013 22:09
 * 修改人:
 * 修改内容:
 * 修改时间:
 */


public class RetrofitClient {
	public static final String HEADER_URL = "header_url";
	public static final String DEFAULT_URL = "default";
	private static final String TAG = "Retrofit";


	private static RetrofitClient instance;
	private Retrofit client;

	private ApiService apiService;

	private IResponseErrorMsg errorResponse;

	private IRefreshToken iRefreshToken;




	/**
	 * 初始化网络
	 *
	 * @param config
	 */
	public static void init(RetrofitConfig config) {
		initRetrofitClient(config);
	}

	public IResponseErrorMsg getErrorResponse() {
		return errorResponse;
	}

	@SuppressLint("NewApi")
	private RetrofitClient(RetrofitConfig config) {
		errorResponse = config.getiResponseErrorMsg();
		iRefreshToken = config.getiRefreshToken();
		if(config.getGsonClass()==null){
			throw new NullPointerException("gsonClazz is null");
		}

		OkHttpClient.Builder builder = config.getBuilder() == null ? new OkHttpClient.Builder() : config.getBuilder();
		builder.addInterceptor(new Interceptor() {
			@Override
			public Response intercept(Chain chain) throws IOException {
				Request request = chain.request();
				HttpUrl oldUrl = request.url();
				Request.Builder newBuilder = request.newBuilder();
				List<String> headers = request.headers(HEADER_URL);
				if (headers != null && !headers.isEmpty() && !DEFAULT_URL.equals(headers.get(0))) {
					newBuilder.removeHeader(HEADER_URL);
					String urlname = headers.get(0);
					HttpUrl httpUrl = HttpUrl.parse(urlname);
					HttpUrl newHttpurl = oldUrl.newBuilder()
							.scheme(httpUrl.scheme())
							.host(httpUrl.host())
							.port(httpUrl.port())
							.build();
					//TODO  需要解决
					String replace = newHttpurl.toString().replace("%2F", "/");
					newBuilder.url(replace);
					return chain.proceed(newBuilder.build());
				} else {
					if (headers != null) newBuilder.removeHeader(HEADER_URL);
					return chain.proceed(newBuilder.build());
				}

			}
		});
		builder.addInterceptor(new Interceptor() {
			@Override
			public Response intercept(Chain chain) throws IOException {
				Request request = chain.request();
				LogUtils.e(TAG, "url===>" + request.url().toString());
				LogUtils.e(TAG, "method===>" + request.method());
				LogUtils.e(TAG, "header===>" + request.headers().toString());
				LogUtils.e(TAG, "response--------------------------------");
				Response proceed = chain.proceed(request);
				okhttp3.MediaType mediaType = proceed.body().contentType();
				String content = proceed.body().string();
				LogUtils.e(TAG, "time" + proceed.sentRequestAtMillis());
				LogUtils.e(TAG, "message" + proceed.message());
				LogUtils.e(TAG, "headers===>" + proceed.headers().toString());
				LogUtils.e(TAG, "body===>" + content);
				return proceed.newBuilder()
						.body(okhttp3.ResponseBody.create(mediaType, content))
						.build();
			}
		});
		client = new Retrofit.Builder()
				.client(builder.build())
				.baseUrl(config.getBaseUrl())
				.addCallAdapterFactory(RxJava2CallAdapterFactory.create())
				.addConverterFactory(com.location.mvp.mvproutelibrary.http.conver.GsonConverterFactory.create(config.getGsonClass()))
				.build();

	}

	public static RetrofitClient getInstance() {
		if (instance == null) {
			throw new RuntimeException("you need initialize RetrofitClient");
		}
		return instance;
	}

	/**
	 * 设置内置Apiservice方法  只需要在app只需要调用一次
	 */
	public void createApiService() {
		apiService = client.create(ApiService.class);
	}


	public @NonNull
	ParamsBuilder get() {
		return new ParamsBuilder(apiService, ParamsBuilder.METHOD_GET);
	}

	private static RetrofitClient initRetrofitClient(RetrofitConfig retrofitConfig) {
		if (instance == null) {
			synchronized (RetrofitClient.class) {
				if (instance == null) {
					instance = new RetrofitClient(retrofitConfig);
				}
			}
		}
		return instance;
	}

	public <T> T createApi(Class<? extends T> clazz) {
		T t = client.create(clazz);
		if(iRefreshToken==null){
			return t;
		}
		return (T) Proxy.newProxyInstance(clazz.getClassLoader(),new Class[]{clazz},new ProxyHandler(t,iRefreshToken));
	}


}
