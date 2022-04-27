package me.shreb.vanillabosses.listeners;

import me.shreb.vanillabosses.bosses.VBBoss;
import me.shreb.vanillabosses.bosses.utility.BossDataRetriever;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class DamageModifiers implements Listener {

    /**
     * Event handler in order to modify damage dealt by bosses
     * @param event the event to check for the damager being a boss in.
     */
    @EventHandler
    public void onBossDamageEntity(EntityDamageByEntityEvent event) {

        EntityType type = event.getDamager().getType();

        if(!event.getDamager().getScoreboardTags().contains(VBBoss.BOSSTAG)) return;

        BossDataRetriever retriever;
        //TODO implement projectile damage modifiers
        try {
            retriever = new BossDataRetriever(type);
        } catch(IllegalArgumentException ignored){
            return;
        }

        double modifier = retriever.damageModifier;

        event.setDamage(event.getFinalDamage() * modifier);

    }
}
