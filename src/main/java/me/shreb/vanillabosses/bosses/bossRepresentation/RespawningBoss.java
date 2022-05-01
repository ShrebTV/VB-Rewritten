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
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

public class RespawningBoss extends Boss {

    //A list of respawning bosses which are enabled and spawned.
    static List<RespawningBoss> bossList = new ArrayList<>();

    // The String a respawning boss has in its ScoreboardTags container
    public static final String RESPAWNING_BOSS_TAG = "Respawning Boss";

    //The NamespaceKey used to store the commands for respawning bosses
    public static final NamespacedKey RESPAWNING_BOSS_PDC = new NamespacedKey(Vanillabosses.getInstance(), "Respawning Boss");

    //A map which contains all Respawning bosses from the bossList as keys and the UUID of the currently alive boss.
    public static final HashMap<RespawningBoss, UUID> livingRespawningBossesMap = new HashMap<>();

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


    /**
     * This method is used to initialize the spawning of respawning bosses.
     * Should only be called once by the onEnable() method
     */
    public static void spawnRespawningBosses() {
//TODO implement spawnRespawningBosses() method
    }

    /**
     * A method to get a RespawningBoss object from a String
     *
     * @param json the string to make into a RespawningBoss
     * @return the Respawning Boss made from the String
     * @throws IllegalArgumentException if the json is not parsable
     */
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

    /**
     * Serializes the object it is called on
     *
     * @return the Json string representing this object
     */
    public String serialize() {
        return new Gson().toJson(this);
    }


    /**
     * This Method spawns the boss specified by the RespawningBoss object it is called on.
     * fails if the enableBoss is set to false
     * fails if the world specified cannot be found
     * fails if there is a type mismatch between the specified type and a BossDataRetriever object
     */
    public LivingEntity spawnBoss() throws BossCreationException {

        if (!this.enableBoss) {
            new VBLogger(getClass().getName(), Level.WARNING, "Boss declared but disabled. Check whether this one is still needed! line: " + this.serialize()).logToFile();
            return null;
        }

        World world = Vanillabosses.getInstance().getServer().getWorld(this.world);
        if (world == null) {
            new VBLogger("RespawningBoss", Level.SEVERE, "Could not find specified world for respawning boss. World: " + this.world).logToFile();
            throw new BossCreationException("Could not find specified world for respawning boss");
        }

        Location location = new Location(world, this.x, this.y, this.z);
        BossDataRetriever retriever;
        try {
            retriever = new BossDataRetriever(this.type);
        } catch (IllegalArgumentException e) {
            new VBLogger("RespawningBoss", Level.SEVERE, "An Error occurred while matching the type of a Respawning boss to a boss type. Type:" + this.type).logToFile();
            throw new BossCreationException("An Error occurred while matching the type of a Respawning boss to a boss type");
        }

        LivingEntity entity = (LivingEntity) world.spawnEntity(location, this.type);

        try {
            retriever.instance.makeBoss(entity);
        } catch (BossCreationException e) {
            new VBLogger("RespawningBoss", Level.SEVERE, "Could not edit the Entity to a boss. Exception: " + e + "\n" +
                    "Retriever: " + retriever).logToFile();
        }

        addPDCTags(entity);

        livingRespawningBossesMap.put(this, entity.getUniqueId());

        entity.getScoreboardTags().add(RESPAWNING_BOSS_TAG);
        entity.getPersistentDataContainer().set(RESPAWNING_BOSS_PDC, PersistentDataType.INTEGER_ARRAY, this.commandIndexes);
        return entity;
    }


    public static final NamespacedKey SPAWN_WORLD   = new NamespacedKey(Vanillabosses.getInstance(), "VanillaBossesSpawnWorld");
    public static final NamespacedKey COMMANDS      = new NamespacedKey(Vanillabosses.getInstance(), "VanillaBossesCommandsOnDeath");
    public static final NamespacedKey RESPAWN_TIMER = new NamespacedKey(Vanillabosses.getInstance(), "VanillaBossesRespawnTime");
    public static final NamespacedKey X_COORDS      = new NamespacedKey(Vanillabosses.getInstance(), "VanillaBossesSpawnLocationX");
    public static final NamespacedKey Y_COORDS      = new NamespacedKey(Vanillabosses.getInstance(), "VanillaBossesSpawnLocationY");
    public static final NamespacedKey Z_COORDS      = new NamespacedKey(Vanillabosses.getInstance(), "VanillaBossesSpawnLocationZ");



    private void addPDCTags(LivingEntity entity) {

        PersistentDataContainer container = entity.getPersistentDataContainer();

        container.set(SPAWN_WORLD, PersistentDataType.STRING, this.world);

        container.set(COMMANDS, PersistentDataType.INTEGER_ARRAY, this.commandIndexes);

        container.set(RESPAWN_TIMER, PersistentDataType.LONG, this.respawnTime);

        container.set(X_COORDS, PersistentDataType.DOUBLE, this.x);

        container.set(Y_COORDS, PersistentDataType.DOUBLE, this.y);

        container.set(Z_COORDS, PersistentDataType.DOUBLE, this.z);

    }


    public EntityType getType() {
        return type;
    }

    public String getWorld() {
        return world;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public long getRespawnTime() {
        return respawnTime;
    }

    public boolean isEnableBoss() {
        return enableBoss;
    }
}
