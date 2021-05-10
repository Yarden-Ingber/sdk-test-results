package com.yarden.restServiceDemo.kpis;

import com.yarden.restServiceDemo.Logger;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;

import java.net.URISyntaxException;
import java.util.List;

public class TrelloTicketCreator {

    public static void create(String formParams) throws URISyntaxException {
        formParams = "http://www.website.com/dummyHead?" + formParams;
        List<NameValuePair> parameters = new URIBuilder(formParams).getQueryParams();
        Logger.info(parameters.toString());
    }

}
