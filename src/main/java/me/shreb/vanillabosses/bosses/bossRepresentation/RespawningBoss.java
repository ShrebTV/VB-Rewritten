package me.shreb.vanillabosses.bosses.bossRepresentation;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;
import me.shreb.vanillabosses.Vanillabosses;
import me.shreb.vanillabosses.bosses.utility.BossCreationException;
import me.shreb.vanillabosses.bosses.utility.BossDataRetriever;
import me.shreb.vanillabosses.bosses.utility.VBBossBar;
import me.shreb.vanillabosses.logging.VBLogger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;
import java.util.logging.Level;

public class RespawningBoss extends Boss {

    //A list of respawning bosses which are enabled and spawned.
    static List<RespawningBoss> bossList = new ArrayList<>();

    // The String a respawning boss has in its ScoreboardTags container
    public static final String RESPAWNING_BOSS_TAG = "Respawning Boss";

    //The NamespaceKey used to store the commands for respawning bosses
    public static final NamespacedKey RESPAWNING_BOSS_PDC = new NamespacedKey(Vanillabosses.getInstance(), "RespawningBoss");

    //A map which contains all Respawning bosses from the bossList as keys and the UUID of the currently alive boss.
    public static final HashMap<RespawningBoss, UUID> livingRespawningBossesMap = new HashMap<>();

    @SerializedName("worldName")
    String world;
    int x;
    int y;
    int z;

    long respawnTime;

    boolean enableBoss;

    // put the commands from the command list and their indexes into the commandMap in order to easily execute them later
    static {
        List<String> bossJsonList = new ArrayList<>(Vanillabosses.getInstance().getConfig().getStringList("Bosses.RespawningBosses"));

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
    public RespawningBoss(EntityType type, String world, int x, int y, int z, long respawnTime, int[] commands, boolean enableBoss) {
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

        if(bossList.isEmpty()){
            new VBLogger("RespawningBoss", Level.INFO, "Respawning boss list was empty. Not spawning any respawning bosses").logToFile();
        }

        for(RespawningBoss boss : bossList){
            try {
                boss.spawnBoss();
            } catch (BossCreationException e) {
                new VBLogger("RespawningBoss", Level.WARNING, "Could not initially spawn respawning boss. Boss: " + boss).logToFile();
            }
        }
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

        if (boss.respawnTime < 1) {
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

        entity.setRemoveWhenFarAway(false);

        livingRespawningBossesMap.put(this, entity.getUniqueId());

        entity.getScoreboardTags().add(RESPAWNING_BOSS_TAG);
        entity.getPersistentDataContainer().set(RESPAWNING_BOSS_PDC, PersistentDataType.INTEGER_ARRAY, this.commandIndexes);

        if (Vanillabosses.getInstance().getConfig().getBoolean("Bosses.RespawningBossesHaveBossbars")) {
            new VBBossBar(entity, Bukkit.createBossBar(entity.getName(), BarColor.RED, BarStyle.SOLID, BarFlag.PLAY_BOSS_MUSIC));
        }
        return entity;
    }

    //Tag constants for PDC of respawning bosses
    public static final NamespacedKey SPAWN_WORLD   = new NamespacedKey(Vanillabosses.getInstance(), "VanillaBossesSpawnWorld");
    public static final NamespacedKey COMMANDS      = new NamespacedKey(Vanillabosses.getInstance(), "VanillaBossesCommandsOnDeath");
    public static final NamespacedKey RESPAWN_TIMER = new NamespacedKey(Vanillabosses.getInstance(), "VanillaBossesRespawnTime");
    public static final NamespacedKey X_COORDS      = new NamespacedKey(Vanillabosses.getInstance(), "VanillaBossesSpawnLocationX");
    public static final NamespacedKey Y_COORDS      = new NamespacedKey(Vanillabosses.getInstance(), "VanillaBossesSpawnLocationY");
    public static final NamespacedKey Z_COORDS      = new NamespacedKey(Vanillabosses.getInstance(), "VanillaBossesSpawnLocationZ");


    /**
     * Adds the PDC tags of the respawning boss to the entity passed in
     * @param entity the entity to add the tags to
     */
    private void addPDCTags(LivingEntity entity) {

        PersistentDataContainer container = entity.getPersistentDataContainer();

        container.set(SPAWN_WORLD, PersistentDataType.STRING, this.world);

        container.set(COMMANDS, PersistentDataType.INTEGER_ARRAY, this.commandIndexes);

        container.set(RESPAWN_TIMER, PersistentDataType.LONG, this.respawnTime);

        container.set(X_COORDS, PersistentDataType.INTEGER, this.x);

        container.set(Y_COORDS, PersistentDataType.INTEGER, this.y);

        container.set(Z_COORDS, PersistentDataType.INTEGER, this.z);

    }

    @Override
    public String toString(){
        return "Type: '" + this.type + "'" +
                "World: '" + this.world + "'" +
                "Respawn Timer: '" + this.respawnTime + "'" +
                "Commands: '" + Arrays.toString(this.commandIndexes) + "'" +
                "Enabled: '" + this.enableBoss + "'" +
                "X-Coordinates: '" + this.x + "'" +
                "Y-Coordinates: '" + this.y + "'" +
                "Y-Coordinates: '" + this.z + "'";
    }

    /**
     * A method to respawn all non alive and non null respawning bosses using their specific details.
     * Using this method will attempt to respawn all respawning bosses in the bossList created at startup of the plugin
     * Will log an error if the bossList contains a value not present in the livingRespawningBossesMap
     * Will log an error if the spawning process failed for any reason
     */
    public static void respawnBosses(){
        for(RespawningBoss boss: bossList){

            if(livingRespawningBossesMap.containsKey(boss)){

                UUID uuid = livingRespawningBossesMap.get(boss);
                Entity entity = Bukkit.getEntity(uuid);

                if(entity == null || entity.isDead()){
                    Bukkit.getScheduler().scheduleSyncDelayedTask(Vanillabosses.getInstance(), () -> {

                        try {
                            LivingEntity e = boss.spawnBoss();
                        } catch (BossCreationException e) {
                            new VBLogger("RespawningBoss", Level.WARNING, "Respawning Boss could not be respawned. Please report this.\n" +
                                    "Error: " + e).logToFile();
                        }

                    }, boss.respawnTime * 20);
                }

            } else {
                new VBLogger("RespawningBoss", Level.WARNING, "Respawning Bosses map did not contain respawning boss. Please report this error").logToFile();
            }
        }
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
