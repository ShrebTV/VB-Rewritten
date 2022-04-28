package me.shreb.vanillabosses.listeners;

import me.shreb.vanillabosses.bosses.utility.BossCommand;
import me.shreb.vanillabosses.bosses.utility.BossDataRetriever;
import me.shreb.vanillabosses.bosses.utility.BossDrops;
import me.shreb.vanillabosses.logging.VBLogger;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

import java.util.logging.Level;

public class BossDeathEvent implements Listener {

    @EventHandler
    public void onBossDeath(EntityDeathEvent event) {

        LivingEntity entity = event.getEntity();

        BossDataRetriever bossData;

        try {
            bossData = new BossDataRetriever(entity);
        } catch (IllegalArgumentException ignored) {
            return;
        }

        BossDrops drops = new BossDrops(bossData);

        BossDrops.dropItemStacks(drops.toItemStacks(), event.getEntity().getLocation());

        for (int i : bossData.commandIndexes) {
            String[] strings = BossCommand.getCommandMap().get(i).split("DELAY:");

            if (strings.length != 2) {
                new VBLogger(getClass().getName(), Level.WARNING, "Bad Command, 'DELAY:' was found more than one time or not at all. \n" +
                        "If you do not want to have a delay to the command, please use 'DELAY: 0'\n" +
                        "Command: " + BossCommand.getCommandMap().get(i)).logToFile();
                continue;
            }

            int delay = Integer.parseInt(strings[1]);

            BossCommand command = new BossCommand(i, delay, strings[0]);
            command.executeBossCommand(event);
        }
    }
}
