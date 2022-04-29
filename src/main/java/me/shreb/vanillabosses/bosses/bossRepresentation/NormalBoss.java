package me.shreb.vanillabosses.bosses.bossRepresentation;

import me.shreb.vanillabosses.Vanillabosses;
import me.shreb.vanillabosses.bosses.utility.BossCreationException;
import me.shreb.vanillabosses.bosses.utility.BossDataRetriever;
import me.shreb.vanillabosses.logging.VBLogger;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;

import java.util.logging.Level;

public class NormalBoss extends Boss {

    BossDataRetriever retriever;

    public NormalBoss(EntityType type) {
        this.type = type;
        try {
            this.retriever = new BossDataRetriever(type);
        } catch (IllegalArgumentException e) {
            new VBLogger("NormalBoss", Level.WARNING, "Could not properly create NormalBoss object. This is usually due to a type mismatch and has to be fixed by the author. Type: " + this.type).logToFile();
        }
        setCommands();
    }

    /**
     * Sets the command for this Boss object.
     */
    private void setCommands() {

        String commandPath = "Bosses." + this.retriever.CONFIGSECTION + ".CommandToBeExecutedOnDeath";
        String commandIndexes = Vanillabosses.getInstance().getConfig().getString(commandPath);

        if (commandIndexes == null || commandIndexes.equals("")) {
            this.commandIndexes = new int[0];
            return;
        }

        String[] strings = commandIndexes.split(";");

        this.commandIndexes = new int[strings.length];


        for (int i = 0; i < strings.length; i++) {
            try {
                this.commandIndexes[i] = Integer.parseInt(strings[i]);
            } catch (NumberFormatException e) {
                new VBLogger(getClass().getName(), Level.WARNING, "Could not parse as Integer value at NormalBoss.setCommands().\n" +
                        "Please edit the commands for " + retriever.CONFIGSECTION + " to only be numbers separated by ';'").logToFile();
            }
        }
    }

    public void spawnBoss(Location location) {
        try {
            this.retriever.instance.makeBoss(location);
        } catch (BossCreationException e) {
            new VBLogger(getClass().getName(), Level.WARNING, "Could not spawn Boss. " + retriever.instance.getClass().getName() + " could not be spawned.\nError: " + e).logToFile();
        }
    }

    /**
     * A Method to make a NormalBoss object from a String. Null if String could not be matched with a boss type
     * @param string The String to parse for a Boss type for
     * @return A new NormalBoss object matching the type specified in the String. \n Null if the String could not be parsed for a Boss type.
     * @throws IllegalArgumentException If the String parameter is null
     * @throws NullPointerException If the String did not contain any Entity type at all
     */
    public static NormalBoss of(String string) throws IllegalArgumentException, NullPointerException {

        EntityType type = EntityType.valueOf(string.toUpperCase());

        try{
            new BossDataRetriever(type);
        } catch(IllegalArgumentException ignored){
            return null;
        }

        return new NormalBoss(type);
    }

    /**
     * A Method to make a NormalBoss object from an EntityType
     * @param type the type to make the NormalBoss Object from
     * @return A new NormalBoss object matching the type specified. \n Null if the type passed in did not match any Boss types.
     */
    public static NormalBoss of(EntityType type) {

        try{
            new BossDataRetriever(type);
        } catch(IllegalArgumentException ignored){
            return null;
        }

        return new NormalBoss(type);
    }
}
