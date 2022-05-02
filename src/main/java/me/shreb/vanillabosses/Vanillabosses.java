package me.shreb.vanillabosses;

import me.shreb.vanillabosses.bosses.VBBoss;
import me.shreb.vanillabosses.bosses.bossRepresentation.RespawningBoss;
import me.shreb.vanillabosses.bosses.utility.BossCommand;
import me.shreb.vanillabosses.commands.VBCommands;
import me.shreb.vanillabosses.listeners.VBListeners;
import me.shreb.vanillabosses.logging.VBLogger;
import me.shreb.vanillabosses.utility.Languages;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
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
    private static Languages currentLanguage;


    @Override
    public void onEnable() {
        // Plugin startup logic

        //general things I still have to do
        //TODO boss bars
        //TODO Passive Wither
        //TODO Creeper boss exploding ability


        instance = this;

        createConfigFile();
        createLogFile();

        loadConfig();

        Logger.getLogger("Vanilla Bosses").log(Level.INFO, "Vanilla Bosses plugin enabled! Check log file for warnings if you notice bugs or errors");

        try {
            currentLanguage = Languages.valueOf(config.getString("Bosses.PluginLanguage"));
            getInstance().getServer().getLogger().log(Level.INFO, "[VanillaBosses] Language Setting " + config.getString("Bosses.PluginLanguage") + " successfully enabled!");
        } catch (IllegalArgumentException | NullPointerException e) {
            getInstance().getServer().getLogger().log(Level.WARNING, "[VanillaBosses] Language specified in the config could not be found! Defaulting to English.");
            currentLanguage = Languages.EN;
        }

        //registering listeners and commands
        BossCommand.registerListeners();
        VBBoss.registerListeners();
        VBCommands.registerAll();
        VBListeners.registerListeners();

        //initialize respawning bosses
        RespawningBoss.spawnRespawningBosses();


        new VBLogger(getClass().getName(), Level.INFO, "Plugin enabled!").logToFile();

    }

    @Override
    public void onDisable() {

        VBLogger.exitLogger();

        //TODO Implement removal of Bosses with the Scoreboard tag VBBoss.REMOVE_ON_DISABLE_TAG


        //remove all Entities in all worlds on the server which have the Scoreboard tag which marks the entity for removal on disable of the plugin
        getInstance().getServer().getWorlds()
                .forEach(world -> world.getEntities()
                        .stream()
                        .filter(n -> n.getScoreboardTags().contains(VBBoss.REMOVE_ON_DISABLE_TAG))
                        .forEach(Entity::remove));

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

    public static Languages getCurrentLanguage() {
        return currentLanguage;
    }

}
