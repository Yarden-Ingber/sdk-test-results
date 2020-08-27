package com.yarden.restServiceDemo.repoMonitoringService.githubApiService;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class GithubApiRestRequests {

    public static final String GITHUB_API_BASE_URL = "https://api.github.com/";

    public static GithubApiEndpoints getAPIService() {
        return getClient(GITHUB_API_BASE_URL).create(GithubApiEndpoints.class);
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
