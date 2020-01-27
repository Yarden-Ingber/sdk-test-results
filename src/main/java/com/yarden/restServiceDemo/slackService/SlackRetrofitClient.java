package com.yarden.restServiceDemo.slackService;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SlackRetrofitClient {

    public static final String BASE_URL = "https://hooks.slack.com/services/";

    public static SlackEndpoints getAPIService() {
        return getClient(BASE_URL).create(SlackEndpoints.class);
    }

    private static Retrofit retrofit = null;

    public static Retrofit getClient(String baseUrl) {
        if (retrofit==null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

}
