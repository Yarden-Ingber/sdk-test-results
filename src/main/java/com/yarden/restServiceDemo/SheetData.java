package com.yarden.restServiceDemo;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class SheetData {

    private static JsonArray sheetData = null;
    private static JsonArray highLevelSheetData = null;

    public synchronized static JsonArray getSheetData(String googleSheetTabName){
        if (sheetData == null) {
            try {
                try {
                    sheetData = SheetDBApiService.getService().getAllSheet(googleSheetTabName).execute().body();
                } catch (Throwable t1) {
                    sheetData = SheetDBApiService.getService().getAllSheet(googleSheetTabName).execute().body();
                }
            } catch (Throwable t) {
                System.out.println("ERROR: failed getting sheet:" + t.getMessage());
            }
        }
        return sheetData;
    }

    public synchronized static JsonArray getHighLevelSheet(){
        if (highLevelSheetData == null){
            try {
                try {
                    highLevelSheetData = SheetDBApiService.getService().getAllSheet(Enums.SheetTabsNames.HighLevel.value).execute().body();
                } catch (Throwable t1) {
                    highLevelSheetData = SheetDBApiService.getService().getAllSheet(Enums.SheetTabsNames.HighLevel.value).execute().body();
                }
            } catch (Throwable t) {
                System.out.println("ERROR: failed getting sheet:" + t.getMessage());
            }
        }
        return highLevelSheetData;
    }

    public static synchronized void validateThereIsIdRowOnSheet(String googleSheetTabName, RequestJson requestJson){
        for (JsonElement sheetEntry : getSheetData(googleSheetTabName)) {
            if (sheetEntry.getAsJsonObject().get(Enums.SheetColumnNames.TestName.value).getAsString().equals(Enums.SheetColumnNames.IDRow.value)) {
                return;
            }
        }
        System.out.println("There was no ID row");
        JsonElement newEntry = new JsonParser().parse("{\"" + Enums.SheetColumnNames.TestName.value + "\":\"" + Enums.SheetColumnNames.IDRow.value + "\",\"" + requestJson.getSdk() + "\":\"" + requestJson.getId() + "\"}");
        addElementToBeginningOfReportSheet(newEntry);
    }

    public synchronized static void addElementToBeginningOfReportSheet(JsonElement jsonElement){
        JsonArray jsonArray = new JsonArray();
        jsonArray.add(jsonElement);
        for (JsonElement sheetEntry: sheetData){
            jsonArray.add(sheetEntry);
        }
        sheetData = jsonArray;
    }

    public synchronized static void clearCachedSheetData(){
        sheetData = null;
        highLevelSheetData = null;
    }

}
