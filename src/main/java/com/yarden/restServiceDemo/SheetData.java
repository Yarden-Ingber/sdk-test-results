package com.yarden.restServiceDemo;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class SheetData {

    private JsonArray sheetData = null;
    private JsonArray highLevelSheetData = null;

    public JsonArray getSheetData(String googleSheetTabName){
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

    public JsonArray getHighLevelSheet(){
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

    public void validateThereIsIdRowOnSheet(String googleSheetTabName, RequestJson requestJson){
        for (JsonElement sheetEntry : getSheetData(googleSheetTabName)) {
            if (sheetEntry.getAsJsonObject().get(Enums.SheetColumnNames.TestName.value).getAsString().equals(Enums.SheetColumnNames.IDRow.value)) {
                return;
            }
        }
        System.out.println("There was no ID row");
        JsonElement newEntry = new JsonParser().parse("{\"" + Enums.SheetColumnNames.TestName.value + "\":\"" + Enums.SheetColumnNames.IDRow.value + "\",\"" + requestJson.getSdk() + "\":\"" + requestJson.getId() + "\"}");
        addElementToBeginningOfReportSheet(newEntry);
        System.out.println("Now the cached sheet looks like this: " + sheetData.toString());
    }

    public void addElementToBeginningOfReportSheet(JsonElement jsonElement){
        JsonArray jsonArray = new JsonArray();
        jsonArray.add(jsonElement);
        for (JsonElement sheetEntry: sheetData){
            jsonArray.add(sheetEntry);
        }
        sheetData = jsonArray;
    }

    public void clearCachedSheetData(){
        sheetData = null;
        highLevelSheetData = null;
    }

}
