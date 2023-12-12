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

public class LibertyClient {

    private static HashMap<String,String> flagsValuesCache = new HashMap<String,String>();
    private static String endpointUrl = new String();
    private static String contextKey = new String();
    private static Integer cacheTimeStamp = 0;
    private static Integer cacheSecondsTimeout = 0;
    
    
    public LibertyClient(String endpointUrl, String contextKey, Integer cacheSecondsTimeout, HashMap<String,String> defaultFlagsValues) {
        
        this.endpointUrl = endpointUrl;
        this.cacheSecondsTimeout = cacheSecondsTimeout;
        this.contextKey = contextKey;
        
        if (this.flagsValuesCache.size() == 0) {
            this.flagsValuesCache = defaultFlagsValues;
            this.updateCache();
            
        }
        

    }

    
    public String getFlagValue(String flagName) {
        this.updateCache();
        return flagsValuesCache.get(flagName);
        
    }
    
    public Boolean flagIsEnabled(String flagName) {
        this.updateCache();
        String flagValue=this.flagsValuesCache.get(flagName);
        if (flagValue.strip().equals("1")) {
            return true;
        }
        else return false;
        
    }
    
    private void updateCache() {
        synchronized (this) {
            Long currentTimeStamp = System.currentTimeMillis()/1000;
            if ((currentTimeStamp - this.cacheTimeStamp) > this.cacheSecondsTimeout) {
                this.cacheTimeStamp = currentTimeStamp.intValue();
                try {
                    JSONObject jsonBody = new JSONObject();
                    jsonBody.put("context-key", this.contextKey);
                    String httpResult = this.executePost(this.endpointUrl+"/values", jsonBody.toJSONString());
                    JSONParser jsonParser = new JSONParser();
                    JSONArray flagsList = (JSONArray)jsonParser.parse(httpResult);
                    Iterator<JSONObject> flagsListIterator = flagsList.iterator();
                    
                    while (flagsListIterator.hasNext()) {
                        JSONObject flagValuePair = flagsListIterator.next();
                        String flagName = (String) flagValuePair.get("name");
                        String flagValue = (String) flagValuePair.get("value");
                        
                        if (this.flagsValuesCache.containsKey(flagName)) {
                            this.flagsValuesCache.put(flagName, flagValue);
                            
                        }
                        
                    }
                    
                    
                } catch (Exception e) {
                    // TODO: handle exception
                }
                
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
