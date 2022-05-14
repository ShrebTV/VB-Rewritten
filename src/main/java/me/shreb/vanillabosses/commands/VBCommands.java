package me.shreb.vanillabosses.commands;

import org.bukkit.command.CommandExecutor;

public abstract class VBCommands implements CommandExecutor {

    /**
     * Makes sure the command classes have this method so I don't forget to add it into registerAll()
     */
    abstract void registerCommand();


    public static void registerAll(){
       AdminCommands.getInstance().registerCommand();
       VBInfo.getInstance().registerCommand();
        VBUtil.getInstance().registerCommand();
    }
}