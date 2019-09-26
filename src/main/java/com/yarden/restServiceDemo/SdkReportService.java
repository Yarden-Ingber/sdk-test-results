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
            if (isSandbox()) {
                googleSheetTabName = Enums.SheetTabsNames.Sandbox.value;
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
            SheetData.validateThereIsIdRowOnSheet(googleSheetTabName, requestJson);
            writeEntireSheetData(SheetData.getSheetData(googleSheetTabName), googleSheetTabName, requestJson);
            if (!isSandbox()) {
                updateLocalCachedHighLevelReport();
                writeEntireSheetData(SheetData.getHighLevelSheet(), Enums.SheetTabsNames.HighLevel.value, requestJson);
            }
        } catch (Throwable t) {
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity(json, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.GET, path = "/health")
    public ResponseEntity getHealth(){
        return new ResponseEntity("Up and running!", HttpStatus.OK);
    }

    private void updateLocalCachedHighLevelReport(){
        updateLocalCachedHighLevelSuccessPercentage();
        updateLocalCachedHighLevelTestCount();
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
    }

    private synchronized void updateLocalCachedHighLevelSuccessPercentage(){
        for (JsonElement sheetEntry: SheetData.getHighLevelSheet()) {
            if (sheetEntry.getAsJsonObject().get(Enums.HighLevelSheetColumnNames.Sdk.value).getAsString().equals(requestJson.getSdk()) &&
                    sheetEntry.getAsJsonObject().get(Enums.HighLevelSheetColumnNames.ID.value).getAsString().equals(requestJson.getId())) {
                sheetEntry.getAsJsonObject().addProperty(Enums.HighLevelSheetColumnNames.SuccessPercentage.value, getCurrentRunIdSuccessPercentage());
            }
        }
    }

    private synchronized void updateLocalCachedHighLevelTestCount(){
        for (JsonElement sheetEntry: SheetData.getHighLevelSheet()) {
            if (sheetEntry.getAsJsonObject().get(Enums.HighLevelSheetColumnNames.Sdk.value).getAsString().equals(requestJson.getSdk()) &&
                    sheetEntry.getAsJsonObject().get(Enums.HighLevelSheetColumnNames.ID.value).getAsString().equals(requestJson.getId())) {
                sheetEntry.getAsJsonObject().addProperty(Enums.HighLevelSheetColumnNames.AmountOfTests.value, getTotalAmountOfTests());
            }
        }
    }

    private float getCurrentRunIdSuccessPercentage(){
        int countPassedTests = 0; int countFailedTests = 0;
        for (JsonElement sheetEntry: SheetData.getSheetData(googleSheetTabName)){
            int passedValueInteger = getPermutationResultCountForTestEntry(sheetEntry, Enums.SheetColumnNames.Pass);
            int failedValueInteger = getPermutationResultCountForTestEntry(sheetEntry, Enums.SheetColumnNames.Fail);
            countPassedTests += passedValueInteger;
            countFailedTests += failedValueInteger;
        }
        return (countPassedTests*100)/(countPassedTests+countFailedTests);
    }

    private int getPermutationResultCountForTestEntry(JsonElement sheetEntry, Enums.SheetColumnNames permutationResult){
        JsonElement passedValue = sheetEntry.getAsJsonObject().get(requestJson.getSdk() + permutationResult.value);
        return (passedValue == null || passedValue.getAsString().isEmpty()) ?
                0 :
                sheetEntry.getAsJsonObject().get(requestJson.getSdk() + permutationResult.value).getAsInt();
    }

    private int getTotalAmountOfTests(){
        int totalAmount = 0;
        for (JsonElement sheetEntry: SheetData.getSheetData(googleSheetTabName)){
            int passedValueInteger = getPermutationResultCountForTestEntry(sheetEntry, Enums.SheetColumnNames.Pass);
            int failedValueInteger = getPermutationResultCountForTestEntry(sheetEntry, Enums.SheetColumnNames.Fail);
            totalAmount += passedValueInteger + failedValueInteger;
        }
        return totalAmount;
    }

    private String getTestParamsAsString(TestResultData testResult){
        if (testResult.getParameters() == null)
            return "";
        List<Map.Entry<String, JsonElement>> paramsList = new ArrayList<>(testResult.getParameters().entrySet());
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
            deleteEntireSdkColumn(requestJson.getSdk());
            updateTestResultId(requestJson.getSdk(), requestJson.getId());
            if (!isSandbox()) {
                addLocalCachedHighLevelReportEntry(requestJson.getSdk(), requestJson.getId());
            }
        }
    }

    private synchronized String getCurrentColumnId(String sdk){
        for (JsonElement sheetEntry: SheetData.getSheetData(googleSheetTabName)){
            if (sheetEntry.getAsJsonObject().get(Enums.SheetColumnNames.TestName.value).getAsString().equals(Enums.SheetColumnNames.IDRow.value)){
                return sheetEntry.getAsJsonObject().get(sdk).getAsString();
            }
        }
        return "";
    }

    private synchronized void updateTestResultId(String sdk, String id){
        for (JsonElement sheetEntry: SheetData.getSheetData(googleSheetTabName)){
            if (sheetEntry.getAsJsonObject().get(Enums.SheetColumnNames.TestName.value).getAsString().equals(Enums.SheetColumnNames.IDRow.value)){
                sheetEntry.getAsJsonObject().addProperty(sdk, id);
                return;
            }
        }
        return;
    }

    private synchronized void updateSingleTestResult(String sdk, String testName, boolean passed){
        String testResult = passed ? Enums.TestResults.Passed.value : Enums.TestResults.Failed.value;
        for (JsonElement sheetEntry: SheetData.getSheetData(googleSheetTabName)){
            if (sheetEntry.getAsJsonObject().get(Enums.SheetColumnNames.TestName.value).getAsString().equals(testName)){
                if (!sheetEntry.getAsJsonObject().get(sdk).getAsString().equals(Enums.TestResults.Failed.value)) {
                    sheetEntry.getAsJsonObject().addProperty(sdk, testResult);
                }
                incrementPassFailColumn(sdk, sheetEntry, passed);
                return;
            }
        }
        JsonElement newEntry = new JsonParser().parse("{\"" + Enums.SheetColumnNames.TestName.value + "\":\"" + testName + "\",\"" + sdk + "\":\"" + testResult + "\"}");
        SheetData.getSheetData(googleSheetTabName).add(newEntry);
        incrementPassFailColumn(sdk, newEntry, passed);
    }

    private void incrementPassFailColumn(String sdk, JsonElement sheetEntry, boolean passed){
        if (passed) {
            int valueBeforeIncrementInteger = getPermutationResultCountForTestEntry(sheetEntry, Enums.SheetColumnNames.Pass);
            sheetEntry.getAsJsonObject().addProperty(sdk + Enums.SheetColumnNames.Pass.value, valueBeforeIncrementInteger + 1);
        } else {
            int valueBeforeIncrementInteger = getPermutationResultCountForTestEntry(sheetEntry, Enums.SheetColumnNames.Fail);
            sheetEntry.getAsJsonObject().addProperty(sdk + Enums.SheetColumnNames.Fail.value, valueBeforeIncrementInteger + 1);
        }
    }

    private synchronized void addLocalCachedHighLevelReportEntry(String sdk, String id){
        for (JsonElement sheetEntry: SheetData.getHighLevelSheet()) {
            if (sheetEntry.getAsJsonObject().get(Enums.HighLevelSheetColumnNames.ID.value).getAsString().equals(id) &&
                    sheetEntry.getAsJsonObject().get(Enums.HighLevelSheetColumnNames.Sdk.value).getAsString().equals(sdk)){
                return;
            }
        }
        SheetData.getHighLevelSheet().add(new JsonParser().parse("{\"" + Enums.HighLevelSheetColumnNames.Sdk.value + "\":\"" + sdk + "\"," +
                "\"" + Enums.HighLevelSheetColumnNames.LastRun.value + "\":\"" + new Timestamp(System.currentTimeMillis()) + "\"," +
                "\"" + Enums.HighLevelSheetColumnNames.ID.value + "\":\"" + id + "\"}"));
    }

    private synchronized void deleteEntireSdkColumn(String sdk){
        for (JsonElement sheetEntry: SheetData.getSheetData(googleSheetTabName)){
            sheetEntry.getAsJsonObject().addProperty(sdk, "");
            sheetEntry.getAsJsonObject().addProperty(sdk + Enums.SheetColumnNames.Fail.value, "");
            sheetEntry.getAsJsonObject().addProperty(sdk + Enums.SheetColumnNames.Pass.value, "");
        }
    }

    private static synchronized void writeEntireSheetData(JsonArray modifiedSheetData, String sheetTabName, RequestJson requestJson){
        try {
            int retryCount = 0;
            int maxRetry = 5;
            while (retryCount < maxRetry) {
                try {
                    SheetDBApiService.getService().deleteEntireSheet(sheetTabName).execute();
                    SheetDBApiService.getService().updateSheet(new JsonParser().parse("{\"data\":" + modifiedSheetData.toString() + "}").getAsJsonObject(), sheetTabName).execute();
                    return;
                } catch (Throwable t1) {
                    Thread.sleep(1000 );
                    retryCount++;
                }
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

    private boolean isSandbox(){
        return (requestJson.getSandbox() != null) &&
                (requestJson.getSandbox());
    }

}
