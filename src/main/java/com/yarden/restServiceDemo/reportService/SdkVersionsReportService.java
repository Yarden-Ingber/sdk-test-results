package com.yarden.restServiceDemo.reportService;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.yarden.restServiceDemo.Enums;
import com.yarden.restServiceDemo.Logger;
import com.yarden.restServiceDemo.pojos.SlackReportNotificationJson;

import java.io.IOException;

public class SdkVersionsReportService {

    private SlackReportNotificationJson requestJson;
    private SheetData sheetData = new SheetData(new SheetTabIdentifier(Enums.SpreadsheetIDs.SdkVersions.value, Enums.SdkVersionsSheetTabsNames.Versions.value));

    public void updateVersion(String json) throws IOException {
        requestJson = new Gson().fromJson(json, SlackReportNotificationJson.class);
        String sdk = requestJson.getSdk();
        String version = requestJson.getVersion();
        version = version.replaceAll("RELEASE_CANDIDATE-", "")
                .replaceAll("RELEASE_CANDIDATE;", "")
                .replaceAll("RELEASE_CANDIDATE", "");
        String[] versionsList = version.split(";");
        if (version.contains("@")) {
            for (String sdkPackageVersion: versionsList) {
                String[] packageCouple = sdkPackageVersion.split("@");
                String sdkPackage = packageCouple[0];
                String packageVersion = packageCouple[1];
                addVersionToSheet(sdk + "@" + sdkPackage, packageVersion);
            }
        } else {
            addVersionToSheet(sdk, version);
        }
        SheetData.writeAllTabsToSheet();
    }

    private void addVersionToSheet(String sdk, String version){
        for (JsonElement sheetEntry: sheetData.getSheetData()){
            if (sheetEntry.getAsJsonObject().get(Enums.SdkVersionsSheetColumnNames.Sdk.value).getAsString().toLowerCase().equals(sdk.toLowerCase())){
                sheetEntry.getAsJsonObject().addProperty(Enums.SdkVersionsSheetColumnNames.Version.value, version);
                return;
            }
        }
        JsonElement newEntry = new JsonParser().parse("{\"" + Enums.SdkVersionsSheetColumnNames.Sdk.value + "\":\"" + sdk + "\",\"" + Enums.SdkVersionsSheetColumnNames.Version.value + "\":\"" + version + "\"}");
        Logger.info("Adding new result entry: " + newEntry.toString() + " to sheet");
        sheetData.getSheetData().add(newEntry);
    }
}
