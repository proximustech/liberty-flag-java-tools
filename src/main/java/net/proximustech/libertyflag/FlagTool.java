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
    private static boolean verboseErrorLog = false;
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
            if (inputFlags != null) {
                flagValuesProperties.load(inputFlags);
                flagValuesProperties.forEach((key, value) -> {
                    defaultFlagsValues.put(key.toString(),value.toString());
                });
                java.net.URL flagsUrl = classLoader.getResource("application.flags");
                logger.info ("FlagTool Class. application.flags file FOUND at: " + flagsUrl);
            } else {
                logger.warn("FlagTool Class. application.flags file NOT found");
            }
            
            if (inputProperties != null) {
                flagsControllerProperties.load(inputProperties);
                java.net.URL propertiesUrl = classLoader.getResource("application.properties");
                logger.info("FlagTool Class. application.properties file FOUND at: " + propertiesUrl);
            } else {
                logger.warn("FlagTool Class. application.properties file NOT found");
            }

            // Environment variables take precedence over application.properties values.
            String configValue;

            configValue = getConfigValue("flags.controller.endpoint", "FLAGS_CONTROLLER_ENDPOINT");
            if (configValue != null) {
                flagsControllerEndpoint = configValue;
            }
            configValue = getConfigValue("flags.controller.access.token", "FLAGS_CONTROLLER_ACCESS_TOKEN");
            if (configValue != null) {
                flagsControllerAccessToken = configValue;
            }
            configValue = getConfigValue("flags.controller.context.key", "FLAGS_CONTROLLER_CONTEXT_KEY");
            if (configValue != null) {
                flagsContextKey = configValue;
            }
            configValue = getConfigValue("flags.controller.client.id", "FLAGS_CONTROLLER_CLIENT_ID");
            if (configValue != null) {
                flagsClientId = configValue;
            }
            configValue = getConfigValue("flags.controller.cache.seconds.timeout", "FLAGS_CONTROLLER_CACHE_SECONDS_TIMEOUT");
            if (configValue != null) {
                flagsCacheSecondsTimeout = Integer.parseInt(configValue);
            }
            configValue = getConfigValue("flags.controller.verbose.error.log", "FLAGS_CONTROLLER_VERBOSE_ERROR_LOG");
            if (configValue != null) {
                verboseErrorLog = Boolean.parseBoolean(configValue);
            }
            configValue = getConfigValue("flags_controller.send.data.pulse", "FLAGS_CONTROLLER_SEND_DATA_PULSE");
            if (configValue != null) {
                sendDataPulse = Boolean.parseBoolean(configValue);
            }
            if ( flagsClientId != "" &&  flagsControllerEndpoint != "" && flagsControllerAccessToken != "" && flagsContextKey != "") {
                logger.info("SDK Config values loaded OK");
            }
            else {
                logger.error("FlagTool Class loading SDK values");
            }
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
            logger.error("IOException Creating Flag Tool Client: "+e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            logger.error("GeneralException Creating Flag Tool Client: "+e.getMessage());
            e.printStackTrace();
        }        
    }

    /**
     * Resolves a configuration value: uses the environment variable when set,
     * otherwise falls back to the application.properties value. Returns null
     * when neither source provides a value.
     */
    private static String getConfigValue(String propertyName, String envName) {
        String envValue = System.getenv(envName);
        if (envValue != null && !envValue.trim().isEmpty()) {
            return envValue;
        }
        String propertyValue = flagsControllerProperties.getProperty(propertyName);
        if (propertyValue != null && !propertyValue.trim().isEmpty()) {
            return propertyValue;
        }
        return null;
    }
}
