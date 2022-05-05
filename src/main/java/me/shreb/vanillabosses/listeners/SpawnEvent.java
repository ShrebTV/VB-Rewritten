package me.shreb.vanillabosses.listeners;

import me.shreb.vanillabosses.Vanillabosses;
import me.shreb.vanillabosses.bosses.*;
import me.shreb.vanillabosses.bosses.utility.BossCreationException;
import me.shreb.vanillabosses.bosses.utility.BossDataRetriever;
import me.shreb.vanillabosses.bosses.utility.VBBossBar;
import me.shreb.vanillabosses.logging.VBLogger;
import me.shreb.vanillabosses.utility.Utility;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

import java.util.ArrayList;
import java.util.logging.Level;

public class SpawnEvent implements Listener {

    public static boolean spawn = true;

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {

        if (!spawn) return;

        FileConfiguration config = Vanillabosses.getInstance().getConfig();

        EntityType type = event.getEntityType();

        LivingEntity entity = event.getEntity();

        String section;
        try {
            section = new BossDataRetriever(event.getEntity().getType()).CONFIGSECTION;
        } catch (IllegalArgumentException ignored) {
            return;
        }

        String chancePath = "Bosses." + section + ".spawnChance";
        double chance = config.getDouble(chancePath);

        if (!spawnWorldChecker(event)) return;

        if (Utility.roll(chance)) {

            LivingEntity spawnedBoss = null;

            switch (type) {

                case BLAZE:

                    try {
                        spawnedBoss = BlazeBoss.instance.makeBoss(entity);
                    } catch (BossCreationException e) {
                        new VBLogger(getClass().getName(), Level.WARNING, "Problem detected while naturally spawning Blaze Boss.\n" +
                                e + "\n" + entity + "\n" + event.getSpawnReason());
                    }
                    break;

                case CREEPER:

                    try {
                        spawnedBoss = CreeperBoss.instance.makeBoss(entity);
                    } catch (BossCreationException e) {
                        new VBLogger(getClass().getName(), Level.WARNING, "Problem detected while naturally spawning Creeper Boss.\n" +
                                e + "\n" + entity + "\n" + event.getSpawnReason());
                    }
                    break;

                case ENDERMAN:

                    try {
                        spawnedBoss = EndermanBoss.instance.makeBoss(entity);
                    } catch (BossCreationException e) {
                        new VBLogger(getClass().getName(), Level.WARNING, "Problem detected while naturally spawning Enderman Boss.\n" +
                                e + "\n" + entity + "\n" + event.getSpawnReason());
                    }
                    break;

                case MAGMA_CUBE:

                    try {
                        spawnedBoss = MagmacubeBoss.instance.makeBoss(entity);
                    } catch (BossCreationException e) {
                        new VBLogger(getClass().getName(), Level.WARNING, "Problem detected while naturally spawning Magmacube Boss.\n" +
                                e + "\n" + entity + "\n" + event.getSpawnReason());
                    }
                    break;

                case SKELETON:

                    try {
                        spawnedBoss = SkeletonBoss.instance.makeBoss(entity);
                    } catch (BossCreationException e) {
                        new VBLogger(getClass().getName(), Level.WARNING, "Problem detected while naturally spawning Skeleton Boss.\n" +
                                e + "\n" + entity + "\n" + event.getSpawnReason());
                    }
                    break;

                case SLIME:

                    try {
                        spawnedBoss = SlimeBoss.instance.makeBoss(entity);
                    } catch (BossCreationException e) {
                        new VBLogger(getClass().getName(), Level.WARNING, "Problem detected while naturally spawning Slime Boss.\n" +
                                e + "\n" + entity + "\n" + event.getSpawnReason());
                    }
                    break;

                case SPIDER:

                    try {
                        spawnedBoss = SpiderBoss.instance.makeBoss(entity);
                    } catch (BossCreationException e) {
                        new VBLogger(getClass().getName(), Level.WARNING, "Problem detected while naturally spawning Spider Boss.\n" +
                                e + "\n" + entity + "\n" + event.getSpawnReason());
                    }
                    break;

                case WITCH:

                    try {
                        spawnedBoss = WitchBoss.instance.makeBoss(entity);
                    } catch (BossCreationException e) {
                        new VBLogger(getClass().getName(), Level.WARNING, "Problem detected while naturally spawning Witch Boss.\n" +
                                e + "\n" + entity + "\n" + event.getSpawnReason());
                    }
                    break;

                case ZOMBIE:

                    try {
                        spawnedBoss = ZombieBoss.instance.makeBoss(entity);
                        ZombieBoss.zombieHorde(
                                config.getInt("Bosses.ZombieBoss.zombieHorde.radius"),
                                config.getInt("Bosses.ZombieBoss.zombieHorde.amount"),
                                event.getLocation()
                        );
                    } catch (BossCreationException e) {
                        new VBLogger(getClass().getName(), Level.WARNING, "Problem detected while naturally spawning Zombie Boss.\n" +
                                e + "\n" + entity + "\n" + event.getSpawnReason());
                    }

                    break;

                case ZOMBIFIED_PIGLIN:

                    try {
                        spawnedBoss = Zombified_PiglinBoss.instance.makeBoss(entity);
                    } catch (BossCreationException e) {
                        new VBLogger(getClass().getName(), Level.WARNING, "Problem detected while naturally spawning Zombified Piglin Boss.\n" +
                                e + "\n" + entity + "\n" + event.getSpawnReason());
                    }
                    break;

                default:
                    return;
            }

            if(spawnedBoss != null && config.getBoolean("Bosses.AllBossesHaveBossBars")){
                new VBBossBar(spawnedBoss, Bukkit.createBossBar(spawnedBoss.getName(), BarColor.BLUE, BarStyle.SOLID, BarFlag.PLAY_BOSS_MUSIC)).showBossBar();
            }
        }
    }

    /**
     * a method to check whether the boss version of the spawned entity is allowed to naturally spawn inside that world.
     *
     * @param event The event to check for the boss' spawn worlds in
     * @return true if and only if the boss version of the entity is allowed to spawn inside the world the entity spawned in. False if the boss version is not allowed to spawn or the entity passed in does not have a boss version
     */
    private boolean spawnWorldChecker(CreatureSpawnEvent event) {

        LivingEntity entity = event.getEntity();
        String section;

        try { // Attempt to get the section from the entity passed in by the event
            section = new BossDataRetriever(entity.getType()).CONFIGSECTION; //entity type passed in has a boss version
        } catch (IllegalArgumentException ignored) {
            return false; //Entity type passed in does not have a boss version
        }

        String configPath = "Bosses." + section + ".spawnWorlds"; // making a path out of the section which was just retrieved

        ArrayList<String> worlds = new ArrayList<>(Vanillabosses.getInstance().getConfig().getStringList(configPath)); //a new List with all the values of the config String list corresponding to the entity type

        if (worlds.isEmpty()) return true; //no worlds specified, returns true

        return worlds.contains(event.getLocation().getWorld().getName());
        //the world the entity spawned in was specified inside the config, returns true;
        //the world the entity spawned in was not specified inside the config, returns false;
    }
}
