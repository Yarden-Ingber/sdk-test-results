package com.yarden.restServiceDemo.kpis;

import com.yarden.restServiceDemo.Logger;
import com.yarden.restServiceDemo.reportService.SdkReportService;
import org.apache.commons.io.IOUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class TrelloTicketCreator {

    public static String getTicketCreationForm() throws IOException {
        InputStream inputStream = SdkReportService.class.getResourceAsStream("/create-ticket-page.html");
        String page = IOUtils.toString(inputStream, StandardCharsets.UTF_8.name());
        inputStream.close();
        return page;
    }

    public static void create(String formParams) throws URISyntaxException {
        formParams = "http://www.website.com/dummyHead?" + formParams;
        List<NameValuePair> parameters = new URIBuilder(formParams).getQueryParams();
        Logger.info(parameters.toString());
    }

    public static String getSdksList(){
        return "Java,Java Appium,Python,Ruby,Dotnet,Espresso,XCUI,Earlgrey,PHP,Images,DOM capture,UFT,XCTest,DOM snapshot,Integrations,Storybook,Cypress,Testcafe,JS Selenium 4,JS Selenium 3,WDIO 4,WDIO 5,Protractor,Playwright,Nightwatch,Selenium IDE,JS images,Not relevant";
    }

}
