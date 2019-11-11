package com.yarden.restServiceDemo;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.yarden.restServiceDemo.pojos.RequestJson;

import java.util.List;

public class SheetData {

    private JsonArray sheetData = null;
    private List<String> columnNames = null;
    private String sheetTabName;

    public SheetData(String googleSheetTabName){
        this.sheetTabName = googleSheetTabName;
    }

    public JsonArray getSheetData(){
        if (sheetData == null) {
            try {
                try {
                    List<List<Object>> sheet = SheetDBApiService.getAllSheet(sheetTabName);
                    columnNames = SheetDBApiService.getKeyList(sheet);
                    sheetData = SheetDBApiService.listToJsonArray(sheet);
                } catch (Throwable t1) {
                    List<List<Object>> sheet = SheetDBApiService.getAllSheet(sheetTabName);
                    columnNames = SheetDBApiService.getKeyList(sheet);
                    sheetData = SheetDBApiService.listToJsonArray(sheet);
                }
            } catch (Throwable t) {
                System.out.println("ERROR: failed getting sheet:" + t.getMessage());
            }
        }
        return sheetData;
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

    public List<String> getColumnNames(){
        return columnNames;
    }

    public String getSheetTabName() {
        return sheetTabName;
    }

    public void clearCachedSheetData(){
        sheetData = null;
    }

}
