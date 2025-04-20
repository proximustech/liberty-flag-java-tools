package com.libertyflag.tools;

import org.json.simple.JSONObject;

public class EngineNumeric {

	/**
	* Access the value parameter.
    *
    * @param  parameters Flag configuration parameters
    * @return Value of the flag  as Integer
	*/
    
	*/
    public static Integer getValue(JSONObject parameters){
        return Integer.parseInt(parameters.get("value").toString().strip());
    }

}
