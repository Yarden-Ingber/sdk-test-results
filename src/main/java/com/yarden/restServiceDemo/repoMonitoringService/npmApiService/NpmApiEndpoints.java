package com.yarden.restServiceDemo.repoMonitoringService.npmApiService;

import com.yarden.restServiceDemo.repoMonitoringService.npmApiService.pojos.NpmPackagesListPojo;
import com.yarden.restServiceDemo.repoMonitoringService.npmApiService.pojos.NpmPackageData;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface NpmApiEndpoints {

    @GET("-/v1/search")
    Call<NpmPackagesListPojo> getRepos(@Query("text") String text, @Query("size") int size);

    @GET("-/package/{packageName}/dist-tags")
    Call<NpmPackageData> getPackageData(@Path("packageName") String packageName);

}
