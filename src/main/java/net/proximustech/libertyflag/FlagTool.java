package net.proximustech.libertyflag;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Properties;

/**
* Creates and exposes the Liberty Flag Client according with the application.properties in the resources directory.
*/
public class FlagTool {

    private static String flagsControllerEndpoint = "";
    private static String flagsControllerAccessToken = "";
    private static String flagsContextKey = "";
    private static String flagsClientId= "";

    private static Integer flagsCacheSecondsTimeout = 60;
    private static boolean verboseErrorLog = true;
    private static boolean sendDataPulse = false;

    public final static HashMap<String, String> defaultFlagsValues = new HashMap<String, String>();
    public static LibertyClient client = null;
    
    static Properties flagValuesProperties = new Properties();
    static Properties flagsControllerProperties = new Properties();
    static {

        LoggerTool logger = new LoggerTool();

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        InputStream inputFlags = classLoader.getResourceAsStream("application.flags");
        InputStream inputProperties = classLoader.getResourceAsStream("application.properties");

        try {
            flagValuesProperties.load(inputFlags);
            flagValuesProperties.forEach((key, value) -> {
                defaultFlagsValues.put(key.toString(),value.toString());
            });       
            
            flagsControllerProperties.load(inputProperties);
            flagsControllerProperties.forEach((key, value) -> {
                if (key.equals("flags_controller.endpoint")) {
                    flagsControllerEndpoint = value.toString(); 
                }else if(key.equals("flags_controller.access_token")) {
                    flagsControllerAccessToken = value.toString();
                }else if(key.equals("flags_controller.context_key")) {
                    flagsContextKey = value.toString();
                }else if(key.equals("flags_controller.client_id")) {
                    flagsClientId = value.toString();
                }else if(key.equals("flags_controller.cache_seconds_timeout")) {
                    flagsCacheSecondsTimeout = Integer.parseInt(value.toString());
                }else if(key.equals("flags_controller.verbose_error_log")) {
                    verboseErrorLog = Boolean.parseBoolean(value.toString());
                }else if(key.equals("flags_controller.send_data_pulse")) {
                    sendDataPulse = Boolean.parseBoolean(value.toString());
                }                 
            });
            
            client = new LibertyClient(
                    logger,
            		flagsClientId,
            		flagsControllerEndpoint,
            		flagsControllerAccessToken, 
            		flagsContextKey, 
            		flagsCacheSecondsTimeout,
                    verboseErrorLog,
                    sendDataPulse,
            		defaultFlagsValues
            );
            
        } catch (IOException e) {
            logger.error("Creating Flag Tool Client: "+e.getMessage());
            e.printStackTrace();
        }
    }
}
