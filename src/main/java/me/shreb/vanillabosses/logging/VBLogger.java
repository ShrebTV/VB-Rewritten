package me.shreb.vanillabosses.logging;

import me.shreb.vanillabosses.Vanillabosses;

import java.io.IOException;
import java.util.logging.*;

public class VBLogger {

    private String className;
    private String stringToLog;
    private Level logLevel;

    private static FileHandler handler;
    private static final Formatter formatter = new SimpleFormatter();

    static{
        try {
            handler = new FileHandler(Vanillabosses.getInstance().getDataFolder() + "/log.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }
        handler.setFormatter(formatter);
    }

    public VBLogger(String className, Level logLevel, String stringToLog) {
        this.className = className;
        this.stringToLog = stringToLog;
        this.logLevel = logLevel;
    }

    public void logToFile(){
        Logger logger = Logger.getLogger(className);
        logger.setUseParentHandlers(false);
        logger.addHandler(handler);
        logger.log(logLevel, stringToLog);
    }

    public void setStringToLog(String stringToLog) {
        this.stringToLog = stringToLog;
    }

    public static void exitLogger(){
        handler.close();
    }
}
