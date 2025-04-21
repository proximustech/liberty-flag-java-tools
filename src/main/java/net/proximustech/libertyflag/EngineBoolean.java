package net.proximustech.libertyflag;

import org.json.simple.JSONObject;

public class EngineBoolean {

	/**
	* Reads the status boolean parameter and returns it's value

    * @param  parameters Flag configuration parameters
    * @return Configured value of the flag    
	*/
    public static Boolean getValue(JSONObject parameters){
        Boolean resultValue = false;
        if(parameters.get("status").toString().strip().equals("true")){
            resultValue = true;
        }
        return resultValue;
    }

}
