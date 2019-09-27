package com.yarden.restServiceDemo;

import java.sql.Timestamp;

public class Logger {

    public static void info(String msg){
        System.out.println(new Timestamp(System.currentTimeMillis()) + " == INFO: " + msg);
    }

    public static void warn(String msg){
        System.out.println(new Timestamp(System.currentTimeMillis()) + " == WARNING: " + msg);
    }

    public static void error(String msg){
        System.out.println(new Timestamp(System.currentTimeMillis()) + " == ERROR: " + msg);
    }
}
