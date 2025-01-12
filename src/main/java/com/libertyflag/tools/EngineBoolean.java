package com.libertyflag.tools;

import org.json.simple.JSONObject;

public class EngineBoolean {

	/**
	* Reads the status boolean parameter and returns it's value
	*/
    public static Boolean getValue(JSONObject parameters){
        Boolean resultValue = false;
        if(parameters.get("status").toString().strip().equals("true")){
            resultValue = true;
        }
        return resultValue;
    }

}
