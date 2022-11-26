package me.shreb.vanillabosses.listeners;

import me.shreb.vanillabosses.Vanillabosses;
import me.shreb.vanillabosses.bosses.VBBoss;
import me.shreb.vanillabosses.bosses.utility.BossDataRetriever;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPortalExitEvent;

import java.util.List;

/**
 * Aims to not let bosses go through portals into worlds they are not allowed in
 */
public class BossUsePortalListener implements Listener {

    @EventHandler
    public void onBossUsePortal(EntityPortalExitEvent event) {
        Entity entity = event.getEntity();
        if (!entity.getScoreboardTags().contains(VBBoss.BOSSTAG)
                || !(entity instanceof LivingEntity)) {
            return;
        }

        BossDataRetriever retriever;
        try {
            retriever = new BossDataRetriever((LivingEntity) entity);
        } catch (IllegalArgumentException ignored) {
            return;
        }

        List<String> worlds = retriever.instance.config.getStringList("spawnWorlds");

        if (worlds.isEmpty()) {
            return;
        }
        Location exitLocation = event.getTo();

        if (exitLocation == null || exitLocation.getWorld() == null || worlds.contains(exitLocation.getWorld().getName()))
            return;

        Bukkit.getScheduler().scheduleSyncDelayedTask(Vanillabosses.getInstance(), entity::remove, 2);

        event.setCancelled(true);
    }
}
