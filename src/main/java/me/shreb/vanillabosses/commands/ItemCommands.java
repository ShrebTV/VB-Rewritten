package me.shreb.vanillabosses.commands;

import me.shreb.vanillabosses.Vanillabosses;
import me.shreb.vanillabosses.logging.VBLogger;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.logging.Level;

/**
 * Command: vbItem
 * Subcommands: - give
 *              -
 */
public class ItemCommands extends VBCommands implements CommandExecutor {

    private static final ItemCommands INSTANCE = new ItemCommands();

    public static ItemCommands getInstance(){
        return INSTANCE;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        return false;
    }


    @Override
    void registerCommand() {

        try {
            Vanillabosses.getInstance().getCommand("vbItem").setExecutor(getInstance());
            new VBLogger("AdminCommands", Level.INFO, "Successfully registered command \"vbAdmin\"").logToFile();
        } catch(NullPointerException e){
            new VBLogger("AdminCommands", Level.SEVERE, "Could not register command \"vbAdmin\"").logToFile();
        }

    }
}
