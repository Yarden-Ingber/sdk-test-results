package com.yarden.restServiceDemo;

import com.google.gson.*;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;

import java.util.Map;
import java.util.Set;

@RestController
public class HelloWorldController {

    private static final String apiKey = "ux9w3vd819op9";
    private static SheetService sheetApiService = null;

    @RequestMapping(method = RequestMethod.GET, path = "/result")
    public String getRequest(@RequestBody String json) {
        RequestJson requestJson = new Gson().fromJson(json, RequestJson.class);
        if (requestJson.getId() == null){
            deleteEntireSdkColumn(requestJson.getSdk());
        }
        Set<Map.Entry<String, JsonElement>> resultsEntrySet = requestJson.getResults().entrySet();
        for (Map.Entry<String, JsonElement> entry: resultsEntrySet) {
            TestResultData testResult = new Gson().fromJson(entry.getValue(), TestResultData.class);
            Set<Map.Entry<String, JsonElement>> paramsSet = testResult.getParameters().entrySet();
            String testName = entry.getKey();
            String paramsString = new String();
            for (Map.Entry<String, JsonElement> param: paramsSet) {
                paramsString = paramsString + "(" + param.getKey() + ":" + param.getValue().getAsString() + ") ";
            }
            paramsString = paramsString.trim();
            updateSingleTestResult(requestJson.getSdk(), testName + paramsString, testResult.getPassed());
        }
        writeEntireSheetData(SheetData.getSheetData());
        return json;
    }

    private void updateSingleTestResult(String sdk, String testName, boolean passed){
        String testResult = passed ? TestResults.Passed.value : TestResults.Failed.value;
        for (JsonElement sheetEntry: SheetData.getSheetData()){
            if (sheetEntry.getAsJsonObject().get("test_name").getAsString().equals(testName)){
                sheetEntry.getAsJsonObject().addProperty(sdk, testResult);
                return;
            }
        }
        SheetData.getSheetData().add(new JsonParser().parse("{\"test_name\":\"" + testName + "\",\"" + sdk + "\":\"" + testResult + "\"}"));
    }

    private void deleteEntireSdkColumn(String sdk){
        for (JsonElement sheetEntry: SheetData.getSheetData()){
            sheetEntry.getAsJsonObject().addProperty(sdk, "");
        }
    }

    private void writeEntireSheetData(JsonArray modifiedSheetData){
        try {
            getSheetApiService().deleteEntireSheet().execute();
            getSheetApiService().updateSheet(new JsonParser().parse("{\"data\":" + modifiedSheetData.toString() + "}").getAsJsonObject()).execute();
        } catch (Throwable t) {
            System.out.println("ERROR: failed to update sheet: " + t.getMessage());
        }
    }

    public static SheetService getSheetApiService(){
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

        @POST(".")
        Call<JsonObject>  updateSheet(@Body JsonObject jsonObject);

        @DELETE("all")
        Call<JsonObject> deleteEntireSheet();
    }

    public enum TestResults{
        Passed("1"), Failed("-1");

        String value;

        TestResults(String value){
            this.value = value;
        }
    }

}
