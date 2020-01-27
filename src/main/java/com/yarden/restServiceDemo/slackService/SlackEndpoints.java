package com.yarden.restServiceDemo.slackService;

import com.yarden.restServiceDemo.pojos.SdkReportMessage;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Url;

public interface SlackEndpoints {

    @POST
    Call<ResponseBody> sendReport(@Url String url, @Body SdkReportMessage body);

}
