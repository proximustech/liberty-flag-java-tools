package net.proximustech.libertyflag;

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

    /**
    * Sends an error message to the log Handlers(Console is Default)
    *
    * @param message the message to be logged
    */
    public void error(String message){
        logger.log(Level.SEVERE, message);
    }

    /**
    * Sends a warning message to the log Handlers(Console is Default)
    *
    * @param message the message to be logged
    */
    public void warn(String message){
        logger.log(Level.WARNING, message);
    }    

}
