package me.shreb.vanillabosses;

import me.shreb.vanillabosses.bosses.VBBoss;
import me.shreb.vanillabosses.bosses.Zombified_PiglinBoss;
import me.shreb.vanillabosses.bosses.bossRepresentation.RespawningBoss;
import me.shreb.vanillabosses.bosses.utility.BossCommand;
import me.shreb.vanillabosses.bosses.utility.VBBossBar;
import me.shreb.vanillabosses.commands.VBCommands;
import me.shreb.vanillabosses.items.InvisibilityCloak;
import me.shreb.vanillabosses.items.WitherEgg;
import me.shreb.vanillabosses.items.utility.ItemListeners;
import me.shreb.vanillabosses.items.utility.VBItemRecipe;
import me.shreb.vanillabosses.listeners.VBListeners;
import me.shreb.vanillabosses.logging.VBLogger;
import me.shreb.vanillabosses.utility.ConfigVerification;
import me.shreb.vanillabosses.utility.Languages;
import me.shreb.vanillabosses.utility.Metrics;
import me.shreb.vanillabosses.utility.UpdateChecker;
import me.shreb.vanillabosses.utility.configFiles.FileCreator;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
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

        instance = this;

        createConfigFile();
        loadConfig();

        createLogFile();

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
        ItemListeners.registerItemListeners();
        VBCommands.registerAll();
        VBListeners.registerListeners();

        WitherEgg.initializePassiveWithers();

        VBItemRecipe.registerAllRecipes();

        //initialize respawning bosses
        RespawningBoss.spawnRespawningBosses();

        InvisibilityCloak.instance.initializeChecks();

        VBBossBar.startBarShowTimer();

        Zombified_PiglinBoss.aggressionTimer();

        new VBLogger(getClass().getName(), Level.INFO, "Plugin enabled!").logToFile();

        //Initializing metrics
        int pluginId = 12433;
        Metrics metrics = new Metrics(this, pluginId);

        //Checking for an update on spigot
        Bukkit.getScheduler().runTaskAsynchronously(this, () -> {

            new UpdateChecker(this, 95205).getVersion(version1 -> {

                if (this.getDescription().getVersion().equals(version1)) {
                    getLogger().info("There is not a new update available.");
                } else {
                    getLogger().info("There is a new update available.");
                    getServer().getConsoleSender().sendMessage(ChatColor.RED + "New Update available for the Vanilla Bosses Plugin!");
                }
            });
        });

        ConfigVerification.verifyAllConfigs();

    }

    @Override
    public void onDisable() {

        DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy_HHmmss");
        VBLogger.exitLogger();
        //
        // Plugin shutdown logic
        if (!config.getBoolean("Bosses.SaveLog")) {

            File folder = new File(getDataFolder(), "Logs");
            folder.mkdirs();

            File lFile = new File(folder, "Logfile_" + dateFormat.format(new Date()) + ".txt");

            if (logFile.renameTo(lFile)) {
                System.out.println("Vanilla Bosses: Saved log file!");
                logFile.delete();
            } else {
                System.out.println("Vanilla Bosses: Could not properly save log file!");
            }

            String[] strings = getDataFolder().list();

            if (strings != null && strings.length > 1) {
                for (String fileName : strings) {

                    String[] nameParts = fileName.split("\\.");

                    if (nameParts.length == 3) {
                        try {
                            Integer.parseInt(nameParts[2]);

                            File file = new File(getDataFolder(), fileName);

                            file.delete();

                        } catch (NumberFormatException ignored) {

                        }
                    }
                }
            }
        }


        String[] names = getDataFolder().list();

        if (names != null && names.length > 1) {
            for (String s : names) {
                if (s.endsWith(".lck")) {
                    File file = new File(getDataFolder(), s);
                    try {
                        file.delete();
                    } catch (SecurityException ignored) {
                    }
                }
            }
        }


        //remove all Entities in all worlds on the server which have the Scoreboard tag which marks the entity for removal on disable of the plugin
        getInstance().getServer().getWorlds()
                .forEach(world -> world.getEntities()
                        .stream()
                        .filter(n -> n.getScoreboardTags().contains(VBBoss.REMOVE_ON_DISABLE_TAG))
                        .forEach(Entity::remove));


        for (VBBossBar bar : VBBossBar.bossBarMap.values()) {
            bar.bossBar.removeAll();
        }

        VBItemRecipe.removeAllRecipes();

    }

    private void createConfigFile() {

        FileCreator.makeConfigFolders();

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

        if (logFile.exists() && config.getBoolean("Bosses.CleanUpConfig")) logFile.delete();

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
