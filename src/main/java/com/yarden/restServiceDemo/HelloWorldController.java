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
    public String helloWorld(@RequestBody String json) {
        JsonObject jsonObject = new JsonParser().parse(json).getAsJsonObject();
        String sdkName = jsonObject.get("sdk").getAsString();
        String id = jsonObject.get("id").getAsString();
        String sandbox = jsonObject.get("sandbox").getAsString();
        JsonObject results = jsonObject.get("results").getAsJsonObject();
        Set<Map.Entry<String, JsonElement>> entrySet = results.entrySet();
        for (Map.Entry<String, JsonElement> entry: entrySet) {
            String testName = entry.getKey();
            JsonObject testResult = entry.getValue().getAsJsonObject();
            JsonObject testParams = testResult.get("parameters").getAsJsonObject();
            Set<Map.Entry<String, JsonElement>> paramsSet = testParams.entrySet();
            String paramsString = new String();
            for (Map.Entry<String, JsonElement> param: paramsSet) {
                paramsString = paramsString + "(" + param.getKey() + ":" + param.getValue() + ") ";
            }
            boolean isPassed = testResult.get("passed").getAsBoolean();
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
