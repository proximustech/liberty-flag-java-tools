package net.proximustech.libertyflag;

import org.json.simple.JSONObject;

public class EngineString {

	/**
	* Access the value parameter.
    *
    * @param  parameters Flag configuration parameters
    * @return Value of the flag
	*/
    public static String getValue(JSONObject parameters){
        return parameters.get("value").toString().strip();
    }

}
