package org.sysu.nameservice.interactionRouter;


import com.alibaba.fastjson.JSON;
import okhttp3.*;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 先使用单例的模式
 */
public class   OkHttpClientRouter implements IIteractionRouter {

    private static OkHttpClientRouter instance = new OkHttpClientRouter();

    private OkHttpClient okHttpClient;

    private OkHttpClientRouter() {
        okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    public static OkHttpClientRouter getInstance() {
        return instance;
    }

    /** 同步 get */
    public Response syncGet(String url, Map<String, String> headers, Map<String, Object> params) {
        HttpUrl.Builder urlBuilder = HttpUrl.parse(url).newBuilder();
        /** 添加请求参数*/
        if(params != null) {
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                urlBuilder.addQueryParameter(entry.getKey(), (String) entry.getValue());
            }
        }

        Request.Builder requestBuilder = new Request.Builder().url(urlBuilder.build());

        if(headers != null) {
            headers.forEach((key,value) -> {
                requestBuilder.addHeader(key, value);
            });
        }

        try {
            Response response = okHttpClient.newCall(requestBuilder.build()).execute();
            return response;
        }catch (Exception e) {
            return null;
        }
    }

    /** 异步 get */
    public void asyncGet(String url, Map<String, String> headers, Map<String, Object> params, OkHttpCallback callback) {
        HttpUrl.Builder urlBuilder = HttpUrl.parse(url).newBuilder();
        /** 添加请求参数*/
        if(params != null) {
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                urlBuilder.addQueryParameter(entry.getKey(), (String) entry.getValue());
            }
        }

        Request.Builder requestBuilder = new Request.Builder().url(urlBuilder.build());

        if(headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                requestBuilder.addHeader(entry.getKey(), entry.getValue());
            }
        }
        okHttpClient.newCall(requestBuilder.build()).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                throw new RuntimeException(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                callback.call(call, response);
            }
        });
    }

    /** 同步 post */
    public Response syncPost(String url, Map<String,String> headers, Map<String, Object> params) {
        Request.Builder requestBuilder = new Request.Builder();
        requestBuilder.url(url);
        FormBody.Builder formBuilder = new FormBody.Builder();
        if(params != null) {
            params.forEach((key, value) -> {
                formBuilder.add(key, JSON.toJSONString(value));
            });
        }
        requestBuilder.post(formBuilder.build());

        if(headers != null) {
            headers.forEach((key, value) -> {
                requestBuilder.addHeader(key, value);
            });
        }

        try {
            Response response = okHttpClient.newCall(requestBuilder.build()).execute();
            return response;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /** 异步 post */
    public void asyncPost(String url, Map<String,String> headers, Map<String, Object> params, OkHttpCallback callback) {
        Request.Builder requestBuilder = new Request.Builder();
        requestBuilder.url(url);

        FormBody.Builder formBuilder = new FormBody.Builder();
        if(params != null) {
            params.forEach((key, value) -> {
                formBuilder.add(key, JSON.toJSONString(value));
            });
        }
        requestBuilder.post(formBuilder.build());
        if(headers != null) {
            headers.forEach((key, value) -> {
                requestBuilder.addHeader(key, value);
            });
        }
        okHttpClient.newCall(requestBuilder.build()).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                throw new RuntimeException(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                callback.call(call, response);
            }
        });

    }

}
