package com.libertyflag.tools;

import java.util.HashMap;
import java.util.Iterator;
import java.net.HttpURLConnection;
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
* Connects to the Liberty Flag Server and provides sdk tools for serving flag values through the FlagTool Class
*/
public class LibertyClient {

    private static HashMap<String,Integer> defaultFlagsValues = new HashMap<String,Integer>();
    private static HashMap<String,String> flagsValuesCache = new HashMap<String,String>();
    private static String endpointUrl = new String();
    private static String contextKey = new String();
    private static String accessToken = new String();
    private static Integer cacheTimeStamp = 0;
    private static Integer cacheSecondsTimeout = 0;
    
    public LibertyClient(String endpointUrl, String accessToken, String contextKey,Integer cacheSecondsTimeout, HashMap<String,Integer> defaultFlagsValues) {
        
        this.endpointUrl = endpointUrl;
        this.cacheSecondsTimeout = cacheSecondsTimeout;
        this.contextKey = contextKey;
        this.accessToken = accessToken;
        this.defaultFlagsValues = defaultFlagsValues;
        
        if (this.flagsValuesCache.size() == 0) {
            this.updateCache();
        }
        

    }

    /**
    * Returns unprocessed cached flag Value. Must be used with flags of type "text"
    * TODO: Implement text flag type on the server
    */
    public String getStringFlagValue(String flagName) {
        this.updateCache();
        return flagsValuesCache.get(flagName);
        
    }

    /**
    * Returns booleanFlag value for the "boolean" engine only
    */
    public Boolean booleanFlagIsTrue(String flagName) {
      HashMap<String, String> data = new HashMap<String, String>();
      return booleanFlagIsTrue(flagName,data);
    }
    
    /**
    * Returns booleanFlag value for engines that may require additional data
    */    
    public Boolean booleanFlagIsTrue(String flagName,HashMap<String, String> data) {
        this.updateCache();

        Boolean resultValue = false;
        if(defaultFlagsValues.get(flagName).equals(1)){
          resultValue = true;
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
        } catch (Exception e) {
            System.out.println("Error. Flag ("+flagName+"): "+e.getMessage());            
        }

        return resultValue;
        
    }
    
    /**
    * Updates de flag value local cache if the timeout expired
    */
    private void updateCache() {
        synchronized (this) {

            Long currentTimeStamp = System.currentTimeMillis()/1000;
            if ((currentTimeStamp - this.cacheTimeStamp) > this.cacheSecondsTimeout) {
              
                this.cacheTimeStamp = currentTimeStamp.intValue();
                try {
                    JSONObject jsonBody = new JSONObject();
                    jsonBody.put("context-key", this.contextKey);
                    jsonBody.put("access-token", this.accessToken);
                    String httpResult = this.executePost(this.endpointUrl+"/flags-config", jsonBody.toJSONString());
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
                    
                    
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
                
            }

        } 
        
    }
    
    private String executePost(String targetURL, String urlParameters) {
        HttpURLConnection connection = null;

        //TODO: Send client identification

        try {
          //Create connection
          URL url = new URL(targetURL);
          connection = (HttpURLConnection) url.openConnection();
          connection.setRequestMethod("POST");
          connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");;

          connection.setRequestProperty("Content-Length",Integer.toString(urlParameters.getBytes().length));

          connection.setUseCaches(false);
          connection.setDoOutput(true);

          //Send request
          DataOutputStream wr = new DataOutputStream (connection.getOutputStream());
          wr.writeBytes(urlParameters);
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
        } catch (Exception e) {
          e.printStackTrace();
          return null;
        } finally {
          if (connection != null) {
            connection.disconnect();
          }
        }
    }    
    
}
