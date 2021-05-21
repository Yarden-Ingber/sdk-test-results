package com.yarden.restServiceDemo.kpis;

import com.mashape.unirest.http.exceptions.UnirestException;
import com.yarden.restServiceDemo.reportService.SdkReportService;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.ui.ModelMap;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class TrelloTicketCreator {

    private static final String sdks = "Java,Java Appium,Python,Ruby,Dotnet,Espresso,XCUI,Earlgrey,PHP,Images,DOM capture,UFT,XCTest,DOM snapshot,Integrations,Storybook,Cypress,Testcafe,JS Selenium 4,JS Selenium 3,WDIO 4,WDIO 5,Protractor,Playwright,Nightwatch,Selenium IDE,JS images,Not relevant";

    public static String getTicketCreationFormHtml() throws IOException, UnirestException {
        InputStream inputStream = SdkReportService.class.getResourceAsStream("/create-ticket-page.html");
        String page = IOUtils.toString(inputStream, StandardCharsets.UTF_8.name());
        page = page.replace("<<<ACCOUNTS>>>", getTrelloAccountsHtmlOptions());
        page = page.replace("<<<SDKS>>>", getSdksHtmlOptions());
        inputStream.close();
        return page;
    }

    private static String getSdksHtmlOptions(){
        StringBuilder stringBuilder = new StringBuilder("\n");
        String[] sdksList = sdks.split(",");
        for (String sdk : sdksList) {
            String option = "<option value=\"" + sdk + "\">" + sdk + "</option>\n";
            stringBuilder.append(option);
        }
        return stringBuilder.toString();
    }

    private static String getTrelloAccountsHtmlOptions() throws UnirestException {
        JSONArray accountsArray = TrelloApi.getTrelloAccountsArray();
        return convertAccountsArrayToHtmlOptions(accountsArray);
    }

    private static String convertAccountsArrayToHtmlOptions(JSONArray accountsArray) {
        StringBuilder stringBuilder = new StringBuilder("\n");
        int arraySize = accountsArray.length();
        for (int i = 0; i < arraySize ; i++) {
            String memberName = (String)((JSONObject)accountsArray.get(i)).get("fullName");
            String memberId = (String)((JSONObject)accountsArray.get(i)).get("id");
            String option = "<option value=\"" + memberName + "," + memberId + "\">" + memberName + "</option>\n";
            stringBuilder.append(option);
        }
        return stringBuilder.toString();
    }

    public static void createTicket(ModelMap ticketFormFields) throws UnirestException {
        String ticketId = TrelloApi.createTicket(ticketFormFields);
        TrelloApi.addMemberToTicket(ticketId, ticketFormFields);
        MultipartFile[] logFiles = ((MultipartFile[])ticketFormFields.get(FormFields.logFiles.name()));
        TrelloApi.uploadFilesToTicket(ticketId, logFiles);
        MultipartFile[] reproducableFiles = ((MultipartFile[])ticketFormFields.get(FormFields.reproducableFiles.name()));
        TrelloApi.uploadFilesToTicket(ticketId, reproducableFiles);
        updateCustomFields(ticketFormFields, ticketId);
    }

    private static void updateCustomFields(ModelMap ticketFormFields, String ticketId) {
        String fieldName = "TESt fiELD";
        String fieldValue = "value";
        TrelloApi.updateCustomFieldValue(ticketFormFields, fieldName, ticketId, fieldValue);
    }

    public enum FormFields {
        accountName, accountID, board, listID, ticketTitle, ticketDescription, customerAppUrl, sdk, sdkVersion, linkToTestResults, logFiles, reproducableFiles
    }

}
