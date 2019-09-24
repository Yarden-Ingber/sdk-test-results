package com.yarden.restServiceDemo;

import com.google.gson.JsonArray;

import java.io.IOException;

public class SheetData {

    private static JsonArray sheetData = null;

    public synchronized static JsonArray getSheetData(){
        if (sheetData == null) {
            try {
                try {
                    sheetData = HelloWorldController.getSheetApiService().getAllSheet().execute().body();
                } catch (Throwable t1) {
                    sheetData = HelloWorldController.getSheetApiService().getAllSheet().execute().body();
                }
            } catch (Throwable t) {
                System.out.println("ERROR: failed getting sheet:" + t.getMessage());
            }
        }
        return sheetData;
    }

    public synchronized static void clearCachedSheetData(){
        sheetData = null;
    }

}
