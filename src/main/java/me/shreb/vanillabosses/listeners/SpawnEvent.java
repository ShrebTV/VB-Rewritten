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

        EntityType type = event.getEntityType();

        LivingEntity entity = event.getEntity();

        BossDataRetriever retriever;

        try {
            retriever = new BossDataRetriever(event.getEntity().getType());
        } catch (IllegalArgumentException ignored) {
            return;
        }

        FileConfiguration config = retriever.instance.config;

        String chancePath = "spawnChance";
        double chance = config.getDouble(chancePath);

        if (!spawnWorldChecker(event)) return;

        if (Utility.roll(chance)) {

            switch (type) {

                case BLAZE:

                    try {
                        BlazeBoss.instance.makeBoss(entity);
                    } catch (BossCreationException e) {
                        new VBLogger(getClass().getName(), Level.WARNING, "Problem detected while naturally spawning Blaze Boss.\n" +
                                e + "\n" + entity + "\n" + event.getSpawnReason());
                    }
                    break;

                case CREEPER:

                    try {
                        CreeperBoss.instance.makeBoss(entity);
                    } catch (BossCreationException e) {
                        new VBLogger(getClass().getName(), Level.WARNING, "Problem detected while naturally spawning Creeper Boss.\n" +
                                e + "\n" + entity + "\n" + event.getSpawnReason());
                    }
                    break;

                case ENDERMAN:

                    try {
                        EndermanBoss.instance.makeBoss(entity);
                    } catch (BossCreationException e) {
                        new VBLogger(getClass().getName(), Level.WARNING, "Problem detected while naturally spawning Enderman Boss.\n" +
                                e + "\n" + entity + "\n" + event.getSpawnReason());
                    }
                    break;

                case MAGMA_CUBE:

                    try {
                        MagmacubeBoss.instance.makeBoss(entity);
                    } catch (BossCreationException e) {
                        new VBLogger(getClass().getName(), Level.WARNING, "Problem detected while naturally spawning Magmacube Boss.\n" +
                                e + "\n" + entity + "\n" + event.getSpawnReason());
                    }
                    break;

                case SKELETON:

                    try {
                        SkeletonBoss.instance.makeBoss(entity);
                    } catch (BossCreationException e) {
                        new VBLogger(getClass().getName(), Level.WARNING, "Problem detected while naturally spawning Skeleton Boss.\n" +
                                e + "\n" + entity + "\n" + event.getSpawnReason());
                    }
                    break;

                case SLIME:

                    try {
                        SlimeBoss.instance.makeBoss(entity);
                    } catch (BossCreationException e) {
                        new VBLogger(getClass().getName(), Level.WARNING, "Problem detected while naturally spawning Slime Boss.\n" +
                                e + "\n" + entity + "\n" + event.getSpawnReason());
                    }
                    break;

                case SPIDER:

                    try {
                        SpiderBoss.instance.makeBoss(entity);
                    } catch (BossCreationException e) {
                        new VBLogger(getClass().getName(), Level.WARNING, "Problem detected while naturally spawning Spider Boss.\n" +
                                e + "\n" + entity + "\n" + event.getSpawnReason());
                    }
                    break;

                case WITCH:

                    try {
                        WitchBoss.instance.makeBoss(entity);
                    } catch (BossCreationException e) {
                        new VBLogger(getClass().getName(), Level.WARNING, "Problem detected while naturally spawning Witch Boss.\n" +
                                e + "\n" + entity + "\n" + event.getSpawnReason());
                    }
                    break;

                case ZOMBIE:

                    try {
                        ZombieBoss.instance.makeBoss(entity);
                        ZombieBoss.zombieHorde(
                                config.getInt("zombieHorde.radius"),
                                config.getInt("zombieHorde.amount"),
                                event.getLocation()
                        );
                    } catch (BossCreationException e) {
                        new VBLogger(getClass().getName(), Level.WARNING, "Problem detected while naturally spawning Zombie Boss.\n" +
                                e + "\n" + entity + "\n" + event.getSpawnReason());
                    }

                    break;

                case ZOMBIFIED_PIGLIN:

                    try {
                        Zombified_PiglinBoss.instance.makeBoss(entity);
                    } catch (BossCreationException e) {
                        new VBLogger(getClass().getName(), Level.WARNING, "Problem detected while naturally spawning Zombified Piglin Boss.\n" +
                                e + "\n" + entity + "\n" + event.getSpawnReason());
                    }
                    break;
            }

            if (Vanillabosses.getInstance().getConfig().getBoolean("Bosses.AllBossesHaveBossBars")) {
                new VBBossBar(event.getEntity(), Bukkit.createBossBar(event.getEntity().getName(), BarColor.BLUE, BarStyle.SOLID, BarFlag.PLAY_BOSS_MUSIC));
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

        BossDataRetriever retriever;

        try { // Attempt to get the section from the entity passed in by the event
            retriever = new BossDataRetriever(entity.getType()); //entity type passed in has a boss version
        } catch (IllegalArgumentException ignored) {
            return false; //Entity type passed in does not have a boss version
        }

        String configPath = "spawnWorlds"; // making a path out of the section which was just retrieved

        ArrayList<String> worlds = new ArrayList<>(retriever.instance.config.getStringList(configPath)); //a new List with all the values of the config String list corresponding to the entity type

        if (worlds.isEmpty()) return true; //no worlds specified, returns true

        return worlds.contains(event.getLocation().getWorld().getName());
        //the world the entity spawned in was specified inside the config, returns true;
        //the world the entity spawned in was not specified inside the config, returns false;
    }
}
