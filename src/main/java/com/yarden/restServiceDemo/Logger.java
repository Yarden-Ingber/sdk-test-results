package com.yarden.restServiceDemo;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

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

    public static Date timestampToDate(String timestamp) throws ParseException {
        String pattern = "yyyy-MM-dd hh:mm:ss";
        timestamp = timestamp.substring(0,pattern.length());
        SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);
        return dateFormat.parse(timestamp);
    }
}
