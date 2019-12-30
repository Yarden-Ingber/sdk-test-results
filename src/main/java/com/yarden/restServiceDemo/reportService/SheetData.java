package com.yarden.restServiceDemo.reportService;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.yarden.restServiceDemo.Enums;
import com.yarden.restServiceDemo.Logger;
import com.yarden.restServiceDemo.RestCalls;
import com.yarden.restServiceDemo.pojos.RequestJson;

import java.io.IOException;
import java.util.*;

public class SheetData {

    private List<String> columnNames = null;
    private String sheetTabName;
    private static Map<String, List<String>> columnsNamesMap = new HashMap<>();
    private static Map<String, JsonArray> sheetDataPerTabMap = new HashMap<>();

    public SheetData(String googleSheetTabName){
        this.sheetTabName = googleSheetTabName;
    }

    public JsonArray getSheetData(){
        if (!sheetDataPerTabMap.containsKey(sheetTabName)) {
            try {
                try {
                    List<List<Object>> sheet = SheetDBApiService.getAllSheet(sheetTabName);
                    columnsNamesMap.put(sheetTabName, SheetDBApiService.getKeyList(sheet));
                    sheetDataPerTabMap.put(sheetTabName, SheetDBApiService.listToJsonArray(sheet));
                } catch (Throwable t1) {
                    List<List<Object>> sheet = SheetDBApiService.getAllSheet(sheetTabName);
                    sheetDataPerTabMap.put(sheetTabName, SheetDBApiService.listToJsonArray(sheet));
                }
            } catch (Throwable t) {
                System.out.println("ERROR: failed getting sheet:" + t.getMessage());
            }
        }
        return sheetDataPerTabMap.get(sheetTabName);
    }

    public void writeSheet() throws IOException {
        if (RestCalls.resultsCount.get() == RestCalls.NumOfPostResultsBeforeWriteSheet) {
            this.columnNames = columnsNamesMap.get(sheetTabName);
            SheetDBApiService.updateSheet(this);
        }
    }

    public static void writeAllTabsToSheet() throws IOException {
        Logger.info("Writing all sheets to google");
        for (Enums.GeneralSheetTabsNames tab: Enums.GeneralSheetTabsNames.values()) {
            if (sheetDataPerTabMap.containsKey(tab.value)) {
                SheetData sheetData = new SheetData(tab.value);
                sheetData.columnNames = columnsNamesMap.get(tab.value);
                sheetData.sheetTabName = tab.value;
                SheetDBApiService.updateSheet(sheetData);
            }
        }
        for (Enums.SdkGroupsSheetTabNames tab: Enums.SdkGroupsSheetTabNames.values()) {
            if (sheetDataPerTabMap.containsKey(tab.value)) {
                SheetData sheetData = new SheetData(tab.value);
                sheetData.columnNames = columnsNamesMap.get(tab.value);
                sheetData.sheetTabName = tab.value;
                SheetDBApiService.updateSheet(sheetData);
            }
        }
        clearCachedSheetData();
    }

    public void validateThereIsIdRowOnSheet(RequestJson requestJson){
        for (JsonElement sheetEntry : getSheetData()) {
            if (sheetEntry.getAsJsonObject().get(Enums.SheetColumnNames.TestName.value).getAsString().equals(Enums.SheetColumnNames.IDRow.value)) {
                return;
            }
        }
        System.out.println("There was no ID row");
        JsonElement newEntry = new JsonParser().parse("{\"" + Enums.SheetColumnNames.TestName.value + "\":\"" + Enums.SheetColumnNames.IDRow.value + "\",\"" + requestJson.getSdk() + "\":\"" + requestJson.getId() + "\"}");
        addElementToBeginningOfReportSheet(newEntry);
        System.out.println("Now the cached sheet looks like this: " + getSheetData().toString());
    }

    public void addElementToBeginningOfReportSheet(JsonElement jsonElement){
        JsonArray jsonArray = new JsonArray();
        jsonArray.add(jsonElement);
        for (JsonElement sheetEntry: getSheetData()){
            jsonArray.add(sheetEntry);
        }
        sheetDataPerTabMap.put(sheetTabName, jsonArray);
    }

    public List<String> getColumnNames(){
        return columnNames;
    }

    public String getSheetTabName() {
        return sheetTabName;
    }

    public static void clearCachedSheetData(){
        sheetDataPerTabMap.clear();
        columnsNamesMap.clear();
    }

}
