package com.yarden.restServiceDemo.kpis;

import com.yarden.restServiceDemo.Logger;

import java.util.HashMap;
import java.util.Map;

public class TrelloTicketCreator {

    public static void create(String formParams) {
        Logger.info(urlParamsToMap(formParams).toString());
    }

    private static Map urlParamsToMap(String urlParams) {
        Map map = new HashMap();
        String[] paramsList = urlParams.split("&");
        for (String param : paramsList) {
            String[] singleParamList = param.split("=");
            map.put(singleParamList[0], singleParamList[1]);
        }
        return map;
    }

}
