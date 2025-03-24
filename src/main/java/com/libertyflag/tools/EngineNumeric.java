package com.libertyflag.tools;

import org.json.simple.JSONObject;

public class EngineNumeric {

	/**
	* Reads the value parameter and returns it as Integer.
	*/
    public static Integer getValue(JSONObject parameters){
        return Integer.parseInt(parameters.get("value").toString().strip());
    }

}
