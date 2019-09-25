package com.yarden.restServiceDemo;

import com.google.gson.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Timestamp;
import java.util.*;

@RestController
public class SdkReportService {

    private RequestJson requestJson;
    private String googleSheetTabName = null;

    @RequestMapping(method = RequestMethod.POST, path = "/result")
    public ResponseEntity postResults(@RequestBody String json) {
        SheetData.clearCachedSheetData();
        googleSheetTabName = null;
        try {
            requestJson = new Gson().fromJson(json, RequestJson.class);
            if (requestJson.getSandbox() != null && requestJson.getSandbox()) {
                googleSheetTabName = SheetTabsNames.Sandbox.value;
            }
        } catch (JsonSyntaxException e) {
            return new ResponseEntity("Failed parsing the json: " + json, HttpStatus.BAD_REQUEST);
        }
        try {
            new RequestJsonValidator(requestJson).validate(googleSheetTabName);
        } catch (JsonParseException e) {
            return new ResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
        try {
            deleteColumnForNewTestId();
            updateSheetWithNewResults();
        } catch (Throwable t) {
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity(json, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.GET, path = "/health")
    public ResponseEntity getHealth(){
        return new ResponseEntity("Up and running!", HttpStatus.OK);
    }

    private void updateSheetWithNewResults(){
        JsonArray resultsArray = requestJson.getResults();
        for (JsonElement result: resultsArray) {
            TestResultData testResult = new Gson().fromJson(result, TestResultData.class);
            String testName = capitalize(testResult.getTestName());
//            Ignore parameters until we fix issue with java parameter names
//            String paramsString = getTestParamsAsString(testResult);
            String paramsString = "";
            updateSingleTestResult(requestJson.getSdk(), testName + paramsString, testResult.getPassed());
        }
        writeEntireSheetData(SheetData.getSheetData(googleSheetTabName));
    }

    private String getTestParamsAsString(TestResultData testResult){
        if (testResult.getParameters() == null) {
            return "";
        }
        Set<Map.Entry<String, JsonElement>> paramsSet = testResult.getParameters().entrySet();
        List<Map.Entry<String, JsonElement>> paramsList = new ArrayList<>(paramsSet);
        Collections.sort(paramsList, new ParamsComperator());
        String paramsString = new String();
        for (Map.Entry<String, JsonElement> param: paramsList) {
            paramsString = paramsString + "(" + capitalize(param.getKey()) + ":" + capitalize(param.getValue().getAsString()) + ") ";
        }
        return paramsString.trim();
    }

    private synchronized void deleteColumnForNewTestId(){
        if (requestJson.getId() == null ||
                !requestJson.getId().equals(getCurrentColumnId(requestJson.getSdk()))){
            SheetData.clearCachedSheetData();
            deleteEntireSdkColumn(requestJson.getSdk());
            updateTestResultId(requestJson.getSdk(), requestJson.getId());
            addHighLevelReportEntry(requestJson.getSdk());
        }
    }

    private synchronized String getCurrentColumnId(String sdk){
        for (JsonElement sheetEntry: SheetData.getSheetData(googleSheetTabName)){
            if (sheetEntry.getAsJsonObject().get(SheetColumnNames.TestName.value).getAsString().equals(SheetColumnNames.IDRow.value)){
                return sheetEntry.getAsJsonObject().get(sdk).getAsString();
            }
        }
        return "";
    }

    private synchronized void updateTestResultId(String sdk, String id){
        for (JsonElement sheetEntry: SheetData.getSheetData(googleSheetTabName)){
            if (sheetEntry.getAsJsonObject().get(SheetColumnNames.TestName.value).getAsString().equals(SheetColumnNames.IDRow.value)){
                sheetEntry.getAsJsonObject().addProperty(sdk, id);
                return;
            }
        }
        return;
    }

    private synchronized void updateSingleTestResult(String sdk, String testName, boolean passed){
        String testResult = passed ? TestResults.Passed.value : TestResults.Failed.value;
        for (JsonElement sheetEntry: SheetData.getSheetData(googleSheetTabName)){
            if (sheetEntry.getAsJsonObject().get(SheetColumnNames.TestName.value).getAsString().equals(testName)){
                if (!sheetEntry.getAsJsonObject().get(sdk).getAsString().equals(TestResults.Failed.value)) {
                    sheetEntry.getAsJsonObject().addProperty(sdk, testResult);
                }
                return;
            }
        }
        SheetData.getSheetData(googleSheetTabName).add(new JsonParser().parse("{\"" + SheetColumnNames.TestName.value + "\":\"" + testName + "\",\"" + sdk + "\":\"" + testResult + "\"}"));
    }

    private synchronized void addHighLevelReportEntry(String sdk){
        SheetData.getHighLevelSheet().add(new JsonParser().parse("{\"" + HighLevelSheetColumnNames.Sdk.value + "\":\"" + sdk + "\",\"" + HighLevelSheetColumnNames.LastRun.value + "\":\"" + new Timestamp(System.currentTimeMillis()) + "\"}"));
        try {
            SheetDBApiService.getService().deleteEntireSheet(SheetTabsNames.HighLevel.value).execute();
            SheetDBApiService.getService().updateSheet(new JsonParser().parse("{\"data\":" + SheetData.getHighLevelSheet().toString() + "}").getAsJsonObject(), SheetTabsNames.HighLevel.value).execute();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private synchronized void deleteEntireSdkColumn(String sdk){
        for (JsonElement sheetEntry: SheetData.getSheetData(googleSheetTabName)){
            sheetEntry.getAsJsonObject().addProperty(sdk, "");
        }
    }

    private synchronized void writeEntireSheetData(JsonArray modifiedSheetData){
        try {
            try {
                SheetDBApiService.getService().deleteEntireSheet(googleSheetTabName).execute();
                SheetDBApiService.getService().updateSheet(new JsonParser().parse("{\"data\":" + modifiedSheetData.toString() + "}").getAsJsonObject(), googleSheetTabName).execute();
            } catch (Throwable t1) {
                SheetDBApiService.getService().deleteEntireSheet(googleSheetTabName).execute();
                SheetDBApiService.getService().updateSheet(new JsonParser().parse("{\"data\":" + modifiedSheetData.toString() + "}").getAsJsonObject(), googleSheetTabName).execute();
            }
        } catch (Throwable t) {
            System.out.println("ERROR: failed to update sheet: " + t.getMessage());
        }
    }

    private static String capitalize(String s) {
        final String ACTIONABLE_DELIMITERS = " '-/_"; // these cause the character following to be capitalized
        StringBuilder sb = new StringBuilder();
        boolean capNext = true;
        for (char c : s.toCharArray()) {
            c = capNext ? Character.toUpperCase(c) : c;
            sb.append(c);
            capNext = (ACTIONABLE_DELIMITERS.indexOf((int) c) >= 0); // explicit cast not needed
        }
        return sb.toString().replaceAll("[ |\\-|_]", "");
    }

    public enum TestResults{
        Passed("1"), Failed("-1");

        String value;

        TestResults(String value){
            this.value = value;
        }
    }

    public enum SheetTabsNames {
        TestData("test_data"), HighLevel("high_level"), Sandbox("sandbox");

        String value;

        SheetTabsNames(String value){
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

    public enum HighLevelSheetColumnNames {
        Sdk("sdk"), LastRun("last_run");

        String value;

        HighLevelSheetColumnNames(String value){
            this.value = value;
        }
    }

    public class ParamsComperator implements Comparator<Map.Entry<String, JsonElement>> {
        @Override
        public int compare(Map.Entry<String, JsonElement> lhs, Map.Entry<String, JsonElement> rhs) {
            if (lhs == rhs)
                return 0;
            if (lhs == null)
                return -1;
            if (rhs == null)
                return 1;
            return capitalize(lhs.getKey()).compareTo(capitalize(rhs.getKey()));
        }
    }

}
