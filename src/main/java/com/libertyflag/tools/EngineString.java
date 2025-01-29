package com.libertyflag.tools;

import org.json.simple.JSONObject;

public class EngineString {

	/**
	* Reads the value parameter and returns it.
	*/
    public static String getValue(JSONObject parameters){
        return parameters.get("value").toString().strip();
    }

}
