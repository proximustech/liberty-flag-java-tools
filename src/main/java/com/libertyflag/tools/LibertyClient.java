package com.libertyflag.tools;

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

    private HashMap<String,String> defaultFlagsValues = new HashMap<String,String>();
    private HashMap<String,String> flagsValuesCache = new HashMap<String,String>();
    private String clientId = new String();
    private String endpointUrl = new String();
    private String contextKey = new String();
    private String accessToken = new String();
    private Integer cacheTimeStamp = 0;
    private Integer cacheSecondsTimeout = 0;

    private HashMap<String,String> data = new HashMap<String,String>();
    
    public LibertyClient(String clientId,String endpointUrl, String accessToken, String contextKey,Integer cacheSecondsTimeout, HashMap<String,String> defaultFlagsValues) {
        
        this.clientId = clientId;
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
    * Returns string flag Value. Must be used with flags of type "string"
    */
    public String getStringFlagValue(String flagName) {
        this.updateCache();

        String resultValue = defaultFlagsValues.get(flagName);

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
            System.out.println("Error Parsing Flag ("+flagName+") Configuration: "+e.getMessage());
        } catch (Exception e) {
            System.out.println("Error. Flag ("+flagName+"): "+e.getMessage());
        }

        return resultValue;
        
    }

    /**
    * Returns string flag Value. Must be used with flags of type "numeric"
    */
    public Integer getIntegerFlagValue(String flagName) {
        this.updateCache();

        Integer resultValue = Integer.parseInt(defaultFlagsValues.get(flagName));

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
            System.out.println("Error Parsing Flag ("+flagName+") Configuration: "+e.getMessage());
        } catch (Exception e) {
            System.out.println("Error. Flag ("+flagName+"): "+e.getMessage());
        }

        return resultValue;
        
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
        this.data = data;
        this.updateCache();

        Boolean resultValue = false;
        if(defaultFlagsValues.get(flagName).equals("1")){
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
        } catch (ParseException e) {
            System.out.println("Error Parsing Flag ("+flagName+") Configuration: "+e.getMessage());
        } catch (Exception e) {
            System.out.println("Error. Flag ("+flagName+"): "+e.getMessage());            
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
              jsonBody.put("data-pulse", jsonDataPulse);
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
              System.out.println("Error Parsing HTTP result: "+e.getMessage());
          } catch (Exception e) {
              System.out.println(e.getMessage());
          }
          
      }
        
    }
    
    private String executePost(String targetURL, String urlParameters) {
        HttpURLConnection connection = null;

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
        } catch (ConnectException e) {
            System.out.println("Error in HTTP connection to "+targetURL+") Detaill: "+e.getMessage());
            return null;
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
