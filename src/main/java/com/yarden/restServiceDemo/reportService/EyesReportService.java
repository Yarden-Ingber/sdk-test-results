package com.yarden.restServiceDemo.reportService;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.yarden.restServiceDemo.Enums;
import com.yarden.restServiceDemo.Logger;
import com.yarden.restServiceDemo.pojos.EyesResultRequestJson;
import com.yarden.restServiceDemo.pojos.TestResultData;

import java.util.*;

public class EyesReportService {

    private EyesResultRequestJson eyesResultRequestJson;
    private String googleSheetTabName;
    private SheetData sheetData = null;

    public void postResults(String json) {
        SheetData.incrementResultsCounter();
        eyesResultRequestJson = new Gson().fromJson(json, EyesResultRequestJson.class);
        setGoogleSheetTabName();
        new EyesRequestJsonValidator(eyesResultRequestJson).validate();
        sheetData = new SheetData(new SheetTabIdentifier(Enums.SpreadsheetIDs.Eyes.value, googleSheetTabName));
        try {
            deleteColumnForNewTestId();
            updateSheetWithNewResults();
            validateThereIsIdRowOnSheet(sheetData);
            writeEntireSheetData(sheetData);
        } catch (Throwable t) {
            Logger.error("Something went wrong: " + t.getMessage());
            t.printStackTrace();
            throw new InternalError();
        }
        Logger.info("Test result count is: " + SheetData.resultsCount.get());
        SheetData.resetResultsCounterIfBiggerThankResultsBufferSize();
    }

    private void setGoogleSheetTabName(){
        googleSheetTabName = eyesResultRequestJson.getGroup();
        if (isSandbox()) {
            googleSheetTabName = Enums.EyesSheetTabsNames.Sandbox.value;
        }
        Logger.info("Posting result to sheet: " + googleSheetTabName);
    }

    private boolean isSandbox(){
        return (((eyesResultRequestJson.getSandbox() != null) && eyesResultRequestJson.getSandbox()));
    }

    private void deleteColumnForNewTestId(){
        String currentColumnId = getCurrentColumnId();
        Logger.info("Current id in sheet is: " + currentColumnId);
        Logger.info("New requested id is: " + eyesResultRequestJson.getId());
        if (!eyesResultRequestJson.getId().equals(currentColumnId)){
            Logger.info("Updating id");
            deleteEntireColumn();
            updateTestResultId(eyesResultRequestJson.getId());
        }
    }

    private String getCurrentColumnId(){
        for (JsonElement sheetEntry: sheetData.getSheetData()){
            if (sheetEntry.getAsJsonObject().get(Enums.EyesSheetColumnNames.TestName.value).getAsString().equals(Enums.EyesSheetColumnNames.IDRow.value)){
                return sheetEntry.getAsJsonObject().get(Enums.EyesSheetColumnNames.Status.value).getAsString();
            }
        }
        return "";
    }

    private void deleteEntireColumn(){
        Logger.info("Deleting entire column");
        for (JsonElement sheetEntry: sheetData.getSheetData()){
            sheetEntry.getAsJsonObject().addProperty(Enums.EyesSheetColumnNames.Status.value, "");
            sheetEntry.getAsJsonObject().addProperty(Enums.EyesSheetColumnNames.Url.value, "");
        }
    }

    private void updateTestResultId(String id){
        for (JsonElement sheetEntry: sheetData.getSheetData()){
            if (sheetEntry.getAsJsonObject().get(Enums.EyesSheetColumnNames.TestName.value).getAsString().equals(Enums.EyesSheetColumnNames.IDRow.value)){
                sheetEntry.getAsJsonObject().addProperty(Enums.EyesSheetColumnNames.Status.value, id);
                return;
            }
        }
    }

    private void updateSheetWithNewResults(){
        Logger.info("Updating results in local cached sheet");
        JsonArray resultsArray = eyesResultRequestJson.getResults();
        for (JsonElement result: resultsArray) {
            TestResultData testResult = new Gson().fromJson(result, TestResultData.class);
            String testName = testResult.getTestName();
            String paramsString = getTestParamsAsString(testResult);
            testName = testName + paramsString;
            updateSingleTestResult(testName, testResult.getPassed(), testResult.getResultUrl());
        }
    }

    private String getTestParamsAsString(TestResultData testResult){
        if (testResult.getParameters() == null)
            return "";
        List<Map.Entry<String, JsonElement>> paramsList = new ArrayList<>(testResult.getParameters().entrySet());
        String paramsString = new String();
        for (Map.Entry<String, JsonElement> param: paramsList) {
            paramsString = paramsString + "(" + param.getKey() + ":" + param.getValue().getAsString() + ") ";
        }
        return paramsString.trim();
    }

    private void updateSingleTestResult(String testName, boolean passed, String url){
        String testResult = passed ? Enums.TestResults.Passed.value : Enums.TestResults.Failed.value;
        for (JsonElement sheetEntry: sheetData.getSheetData()){
            if (sheetEntry.getAsJsonObject().get(Enums.EyesSheetColumnNames.TestName.value).getAsString().equals(testName)){
                Logger.info("Adding test result: " + testName + "=" + testResult);
                sheetEntry.getAsJsonObject().addProperty(Enums.EyesSheetColumnNames.Status.value, testResult);
                if (!passed) {
                    sheetEntry.getAsJsonObject().addProperty(Enums.EyesSheetColumnNames.Url.value, url);
                }
                return;
            }
        }
        JsonElement newEntry = new JsonParser().parse("{\"" + Enums.EyesSheetColumnNames.TestName.value + "\":\"" + testName + "\",\"" + Enums.EyesSheetColumnNames.Status.value + "\":\"" + testResult + "\",\"" + Enums.EyesSheetColumnNames.Url.value + "\":\"" + url + "\"}");
        Logger.info("Adding new result entry: " + newEntry.toString() + " to sheet");
        sheetData.getSheetData().add(newEntry);
    }

    public void validateThereIsIdRowOnSheet(SheetData sheetData){
        for (JsonElement sheetEntry : sheetData.getSheetData()) {
            if (sheetEntry.getAsJsonObject().get(Enums.EyesSheetColumnNames.TestName.value).getAsString().equals(Enums.EyesSheetColumnNames.IDRow.value)) {
                return;
            }
        }
        Logger.warn("There was no ID row");
        JsonElement newEntry = new JsonParser().parse("{\"" + Enums.EyesSheetColumnNames.TestName.value + "\":\"" + Enums.EyesSheetColumnNames.IDRow.value + "\",\"" + Enums.EyesSheetColumnNames.Status.value + "\":\"" + eyesResultRequestJson.getId() + "\"}");
        sheetData.addElementToBeginningOfReportSheet(newEntry);
        Logger.info("Now the cached sheet looks like this: " + sheetData.getSheetData().toString());
    }

    private void writeEntireSheetData(SheetData sheetData){
        try {
            int retryCount = 0;
            int maxRetry = 5;
            while (retryCount < maxRetry) {
                try {
                    sheetData.writeSheet();
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
}
