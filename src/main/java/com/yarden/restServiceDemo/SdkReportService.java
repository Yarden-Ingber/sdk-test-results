package com.yarden.restServiceDemo;

import com.google.gson.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import java.util.*;

@RestController
public class SdkReportService {

    private RequestJson requestJson;
    private String googleSheetTabName = null;
    private SheetData sheetData = null;
    public static final String lock = "LOCK";

    @RequestMapping(method = RequestMethod.POST, path = "/result")
    public ResponseEntity postResults(@RequestBody String json) {
        Logger.info("New result request detected: " + json);
        googleSheetTabName = null;
        try {
            requestJson = new Gson().fromJson(json, RequestJson.class);
            if (isSandbox()) {
                googleSheetTabName = Enums.SheetTabsNames.Sandbox.value;
            }
        } catch (JsonSyntaxException e) {
            return new ResponseEntity("Failed parsing the json: " + json, HttpStatus.BAD_REQUEST);
        }
        synchronized (lock) {
            sheetData = new SheetData();
            try {
                new RequestJsonValidator(requestJson).validate(sheetData, googleSheetTabName);
            } catch (JsonParseException e) {
                return new ResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
            }
            try {
                deleteColumnForNewTestId();
                updateSheetWithNewResults();
                sheetData.validateThereIsIdRowOnSheet(googleSheetTabName, requestJson);
                writeEntireSheetData(sheetData.getSheetData(googleSheetTabName), googleSheetTabName);
                if (!isSandbox()) {
                    updateLocalCachedHighLevelReport();
                    writeEntireSheetData(sheetData.getHighLevelSheet(), Enums.SheetTabsNames.HighLevel.value);
                }
            } catch (Throwable t) {
                return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
            }
            return new ResponseEntity(json, HttpStatus.OK);
        }
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
        Logger.info("Updating results in local cached sheet");
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

    private void updateLocalCachedHighLevelSuccessPercentage(){
        for (JsonElement sheetEntry: sheetData.getHighLevelSheet()) {
            if (sheetEntry.getAsJsonObject().get(Enums.HighLevelSheetColumnNames.Sdk.value).getAsString().equals(requestJson.getSdk()) &&
                    sheetEntry.getAsJsonObject().get(Enums.HighLevelSheetColumnNames.ID.value).getAsString().equals(requestJson.getId())) {
                float calculatedSuccessPercentage = calculateCurrentRunIdSuccessPercentage();
                Logger.info("Updating success percentage for sdk: " + requestJson.getSdk() + " and id: " + requestJson.getId() + " to: " + calculatedSuccessPercentage);
                sheetEntry.getAsJsonObject().addProperty(Enums.HighLevelSheetColumnNames.SuccessPercentage.value, calculatedSuccessPercentage);
            }
        }
    }

    private void updateLocalCachedHighLevelTestCount(){
        for (JsonElement sheetEntry: sheetData.getHighLevelSheet()) {
            if (sheetEntry.getAsJsonObject().get(Enums.HighLevelSheetColumnNames.Sdk.value).getAsString().equals(requestJson.getSdk()) &&
                    sheetEntry.getAsJsonObject().get(Enums.HighLevelSheetColumnNames.ID.value).getAsString().equals(requestJson.getId())) {
                int amountOfTests = getTotalAmountOfTests();
                Logger.info("Updating test count in high level sheet for entry: " + sheetEntry.toString() + " to: " + amountOfTests);
                sheetEntry.getAsJsonObject().addProperty(Enums.HighLevelSheetColumnNames.AmountOfTests.value, amountOfTests);
                sheetEntry.getAsJsonObject().addProperty(Enums.HighLevelSheetColumnNames.LastUpdate.value, Logger.getTimaStamp());
            }
        }
    }

    private float calculateCurrentRunIdSuccessPercentage(){
        int countPassedTests = 0; int countFailedTests = 0;
        for (JsonElement sheetEntry: sheetData.getSheetData(googleSheetTabName)){
            int passedValueInteger = getPermutationResultCountForSingleTestEntry(sheetEntry, Enums.SheetColumnNames.Pass);
            int failedValueInteger = getPermutationResultCountForSingleTestEntry(sheetEntry, Enums.SheetColumnNames.Fail);
            countPassedTests += passedValueInteger;
            countFailedTests += failedValueInteger;
        }
        return (countPassedTests*100)/(countPassedTests+countFailedTests);
    }

    private int getPermutationResultCountForSingleTestEntry(JsonElement sheetEntry, Enums.SheetColumnNames permutationResult){
        JsonElement passedValue = sheetEntry.getAsJsonObject().get(requestJson.getSdk() + permutationResult.value);
        return (passedValue == null || passedValue.getAsString().isEmpty()) ?
                0 :
                sheetEntry.getAsJsonObject().get(requestJson.getSdk() + permutationResult.value).getAsInt();
    }

    private int getTotalAmountOfTests(){
        int totalAmount = 0;
        for (JsonElement sheetEntry: sheetData.getSheetData(googleSheetTabName)){
            int passedValueInteger = getPermutationResultCountForSingleTestEntry(sheetEntry, Enums.SheetColumnNames.Pass);
            int failedValueInteger = getPermutationResultCountForSingleTestEntry(sheetEntry, Enums.SheetColumnNames.Fail);
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

    private void deleteColumnForNewTestId(){
        Logger.info("Current id in sheet for sdk " + requestJson.getSdk() + " is: " + getCurrentColumnId(requestJson.getSdk()));
        Logger.info("New requested id for sdk " + requestJson.getSdk() + " is: " + requestJson.getId());
        if (requestJson.getId() == null ||
                !requestJson.getId().equals(getCurrentColumnId(requestJson.getSdk()))){
            Logger.info("Updating id for sdk: " + requestJson.getSdk());
            deleteEntireSdkColumn(requestJson.getSdk());
            updateTestResultId(requestJson.getSdk(), requestJson.getId());
            if (!isSandbox()) {
                addLocalCachedHighLevelReportEntry(requestJson.getSdk(), requestJson.getId());
            }
        }
    }

    private String getCurrentColumnId(String sdk){
        for (JsonElement sheetEntry: sheetData.getSheetData(googleSheetTabName)){
            if (sheetEntry.getAsJsonObject().get(Enums.SheetColumnNames.TestName.value).getAsString().equals(Enums.SheetColumnNames.IDRow.value)){
                return sheetEntry.getAsJsonObject().get(sdk).getAsString();
            }
        }
        return "";
    }

    private void updateTestResultId(String sdk, String id){
        for (JsonElement sheetEntry: sheetData.getSheetData(googleSheetTabName)){
            if (sheetEntry.getAsJsonObject().get(Enums.SheetColumnNames.TestName.value).getAsString().equals(Enums.SheetColumnNames.IDRow.value)){
                sheetEntry.getAsJsonObject().addProperty(sdk, id);
                return;
            }
        }
    }

    private void updateSingleTestResult(String sdk, String testName, boolean passed){
        String testResult = passed ? Enums.TestResults.Passed.value : Enums.TestResults.Failed.value;
        for (JsonElement sheetEntry: sheetData.getSheetData(googleSheetTabName)){
            if (sheetEntry.getAsJsonObject().get(Enums.SheetColumnNames.TestName.value).getAsString().equals(testName)){
                if (!sheetEntry.getAsJsonObject().get(sdk).getAsString().equals(Enums.TestResults.Failed.value)) {
                    Logger.info("Adding test result for sdk: " + sdk + ", " + testName + "=" + testResult);
                    sheetEntry.getAsJsonObject().addProperty(sdk, testResult);
                }
                incrementPassFailColumn(sdk, sheetEntry, passed);
                return;
            }
        }
        JsonElement newEntry = new JsonParser().parse("{\"" + Enums.SheetColumnNames.TestName.value + "\":\"" + testName + "\",\"" + sdk + "\":\"" + testResult + "\"}");
        Logger.info("Adding new result entry: " + newEntry.toString() + " to sheet");
        sheetData.getSheetData(googleSheetTabName).add(newEntry);
        incrementPassFailColumn(sdk, newEntry, passed);
    }

    private void incrementPassFailColumn(String sdk, JsonElement sheetEntry, boolean passed){
        if (passed) {
            String passedColumn = sdk + Enums.SheetColumnNames.Pass.value;
            Logger.info("Adding 1 to " + passedColumn + " for test " + sheetEntry.getAsJsonObject().get(Enums.SheetColumnNames.TestName.value));
            int valueBeforeIncrementInteger = getPermutationResultCountForSingleTestEntry(sheetEntry, Enums.SheetColumnNames.Pass);
            sheetEntry.getAsJsonObject().addProperty(passedColumn, valueBeforeIncrementInteger + 1);
        } else {
            String failedColumn = sdk + Enums.SheetColumnNames.Fail.value;
            Logger.info("Adding 1 to " + failedColumn + " for test " + sheetEntry.getAsJsonObject().get(Enums.SheetColumnNames.TestName.value));
            int valueBeforeIncrementInteger = getPermutationResultCountForSingleTestEntry(sheetEntry, Enums.SheetColumnNames.Fail);
            sheetEntry.getAsJsonObject().addProperty(failedColumn, valueBeforeIncrementInteger + 1);
        }
    }

    private void addLocalCachedHighLevelReportEntry(String sdk, String id){
        for (JsonElement sheetEntry: sheetData.getHighLevelSheet()) {
            if (sheetEntry.getAsJsonObject().get(Enums.HighLevelSheetColumnNames.ID.value).getAsString().equals(id) &&
                    sheetEntry.getAsJsonObject().get(Enums.HighLevelSheetColumnNames.Sdk.value).getAsString().equals(sdk)){
                return;
            }
        }
        JsonElement newEntry = new JsonParser().parse("{\"" + Enums.HighLevelSheetColumnNames.Sdk.value + "\":\"" + sdk + "\"," +
                "\"" + Enums.HighLevelSheetColumnNames.StartTimestamp.value + "\":\"" + Logger.getTimaStamp() + "\"," +
                "\"" + Enums.HighLevelSheetColumnNames.ID.value + "\":\"" + id + "\"}");
        Logger.info("Adding new entry to high level sheet: " + newEntry.toString());
        sheetData.getHighLevelSheet().add(newEntry);
    }

    private void deleteEntireSdkColumn(String sdk){
        Logger.info("Deleting entire column for sdk: " + sdk);
        for (JsonElement sheetEntry: sheetData.getSheetData(googleSheetTabName)){
            sheetEntry.getAsJsonObject().addProperty(sdk, "");
            sheetEntry.getAsJsonObject().addProperty(sdk + Enums.SheetColumnNames.Fail.value, "");
            sheetEntry.getAsJsonObject().addProperty(sdk + Enums.SheetColumnNames.Pass.value, "");
        }
    }

    private void writeEntireSheetData(JsonArray modifiedSheetData, String sheetTabName){
        try {
            int retryCount = 0;
            int maxRetry = 5;
            while (retryCount < maxRetry) {
                try {
                    Logger.info("Writing local cached sheet to google sheet: " + modifiedSheetData);
                    SheetDBApiService.getService().deleteEntireSheet(sheetTabName).execute();
                    SheetDBApiService.getService().updateSheet(new JsonParser().parse("{\"data\":" + modifiedSheetData.toString() + "}").getAsJsonObject(), sheetTabName).execute();
                    return;
                } catch (Throwable t1) {
                    Logger.warn("Failed writing sheet. retrying...");
                    Thread.sleep(1000 );
                    retryCount++;
                }
            }
        } catch (Throwable t) {
            Logger.error("Failed to update sheet: " + t.getMessage());
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
