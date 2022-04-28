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

        //iterate over the bossData.commandIndexes gotten from config. This contains the indexes of the commands which are supposed to be executed
        for (int i : bossData.commandIndexes) {
            //Separate command from Delay integer
            String[] strings = BossCommand.getCommandMap().get(i).split("DELAY:");

            //make sure the strings array has 2 objects. If it doesn't the Command string was faulty
            if (strings.length != 2) {
                new VBLogger(getClass().getName(), Level.WARNING, "Bad Command, 'DELAY:' was found more than one time or not at all. \n" +
                        "If you do not want to have a delay to the command, please use 'DELAY: 0'\n" +
                        "Command: " + BossCommand.getCommandMap().get(i)).logToFile();
                //go to next command, which may not be faulty, thus not break;
                continue;
            }

            //Parse the second object inside the String array to get the intended delay
            int delay;
            try {
                delay = Integer.parseInt(strings[1]);
            } catch(NumberFormatException e){
                //log to the file and default to the value '0' in case the delay cannot be read
                new VBLogger(getClass().getName(), Level.WARNING, "Could not read delay from command string. Defaulting to 0. Command: " + BossCommand.getCommandMap().get(i)).logToFile();
                delay = 0;
            }

            //additional check for negative values
            if(delay < 0) {
                delay = 0;
                new VBLogger(getClass().getName(), Level.WARNING, "Read a negative value for Delay of the command. Defaulting to 0. Command: " + BossCommand.getCommandMap().get(i)).logToFile();
            }

            //Make a new BossCommand object using the index of the command, the delay intended and the command String which is supposed to be executed
            BossCommand command = new BossCommand(i, delay, strings[0]);
            //replace and read all placeholders, add necessary players to a list to execute the commands for, then execute the command for the players in that list
            command.executeBossCommand(event);
        }
    }
}
