package me.shreb.vanillabosses.commands;

import me.shreb.vanillabosses.Vanillabosses;
import me.shreb.vanillabosses.bosses.bossRepresentation.RespawningBoss;
import me.shreb.vanillabosses.bosses.utility.BossDataRetriever;
import me.shreb.vanillabosses.logging.VBLogger;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.logging.Level;

/**
 * Command name -> "vbAdmin"
 * possible commands:   "vbAdmin respawningBoss (type) (respawnTime) [commands]"
 *                      "bossInfo (type)"
 */
public class AdminCommands extends VBCommands implements CommandExecutor {

    private static final AdminCommands INSTANCE = new AdminCommands();

    public static AdminCommands getInstance(){
        return INSTANCE;
    }

    /**
     * A command for admins to see what they have to put in the config file to make a working respawning Boss requiring them to enter as little information as possible
     */
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        // A command for admins to easily find out what to write into the config file in order to get the respawning boss they want where they want it.
        if (args[0].equalsIgnoreCase("respawningBoss")) {

            //Check whether the sender is a player who has op permissions
            if (!(sender instanceof Player) || !sender.isOp()) {
                sender.sendMessage("User of this command has to be a Player and OP");
                return false;
            }

            Location location = ((Player) sender).getLocation();
            String world;
            EntityType type;
            int respawnTime;
            int[] commands = new int[args.length - 3];

            if (args.length < 4) {
                sender.sendMessage("Not enough arguments");
                return false;
            }

            try {
                world = location.getWorld().getName();
                type = EntityType.valueOf(args[1].toUpperCase());
                new BossDataRetriever(type); //checking whether the boss type exists
                respawnTime = Integer.parseInt(args[2]);
            } catch (NullPointerException e) {
                sender.sendMessage("An Error has occurred. Please look at the log file for more details.");
                new VBLogger("RespawningBossCommand", Level.WARNING, "Nullpointer: " + e);
                return false;
            } catch (NumberFormatException e) { // respawn time was not a number
                sender.sendMessage("Expected Integer number. Found: " + args[2]);
                new VBLogger("RespawningBossCommand", Level.WARNING, "IllegalArgument: " + args[1]);
                return false;
            } catch (IllegalArgumentException e) { // Entity type not found or couldn't make type into a boss data retriever
                sender.sendMessage("Could not resolve Boss type " + args[1]);
                new VBLogger("RespawningBossCommand", Level.WARNING, "IllegalArgument: " + args[1]);
                return false;
            }

            int commandNumber;
            //adding the expected command indexes to a new arrray in order to set the commands array in the respawningBoss object
            for (int i = 3; i < args.length - 1; i++) {
                try {
                    commandNumber = Integer.parseInt(args[i]);
                    commands[i - 3] = commandNumber;

                } catch (NumberFormatException e) {
                    sender.sendMessage("Commands have to be referred to by numbers. Found: " + args[i]);
                    return false;
                }
            }

            RespawningBoss bossObject = new RespawningBoss(type, world, location.getX(), location.getY(), location.getZ(), respawnTime, commands, true);
            sender.sendMessage(bossObject.serialize());
            return true;

        } else if(args[0].equalsIgnoreCase("bossInfo")){
            // Display some of the boss config to the sender in a readable fashion for the boss requested.

            if(args.length < 2) return false;

            EntityType type;
            BossDataRetriever retriever;

            try{
                type = EntityType.valueOf(args[1]);
                retriever = new BossDataRetriever(type);
            } catch(IllegalArgumentException e){
                sender.sendMessage("Could not resolve boss type \"" + args[1] + "\"");
                return true;
            }

            sender.sendMessage(retriever.toString());
        }
        return false;
    }

    @Override
    void registerCommand() {
        try {
            Vanillabosses.getInstance().getCommand("vbAdmin").setExecutor(getInstance());
            new VBLogger("AdminCommands", Level.INFO, "Successfully registered command \"vbAdmin\"").logToFile();
        } catch(NullPointerException e){
            new VBLogger("AdminCommands", Level.SEVERE, "Could not register command \"vbAdmin\"").logToFile();
        }
    }
}
