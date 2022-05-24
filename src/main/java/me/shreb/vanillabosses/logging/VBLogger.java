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
    private static final Level CONFIG_LEVEL;

    static {
        try {
            handler = new FileHandler(Vanillabosses.getInstance().getDataFolder() + "/log.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }
        handler.setFormatter(formatter);

        String level = Vanillabosses.getInstance().getConfig().getString("Bosses.LogLevel");

        if (level == null || level.equals("")) {
            level = "WARNING";
        }

        CONFIG_LEVEL = Level.parse(level);

    }

    public VBLogger(String className, Level logLevel, String stringToLog) {
        this.className = className;
        this.stringToLog = stringToLog;
        this.logLevel = logLevel;
    }

    public void logToFile(){
        Logger logger = Logger.getLogger(className);
        logger.setLevel(CONFIG_LEVEL);
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
