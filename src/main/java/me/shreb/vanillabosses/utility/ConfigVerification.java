package me.shreb.vanillabosses.utility;

import me.shreb.vanillabosses.Vanillabosses;
import me.shreb.vanillabosses.bosses.*;
import me.shreb.vanillabosses.bosses.utility.BossDataRetriever;
import me.shreb.vanillabosses.bosses.utility.BossDrops;
import me.shreb.vanillabosses.logging.VBLogger;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;

import java.util.ArrayList;
import java.util.logging.Level;

public class ConfigVerification {

    private static boolean verifyString(String toCheck, String configPath) {

        VBLogger logger = new VBLogger(configPath, Level.WARNING, "");

        if (toCheck.equalsIgnoreCase("")) {
            logger.setStringToLog("Could not read config value for " + configPath + " Unexpected value: " + toCheck + " @ " + configPath);
            logger.logToFile();
            return false;
        }
        return true;
    }

    private static boolean verifyColorCode(String toCheck, String configPath) {

        VBLogger logger = new VBLogger(configPath, Level.WARNING, "");

        if (!verifyString(toCheck, configPath)) {
            return false;
        }

        try {
            ChatColor.of(toCheck);
        } catch (IllegalArgumentException ignored) {
            logger.setStringToLog("Color code invalid! " + toCheck + " @ " + configPath);
            logger.logToFile();
            return false;
        }

        return true;
    }

    private static boolean verifyBoolean(String toCheck, String configPath) {

        VBLogger logger = new VBLogger(configPath, Level.WARNING, "");

        if (toCheck == null
                || (!toCheck.equalsIgnoreCase("true")
                && !toCheck.equalsIgnoreCase("false"))) {
            logger.setStringToLog("Could not read config value for " + configPath + ". Unexpected value: " + toCheck);
            logger.logToFile();
            return false;
        }
        return true;
    }

    private static boolean verifyDrops(EntityType type) {

        VBLogger logger = new VBLogger(type.name(), Level.WARNING, "");

        BossDrops drops;
        try {
            drops = new BossDrops(new BossDataRetriever(type));
        } catch (IllegalArgumentException e) {
            logger.setStringToLog("Unable to create BossDrops for " + type + ". If there is nothing wrong with the drops, please contact the author and show the log file you found this message in.\n" +
                    "Exception: " + e);
            logger.logToFile();
            return false;
        }

        for (BossDrops.SingleDrop drop : drops.drops) {

            try {
                Material.valueOf(drop.materialName.toUpperCase());
            } catch (IllegalArgumentException ignored) {
                logger.setStringToLog("Unable to turn material found into material for drops. " + drop.materialName);
                logger.logToFile();
                return false;
            }

            if (drop.minAmount < 0) {
                logger.setStringToLog("Found a minAmount of less than 0. minAmount has to be at least 0! " + drop);
                return false;
            }
        }
        return true;
    }

    private static boolean verifyInt(String toCheck, String configPath, int min, int max) {

        FileConfiguration config = Vanillabosses.getInstance().getConfig();

        VBLogger logger = new VBLogger(configPath, Level.WARNING, "");

        String s;
        int i;

        try {

            if ((s = toCheck) == null) {
                logger.setStringToLog("Could not read config value for " + configPath + " Null value: @ " + configPath);
                logger.logToFile();
                return false;
            }

            i = Integer.parseInt(s);
        } catch (NumberFormatException ignored) {
            logger.setStringToLog("Could not read config value for " + configPath + " Unexpected value: " + toCheck + " @ " + configPath);
            logger.logToFile();
            return false;
        }

        if (i > max || i < min) {
            logger.setStringToLog("Could not read config value for " + configPath + " Has to be greater than " + min + " and less than " + max + ". Was actually " + toCheck + " @ " + configPath);
            logger.logToFile();
            return false;
        }
        return true;
    }

    /**
     * Verifies whether the config section in question is a double value within the bounds specified
     *
     * @param configPath the path towards the value
     * @param min        lower bound, inclusive
     * @param max        upper bound, inclusive
     * @return true if the value gotten from config is a valid double value inside the specified bounds, false otherwise
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private static boolean verifyDouble(String toCheck, String configPath, double min, double max) {

        VBLogger logger = new VBLogger(configPath, Level.WARNING, "");

        String s;
        double d;

        try {
            if ((s = toCheck) == null) {
                logger.setStringToLog("Could not read config value for " + configPath + " Null value: @ " + configPath);
                logger.logToFile();
                return false;
            }

            d = Double.parseDouble(s);
        } catch (NumberFormatException ignored) {
            logger.setStringToLog("Could not read config value for " + configPath + " Unexpected value: " + toCheck + " @ " + configPath);
            logger.logToFile();
            return false;
        }

        if (d > max || d < min) {
            logger.setStringToLog("Could not read config value for " + configPath + " Has to be greater than " + min + " and less than " + max + ". Was actually " + toCheck + " @ " + configPath);
            logger.logToFile();
            return false;
        }
        return true;
    }

    /**
     * Checks the ArrayList for invalid world names
     *
     * @param worldList the list of world names
     * @return true if the list is empty or all the worlds specified by the names in the list exist. false if any do not exist.
     */
    private static boolean verifySpawnWorlds(ArrayList<String> worldList, String name) {

        if (worldList.isEmpty()) return true;

        for (String s : worldList) {
            World world = Vanillabosses.getInstance().getServer().getWorld(s);
            if (world == null) {
                new VBLogger(name, Level.SEVERE, "Could not find Boss Spawn world: " + s).logToFile();
                return false;
            }
        }

        return true;
    }

    public static void verifyAllConfigs(){
        verifyBossConfig("Blaze Boss", BlazeBoss.instance.config);
        verifyBossConfig("Creeper Boss", CreeperBoss.instance.config);
        verifyBossConfig("Enderman Boss", EndermanBoss.instance.config);
        verifyBossConfig("Magmacube Boss", MagmacubeBoss.instance.config);
        verifyBossConfig("Skeleton Boss", SkeletonBoss.instance.config);
        verifyBossConfig("Slime Boss", SlimeBoss.instance.config);
        verifyBossConfig("Spider Boss", SpiderBoss.instance.config);
        verifyBossConfig("Witch Boss", WitchBoss.instance.config);
        verifyBossConfig("Wither Boss", WitherBoss.instance.config);
        verifyBossConfig("Zombie Boss", ZombieBoss.instance.config);
        verifyBossConfig("Zombified Piglin Boss", Zombified_PiglinBoss.instance.config);
    }

    private static boolean verifyBossConfig(String name, YamlConfiguration config) {

        VBLogger logger = new VBLogger(name, Level.WARNING, "");

        if (!verifyBoolean(config.getString("enabled"), name + ".enabled")) {
            logger.setStringToLog(name + ": Config Error at enabled, has to be true or false");
            logger.logToFile();
        }

        if (!verifyString(config.getString("displayName"), name + ".displayName")) {
            logger.setStringToLog(name + ": Config Error at displayName, cannot be empty");
            logger.logToFile();
        }

        if (!verifyColorCode(config.getString("displayNameColor"), name + ".displayNameColor")) {
            logger.setStringToLog(name + ": Config Error at displayNameColor, has to be a hexCode");
            logger.logToFile();
        }

        if (!verifyBoolean(config.getString("showDisplayNameAlways"), name + ".showDisplayNameAlways")) {
            logger.setStringToLog(name + ": Config Error at showDisplayNameAlways, has to be true or false");
            logger.logToFile();
        }

        if (!verifyDouble(config.getString("DamageModifier"), name + ".DamageModifier", 0.001, 100)) {
            logger.setStringToLog(name + ": Config Warning/Error at DamageModifier, has to be a value above 0.0, recommended not to put to 100, close to it or even above :P. Has to be a number");
            logger.logToFile();
        }

        if (!verifyDouble(config.getString("SpeedModifier"), name + ".SpeedModifier", 0.001, 100)) {
            logger.setStringToLog(name + ": Config Warning/Error at SpeedModifier, has to be a value above 0.0, recommended not to put to 100, close to it or even above :P. Has to be a number");
            logger.logToFile();
        }

        if (!verifyInt(config.getString("health"), name + ".health", 0, Integer.MAX_VALUE)) {
            logger.setStringToLog(name + ": Config Warning/Error at health, has to be a value above 0, cannot be more than 2147483647, has to be a number");
            logger.logToFile();
        }

        if (!verifyDouble(config.getString("spawnChance"), name + ".spawnChance", 0.0, 1.0)) {
            logger.setStringToLog(name + ": Config Warning/Error at spawnChance, has to be a value between 0 and 1, has to be a number");
            logger.logToFile();
        }

        if (!verifyString(config.getString("killedMessage"), name + ".killedMessage")) {
            logger.setStringToLog(name + ": Config Error at killedMessage, cannot be empty");
            logger.logToFile();
        }

        if (!verifySpawnWorlds((ArrayList<String>) config.getStringList("spawnWorlds"), name)) {
            logger.setStringToLog(name + ": Config Error at killedMessage, cannot be empty");
            logger.logToFile();
        }

        if (!verifyDrops(EntityType.BLAZE)) {
            logger.setStringToLog(name + ": Could not verify boss drops.");
            logger.logToFile();
        }

        if (!verifyInt(config.getString("droppedXP"), name + ".droppedXP", 0, Integer.MAX_VALUE)) {
            logger.setStringToLog(name + ": Config Warning/Error at droppedXP, has to be a value above 0, cannot be more than 2147483647, has to be a number");
            logger.logToFile();
        }

        return true;
    }
}