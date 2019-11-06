package com.yarden.restServiceDemo;

import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class Logger {

    public static void info(String msg){
        System.out.println(getTimaStamp() + " == INFO: " + msg);
    }

    public static void warn(String msg){
        System.out.println(getTimaStamp() + " == WARNING: " + msg);
    }

    public static void error(String msg){
        System.out.println(getTimaStamp() + " == ERROR: " + msg);
    }

    public static String getTimaStamp(){
        ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneId.of("GMT+2"));
        return Timestamp.valueOf(zonedDateTime.toLocalDateTime()).toString();
    }
}
