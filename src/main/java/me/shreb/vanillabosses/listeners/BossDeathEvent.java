package me.shreb.vanillabosses.listeners;

import me.shreb.vanillabosses.bosses.*;
import me.shreb.vanillabosses.bosses.bossRepresentation.RespawningBoss;
import me.shreb.vanillabosses.bosses.utility.BossCommand;
import me.shreb.vanillabosses.bosses.utility.BossDataRetriever;
import me.shreb.vanillabosses.bosses.utility.BossDeathMessage;
import me.shreb.vanillabosses.bosses.utility.BossDrops;
import me.shreb.vanillabosses.items.*;
import me.shreb.vanillabosses.items.utility.ItemCreationException;
import me.shreb.vanillabosses.logging.VBLogger;
import me.shreb.vanillabosses.utility.Utility;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.persistence.PersistentDataType;

import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;

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

        if (bossData.instance.config.getBoolean("disableVanillaDrops")) {
            event.getDrops().clear();
        }

        if (event.getEntity().getKiller() != null) {
            new BossDeathMessage(bossData.bossKilledMessage, event).sendMessage();
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

                if (command.command == null) {
                    new VBLogger(getClass().getName(), Level.WARNING, "Used command which is not declared. Attempted to fetch a command with an index which did not exist.").logToFile();
                    continue;
                }
                //replace and read all placeholders, add necessary players to a list to execute the commands for, then execute the command for the players in that list
                command.executeBossCommand(event);
            }
        } else if (entity.getPersistentDataContainer().has(BossCommand.COMMAND_INDEX_KEY, PersistentDataType.INTEGER_ARRAY)
                && entity.getPersistentDataContainer().get(BossCommand.COMMAND_INDEX_KEY, PersistentDataType.INTEGER_ARRAY) != null) {

            int[] indexes = entity.getPersistentDataContainer().get(BossCommand.COMMAND_INDEX_KEY, PersistentDataType.INTEGER_ARRAY);

            if (indexes == null) return;

            for (int i : indexes) {
                //Make a new BossCommand object using the index of the command, the delay intended and the command String which is supposed to be executed
                BossCommand command = new BossCommand(i);
                if (command.command == null) {
                    new VBLogger(getClass().getName(), Level.WARNING, "Used command which is not declared. Attempted to fetch a command with an index which did not exist.").logToFile();
                    continue;
                }                //replace and read all placeholders, add necessary players to a list to execute the commands for, then execute the command for the players in that list
                command.executeBossCommand(event);
            }

            RespawningBoss.respawnBosses();

        }

        if (event.getEntity() instanceof Spider && event.getEntity().getScoreboardTags().contains(SpiderBoss.SCOREBOARDTAG)) {
            if (Utility.roll(Slingshot.instance.configuration.getDouble("chance"))) {
                try {
                    event.getDrops().add(Slingshot.instance.makeItem());
                } catch (ItemCreationException e) {
                    new VBLogger(getClass().getName(), Level.WARNING, "Could not make a Slingshot for Boss Drops." + e).logToFile();
                }
            }
        }

        if (event.getEntity() instanceof Enderman && event.getEntity().getScoreboardTags().contains(EndermanBoss.SCOREBOARDTAG)) {
            if (Utility.roll(InvisibilityCloak.instance.configuration.getDouble("chance"))) {
                try {
                    event.getDrops().add(InvisibilityCloak.instance.makeItem());
                } catch (ItemCreationException e) {
                    new VBLogger(getClass().getName(), Level.WARNING, "Could not make an Invisibility cloak for Boss Drops." + e).logToFile();
                }
            }
        }

        if (event.getEntity() instanceof Slime && event.getEntity().getScoreboardTags().contains(SlimeBoss.SCOREBOARDTAG)) {
            try {

                int toDrop = ThreadLocalRandom.current().
                        nextInt(BouncySlime.instance.configuration.getInt("MinDropped"), BouncySlime.instance.configuration.getInt("MaxDropped") + 1);

                event.getDrops().add(BouncySlime.instance.makeItem(toDrop));
                if (Utility.roll(SlimeBoots.instance.configuration.getDouble("dropChance"))) {

                    event.getDrops().add(SlimeBoots.instance.makeItem());

                }
            } catch (ItemCreationException e) {
                new VBLogger(getClass().getName(), Level.WARNING, "Could not make Slime boots for Boss Drops." + e).logToFile();
            }
        }

        if (event.getEntity() instanceof Blaze && event.getEntity().getScoreboardTags().contains(BlazeBoss.SCOREBOARDTAG)) {
            if (Utility.roll(Blazer.instance.configuration.getDouble("dropChance"))) {
                try {
                    event.getDrops().add(Blazer.instance.makeItem());
                } catch (ItemCreationException e) {
                    new VBLogger(getClass().getName(), Level.WARNING, "Could not make Blazer for Boss Drops." + e).logToFile();
                }
            }
        }

        if (event.getEntity() instanceof MagmaCube && event.getEntity().getScoreboardTags().contains(MagmacubeBoss.SCOREBOARDTAG)) {

            int drop1 = ThreadLocalRandom.current().nextInt(0, HeatedMagmaCream.instance.configuration.getInt("Level1.maxDropped") + 1);
            int drop2 = ThreadLocalRandom.current().nextInt(0, HeatedMagmaCream.instance.configuration.getInt("Level2.maxDropped") + 1);
            int drop3 = ThreadLocalRandom.current().nextInt(0, HeatedMagmaCream.instance.configuration.getInt("Level3.maxDropped") + 1);

            try {
                event.getDrops().add(new HeatedMagmaCream(1).makeItem(drop1));
                event.getDrops().add(new HeatedMagmaCream(2).makeItem(drop2));
                event.getDrops().add(new HeatedMagmaCream(3).makeItem(drop3));
            } catch (ItemCreationException e) {
                new VBLogger(getClass().getName(), Level.WARNING, "Could not make Blazer for Boss Drops." + e).logToFile();
            }
        }

        if (event.getEntityType() == EntityType.WITHER
                && event.getEntity().getScoreboardTags().contains(WitherBoss.SCOREBOARDTAG)
                && WitherEgg.instance.configuration.getBoolean("witherBossDropsEgg")) {
            try {
                event.getDrops().add(WitherEgg.instance.makeItem());
            } catch (ItemCreationException e) {
                new VBLogger(getClass().getName(), Level.WARNING, "Could not make Wither egg for Boss Drops." + e).logToFile();
            }
        }
    }
}