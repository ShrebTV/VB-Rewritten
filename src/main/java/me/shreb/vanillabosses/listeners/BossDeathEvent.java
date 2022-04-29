package me.shreb.vanillabosses.listeners;

import me.shreb.vanillabosses.bosses.bossRepresentation.RespawningBoss;
import me.shreb.vanillabosses.bosses.utility.BossCommand;
import me.shreb.vanillabosses.bosses.utility.BossDataRetriever;
import me.shreb.vanillabosses.bosses.utility.BossDrops;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.persistence.PersistentDataType;

public class BossDeathEvent implements Listener {

    @EventHandler
    public void onBossDeath(EntityDeathEvent event) {

        LivingEntity entity = event.getEntity();

        //boss data object
        BossDataRetriever bossData;

        try {
            //put in the boss data
            bossData = new BossDataRetriever(entity);
        } catch (IllegalArgumentException ignored) {
            //If it fails, the entity was not a boss, ignore the error
            return;
        }

        //Get the boss drops corresponding to the boss data object
        BossDrops drops = new BossDrops(bossData);
        //Drop the items from the BossDrops object after converting them toItemStacks()
        BossDrops.dropItemStacks(drops.toItemStacks(), event.getEntity().getLocation());

        //Make this section only applies for normal bosses, not respawning bosses
        if (!entity.getScoreboardTags().contains(RespawningBoss.RESPAWNING_BOSS_TAG)) {
            //iterate over the bossData.commandIndexes gotten from config. This contains the indexes of the commands which are supposed to be executed
            for (int i : bossData.commandIndexes) {
                //Make a new BossCommand object using the index of the command, the delay intended and the command String which is supposed to be executed
                BossCommand command = new BossCommand(i);
                //replace and read all placeholders, add necessary players to a list to execute the commands for, then execute the command for the players in that list
                command.executeBossCommand(event);
            }
        } else if (entity.getPersistentDataContainer().has(RespawningBoss.RESPAWNING_BOSS_PDC, PersistentDataType.INTEGER_ARRAY)
                && entity.getPersistentDataContainer().get(RespawningBoss.RESPAWNING_BOSS_PDC, PersistentDataType.INTEGER_ARRAY) != null) {

            int[] indexes = entity.getPersistentDataContainer().get(RespawningBoss.RESPAWNING_BOSS_PDC, PersistentDataType.INTEGER_ARRAY);

            if(indexes == null) return;

            for (int i : indexes) {
                //Make a new BossCommand object using the index of the command, the delay intended and the command String which is supposed to be executed
                BossCommand command = new BossCommand(i);
                //replace and read all placeholders, add necessary players to a list to execute the commands for, then execute the command for the players in that list
                command.executeBossCommand(event);
            }
        }
    }
}
