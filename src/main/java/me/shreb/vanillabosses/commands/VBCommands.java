package me.shreb.vanillabosses.commands;

public abstract class VBCommands {

    /**
     * Makes sure the command classes have this method so I don't forget to add it into registerAll()
     */
    abstract void registerCommand();


    public static void registerAll(){
       AdminCommands.getInstance().registerCommand();
       ItemCommands.getInstance().registerCommand();
    }
}
