package com.yarden.restServiceDemo;

import com.google.gson.JsonArray;

public class SheetData {

    private static JsonArray sheetData = null;

    public static JsonArray getSheetData(){
        if (sheetData == null) {
            try {
                sheetData = HelloWorldController.getSheetApiService().getAllSheet().execute().body();
            } catch (Throwable t) {
                System.out.println("ERROR: failed getting sheet:" + t.getMessage());
            }
        }
        return sheetData;
    }

}
