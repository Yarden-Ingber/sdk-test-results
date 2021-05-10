package com.yarden.restServiceDemo.kpis;

import com.yarden.restServiceDemo.Logger;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TrelloTicketCreator {

    public static void create(String formParams) throws URISyntaxException {
        formParams = "http://www.website.com/head?" + formParams;
        List<NameValuePair> parameters = new URIBuilder(formParams).getQueryParams();
        Logger.info(parameters.toString());
    }

    private static Map urlParamsToMap(String urlParams) {
        Map map = new HashMap();
        String[] paramsList = urlParams.replace("+", " ").split("&");
        for (String param : paramsList) {
            String[] singleParamList = param.split("=");
            map.put(singleParamList[0], singleParamList[1]);
        }
        return map;
    }

}
