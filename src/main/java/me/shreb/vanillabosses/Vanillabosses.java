package me.shreb.vanillabosses;

import me.shreb.vanillabosses.bosses.VBBoss;
import me.shreb.vanillabosses.bosses.utility.BossCommand;
import me.shreb.vanillabosses.commands.VBCommands;
import me.shreb.vanillabosses.logging.VBLogger;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class Vanillabosses extends JavaPlugin {

    private static Vanillabosses instance;

    private File configF;
    private File logFile;
    private FileConfiguration config;


    @Override
    public void onEnable() {
        // Plugin startup logic

        instance = this;

        createConfigFile();
        createLogFile();

        loadConfig();

        Logger.getLogger("Vanilla Bosses").log(Level.INFO, "Vanilla Bosses plugin enabled! Check log file for warnings if you notice bugs or errors");

        //registering listeners and commands
        BossCommand.registerListeners();
        VBBoss.registerListeners();
        VBCommands.registerAll();

        new VBLogger(getClass().getName(), Level.INFO, "Plugin enabled!").logToFile();

    }

    @Override
    public void onDisable() {

        VBLogger.exitLogger();

        // Plugin shutdown logic
    }

    private void createConfigFile() {

        configF = new File(getDataFolder(), "config.yml");

        if (!configF.exists()) {
            configF.getParentFile().mkdirs();
            saveResource("config.yml", false);
        }

        config = new YamlConfiguration();
        try {
            config.load(configF);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    private void createLogFile() {
        logFile = new File(getDataFolder(), "log.txt");

        if (logFile.exists()) logFile.delete();


        if (!logFile.exists()) {
            try {
                logFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void loadConfig() {
        this.getConfig().options().copyDefaults(true);
        this.saveDefaultConfig();
    }

    public static Vanillabosses getInstance() {
        return instance;
    }

}
