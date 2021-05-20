package com.yarden.restServiceDemo.kpis;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.yarden.restServiceDemo.Enums;
import com.yarden.restServiceDemo.reportService.SdkReportService;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.springframework.ui.ModelMap;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class TrelloTicketCreator {

    private static final String trelloApiKey = Enums.EnvVariables.TrelloApiKey.value;
    private static final String trelloApiToken = Enums.EnvVariables.TrelloApiToken.value;
    private static final String sdks = "Java,Java Appium,Python,Ruby,Dotnet,Espresso,XCUI,Earlgrey,PHP,Images,DOM capture,UFT,XCTest,DOM snapshot,Integrations,Storybook,Cypress,Testcafe,JS Selenium 4,JS Selenium 3,WDIO 4,WDIO 5,Protractor,Playwright,Nightwatch,Selenium IDE,JS images,Not relevant";

    public static String getTicketCreationForm() throws IOException, UnirestException {
        InputStream inputStream = SdkReportService.class.getResourceAsStream("/create-ticket-page.html");
        String page = IOUtils.toString(inputStream, StandardCharsets.UTF_8.name());
        page = page.replace("<<<ACCOUNTS>>>", getTrelloAccounts());
        page = page.replace("<<<SDKS>>>", getSdksList());
        inputStream.close();
        return page;
    }

    public static String getSdksList(){
        StringBuilder stringBuilder = new StringBuilder("\n");
        String[] sdksList = sdks.split(",");
        for (String sdk : sdksList) {
            String option = "<option value=\"" + sdk + "\">" + sdk + "</option>\n";
            stringBuilder.append(option);
        }
        return stringBuilder.toString();
    }

    public static String getTrelloAccounts() throws UnirestException {
        StringBuilder stringBuilder = new StringBuilder("\n");
        HttpResponse<JsonNode> response = Unirest.get("https://api.trello.com/1/organizations/applitools/members")
                .queryString("key", trelloApiKey)
                .queryString("token", trelloApiToken)
                .asJson();
        int arraySize = response.getBody().getArray().length();
        for (int i = 0; i < arraySize ; i++) {
            String memberName = (String)((JSONObject)response.getBody().getArray().get(i)).get("fullName");
            String memberId = (String)((JSONObject)response.getBody().getArray().get(i)).get("id");
            String option = "<option value=\"" + memberName + "," + memberId + "\">" + memberName + "</option>\n";
            stringBuilder.append(option);
        }
        return stringBuilder.toString();
    }

    public static void create(ModelMap modelMap) throws UnirestException, IOException {
        HttpResponse<JsonNode> createTicketResponse = Unirest.post("https://api.trello.com/1/cards")
                .queryString("key", trelloApiKey)
                .queryString("token", trelloApiToken)
                .queryString("idList", modelMap.get(FormFields.listID.name()))
                .queryString("name", modelMap.get(FormFields.ticketTitle.name()))
                .queryString("desc", modelMap.get(FormFields.ticketDescription.name()))
                .asJson();
        JSONObject myObj = createTicketResponse.getBody().getObject();
        String ticketId = myObj.getString("id");

        // add account
        HttpResponse<String> addAccountToTicketResponse = Unirest
                .post("https://api.trello.com/1/cards/" + ticketId + "/idMembers?key=" + trelloApiKey + "&token=" + trelloApiToken + "&value=" +  modelMap.get(FormFields.accountID.name()))
                .asString();

        // upload files
        MultipartFile[] logFiles = ((MultipartFile[])modelMap.get(FormFields.logFiles.name()));
        MultipartFile[] reproducableFiles = ((MultipartFile[])modelMap.get(FormFields.reproducableFiles.name()));
        for (MultipartFile logFile : logFiles) {
            HttpResponse<String> response2 = Unirest.post("https://api.trello.com/1/cards/" + ticketId + "/attachments")
                    .queryString("key", trelloApiKey)
                    .queryString("token", trelloApiToken)
                    .field("file", convertMultipartFileToFile(logFile, logFile.getOriginalFilename())).asString();
        }
        for (MultipartFile reproducableFile : reproducableFiles) {
            HttpResponse<String> response2 = Unirest.post("https://api.trello.com/1/cards/" + ticketId + "/attachments")
                    .queryString("key", trelloApiKey)
                    .queryString("token", trelloApiToken)
                    .field("file", convertMultipartFileToFile(reproducableFile, reproducableFile.getOriginalFilename())).asString();
        }

        String fieldName = "TESt fiELD";
        var fieldValue = "value";
        updateCustomFieldValue((String)modelMap.get(FormFields.board.name()), fieldName, ticketId, fieldValue);
    }

    public static void updateCustomFieldValue(String boardID, String desiredField, String ticketID, String fieldValue) throws UnirestException {
        try {
            String customFieldID = getCustomFieldId(boardID, desiredField);
            HttpResponse<JsonNode> response2 = Unirest.put("https://api.trello.com/1/cards/" + ticketID + "/customField/" + customFieldID + "/item?key=" + trelloApiKey + "&token=" + trelloApiToken)
                    .header("Content-Type", "application/json")
                    .body("{\"value\":{\"text\":\"" + fieldValue + "\"}}")
                    .asJson();
        } catch (Throwable t) {}

    }

    public static String getCustomFieldId(String boardID, String desiredField) throws UnirestException {
        HttpResponse<JsonNode> response = Unirest.get("https://api.trello.com/1/boards/" + boardID + "/customFields?key=" + trelloApiKey + "&token=" + trelloApiToken).asJson();
        int arraySize = response.getBody().getArray().length();
        for (int i = 0; i < arraySize ; i++) {
            String fieldName = (String)((JSONObject)response.getBody().getArray().get(i)).get("name");
            if (fieldName.equalsIgnoreCase(desiredField)) {
                return (String)((JSONObject)response.getBody().getArray().get(i)).get("id");
            }
        }
        return "not found";
    }

    private static File convertMultipartFileToFile(MultipartFile multipartFile, String fileName) {
        File file = new File(System.getProperty("java.io.tmpdir")+"/"+fileName);
        try (OutputStream os = new FileOutputStream(file)) {
            os.write(multipartFile.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }

    public enum FormFields {
        accountName, accountID, board, listID, ticketTitle, ticketDescription, customerAppUrl, sdk, sdkVersion, linkToTestResults, logFiles, reproducableFiles
    }

}
