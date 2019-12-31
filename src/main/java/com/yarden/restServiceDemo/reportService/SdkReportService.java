package com.yarden.restServiceDemo.reportService;

import com.google.gson.*;
import com.yarden.restServiceDemo.Enums;
import com.yarden.restServiceDemo.Logger;
import com.yarden.restServiceDemo.pojos.*;
import java.util.*;

public class SdkReportService {

    private RequestJson requestJson;
    private ExtraDataRequestJson extraDataRequestJson;
    private String googleSheetTabName;
    private SheetData sheetData = null;

    public void postResults(String json) throws JsonSyntaxException, InternalError{
        SheetData.incrementResultsCounter();
        requestJson = new Gson().fromJson(json, RequestJson.class);
        if (requestJson.getGroup() == null || requestJson.getGroup().isEmpty()){
            throw new JsonSyntaxException("Missing group parameter in json");
        }
        requestJson.setGroup(capitalize(requestJson.getGroup()));
        googleSheetTabName = requestJson.getGroup();
        if (isSandbox()) {
            googleSheetTabName = Enums.GeneralSheetTabsNames.Sandbox.value;
        }
        sheetData = new SheetData(googleSheetTabName);
        new RequestJsonValidator(requestJson).validate(sheetData);
        try {
            deleteColumnForNewTestId();
            updateSheetWithNewResults(false);
            sheetData.validateThereIsIdRowOnSheet(requestJson);
            writeEntireSheetData(sheetData);
        } catch (Throwable t) {
            throw new InternalError();
        }
        postResultToRawData();
        Logger.info("Test result count is: " + SheetData.resultsCount.get());
        SheetData.resetResultsCounter();
    }

    private void postResultToRawData(){
        sheetData = new SheetData(Enums.GeneralSheetTabsNames.RawData.value);
        try {
            deleteColumnForNewTestId();
            updateSheetWithNewResults(true);
            sheetData.validateThereIsIdRowOnSheet(requestJson);
            writeEntireSheetData(sheetData);
        } catch (Throwable t) {
            throw new InternalError();
        }
    }

    public void postExtraTestData(String json) throws JsonSyntaxException, InternalError{
        googleSheetTabName = Enums.GeneralSheetTabsNames.Sandbox.value;
        extraDataRequestJson = new Gson().fromJson(json, ExtraDataRequestJson.class);
        sheetData = new SheetData(googleSheetTabName);
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

    private void updateSheetWithNewResults(boolean shouldAddTestParams){
        Logger.info("Updating results in local cached sheet");
        JsonArray resultsArray = requestJson.getResults();
        for (JsonElement result: resultsArray) {
            TestResultData testResult = new Gson().fromJson(result, TestResultData.class);
            String testName = capitalize(testResult.getTestName());
            String paramsString = "";
            if (shouldAddTestParams) {
                paramsString = getTestParamsAsString(testResult);
                testName = testName + paramsString;
            }
            updateSingleTestResult(requestJson.getSdk(), testName, testResult.getPassed());
            if (shouldAddTestParams && requestJson.getMandatory() && isAllowedToUpdateMandatory()) {
                markTestAsMandatory(testName);
            }
        }
    }

    private int getPermutationResultCountForSingleTestEntry(JsonElement sheetEntry, Enums.SheetColumnNames permutationResult){
        JsonElement passedValue = sheetEntry.getAsJsonObject().get(requestJson.getSdk() + permutationResult.value);
        return (passedValue == null || passedValue.getAsString().isEmpty()) ?
                0 :
                sheetEntry.getAsJsonObject().get(requestJson.getSdk() + permutationResult.value).getAsInt();
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
        }
    }

    private String getCurrentColumnId(String sdk){
        for (JsonElement sheetEntry: sheetData.getSheetData()){
            if (sheetEntry.getAsJsonObject().get(Enums.SheetColumnNames.TestName.value).getAsString().equals(Enums.SheetColumnNames.IDRow.value)){
                return sheetEntry.getAsJsonObject().get(sdk).getAsString();
            }
        }
        return "";
    }

    private void updateTestResultId(String sdk, String id){
        for (JsonElement sheetEntry: sheetData.getSheetData()){
            if (sheetEntry.getAsJsonObject().get(Enums.SheetColumnNames.TestName.value).getAsString().equals(Enums.SheetColumnNames.IDRow.value)){
                sheetEntry.getAsJsonObject().addProperty(sdk, id);
                return;
            }
        }
    }

    private void addExtraDataToSingleTestInSandbox(String sdk, String testName, String extraData){
        for (JsonElement sheetEntry: sheetData.getSheetData()){
            if (sheetEntry.getAsJsonObject().get(Enums.SheetColumnNames.TestName.value).getAsString().equals(testName)){
                sheetEntry.getAsJsonObject().addProperty(sdk + Enums.SheetColumnNames.ExtraData.value, extraData);
                return;
            }
        }
        JsonElement newEntry = new JsonParser().parse("{\"" + Enums.SheetColumnNames.TestName.value + "\":\"" + testName + "\",\"" + sdk + Enums.SheetColumnNames.ExtraData.value + "\":\"" + extraData + "\"}");
        Logger.info("Adding new result entry: " + newEntry.toString() + " to sheet");
        sheetData.getSheetData().add(newEntry);
    }

    private void updateSingleTestResult(String sdk, String testName, boolean passed){
        String testResult = passed ? Enums.TestResults.Passed.value : Enums.TestResults.Failed.value;
        for (JsonElement sheetEntry: sheetData.getSheetData()){
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
        sheetData.getSheetData().add(newEntry);
        incrementPassFailColumn(sdk, newEntry, passed);
    }

    private void markTestAsMandatory(String testName){
        for (JsonElement sheetEntry: sheetData.getSheetData()){
            if (sheetEntry.getAsJsonObject().get(Enums.SheetColumnNames.TestName.value).getAsString().equals(testName) && isAllowedToUpdateMandatory()){
                Logger.info("Marking test: " + testName + " as mandatory");
                sheetEntry.getAsJsonObject().addProperty(Enums.SheetColumnNames.Mandatory.value, Enums.MandatoryTest.Mandatory.value);
                return;
            }
        }
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

    private void deleteEntireSdkColumn(String sdk){
        Logger.info("Deleting entire column for sdk: " + sdk);
        for (JsonElement sheetEntry: sheetData.getSheetData()){
            sheetEntry.getAsJsonObject().addProperty(sdk, "");
            sheetEntry.getAsJsonObject().addProperty(sdk + Enums.SheetColumnNames.Fail.value, "");
            sheetEntry.getAsJsonObject().addProperty(sdk + Enums.SheetColumnNames.Pass.value, "");
            if (isSandbox()) {
                sheetEntry.getAsJsonObject().addProperty(sdk + Enums.SheetColumnNames.ExtraData.value, "");
            }
            if (requestJson.getMandatory() && isAllowedToUpdateMandatory()) {
                try {
                    sheetEntry.getAsJsonObject().addProperty(Enums.SheetColumnNames.Mandatory.value, "");
                } catch (Exception e) {}
            }
        }
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

    private boolean isSandbox(){
        return (((requestJson.getSandbox() != null) && requestJson.getSandbox())
                || isTestedLocally());
    }

    private boolean isAllowedToUpdateMandatory(){
        return requestJson.getSdk().toLowerCase().equals("dotnet");
    }

    private boolean isTestedLocally(){
        return requestJson.getId().equals("0000-0000");
    }

}
