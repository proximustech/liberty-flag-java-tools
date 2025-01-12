package com.libertyflag.tools;

import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import java.util.HashMap;
import java.util.Iterator;

public class EngineBooleanConditionedTrue {

	/**
	* Evaluates each condition against the data parameter for returning the final value
	*/
    public static Boolean getValue(JSONObject parameters,HashMap<String, String> data){
        Boolean conditionFulfilled = false;
        JSONArray conditions = (JSONArray)parameters.get("conditions");
        Iterator<JSONObject> conditionsIterator = conditions.iterator();

		while (conditionsIterator.hasNext()) {

			JSONObject condition = conditionsIterator.next();
			String firstParameter = (String) condition.get("first_parameter");
			String secondParameter = (String) condition.get("second_parameter");
			String operatorParameter = (String) condition.get("operator_parameter");

			if(operatorParameter.equals("equal")){
				if(data.get(firstParameter).toString().equals(secondParameter)){
					conditionFulfilled = true;
				} else {
					conditionFulfilled = false;
				}

			}
			else if(operatorParameter.equals("different")){
				if(!data.get(firstParameter).toString().equals(secondParameter)){
					conditionFulfilled = true;
				} else {
					conditionFulfilled = false;
				}

			}
			else if(operatorParameter.equals("greater_than")){
				if(Integer.parseInt(data.get(firstParameter).toString()) > Integer.parseInt(secondParameter)){
					conditionFulfilled = true;
				} else {
					conditionFulfilled = false;
				}

			}			
			else if(operatorParameter.equals("less_than")){
				if(Integer.parseInt(data.get(firstParameter).toString()) < Integer.parseInt(secondParameter)){
					conditionFulfilled = true;
				} else {
					conditionFulfilled = false;
				}

			}

			if(!conditionFulfilled){
				break;
			}						
			

		}

        return conditionFulfilled;
    }

}
