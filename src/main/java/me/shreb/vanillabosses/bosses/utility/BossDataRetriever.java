package me.shreb.vanillabosses.bosses.utility;

import me.shreb.vanillabosses.bosses.*;
import me.shreb.vanillabosses.logging.VBLogger;
import me.shreb.vanillabosses.utility.DataRetriever;
import org.bukkit.ChatColor;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * A class to quickly and easily retrieve the data from any boss type.
 */
public class BossDataRetriever extends DataRetriever {

    public String SCOREBOARDTAG;
    public VBBoss instance;
    public double health;
    public double damageModifier;
    public double spawnChance;
    public int droppedXP;
    public String bossKilledMessage;
    public ArrayList<String> droppedItems;
    public boolean spawnNaturally;
    public List<Integer> commandIndexes;

    /**
     * Gets a new BossDataRetriever Object which contains the instance of the corresponding boss type, the corresponding Config section and the corresponding Scoreboard tag.
     *
     * @param type the type which to retrieve the data for
     * @throws IllegalArgumentException if the type passed in was null or the type did not correspond to any of the existing Boss types.
     */
    public BossDataRetriever(EntityType type) throws IllegalArgumentException {

        if (type == null) throw new IllegalArgumentException("Type passed in was null");

        switch (type) {

            case BLAZE:
                this.CONFIGSECTION = BlazeBoss.CONFIGSECTION;
                this.SCOREBOARDTAG = BlazeBoss.SCOREBOARDTAG;
                this.instance = BlazeBoss.instance;
                break;

            case CREEPER:
                this.CONFIGSECTION = CreeperBoss.CONFIGSECTION;
                this.SCOREBOARDTAG = CreeperBoss.SCOREBOARDTAG;
                this.instance = CreeperBoss.instance;
                break;


            case ENDERMAN:
                this.CONFIGSECTION = EndermanBoss.CONFIGSECTION;
                this.SCOREBOARDTAG = EndermanBoss.SCOREBOARDTAG;
                this.instance = EndermanBoss.instance;
                break;


            case MAGMA_CUBE:
                this.CONFIGSECTION = MagmacubeBoss.CONFIGSECTION;
                this.SCOREBOARDTAG = MagmacubeBoss.SCOREBOARDTAG;
                this.instance = MagmacubeBoss.instance;
                break;


            case SKELETON:
                this.CONFIGSECTION = SkeletonBoss.CONFIGSECTION;
                this.SCOREBOARDTAG = SkeletonBoss.SCOREBOARDTAG;
                this.instance = SkeletonBoss.instance;
                break;


            case SLIME:
                this.CONFIGSECTION = SlimeBoss.CONFIGSECTION;
                this.SCOREBOARDTAG = SlimeBoss.SCOREBOARDTAG;
                this.instance = SlimeBoss.instance;
                break;


            case SPIDER:
                this.CONFIGSECTION = SpiderBoss.CONFIGSECTION;
                this.SCOREBOARDTAG = SpiderBoss.SCOREBOARDTAG;
                this.instance = SpiderBoss.instance;
                break;


            case WITCH:
                this.CONFIGSECTION = WitchBoss.CONFIGSECTION;
                this.SCOREBOARDTAG = WitchBoss.SCOREBOARDTAG;
                this.instance = WitchBoss.instance;
                break;

            case WITHER:
                this.CONFIGSECTION = WitherBoss.CONFIGSECTION;
                this.SCOREBOARDTAG = WitherBoss.SCOREBOARDTAG;
                this.instance = WitherBoss.instance;
                break;


            case ZOMBIE:
                this.CONFIGSECTION = ZombieBoss.CONFIGSECTION;
                this.SCOREBOARDTAG = ZombieBoss.SCOREBOARDTAG;
                this.instance = ZombieBoss.instance;
                break;


            case ZOMBIFIED_PIGLIN:
                this.CONFIGSECTION = Zombified_PiglinBoss.CONFIGSECTION;
                this.SCOREBOARDTAG = Zombified_PiglinBoss.SCOREBOARDTAG;
                this.instance = Zombified_PiglinBoss.instance;
                break;

            default:
                throw new IllegalArgumentException("Type specified was not a type of boss.");
        }

        this.health = this.instance.config.getDouble("health");
        this.damageModifier = this.instance.config.getDouble("DamageModifier");
        this.spawnChance = this.instance.config.getDouble("spawnChance");
        this.droppedXP = this.instance.config.getInt("droppedXP");
        this.bossKilledMessage = this.instance.config.getString("killedMessage");
        this.droppedItems = (ArrayList<String>) this.instance.config.getStringList("droppedItems");
        this.spawnNaturally = this.instance.config.getBoolean("spawnNaturally");

        String indexString = this.instance.config.getString("CommandToBeExecutedOnDeath");

        if (indexString != null) {
            String[] strings = indexString.split(";");
            try {
                this.commandIndexes = Arrays.stream(strings).map(Integer::parseInt).collect(Collectors.toList());
            } catch (NumberFormatException e) {
                new VBLogger("BossDataRetriever", Level.WARNING, "Could not retrieve command indexes from the following String: " + indexString).logToFile();
            }
        }
    }

    /**
     * Gets a new BossDataRetriever Object which contains the instance of the corresponding boss type, the corresponding Config section and the corresponding Scoreboard tag.
     *
     * @param entity The Entity which to get the data from
     * @throws IllegalArgumentException if the entity passed in does not have the VBBoss.BOSSTAG Scoreboardtag.
     */
    public BossDataRetriever(LivingEntity entity) throws IllegalArgumentException {

        if (!entity.getScoreboardTags().contains(VBBoss.BOSSTAG))
            throw new IllegalArgumentException("Attempted to retrieve Boss data from a non Boss entity");

        EntityType type = entity.getType();

        switch (type) {

            case BLAZE:
                this.CONFIGSECTION = BlazeBoss.CONFIGSECTION;
                this.SCOREBOARDTAG = BlazeBoss.SCOREBOARDTAG;
                this.instance = BlazeBoss.instance;
                break;

            case CREEPER:
                this.CONFIGSECTION = CreeperBoss.CONFIGSECTION;
                this.SCOREBOARDTAG = CreeperBoss.SCOREBOARDTAG;
                this.instance = CreeperBoss.instance;
                break;


            case ENDERMAN:
                this.CONFIGSECTION = EndermanBoss.CONFIGSECTION;
                this.SCOREBOARDTAG = EndermanBoss.SCOREBOARDTAG;
                this.instance = EndermanBoss.instance;
                break;


            case MAGMA_CUBE:
                this.CONFIGSECTION = MagmacubeBoss.CONFIGSECTION;
                this.SCOREBOARDTAG = MagmacubeBoss.SCOREBOARDTAG;
                this.instance = MagmacubeBoss.instance;
                break;


            case SKELETON:
                this.CONFIGSECTION = SkeletonBoss.CONFIGSECTION;
                this.SCOREBOARDTAG = SkeletonBoss.SCOREBOARDTAG;
                this.instance = SkeletonBoss.instance;
                break;


            case SLIME:
                this.CONFIGSECTION = SlimeBoss.CONFIGSECTION;
                this.SCOREBOARDTAG = SlimeBoss.SCOREBOARDTAG;
                this.instance = SlimeBoss.instance;
                break;


            case SPIDER:
                this.CONFIGSECTION = SpiderBoss.CONFIGSECTION;
                this.SCOREBOARDTAG = SpiderBoss.SCOREBOARDTAG;
                this.instance = SpiderBoss.instance;
                break;


            case WITCH:
                this.CONFIGSECTION = WitchBoss.CONFIGSECTION;
                this.SCOREBOARDTAG = WitchBoss.SCOREBOARDTAG;
                this.instance = WitchBoss.instance;
                break;

            case WITHER:
                this.CONFIGSECTION = WitherBoss.CONFIGSECTION;
                this.SCOREBOARDTAG = WitherBoss.SCOREBOARDTAG;
                this.instance = WitherBoss.instance;
                break;


            case ZOMBIE:
                this.CONFIGSECTION = ZombieBoss.CONFIGSECTION;
                this.SCOREBOARDTAG = ZombieBoss.SCOREBOARDTAG;
                this.instance = ZombieBoss.instance;
                break;


            case ZOMBIFIED_PIGLIN:
                this.CONFIGSECTION = Zombified_PiglinBoss.CONFIGSECTION;
                this.SCOREBOARDTAG = Zombified_PiglinBoss.SCOREBOARDTAG;
                this.instance = Zombified_PiglinBoss.instance;
                break;

            default:
                throw new IllegalArgumentException("The Entity passed in had the correct tags, but did not have the type of an existing boss.");

        }

        this.health = this.instance.config.getDouble("health");
        this.damageModifier = this.instance.config.getDouble("DamageModifier");
        this.spawnChance = this.instance.config.getDouble("spawnChance");
        this.droppedXP = this.instance.config.getInt("droppedXP");
        this.bossKilledMessage = this.instance.config.getString("killedMessage");
        this.droppedItems = (ArrayList<String>) this.instance.config.getStringList("droppedItems");
        this.spawnNaturally = this.instance.config.getBoolean("spawnNaturally");

        String indexString = this.instance.config.getString("CommandToBeExecutedOnDeath");

        if (indexString != null) {
            String[] strings = indexString.split(";");
            try {
                this.commandIndexes = Arrays.stream(strings).map(Integer::parseInt).collect(Collectors.toList());
            } catch (NumberFormatException e) {
                new VBLogger("BossDataRetriever", Level.WARNING, "Could not retrieve command indexes from the following String: " + indexString).logToFile();
            }
        }
    }

    /**
     * @return all important data for the boss in a format which is easily readable in game chat or console
     */
    @Override
    public String toString() {
        return "Config Section: " + ChatColor.AQUA + this.CONFIGSECTION + "\n" +
                "Scoreboard tag: " + ChatColor.AQUA + this.SCOREBOARDTAG + "\n" +
                "Boss health: " + ChatColor.AQUA + this.health + "\n" +
                "Damage Modifier: " + ChatColor.AQUA + this.damageModifier + "\n" +
                "Spawn Chance: " + ChatColor.AQUA + this.spawnChance + "\n" +
                "Dropped EXP: " + ChatColor.AQUA + this.droppedXP + "\n" +
                "Message on Kill: " + ChatColor.AQUA + this.bossKilledMessage + "\n" +
                "Drops: " + ChatColor.AQUA + this.bossKilledMessage + "\n" +
                "Spawns naturally: " + ChatColor.AQUA + this.spawnNaturally;
    }
}