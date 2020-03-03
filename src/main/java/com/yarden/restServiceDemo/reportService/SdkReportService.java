package com.yarden.restServiceDemo.reportService;

import com.google.gson.*;
import com.yarden.restServiceDemo.Enums;
import com.yarden.restServiceDemo.Logger;
import com.yarden.restServiceDemo.pojos.*;
import java.util.*;

public class SdkReportService {

    private SdkResultRequestJson sdkResultRequestJson;
    private ExtraDataRequestJson extraDataRequestJson;
    private String googleSheetTabName;
    private SheetData sheetData = null;

    public void postResults(String json) throws JsonSyntaxException, InternalError{
        SheetData.incrementResultsCounter();
        sdkResultRequestJson = new Gson().fromJson(json, SdkResultRequestJson.class);
        if (sdkResultRequestJson.getGroup() == null || sdkResultRequestJson.getGroup().isEmpty()){
            throw new JsonSyntaxException("Missing group parameter in json");
        }
        sdkResultRequestJson.setGroup(capitalize(sdkResultRequestJson.getGroup()));
        setGoogleSheetTabName();
        sheetData = new SheetData(new SheetTabIdentifier(Enums.SpreadsheetIDs.SDK.value, googleSheetTabName));
        new SdkRequestJsonValidator(sdkResultRequestJson).validate(sheetData);
        try {
            deleteColumnForNewTestId();
            updateSheetWithNewResults(false);
            validateThereIsIdRowOnSheet(sheetData);
            writeEntireSheetData(sheetData);
        } catch (Throwable t) {
            System.out.println("Something went wrong: " + t.getMessage());
            t.printStackTrace();
            throw new InternalError();
        }
        postResultToRawData();
        Logger.info("Test result count is: " + SheetData.resultsCount.get());
        SheetData.resetResultsCounterIfBiggerThankResultsBufferSize();
    }

    private void postResultToRawData(){
        sheetData = new SheetData(new SheetTabIdentifier(Enums.SpreadsheetIDs.SDK.value, Enums.SdkGeneralSheetTabsNames.RawData.value));
        try {
            deleteColumnForNewTestId();
            updateSheetWithNewResults(true);
            validateThereIsIdRowOnSheet(sheetData);
            writeEntireSheetData(sheetData);
        } catch (Throwable t) {
            throw new InternalError();
        }
    }

    public void postExtraTestData(String json) throws JsonSyntaxException, InternalError{
        googleSheetTabName = Enums.SdkGeneralSheetTabsNames.Sandbox.value;
        extraDataRequestJson = new Gson().fromJson(json, ExtraDataRequestJson.class);
        sheetData = new SheetData(new SheetTabIdentifier(Enums.SpreadsheetIDs.SDK.value, googleSheetTabName));
        try {
            updateSheetWithExtraTestData();
            writeEntireSheetData(sheetData);
        } catch (Throwable t) {
            throw new InternalError();
        }
    }

    private void updateSheetWithExtraTestData(){
        JsonArray extraDataArray = extraDataRequestJson.getExtraData();
        for (JsonElement testData: extraDataArray){
            ExtraDataPojo extraDataPojo = new Gson().fromJson(testData, ExtraDataPojo.class);
            String testName = capitalize(extraDataPojo.getTestName());
            addExtraDataToSingleTestInSandbox(extraDataRequestJson.getSdk(), testName, extraDataPojo.getData());
        }
    }

    private void updateSheetWithNewResults(boolean shouldAddTestParamsToTestName){
        Logger.info("Updating results in local cached sheet");
        JsonArray resultsArray = sdkResultRequestJson.getResults();
        for (JsonElement result: resultsArray) {
            TestResultData testResult = new Gson().fromJson(result, TestResultData.class);
            String testName = capitalize(testResult.getTestName());
            String paramsString = "";
            if (shouldAddTestParamsToTestName) {
                paramsString = getTestParamsAsString(testResult);
                testName = testName + paramsString;
            }
            updateSingleTestResult(sdkResultRequestJson.getSdk(), testName, testResult.getPassed());
            if (shouldAddTestParamsToTestName && sdkResultRequestJson.getMandatory() && isAllowedToUpdateMandatory()) {
                markTestAsMandatory(testName);
            }
        }
    }

    private int getPermutationResultCountForSingleTestEntry(JsonElement sheetEntry, Enums.SdkSheetColumnNames permutationResult){
        JsonElement passedValue = sheetEntry.getAsJsonObject().get(sdkResultRequestJson.getSdk() + permutationResult.value);
        return (passedValue == null || passedValue.getAsString().isEmpty()) ?
                0 :
                sheetEntry.getAsJsonObject().get(sdkResultRequestJson.getSdk() + permutationResult.value).getAsInt();
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
        String currentColumnId = getCurrentColumnId(sdkResultRequestJson.getSdk());
        Logger.info("Current id in sheet for sdk " + sdkResultRequestJson.getSdk() + " is: " + currentColumnId);
        Logger.info("New requested id for sdk " + sdkResultRequestJson.getSdk() + " is: " + sdkResultRequestJson.getId());
        if (!sdkResultRequestJson.getId().equals(currentColumnId)){
            Logger.info("Updating id for sdk: " + sdkResultRequestJson.getSdk());
            deleteEntireSdkColumn(sdkResultRequestJson.getSdk());
            updateTestResultId(sdkResultRequestJson.getSdk(), sdkResultRequestJson.getId());
        }
    }

    private String getCurrentColumnId(String sdk){
        for (JsonElement sheetEntry: sheetData.getSheetData()){
            if (sheetEntry.getAsJsonObject().get(Enums.SdkSheetColumnNames.TestName.value).getAsString().equals(Enums.SdkSheetColumnNames.IDRow.value)){
                return sheetEntry.getAsJsonObject().get(sdk).getAsString();
            }
        }
        return "";
    }

    private void updateTestResultId(String sdk, String id){
        for (JsonElement sheetEntry: sheetData.getSheetData()){
            if (sheetEntry.getAsJsonObject().get(Enums.SdkSheetColumnNames.TestName.value).getAsString().equals(Enums.SdkSheetColumnNames.IDRow.value)){
                sheetEntry.getAsJsonObject().addProperty(sdk, id);
                return;
            }
        }
    }

    private void addExtraDataToSingleTestInSandbox(String sdk, String testName, String extraData){
        Logger.info("Adding extra data to test " + testName + " on sdk " + sdk + ": " + extraData);
        for (JsonElement sheetEntry: sheetData.getSheetData()){
            if (sheetEntry.getAsJsonObject().get(Enums.SdkSheetColumnNames.TestName.value).getAsString().equals(testName)){
                sheetEntry.getAsJsonObject().addProperty(sdk + Enums.SdkSheetColumnNames.ExtraData.value, extraData);
                return;
            }
        }
        JsonElement newEntry = new JsonParser().parse("{\"" + Enums.SdkSheetColumnNames.TestName.value + "\":\"" + testName + "\",\"" + sdk + Enums.SdkSheetColumnNames.ExtraData.value + "\":\"" + extraData + "\"}");
        Logger.info("Adding new result entry: " + newEntry.toString() + " to sheet");
        sheetData.getSheetData().add(newEntry);
    }

    private void updateSingleTestResult(String sdk, String testName, boolean passed){
        String testResult = passed ? Enums.TestResults.Passed.value : Enums.TestResults.Failed.value;
        for (JsonElement sheetEntry: sheetData.getSheetData()){
            if (sheetEntry.getAsJsonObject().get(Enums.SdkSheetColumnNames.TestName.value).getAsString().equals(testName)){
                if (!sheetEntry.getAsJsonObject().get(sdk).getAsString().equals(Enums.TestResults.Failed.value)) {
                    Logger.info("Adding test result for sdk: " + sdk + ", " + testName + "=" + testResult);
                    sheetEntry.getAsJsonObject().addProperty(sdk, testResult);
                }
                incrementPassFailColumn(sdk, sheetEntry, passed);
                return;
            }
        }
        JsonElement newEntry = new JsonParser().parse("{\"" + Enums.SdkSheetColumnNames.TestName.value + "\":\"" + testName + "\",\"" + sdk + "\":\"" + testResult + "\"}");
        Logger.info("Adding new result entry: " + newEntry.toString() + " to sheet");
        sheetData.getSheetData().add(newEntry);
        incrementPassFailColumn(sdk, newEntry, passed);
    }

    private void markTestAsMandatory(String testName){
        for (JsonElement sheetEntry: sheetData.getSheetData()){
            if (sheetEntry.getAsJsonObject().get(Enums.SdkSheetColumnNames.TestName.value).getAsString().equals(testName) && isAllowedToUpdateMandatory()){
                Logger.info("Marking test: " + testName + " as mandatory");
                sheetEntry.getAsJsonObject().addProperty(Enums.SdkSheetColumnNames.Mandatory.value, Enums.MandatoryTest.Mandatory.value);
                return;
            }
        }
    }

    private void incrementPassFailColumn(String sdk, JsonElement sheetEntry, boolean passed){
        if (passed) {
            String passedColumn = sdk + Enums.SdkSheetColumnNames.Pass.value;
            Logger.info("Adding 1 to " + passedColumn + " for test " + sheetEntry.getAsJsonObject().get(Enums.SdkSheetColumnNames.TestName.value));
            int valueBeforeIncrementInteger = getPermutationResultCountForSingleTestEntry(sheetEntry, Enums.SdkSheetColumnNames.Pass);
            sheetEntry.getAsJsonObject().addProperty(passedColumn, valueBeforeIncrementInteger + 1);
        } else {
            String failedColumn = sdk + Enums.SdkSheetColumnNames.Fail.value;
            Logger.info("Adding 1 to " + failedColumn + " for test " + sheetEntry.getAsJsonObject().get(Enums.SdkSheetColumnNames.TestName.value));
            int valueBeforeIncrementInteger = getPermutationResultCountForSingleTestEntry(sheetEntry, Enums.SdkSheetColumnNames.Fail);
            sheetEntry.getAsJsonObject().addProperty(failedColumn, valueBeforeIncrementInteger + 1);
        }
    }

    private void deleteEntireSdkColumn(String sdk){
        Logger.info("Deleting entire column for sdk: " + sdk);
        for (JsonElement sheetEntry: sheetData.getSheetData()){
            sheetEntry.getAsJsonObject().addProperty(sdk, "");
            sheetEntry.getAsJsonObject().addProperty(sdk + Enums.SdkSheetColumnNames.Fail.value, "");
            sheetEntry.getAsJsonObject().addProperty(sdk + Enums.SdkSheetColumnNames.Pass.value, "");
            if (isSandbox()) {
                sheetEntry.getAsJsonObject().addProperty(sdk + Enums.SdkSheetColumnNames.ExtraData.value, "");
            }
            if (sdkResultRequestJson.getMandatory() && isAllowedToUpdateMandatory()) {
                try {
                    sheetEntry.getAsJsonObject().addProperty(Enums.SdkSheetColumnNames.Mandatory.value, "");
                } catch (Exception e) {}
            }
        }
    }

    public void validateThereIsIdRowOnSheet(SheetData sheetData){
        for (JsonElement sheetEntry : sheetData.getSheetData()) {
            if (sheetEntry.getAsJsonObject().get(Enums.SdkSheetColumnNames.TestName.value).getAsString().equals(Enums.SdkSheetColumnNames.IDRow.value)) {
                return;
            }
        }
        System.out.println("There was no ID row");
        JsonElement newEntry = new JsonParser().parse("{\"" + Enums.SdkSheetColumnNames.TestName.value + "\":\"" + Enums.SdkSheetColumnNames.IDRow.value + "\",\"" + sdkResultRequestJson.getSdk() + "\":\"" + sdkResultRequestJson.getId() + "\"}");
        sheetData.addElementToBeginningOfReportSheet(newEntry);
        System.out.println("Now the cached sheet looks like this: " + sheetData.getSheetData().toString());
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

    private void setGoogleSheetTabName(){
        googleSheetTabName = sdkResultRequestJson.getGroup();
        if (isSandbox()) {
            googleSheetTabName = Enums.SdkGeneralSheetTabsNames.Sandbox.value;
        }
        Logger.info("Posting result to sheet: " + googleSheetTabName);
    }

    private boolean isSandbox(){
        return (((sdkResultRequestJson.getSandbox() != null) && sdkResultRequestJson.getSandbox())
                || isTestedLocally());
    }

    private boolean isAllowedToUpdateMandatory(){
        return sdkResultRequestJson.getSdk().toLowerCase().equals("dotnet");
    }

    private boolean isTestedLocally(){
        return sdkResultRequestJson.getId().equals("0000-0000");
    }

}
