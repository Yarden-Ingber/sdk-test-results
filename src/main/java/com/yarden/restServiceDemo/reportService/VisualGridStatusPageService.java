package com.yarden.restServiceDemo.reportService;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.yarden.restServiceDemo.Enums;
import com.yarden.restServiceDemo.Logger;
import com.yarden.restServiceDemo.pojos.VisualGridStatus;
import com.yarden.restServiceDemo.pojos.VisualGridStatusPageRequestJson;

import java.io.IOException;

public class VisualGridStatusPageService {

    VisualGridStatusPageRequestJson visualGridStatusPageRequestJson;
    SheetData sheetData;
    private static final int NumOfResultsToShow = 10;

    public void postResults(String json) throws IOException {
        visualGridStatusPageRequestJson = new Gson().fromJson(json, VisualGridStatusPageRequestJson.class);
        sheetData = new SheetData(new SheetTabIdentifier(Enums.SpreadsheetIDs.VG.value, Enums.VGSheetTabsNames.Status.value));
        if (getNumOfStatusLines() >= NumOfResultsToShow) {
            sheetData.deleteLastRowInSheet();
        }
        sheetData.addElementToBeginningOfReportSheet(parseRequestJsonToStatusEntry());
        SheetData.writeAllTabsToSheet();
    }

    private int getNumOfStatusLines(){
        return sheetData.getSheetData().size();
    }

    private JsonElement parseRequestJsonToStatusEntry() {
        String newEntryJsonString = "\"" + Enums.VGSheetColumnNames.Timestamp.value + "\":\"" + Logger.getTimaStamp() + "\",";
        for (String column : sheetData.getColumnNames()) {
            if (!column.equals(Enums.VGSheetColumnNames.Timestamp.value)) {
                newEntryJsonString = newEntryJsonString + "\"" + column + "\":\"" + getSystemStatus(column) + "\",";
            }
        }
        newEntryJsonString = "{" + newEntryJsonString.substring(0, newEntryJsonString.length() - 1) + "}";
        return new JsonParser().parse(newEntryJsonString);
    }

    private String getSystemStatus(String system) {
        for (VisualGridStatus visualGridStatus : visualGridStatusPageRequestJson.getStatus()) {
            if (visualGridStatus.getSystem().equals(system)) {
                if (visualGridStatus.getStatus()) {
                    return Enums.TestResults.Passed.value;
                } else {
                    return Enums.TestResults.Failed.value;
                }
            }
        }
        Logger.info("Column " + system + " has no result in this request");
        return Enums.TestResults.Missing.value;
    }

}
