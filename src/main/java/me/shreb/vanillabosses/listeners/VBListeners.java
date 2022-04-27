package me.shreb.vanillabosses.listeners;

import me.shreb.vanillabosses.Vanillabosses;
import me.shreb.vanillabosses.logging.VBLogger;
import org.bukkit.plugin.PluginManager;

import java.util.logging.Level;

public class VBListeners {

    public static void registerListeners(){

        PluginManager pm = Vanillabosses.getInstance().getServer().getPluginManager();

        pm.registerEvents(new AntiRepairListener(), Vanillabosses.getInstance());
        pm.registerEvents(new BossDeathEvent(), Vanillabosses.getInstance());
        pm.registerEvents(new DamageModifiers(), Vanillabosses.getInstance());
        pm.registerEvents(new SpawnEvent(), Vanillabosses.getInstance());

        new VBLogger("VBListeners", Level.INFO, "Registered VBListeners").logToFile();
    }
}
