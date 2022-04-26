package me.shreb.vanillabosses.bosses;

import me.shreb.vanillabosses.Vanillabosses;
import me.shreb.vanillabosses.bosses.utility.BossCreationException;
import me.shreb.vanillabosses.logging.VBLogger;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;

import java.util.logging.Level;

public abstract class VBBoss implements Listener {

    public static final String BOSSTAG = "VB-Boss";

    /**
     * creates a completely new Entity and makes it into a boss
     * @param location the location at which the boss should be spawned
     * @return the resulting boss
     * @throws BossCreationException if there is a problem creating the boss
     */
    public abstract LivingEntity makeBoss(Location location) throws BossCreationException;

    /**
     * Edits an existing Entity into a Boss version of said entity
     * @param entity the entity which is to be turned into a boss
     * @return the resulting boss
     * @throws BossCreationException if there is a problem creating the boss
     */
    public abstract LivingEntity makeBoss(LivingEntity entity) throws BossCreationException;

    public static void registerListeners(){

        PluginManager pm = Vanillabosses.getInstance().getServer().getPluginManager();

        pm.registerEvents(new BlazeBoss(), Vanillabosses.getInstance());
        pm.registerEvents(new CreeperBoss(), Vanillabosses.getInstance());
        pm.registerEvents(new EndermanBoss(), Vanillabosses.getInstance());
        pm.registerEvents(new MagmacubeBoss(), Vanillabosses.getInstance());
        pm.registerEvents(new SkeletonBoss(), Vanillabosses.getInstance());
        pm.registerEvents(new SlimeBoss(), Vanillabosses.getInstance());
        pm.registerEvents(new SpiderBoss(), Vanillabosses.getInstance());
        pm.registerEvents(new WitchBoss(), Vanillabosses.getInstance());
        pm.registerEvents(new WitherBoss(), Vanillabosses.getInstance());
        pm.registerEvents(new ZombieBoss(), Vanillabosses.getInstance());
        pm.registerEvents(new Zombified_PiglinBoss(), Vanillabosses.getInstance());

        new VBLogger("VBBoss", Level.INFO, "Registered Boss Event listeners!").logToFile();
    }

}
