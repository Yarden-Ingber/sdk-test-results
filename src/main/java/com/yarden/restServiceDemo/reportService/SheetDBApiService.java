package com.yarden.restServiceDemo.reportService;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class SheetDBApiService {

    private static Sheets sheetApiService = null;
    private static final List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS);
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";
    private static final String APPLICATION_NAME = "SDK test report";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    public static Sheets getService(){
        if (sheetApiService == null) {
            try {
                try {
                    final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
                    sheetApiService = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                            .setApplicationName(APPLICATION_NAME)
                            .build();
                } catch (Throwable t) {
                    final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
                    sheetApiService = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                            .setApplicationName(APPLICATION_NAME)
                            .build();
                }
            } catch (Throwable t1) {
                t1.printStackTrace();
            }
        }
        return sheetApiService;
    }

    public static JsonArray listToJsonArray(List<List<Object>> sheet) {
        List<String> keys = getKeyList(sheet);
        JsonArray resultJsonArray = new JsonArray();
        int rowIndex = 0;
        for (List<Object> row: sheet){
            if (rowIndex != 0) {
                JsonObject jsonObject = new JsonObject();
                for (int i=0; i<keys.size(); i++){
                    if (i<row.size()){
                        jsonObject.addProperty(keys.get(i), (String)row.get(i));
                    } else {
                        jsonObject.addProperty(keys.get(i), "");
                    }
                }
                resultJsonArray.add(jsonObject);
            }
            rowIndex++;
        }
        return resultJsonArray;
    }

    public static List<String> getKeyList(List<List<Object>> sheet){
        List<Object> keysRow = sheet.get(0);
        List<String> keys = new LinkedList<>();
        for (Object cell: keysRow){
            keys.add((String)cell);
        }
        return keys;
    }

    public static List<List<Object>> jsonArrayToList(JsonArray sheet, List<String> keys) {
        List<List<Object>> resultMatrix = new LinkedList<>(new LinkedList<>());
        resultMatrix.add(new LinkedList<>());
        for (String key: keys){
            resultMatrix.get(0).add(key);
        }
        int rowIndex = 1;
        for (JsonElement jsonElement: sheet){
            resultMatrix.add(new LinkedList<>());
            for (String key: keys){
                try {
                    try {
                        resultMatrix.get(rowIndex).add(jsonElement.getAsJsonObject().get(key).getAsInt());
                    } catch (ClassCastException | NumberFormatException e) {
                        resultMatrix.get(rowIndex).add(jsonElement.getAsJsonObject().get(key).getAsString());
                    }
                } catch (Throwable t) {
                    resultMatrix.get(rowIndex).add("");
                }
            }
            rowIndex++;
        }
        return resultMatrix;
    }

    public static List<List<Object>> getAllSheet(SheetTabIdentifier sheetTabIdentifier) {
        ValueRange response = null;
        try {
            response = getService().spreadsheets().values().get(sheetTabIdentifier.spreadsheetID, sheetTabIdentifier.sheetTabName).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response.getValues();
    }

    public static void updateSheet(SheetData sheetData) throws IOException {
        getService().spreadsheets().values()
                .update(sheetData.getSheetTabIdentifier().spreadsheetID, sheetData.getSheetTabIdentifier().sheetTabName + "!A1:BM2000", new ValueRange().setValues(jsonArrayToList(sheetData.getSheetData(), sheetData.getColumnNames())))
                .setValueInputOption("RAW").execute();
    }

    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        InputStream in = SdkReportService.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

}
