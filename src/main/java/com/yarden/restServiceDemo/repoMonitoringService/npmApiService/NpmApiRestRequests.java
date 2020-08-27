package com.yarden.restServiceDemo.repoMonitoringService.npmApiService;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class NpmApiRestRequests {

    public static final String GITHUB_API_BASE_URL = "http://registry.npmjs.com/";

    public static NpmApiEndpoints getAPIService() {
        return getClient(GITHUB_API_BASE_URL).create(NpmApiEndpoints.class);
    }

    private static Retrofit retrofit = null;

    private static Retrofit getClient(String baseUrl) {
        if (retrofit==null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

}
