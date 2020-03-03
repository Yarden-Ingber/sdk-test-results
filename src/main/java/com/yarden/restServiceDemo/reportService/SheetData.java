package com.yarden.restServiceDemo.reportService;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.yarden.restServiceDemo.Enums;
import com.yarden.restServiceDemo.Logger;
import com.yarden.restServiceDemo.pojos.SdkResultRequestJson;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class SheetData {

    private List<String> columnNames = null;
    private SheetTabIdentifier sheetTabIdentifier;
    private static Map<SheetTabIdentifier, List<String>> columnsNamesMap = new HashMap<>();
    private static Map<SheetTabIdentifier, JsonArray> sheetDataPerTabMap = new HashMap<>();

    public SheetData(SheetTabIdentifier sheetTabIdentifier){
        this.sheetTabIdentifier = sheetTabIdentifier;
    }

    public JsonArray getSheetData(){
        if (!sheetDataPerTabMap.containsKey(sheetTabIdentifier)) {
            try {
                try {
                    List<List<Object>> sheet = SheetDBApiService.getAllSheet(sheetTabIdentifier);
                    columnsNamesMap.put(sheetTabIdentifier, SheetDBApiService.getKeyList(sheet));
                    sheetDataPerTabMap.put(sheetTabIdentifier, SheetDBApiService.listToJsonArray(sheet));
                } catch (Throwable t1) {
                    List<List<Object>> sheet = SheetDBApiService.getAllSheet(sheetTabIdentifier);
                    sheetDataPerTabMap.put(sheetTabIdentifier, SheetDBApiService.listToJsonArray(sheet));
                }
            } catch (Throwable t) {
                System.out.println("ERROR: failed getting sheet:" + t.getMessage());
            }
        }
        return sheetDataPerTabMap.get(sheetTabIdentifier);
    }

    public void writeSheet() throws IOException {
        if (resultsCount.get() >= PostResultsBufferSize) {
            writeAllTabsToSheet();
        }
    }

    public static void writeAllTabsToSheet() throws IOException {
        Logger.info("Writing all sheets to google");
        for (Enums.SdkGeneralSheetTabsNames tab: Enums.SdkGeneralSheetTabsNames.values()) {
            SheetTabIdentifier sheetTabIdentifier = new SheetTabIdentifier(Enums.SpreadsheetIDs.SDK.value, tab.value);
            writeSpecificSheetTab(sheetTabIdentifier);
        }
        for (Enums.SdkGroupsSheetTabNames tab: Enums.SdkGroupsSheetTabNames.values()) {
            SheetTabIdentifier sheetTabIdentifier = new SheetTabIdentifier(Enums.SpreadsheetIDs.SDK.value, tab.value);
            writeSpecificSheetTab(sheetTabIdentifier);
        }
        for (Enums.EyesSheetTabsNames tab: Enums.EyesSheetTabsNames.values()) {
            SheetTabIdentifier sheetTabIdentifier = new SheetTabIdentifier(Enums.SpreadsheetIDs.Eyes.value, tab.value);
            writeSpecificSheetTab(sheetTabIdentifier);
        }
        clearCachedSheetData();
    }

    private static void writeSpecificSheetTab(SheetTabIdentifier sheetTabIdentifier) throws IOException {
        if (sheetDataPerTabMap.containsKey(sheetTabIdentifier)) {
            SheetData sheetData = new SheetData(sheetTabIdentifier);
            sheetData.columnNames = columnsNamesMap.get(sheetTabIdentifier);
            sheetData.sheetTabIdentifier = sheetTabIdentifier;
            SheetDBApiService.updateSheet(sheetData);
        }
    }

    public void addElementToBeginningOfReportSheet(JsonElement jsonElement){
        JsonArray jsonArray = new JsonArray();
        jsonArray.add(jsonElement);
        for (JsonElement sheetEntry: getSheetData()){
            jsonArray.add(sheetEntry);
        }
        sheetDataPerTabMap.put(sheetTabIdentifier, jsonArray);
    }

    public List<String> getColumnNames(){
        return columnNames;
    }

    public SheetTabIdentifier getSheetTabIdentifier(){
        return this.sheetTabIdentifier;
    }

    public static void clearCachedSheetData(){
        Logger.info("Clearing cached sheet maps");
        sheetDataPerTabMap.clear();
        columnsNamesMap.clear();
        resultsCount.set(0);
    }

    public static void incrementResultsCounter(){
        try {
            resultsCount.set(resultsCount.get() + 1);
        } catch (Throwable t) {
            resultsCount.set(1);
        }
    }

    public static void resetResultsCounterIfBiggerThankResultsBufferSize(){
        try {
            if (resultsCount.get() >= PostResultsBufferSize) {
                SheetData.clearCachedSheetData();
                resultsCount.set(0);
            }
        } catch (Throwable t) {
            resultsCount.set(0);
        }
    }

    public static final int PostResultsBufferSize = 10;
    public static AtomicReference<Integer> resultsCount = new AtomicReference<>();

}
