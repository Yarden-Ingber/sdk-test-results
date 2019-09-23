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

import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;
import java.util.Set;

@RestController
public class HelloWorldController {

    private static final String apiKey = "ux9w3vd819op9";
    private static SheetService sheetApiService = null;

    @RequestMapping(method = RequestMethod.POST, path = "/result")
    public String getRequest(@RequestBody String json) {
        SheetData.clearCachedSheetData();
        RequestJson requestJson = new Gson().fromJson(json, RequestJson.class);
        deleteColumnForNewTestId(requestJson);
        JsonArray resultsArray = requestJson.getResults();
        for (JsonElement result: resultsArray) {
            TestResultData testResult = new Gson().fromJson(result, TestResultData.class);
            String testName = capitalize(testResult.getTestName());
            String paramsString = getTestParamsAsString(testResult);
            updateSingleTestResult(requestJson.getSdk(), testName + paramsString, testResult.getPassed());
        }
        writeEntireSheetData(SheetData.getSheetData());
        return json;
    }

    private String getTestParamsAsString(TestResultData testResult){
        if (testResult.getParameters() == null) {
            return "";
        }
        Set<Map.Entry<String, JsonElement>> paramsSet = testResult.getParameters().entrySet();
        String paramsString = new String();
        for (Map.Entry<String, JsonElement> param: paramsSet) {
            paramsString = paramsString + "(" + capitalize(param.getKey()) + ":" + capitalize(param.getValue().getAsString()) + ") ";
        }
        return paramsString.trim();
    }

    private synchronized void deleteColumnForNewTestId(RequestJson requestJson){
        if (requestJson.getId() == null ||
                !requestJson.getId().equals(getColumnId(requestJson.getSdk()))){
            deleteEntireSdkColumn(requestJson.getSdk());
            updateTestResultId(requestJson.getSdk(), requestJson.getId());
        }
    }

    private synchronized String getColumnId(String sdk){
        for (JsonElement sheetEntry: SheetData.getSheetData()){
            if (sheetEntry.getAsJsonObject().get(SheetColumnNames.TestName.value).getAsString().equals(SheetColumnNames.IDRow.value)){
                return sheetEntry.getAsJsonObject().get(sdk).getAsString();
            }
        }
        return "";
    }

    private synchronized void updateTestResultId(String sdk, String id){
        for (JsonElement sheetEntry: SheetData.getSheetData()){
            if (sheetEntry.getAsJsonObject().get(SheetColumnNames.TestName.value).getAsString().equals(SheetColumnNames.IDRow.value)){
                sheetEntry.getAsJsonObject().addProperty(sdk, id);
                return;
            }
        }
        return;
    }

    private synchronized void updateSingleTestResult(String sdk, String testName, boolean passed){
        String testResult = passed ? TestResults.Passed.value : TestResults.Failed.value;
        for (JsonElement sheetEntry: SheetData.getSheetData()){
            if (sheetEntry.getAsJsonObject().get(SheetColumnNames.TestName.value).getAsString().equals(testName)){
                if (!sheetEntry.getAsJsonObject().get(sdk).getAsString().equals(TestResults.Failed.value)) {
                    sheetEntry.getAsJsonObject().addProperty(sdk, testResult);
                }
                return;
            }
        }
        SheetData.getSheetData().add(new JsonParser().parse("{\"" + SheetColumnNames.TestName.value + "\":\"" + testName + "\",\"" + sdk + "\":\"" + testResult + "\"}"));
    }

    private synchronized void deleteEntireSdkColumn(String sdk){
        for (JsonElement sheetEntry: SheetData.getSheetData()){
            sheetEntry.getAsJsonObject().addProperty(sdk, "");
        }
    }

    private synchronized void writeEntireSheetData(JsonArray modifiedSheetData){
        try {
            try {
                getSheetApiService().deleteEntireSheet().execute();
                getSheetApiService().updateSheet(new JsonParser().parse("{\"data\":" + modifiedSheetData.toString() + "}").getAsJsonObject()).execute();
            } catch (Throwable t1) {
                getSheetApiService().deleteEntireSheet().execute();
                getSheetApiService().updateSheet(new JsonParser().parse("{\"data\":" + modifiedSheetData.toString() + "}").getAsJsonObject()).execute();
            }
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

    private static String capitalize(String s) {

        final String ACTIONABLE_DELIMITERS = " '-/_"; // these cause the character following
        // to be capitalized

        StringBuilder sb = new StringBuilder();
        boolean capNext = true;

        for (char c : s.toCharArray()) {
            c = (capNext)
                    ? Character.toUpperCase(c)
                    : c;
            sb.append(c);
            capNext = (ACTIONABLE_DELIMITERS.indexOf((int) c) >= 0); // explicit cast not needed
        }
        return sb.toString().replace(" ", "")
                .replace("_", "")
                .replace("-", "")
                .replace(".", "");
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

    public enum SheetColumnNames {
        TestName("test_name"), IDRow("id");

        String value;

        SheetColumnNames(String value){
            this.value = value;
        }
    }

}
