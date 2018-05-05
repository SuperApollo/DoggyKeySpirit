package com.apollo.keyspirit.retrofit;

import com.apollo.keyspirit.constants.Constants;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * 创建REST adapter
 * Created by apollo on 17-3-16.
 */

public class ServiceGenerator {
    public static final String API_BASE_URL = Constants.GITHUB_BASE_URL;
//            Constants.GITHUB_BASE_URL;

    private static OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

    private static Retrofit.Builder builder =
            new Retrofit.Builder()
                    .baseUrl(API_BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create());

    public static <S> S createService(Class<S> serviceClass) {
        Retrofit retrofit = builder.client(httpClient.build()).build();
        return retrofit.create(serviceClass);
    }
}
