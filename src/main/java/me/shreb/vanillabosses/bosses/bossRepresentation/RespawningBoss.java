package me.shreb.vanillabosses.bosses.bossRepresentation;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;
import me.shreb.vanillabosses.Vanillabosses;
import me.shreb.vanillabosses.bosses.utility.BossCreationException;
import me.shreb.vanillabosses.bosses.utility.BossDataRetriever;
import me.shreb.vanillabosses.logging.VBLogger;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class RespawningBoss extends Boss {

    static List<RespawningBoss> bossList = new ArrayList<>();
    public static final String RESPAWNING_BOSS_TAG = "Respawning Boss";
    public static final NamespacedKey RESPAWNING_BOSS_PDC = new NamespacedKey(Vanillabosses.getInstance(), "Respawning Boss");

    EntityType type;
    @SerializedName("worldName")
    String world;
    double x;
    double y;
    double z;

    long respawnTime;

    boolean enableBoss;

    // put the commands from the command list and their indexes into the commandMap in order to easily execute them later
    static {
        FileConfiguration config = Vanillabosses.getInstance().getConfig();
        List<String> bossJsonList = new ArrayList<>(config.getStringList("Bosses.RespawningBosses"));

        for (String string : bossJsonList) {
            try {
                bossList.add(deserialize(string));
            } catch (IllegalArgumentException e) {
                new VBLogger("RespawningBoss", Level.WARNING, "Unable to create respawning Boss object from String: " + string).logToFile();
            }
        }
    }

    /**
     * Constructor for the RespawningBoss class
     *
     * @param type        Entity Type for the Boss
     * @param world       World name
     * @param x           X-Coordinates
     * @param y           Y-Coordinates
     * @param z           Z-Coordinates
     * @param respawnTime Respawn time for the boss in seconds
     * @param commands    an array of commands which are to be executed upon boss death
     */
    public RespawningBoss(EntityType type, String world, double x, double y, double z, long respawnTime, int[] commands, boolean enableBoss) {
        this.type = type;
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.respawnTime = respawnTime;
        this.commandIndexes = commands;
        this.enableBoss = enableBoss;
    }

    //Method to attempt to make a String into a RespawningBoss object
    public static RespawningBoss deserialize(String json) throws IllegalArgumentException {

        RespawningBoss boss;

        try {
            boss = new Gson().fromJson(json, RespawningBoss.class);
        } catch (JsonSyntaxException e) {
            new VBLogger("RespawningBoss", Level.WARNING, "Unable to parse json String " + json + "\n If you cannot figure out what went wrong with your String, please contact the plugin author via discord.");
            throw new IllegalArgumentException("Could not deserialize Respawning Boss Data");
        }

        if (Vanillabosses.getInstance().getServer().getWorld(boss.world) == null) {
            new VBLogger("RespawningBoss", Level.SEVERE, "Could not find specified world: " + boss.world).logToFile();
        }
        try {
            new BossDataRetriever(boss.type);
        } catch (IllegalArgumentException e) {
            new VBLogger("RespawningBoss", Level.SEVERE, "Could not find specified Boss type: " + boss.type).logToFile();
        }

        if (boss.respawnTime > 1) {
            new VBLogger("RespawningBoss", Level.SEVERE, "Respawn time was less than 1. Please specify a respawn time of 1 second or more. Time: " + boss.respawnTime).logToFile();
        }

        return boss;
    }

    //Method to serialize a RespawningBoss object into a json String
    public String serialize() {
        return new Gson().toJson(this);
    }

    public void spawnBoss() {

        World world = Vanillabosses.getInstance().getServer().getWorld(this.world);
        if (world == null) {
            new VBLogger("RespawningBoss", Level.SEVERE, "Could not find specified world for respawning boss. World: " + this.world).logToFile();
            return;
        }

        Location location = new Location(world, this.x, this.y, this.z);
        BossDataRetriever retriever;
        try {
            retriever = new BossDataRetriever(this.type);
        } catch (IllegalArgumentException e) {
            new VBLogger("RespawningBoss", Level.SEVERE, "An Error occurred while matching the type of a Respawning boss to a boss type. Type:" + this.type).logToFile();
            return;
        }

        LivingEntity entity = (LivingEntity) world.spawnEntity(location, this.type);

        try {
            retriever.instance.makeBoss(entity);
        } catch (BossCreationException e) {
            new VBLogger("RespawningBoss", Level.SEVERE, "Could not edit the Entity to a boss. Exception: " + e + "\n" +
                    "Retriever: " + retriever).logToFile();
        }

        entity.getScoreboardTags().add(RESPAWNING_BOSS_TAG);
        entity.getPersistentDataContainer().set(RESPAWNING_BOSS_PDC, PersistentDataType.INTEGER_ARRAY, this.commandIndexes);
    }
}
