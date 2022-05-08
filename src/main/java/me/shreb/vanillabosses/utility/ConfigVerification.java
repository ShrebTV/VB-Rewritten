package me.shreb.vanillabosses.utility;

import me.shreb.vanillabosses.Vanillabosses;
import me.shreb.vanillabosses.bosses.utility.BossDataRetriever;
import me.shreb.vanillabosses.bosses.utility.BossDrops;
import me.shreb.vanillabosses.logging.VBLogger;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;

import java.util.ArrayList;
import java.util.logging.Level;

public interface ConfigVerification {

    boolean verifyConfig();

    default boolean verifyString(String configPath) {

        FileConfiguration config = Vanillabosses.getInstance().getConfig();

        VBLogger logger = new VBLogger(getClass().getName(), Level.WARNING, "");

        if (config.getString(configPath).equalsIgnoreCase("")) {
            logger.setStringToLog("Could not read config value for " + configPath + " Unexpected value: " + config.getString(configPath) + " @ " + configPath);
            logger.logToFile();
            return false;
        }
        return true;
    }

    default boolean verifyColorCode(String configPath) {
        FileConfiguration config = Vanillabosses.getInstance().getConfig();

        VBLogger logger = new VBLogger(getClass().getName(), Level.WARNING, "");

        if (!verifyString(configPath)) {
            return false;
        }

        try {
            ChatColor.of(config.getString(configPath));
        } catch (IllegalArgumentException ignored) {
            logger.setStringToLog("Color code invalid! " + config.getString(configPath) + " @ " + configPath);
            logger.logToFile();
            return false;
        }

        return true;
    }

    default boolean verifyBoolean(String configPath) {
        FileConfiguration config = Vanillabosses.getInstance().getConfig();

        VBLogger logger = new VBLogger(getClass().getName(), Level.WARNING, "");

        if (config.getString(configPath) == null
                || (!config.getString(configPath).equalsIgnoreCase("true")
                && !config.getString(configPath).equalsIgnoreCase("false"))) {
            logger.setStringToLog("Could not read config value for " + configPath + ". Unexpected value: " + config.getString(configPath));
            logger.logToFile();
            return false;
        }
        return true;
    }

    default boolean verifyDrops(EntityType type) {

        VBLogger logger = new VBLogger(getClass().getName(), Level.WARNING, "");

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

    default boolean verifyInt(String configPath, int min, int max) {

        FileConfiguration config = Vanillabosses.getInstance().getConfig();

        VBLogger logger = new VBLogger(getClass().getName(), Level.WARNING, "");

        String s;
        int i;

        try {

            if ((s = config.getString(configPath)) == null) {
                logger.setStringToLog("Could not read config value for " + configPath + " Null value: @ " + configPath);
                logger.logToFile();
                return false;
            }

            i = Integer.parseInt(s);
        } catch (NumberFormatException ignored) {
            logger.setStringToLog("Could not read config value for " + configPath + " Unexpected value: " + config.getString(configPath) + " @ " + configPath);
            logger.logToFile();
            return false;
        }

        if (i > max || i < min) {
            logger.setStringToLog("Could not read config value for " + configPath + " Has to be greater than " + min + " and less than " + max + ". Was actually " + config.getInt(configPath) + " @ " + configPath);
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
    default boolean verifyDouble(String configPath, double min, double max) {

        FileConfiguration config = Vanillabosses.getInstance().getConfig();

        VBLogger logger = new VBLogger(getClass().getName(), Level.WARNING, "");

        String s;
        double d;

        try {
            if ((s = config.getString(configPath)) == null) {
                logger.setStringToLog("Could not read config value for " + configPath + " Null value: @ " + configPath);
                logger.logToFile();
                return false;
            }

            d = Double.parseDouble(s);
        } catch (NumberFormatException ignored) {
            logger.setStringToLog("Could not read config value for " + configPath + " Unexpected value: " + config.getString(configPath) + " @ " + configPath);
            logger.logToFile();
            return false;
        }

        if (d > max || d < min) {
            logger.setStringToLog("Could not read config value for " + configPath + " Has to be greater than " + min + " and less than " + max + ". Was actually " + config.getInt(configPath) + " @ " + configPath);
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
    default boolean verifySpawnWorlds(ArrayList<String> worldList) {

        if (worldList.isEmpty()) return true;

        for (String s : worldList) {
            World world = Vanillabosses.getInstance().getServer().getWorld(s);
            if (world == null) {
                new VBLogger(getClass().getName(), Level.SEVERE, "Could not find Boss Spawn world: " + s).logToFile();
                return false;
            }
        }

        return true;
    }
}