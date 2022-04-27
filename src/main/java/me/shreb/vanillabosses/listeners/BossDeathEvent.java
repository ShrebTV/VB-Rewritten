package me.shreb.vanillabosses.listeners;

import me.shreb.vanillabosses.bosses.utility.BossDataRetriever;
import me.shreb.vanillabosses.bosses.utility.BossDrops;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

public class BossDeathEvent implements Listener {

    @EventHandler
    public void onBossDeath(EntityDeathEvent event) {

        Entity entity = event.getEntity();
        EntityType type = entity.getType();

        BossDataRetriever bossData = new BossDataRetriever(type);

        BossDrops drops = new BossDrops(bossData);

        BossDrops.dropItemStacks(drops.toItemStacks(), event.getEntity().getLocation());

        //TODO implement boss commands here

    }
}
