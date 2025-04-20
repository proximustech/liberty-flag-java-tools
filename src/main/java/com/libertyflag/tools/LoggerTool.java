package com.libertyflag.tools;

import java.util.logging.Logger;
//import java.util.logging.ConsoleHandler;
import java.util.logging.Level;

/**
* Wrapper arround the selected logger library
*/
public class LoggerTool {

    private Logger logger = Logger.getLogger("Flag Tool");

    LoggerTool(){
        //logger.addHandler(new ConsoleHandler()); 
    }

    public void error(String message){
        logger.log(Level.SEVERE, message);
    }

    public void warn(String message){
        logger.log(Level.WARNING, message);
    }    

}