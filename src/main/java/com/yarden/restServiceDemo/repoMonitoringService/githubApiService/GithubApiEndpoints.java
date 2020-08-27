package com.yarden.restServiceDemo.repoMonitoringService.githubApiService;

import com.yarden.restServiceDemo.repoMonitoringService.githubApiService.pojos.GithubRepoPojo;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

import java.util.List;

public interface GithubApiEndpoints {

    @GET("orgs/Applitools/repos")
    Call<List<GithubRepoPojo>> getRepos(@Query("per_page") int perPage, @Query("page") int page);

}