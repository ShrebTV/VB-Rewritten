package me.shreb.vanillabosses.listeners;

import me.shreb.vanillabosses.Vanillabosses;
import me.shreb.vanillabosses.items.*;
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
        pm.registerEvents(new AFKChecker(), Vanillabosses.getInstance());

        BaseballBat.instance.registerListener();
        Blazer.instance.registerListener();
        BossEggs.instance.registerListener();
        BouncySlime.instance.registerListener();
        ButchersAxe.instance.registerListener();
        HeatedMagmaCream.instance.registerListener();
        InvisibilityCloak.instance.registerListener();
        Skeletor.instance.registerListener();
        SlimeBoots.instance.registerListener();
        Slingshot.instance.registerListener();

        new VBLogger("VBListeners", Level.INFO, "Registered VBListeners").logToFile();
    }
}
