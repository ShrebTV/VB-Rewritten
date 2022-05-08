package me.shreb.vanillabosses.utility.configFiles;

import me.shreb.vanillabosses.Vanillabosses;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class FileCreator {

    static Path folderPath = Path.of(String.valueOf(Vanillabosses.getInstance().getDataFolder().toPath()));

    //Items
    public static Path baseballBatPath = Path.of(String.valueOf(folderPath), "Item Config" + File.separator + "Baseball Bat.yml");
    public static Path blazerPath = Path.of(String.valueOf(folderPath), "Item Config" + File.separator + "Blazer.yml");
    public static Path bossEggsPath = Path.of(String.valueOf(folderPath), "Item Config" + File.separator + "Boss Eggs.yml");
    public static Path butchersAxePath = Path.of(String.valueOf(folderPath), "Item Config" + File.separator + "Butchers Axe.yml");
    public static Path hmcPath = Path.of(String.valueOf(folderPath), "Item Config" + File.separator + "Heated Magma cream.yml");
    public static Path invisibilityCloakPath = Path.of(String.valueOf(folderPath), "Item Config" + File.separator + "Invisibility Cloak.yml");
    public static Path skeletorPath = Path.of(String.valueOf(folderPath), "Item Config" + File.separator + "Skeletor.yml");
    public static Path slimeBootsPath = Path.of(String.valueOf(folderPath), "Item Config" + File.separator + "Slime Boots.yml");
    public static Path slingshotPath = Path.of(String.valueOf(folderPath), "Item Config" + File.separator + "Slingshot.yml");
    public static Path witherEggPath = Path.of(String.valueOf(folderPath), "Item Config" + File.separator + "Wither Egg.yml");

    // Bosses
    public static Path blazeBossPath = Path.of(String.valueOf(folderPath), "Boss Config" + File.separator + "Blaze Boss.yml");
    public static Path creeperBossPath = Path.of(String.valueOf(folderPath), "Boss Config" + File.separator + "Creeper Boss.yml");
    public static Path endermanBossPath = Path.of(String.valueOf(folderPath), "Boss Config" + File.separator + "Enderman Boss.yml");
    public static Path magmacubeBossPath = Path.of(String.valueOf(folderPath), "Boss Config" + File.separator + "Magmacube Boss.yml");
    public static Path skeletonBossPath = Path.of(String.valueOf(folderPath), "Boss Config" + File.separator + "Skeleton Boss.yml");
    public static Path slimeBossPath = Path.of(String.valueOf(folderPath), "Boss Config" + File.separator + "Slime Boss.yml");
    public static Path spiderBossPath = Path.of(String.valueOf(folderPath), "Boss Config" + File.separator + "Spider Boss.yml");
    public static Path witchBossPath = Path.of(String.valueOf(folderPath), "Boss Config" + File.separator + "Witch Boss.yml");
    public static Path witherBossPath = Path.of(String.valueOf(folderPath), "Boss Config" + File.separator + "Wither Boss.yml");
    public static Path zombieBossPath = Path.of(String.valueOf(folderPath), "Boss Config" + File.separator + "Zombie Boss.yml");
    public static Path zombified_PiglinBossPath = Path.of(String.valueOf(folderPath), "Boss Config" + File.separator + "Zombified Piglin Boss.yml");


    /**
     * Creates the specified file if it does not exist and loads it into the specified config file
     *
     * @param filePath      the path to load the file from
     * @param configuration the config file to load the file into
     */
    public static void createAndLoad(Path filePath, FileConfiguration configuration) {

        File file = filePath.toFile();

        file.getParentFile().mkdirs();
        if (!file.exists()) {
            Vanillabosses.getInstance().saveResource((file.getParentFile().getName() + File.separator + file.getName()), false);
        }

        try {
            configuration.load(file);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    /**
     * Makes sure that the folders "Item Config" and "Boss Config" exist
     */
    public static void makeConfigFolders() {

        folderPath.toFile().mkdirs();

    }
}