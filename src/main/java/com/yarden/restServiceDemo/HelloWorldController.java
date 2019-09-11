package com.yarden.restServiceDemo;

import com.google.gson.*;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

@RestController
public class HelloWorldController {

    private static final String apiKey = "ux9w3vd819op9";
    private static SheetService sheetApiService = null;

    @RequestMapping(method = RequestMethod.GET, path = "/result")
    public String getRequest(@RequestBody String json) {
        RequestJson requestJson = new Gson().fromJson(json, RequestJson.class);
        Set<Map.Entry<String, JsonElement>> resultsEntrySet = requestJson.getResults().entrySet();
        for (Map.Entry<String, JsonElement> entry: resultsEntrySet) {
            TestResultData testResult = new Gson().fromJson(entry.getValue(), TestResultData.class);
            Set<Map.Entry<String, JsonElement>> paramsSet = testResult.getParameters().entrySet();
            String testName = entry.getKey();
            String paramsString = new String();
            for (Map.Entry<String, JsonElement> param: paramsSet) {
                paramsString = paramsString + "(" + param.getKey() + ":" + param.getValue().getAsString() + ") ";
            }
            paramsString.trim();
        }
        JsonArray sheetData = null;
        try {
            sheetData = getSheetApiService().getAllSheet().execute().body();
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return json;
    }

    private SheetService getSheetApiService(){
        if (sheetApiService == null) {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("https://sheetdb.io/api/v1/" + apiKey + "/")
                    .addConverterFactory(GsonConverterFactory.create(new Gson()))
                    .build();
            sheetApiService = retrofit.create(SheetService.class);
        }
        return sheetApiService;
    }

    public interface SheetService {
        @GET(".")
        Call<JsonArray> getAllSheet();
    }

}
