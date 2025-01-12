package com.libertyflag.tools;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Properties;

/**
* Creates and exposes the liberty Client according with the resource configuration
*/
public class FlagTool {


    public final static HashMap<String, Integer> defaultFlagsValues = new HashMap<String, Integer>();
    public static String flagsControllerEndpoint = "";
    public static String flagsControllerAccessToken = "";
    public static String flagsContextKey = "";
    public static Integer flagsCacheSecondsTimeout = null;
    public static LibertyClient client = null;
    
    static Properties flagValuesProperties = new Properties();
    static Properties flagsControllerProperties = new Properties();
    static {

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        InputStream inputFlags = classLoader.getResourceAsStream("application.flags");
        InputStream inputProperties = classLoader.getResourceAsStream("application.properties");

        try {
            flagValuesProperties.load(inputFlags);
            flagValuesProperties.forEach((key, value) -> {
                defaultFlagsValues.put(key.toString(), Integer.parseInt(value.toString()));
            });       
            
            
            flagsControllerProperties.load(inputProperties);
            flagsControllerProperties.forEach((key, value) -> {
                if (key.equals("flags_controller.endpoint")) {
                    flagsControllerEndpoint = value.toString(); 
                }else if(key.equals("flags_controller.access_token")) {
                    flagsControllerAccessToken = value.toString();
                }else if(key.equals("flags_controller.context_key")) {
                    flagsContextKey = value.toString();
                }else if(key.equals("flags_controller.cache_seconds_timeout")) {
                    flagsCacheSecondsTimeout = Integer.parseInt(value.toString());
                }
            });
            
            client = new LibertyClient(flagsControllerEndpoint,flagsControllerAccessToken, flagsContextKey, flagsCacheSecondsTimeout, defaultFlagsValues);
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
