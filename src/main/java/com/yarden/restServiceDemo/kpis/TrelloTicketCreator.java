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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class TrelloTicketCreator {

    private static final String sdks = "java,java appium,python,ruby,dotnet,espresso,xcui,earlgrey,php,images,DOM capture,UFT,XCTest,DOM snapshot,Integrations,Storybook,Cypress,Testcafe,JS Selenium 4,JS Selenium 3,WDIO 4,WDIO 5,Protractor,Playwright,Nightwatch,Selenium IDE,JS images,Not relevant";
    private static AtomicReference<Map> ticketUrls = new AtomicReference<>();

    public static String getTicketCreationFormHtml() throws IOException, UnirestException {
        InputStream inputStream = SdkReportService.class.getResourceAsStream("/create-ticket-page.html");
        String page = IOUtils.toString(inputStream, StandardCharsets.UTF_8.name());
        page = page.replace("<<<ACCOUNTS>>>", getTrelloAccountsHtmlOptions());
        page = page.replace("<<<SDKS>>>", getSdksHtmlOptions());
        inputStream.close();
        return page;
    }

    public static String getTrelloTicketUrl(String requestID) {
        return (String)ticketUrls.get().get(requestID);
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
        addTicketDetailsToDescription(ticketFormFields);
        JSONObject createResponse = TrelloApi.createTicket(ticketFormFields);
        if (ticketUrls.get() == null) {
            ticketUrls.set(new HashMap());
        }
        ticketUrls.get().put(ticketFormFields.get(FormFields.requestID.name()), createResponse.getString("shortUrl"));
        String ticketId = createResponse.getString("id");
        TrelloApi.addMemberToTicket(ticketId, ticketFormFields);
        MultipartFile[] logFiles = ((MultipartFile[])ticketFormFields.get(FormFields.logFiles.name()));
        TrelloApi.uploadFilesToTicket(ticketId, logFiles);
        MultipartFile[] reproducibleFiles = ((MultipartFile[])ticketFormFields.get(FormFields.reproducibleFiles.name()));
        TrelloApi.uploadFilesToTicket(ticketId, reproducibleFiles);
        updateCustomFields(ticketFormFields, ticketId);
    }

    private static void addTicketDetailsToDescription(ModelMap ticketFormFields) {
        String ticketDescription = (String)ticketFormFields.get(FormFields.ticketDescription.name());
        if (!((String)ticketFormFields.get(FormFields.customerAppUrl.name())).isEmpty()) {
            ticketDescription = ticketDescription + "\n\nCustomer app url: " + ticketFormFields.get(FormFields.customerAppUrl.name());
        }
        if (!((String)ticketFormFields.get(FormFields.sdk.name())).isEmpty()) {
            ticketDescription = ticketDescription + "\n\nSDK: " + ticketFormFields.get(FormFields.sdk.name());
        }
        if (!((String)ticketFormFields.get(FormFields.sdkVersion.name())).isEmpty()) {
            ticketDescription = ticketDescription + "\n\nSDK version: " + ticketFormFields.get(FormFields.sdkVersion.name());
        }
        if (!((String)ticketFormFields.get(FormFields.linkToTestResults.name())).isEmpty()) {
            ticketDescription = ticketDescription + "\n\nEyes dashboard test results: " + ticketFormFields.get(FormFields.linkToTestResults.name());
        }
        if (!((String)ticketFormFields.get(FormFields.isAppAccessible.name())).isEmpty()) {
            ticketDescription = ticketDescription + "\n\nIs customer app accessible: " + ticketFormFields.get(FormFields.isAppAccessible.name());
        }
        if (!((String)ticketFormFields.get(FormFields.renderID.name())).isEmpty()) {
            ticketDescription = ticketDescription + "\n\nRender ID: " + ticketFormFields.get(FormFields.renderID.name());
        }
        ticketFormFields.addAttribute(TrelloTicketCreator.FormFields.ticketDescription.name(), ticketDescription);
    }

    private static void updateCustomFields(ModelMap ticketFormFields, String ticketId) {
        String fieldName;String fieldValue;
        fieldName = "Affected Versions";
        fieldValue = (String)ticketFormFields.get(FormFields.sdkVersion.name());
        TrelloApi.updateCustomFieldValue(ticketFormFields, fieldName, ticketId, fieldValue);
        fieldName = "SDK";
        fieldValue = (String)ticketFormFields.get(FormFields.sdk.name());
        TrelloApi.updateCustomFieldValue(ticketFormFields, fieldName, ticketId, fieldValue);
        fieldName = "KPI SUB PROJECT";
        fieldValue = (String)ticketFormFields.get(FormFields.sdk.name());
        TrelloApi.updateDropdownCustomFieldValue(ticketFormFields, fieldName, ticketId, fieldValue);
        fieldName = "Created by";
        fieldValue = (String)ticketFormFields.get(FormFields.accountName.name());
        TrelloApi.updateCustomFieldValue(ticketFormFields, fieldName, ticketId, fieldValue);
        fieldName = "Render ID";
        fieldValue = (String)ticketFormFields.get(FormFields.renderID.name());
        TrelloApi.updateCustomFieldValue(ticketFormFields, fieldName, ticketId, fieldValue);
    }

    public enum FormFields {
        accountName, accountID, board, listID, ticketTitle, ticketDescription, customerAppUrl, sdk, sdkVersion, linkToTestResults, logFiles, reproducibleFiles, isAppAccessible, renderID, requestID
    }

}
