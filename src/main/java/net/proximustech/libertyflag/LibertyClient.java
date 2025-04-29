package net.proximustech.libertyflag;

import java.util.HashMap;
import java.util.Iterator;
import java.net.HttpURLConnection;
import java.net.ConnectException;
import java.net.URL;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.DataOutputStream;

import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.ParseException;
import org.json.simple.parser.JSONParser;

/**
* Connects to the Liberty Flag Server and provides tools for serving flag values through the FlagTool Class
*/
public class LibertyClient {

    private LoggerTool logger;
    private HashMap<String,String> defaultFlagsValues = new HashMap<String,String>();
    private HashMap<String,String> flagsValuesCache = new HashMap<String,String>();
    private String clientId = new String();
    private String endpointUrl = new String();
    private String contextKey = new String();
    private String accessToken = new String();
    private Integer cacheTimeStamp = 0;
    private Integer cacheSecondsTimeout = 0;
    private boolean verboseErrorLog = false;
    private boolean sendDataPulse = false;

    private HashMap<String,String> data = new HashMap<String,String>();
    
    public LibertyClient(LoggerTool logger,String clientId,String endpointUrl, String accessToken, String contextKey,Integer cacheSecondsTimeout, boolean verboseErrorLog, boolean sendDataPulse, HashMap<String,String> defaultFlagsValues) {
        
        this.logger = logger;

        this.clientId = clientId;
        this.endpointUrl = endpointUrl;
        this.cacheSecondsTimeout = cacheSecondsTimeout;
        this.contextKey = contextKey;
        this.accessToken = accessToken;
        this.verboseErrorLog = verboseErrorLog;
        this.sendDataPulse = sendDataPulse;
        this.defaultFlagsValues = defaultFlagsValues;
        
        if (this.flagsValuesCache.size() == 0) {
            this.updateCache();
        }
        
    }

    /**
    * Returns string flag Value. Must be used with flags of type "string"
    * 
    * @param  flagName the name of the consulted flag
    * @param  dafaultValueBackup default value to be used if an error ocurrs when reading from the application.flags file
    * @return Value of the flag
    */
    public String getStringFlagValue(String flagName, String dafaultValueBackup) {
        this.updateCache();

        String resultValue = defaultFlagsValues.get(flagName);
        if(resultValue==null){
          logger.error("Configuration - Flag ("+flagName+"): Default Value NOT set. Registering default value from backup.");
          defaultFlagsValues.put(flagName,dafaultValueBackup);
          resultValue = dafaultValueBackup;
        }        

        try {
          String flagConfigurationString=this.flagsValuesCache.get(flagName);

          JSONParser jsonParser = new JSONParser();
          JSONObject flagConfiguration = (JSONObject)jsonParser.parse(flagConfigurationString);
          String engine = flagConfiguration.get("engine").toString();
          JSONObject engineParameters = (JSONObject)flagConfiguration.get("parameters");
          if(engine.equals("string")){
            resultValue = EngineString.getValue(engineParameters);
          }

        } catch (ParseException e) {
          logger.error("getStringFlagValue() Configuration ParseException with Flag ("+flagName+"): "+e.getMessage());
          if(this.verboseErrorLog){
            e.printStackTrace();
          }           
        } catch (Exception e) {
          logger.error("getStringFlagValue() with Flag ("+flagName+"): "+e.getMessage());
          if(this.verboseErrorLog){
            e.printStackTrace();
          }           
        }

        return resultValue;
        
    }

    /**
    * Returns string flag Value. Must be used with flags of type "numeric"
    * 
    * @param  flagName the name of the consulted flag
    * @param  dafaultValueBackup default value to be used if an error ocurrs when reading from the application.flags file
    * @return Value of the flag
    */
    public Integer getIntegerFlagValue(String flagName, Integer dafaultValueBackup) {
        this.updateCache();

        String defaultValue = defaultFlagsValues.get(flagName);
        if(defaultValue==null){
          logger.error("Configuration - Flag ("+flagName+"): Default Value NOT set. Registering default value from backup.");
          defaultFlagsValues.put(flagName,dafaultValueBackup.toString());
          defaultValue = dafaultValueBackup.toString();
        }  

        Integer resultValue = null;
        try {
          resultValue = Integer.parseInt(defaultValue);
        } catch (NumberFormatException e) {
          logger.error("Usage - Using getIntegerFlagValue('"+flagName+"') to read a none Integer default flag value. Registering default value from backup.");
          defaultFlagsValues.put(flagName,dafaultValueBackup.toString());
          resultValue = dafaultValueBackup;
        }

        try {
          String flagConfigurationString=this.flagsValuesCache.get(flagName);

          JSONParser jsonParser = new JSONParser();
          JSONObject flagConfiguration = (JSONObject)jsonParser.parse(flagConfigurationString);
          String engine = flagConfiguration.get("engine").toString();
          JSONObject engineParameters = (JSONObject)flagConfiguration.get("parameters");
          if(engine.equals("numeric")){
            resultValue = EngineNumeric.getValue(engineParameters);
          }

        } catch (ParseException e) {
          logger.error("getIntegerFlagValue() Configuration ParseException with Flag ("+flagName+"): "+e.getMessage());
          if(this.verboseErrorLog){
            e.printStackTrace();
          }           
        } catch (Exception e) {
          logger.error("getIntegerFlagValue() with Flag ("+flagName+"): "+e.getMessage());
          if(this.verboseErrorLog){
            e.printStackTrace();
          }           
        }

        return resultValue;
        
    }    

    /**
    * Returns booleanFlag value for the "boolean" engine only
    * 
    * @param  flagName the name of the consulted flag
    * @param  dafaultValueBackup default value to be used if an error ocurrs when reading from the application.flags file
    * @return Value of the flag
    */
    public Boolean booleanFlagIsTrue(String flagName,Integer dafaultValueBackup) {
      HashMap<String, String> data = new HashMap<String, String>();
      return booleanFlagIsTrue(flagName,dafaultValueBackup,data);
    }
    
    /**
    * Returns booleanFlag value for engines that may require additional data
    * 
    * @param  flagName the name of the consulted flag
    * @param  dafaultValueBackup default value to be used if an error ocurrs when reading from the application.flags file
    * @param  data Map with metadata that will be used to compute the value of the flag
    * @return Value of the flag
    */    
    public Boolean booleanFlagIsTrue(String flagName,Integer dafaultValueBackup,HashMap<String, String> data) {
        this.data = data;
        this.updateCache();

        String defaultValue = defaultFlagsValues.get(flagName);
        if(defaultValue==null){
          logger.error("Configuration - Flag ("+flagName+"): Default Value NOT set. Registering default value from backup.");
          defaultFlagsValues.put(flagName,dafaultValueBackup.toString());
          defaultValue = dafaultValueBackup.toString();
        }  

        Boolean resultValue = false;
        if(defaultValue.equals("1")){
          resultValue = true;
        }
        else if(!defaultValue.equals("0")){
          logger.error("Usage - Using booleanFlagIsTrue('"+flagName+"') to read a none 1 or 0 default flag value. Registering default value from backup.");
          defaultFlagsValues.put(flagName,dafaultValueBackup.toString());
          if(dafaultValueBackup.equals(1)){
            resultValue = true;
          }
        }        

        try {
          String flagConfigurationString=this.flagsValuesCache.get(flagName);

          JSONParser jsonParser = new JSONParser();
          JSONObject flagConfiguration = (JSONObject)jsonParser.parse(flagConfigurationString);
          String engine = flagConfiguration.get("engine").toString();
          JSONObject engineParameters = (JSONObject)flagConfiguration.get("parameters");
          if(engine.equals("boolean")){
            resultValue = EngineBoolean.getValue(engineParameters);
          }
          else if(engine.equals("boolean_conditioned_true")){
            resultValue = EngineBooleanConditionedTrue.getValue(engineParameters,data);
          }
          else if(engine.equals("boolean_conditioned_false")){
            resultValue = EngineBooleanConditionedFalse.getValue(engineParameters,data);
          }
          else if(engine.equals("boolean_conditionedor_true")){
            resultValue = EngineBooleanConditionedOrTrue.getValue(engineParameters,data);
          }
          else if(engine.equals("boolean_conditionedor_false")){
            resultValue = EngineBooleanConditionedOrFalse.getValue(engineParameters,data);
          }                                        
        } catch (ParseException e) {
          logger.error("booleanFlagIsTrue() Configuration ParseException with Flag ("+flagName+"): "+e.getMessage());
          if(this.verboseErrorLog){
            e.printStackTrace();
          }             
        } catch (Exception e) {
          logger.error("booleanFlagIsTrue() with Flag ("+flagName+"): "+e.getMessage());
          if(this.verboseErrorLog){
            e.printStackTrace();
          }           
        }

        return resultValue;    
    }
    
    /**
    * Updates the flag value local cache if the timeout is expired
    */
    @SuppressWarnings({ "unchecked" })
	  private synchronized void updateCache() {

      Long currentTimeStamp = System.currentTimeMillis()/1000;
      if ((currentTimeStamp - this.cacheTimeStamp) > this.cacheSecondsTimeout) {  
        this.cacheTimeStamp = currentTimeStamp.intValue();
        try {
            
          JSONObject jsonDataPulse = new JSONObject();
          jsonDataPulse.put("client_id", this.clientId);

          JSONObject jsonDataPulseDataMap = new JSONObject();
          for ( String key : this.data.keySet() ) {
              jsonDataPulseDataMap.put(key, this.data.get(key));
          }
          jsonDataPulse.put("data_map", jsonDataPulseDataMap);
          
          this.data = new HashMap<String,String>();

          JSONObject jsonBody = new JSONObject();
          jsonBody.put("context-key", this.contextKey);
          jsonBody.put("access-token", this.accessToken);
          if(this.sendDataPulse){
            jsonBody.put("data-pulse", jsonDataPulse);
          }
          
          String httpResult = this.executePost(this.endpointUrl+"/get-flags-config", jsonBody.toJSONString());
          JSONParser jsonParser = new JSONParser();
          JSONObject apiResponse = (JSONObject)jsonParser.parse(httpResult);
          JSONArray flagsList = (JSONArray)apiResponse.get("flags");
          Iterator<JSONObject> flagsListIterator = flagsList.iterator();
          
          while (flagsListIterator.hasNext()) {
              JSONObject flagValuePair = flagsListIterator.next();
              String flagName = (String) flagValuePair.get("name");
              String flagConfiguration = flagValuePair.get("configuration").toString();

              if (this.defaultFlagsValues.containsKey(flagName)) {
                  this.flagsValuesCache.put(flagName, flagConfiguration);
                  
              }
              
          }
            
        } catch (ParseException e) {
          logger.error("updateCache() ParseException: "+e.getMessage());
          if(this.verboseErrorLog){
            e.printStackTrace();
          }            
        } catch (Exception e) {
          logger.warn("updateCache(): "+e.getMessage());
          if(this.verboseErrorLog){
            e.printStackTrace();
          }
        }
      } 
    }
    
    /**
    * Makes an http POST request
    * 
    * @return http response
    */    
    private String executePost(String targetURL, String body) {
      HttpURLConnection connection = null;

      try {
        //Create connection
        URL url = new URL(targetURL);
        connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");

        connection.setRequestProperty("Content-Length",Integer.toString(body.getBytes().length));

        connection.setUseCaches(false);
        connection.setDoOutput(true);

        //Send request
        DataOutputStream wr = new DataOutputStream (connection.getOutputStream());
        wr.writeBytes(body);
        wr.close();

        //Get Response  
        InputStream is = connection.getInputStream();
        BufferedReader rd = new BufferedReader(new InputStreamReader(is));
        StringBuilder response = new StringBuilder(); // or StringBuffer if Java version 5+
        String line;
        while ((line = rd.readLine()) != null) {
          response.append(line);
          response.append('\r');
        }
        rd.close();
        return response.toString();
      } catch (ConnectException e) {
        logger.warn("executePost() ConnectException to "+targetURL+": "+e.getMessage());
        if(this.verboseErrorLog){
          e.printStackTrace();
        }          
        return null;
      } catch (Exception e) {
        logger.warn("executePost() to "+targetURL+"): "+e.getMessage());       
        if(this.verboseErrorLog){
          e.printStackTrace();
        }
        return null;
      } finally {
        if (connection != null) {
          connection.disconnect();
        }
      }
    }
}
