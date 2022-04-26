package me.shreb.vanillabosses.bosses.bossRepresentation;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;
import me.shreb.vanillabosses.Vanillabosses;
import me.shreb.vanillabosses.logging.VBLogger;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

public class RespawningBoss extends Boss {

    static HashMap<Integer, String> commandMap = new HashMap<>();
    static List<RespawningBoss> bossList = new ArrayList<>();

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
        List<String> commandList = new ArrayList<>(config.getStringList("Bosses.CommandsExecutedOnBossDeath"));
        List<String> bossJsonList = new ArrayList<>(config.getStringList("Bosses.RespawningBosses"));

        for (String string : commandList) {
            int i = commandList.indexOf(string);
            commandMap.put(i, string);
        }

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
        try {
            return new Gson().fromJson(json, RespawningBoss.class);
        } catch (JsonSyntaxException e) {
            new VBLogger("RespawningBoss", Level.WARNING, "Unable to parse json String " + json + "\n If you cannot figure out what went wrong with your String, please contact the plugin author via discord.");
            throw new IllegalArgumentException("Could not deserialize Respawning Boss Data");
        }
    }

    //Method to serialize a RespawningBoss object into a json String
    public String serialize() {
        return new Gson().toJson(this);
    }

    @Override
    void spawnBoss(Location location) {

    }
}
