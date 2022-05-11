package me.shreb.vanillabosses.commands;

import me.shreb.vanillabosses.Vanillabosses;
import me.shreb.vanillabosses.bosses.utility.BossDataRetriever;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;

/**
 * This class is supposed to take and react to following commands:
 * <p>
 * /vbInfo <BossType> //gives language specific info about the boss
 * /vbInfo <info> //gives language specific info about the plugin
 * /vbInfo <bosslist> //gives a list of bosstypes
 * /vbInfo <itemlist> gives a list of items added by this plugin
 */
public class VBInfo extends VBCommands {

    @Override
    void registerCommand() {

    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        String message = "";

        if (args.length < 1) return false;

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {

            case "info":
                message = Vanillabosses.getCurrentLanguage().vbh0;
                break;

            case "bosslist":
                message = ChatColor.AQUA +
                        "Blaze Boss \n" +
                        "Creeper Boss \n" +
                        "Enderman Boss \n" +
                        "Magma_cube Boss \n" +
                        "Skeleton Boss \n" +
                        "Slime Boss \n" +
                        "Spider Boss \n" +
                        "Witch Boss \n" +
                        "Wither Boss \n" +
                        "Zombie Boss \n" +
                        "Zombified_Piglin Boss";
                break;

            case "itemlist":
                message = ChatColor.AQUA +
                        "Baseball Bat \n" +
                        "Blazer \n" +
                        "Boss Egg \n" +
                        "BouncySlime \n" +
                        "Butchers Axe \n" +
                        "Heated Magma Cream (HMC) \n" +
                        "Invisibility Cloak \n" +
                        "Skeletor \n" +
                        "Slime Boots \n" +
                        "Slingshot \n" +
                        "Wither Egg";
                break;

        }

        if (!message.equals("")) {
            sender.sendMessage(message);
            return true;
        }

        if(args.length < 2) return false;

        try {

            EntityType type = EntityType.valueOf(args[1].toUpperCase());

            sender.sendMessage(new BossDataRetriever(type).infoMessage);
            return true;

        } catch (IllegalArgumentException ignored) {
            sender.sendMessage("Could not find boss type!");
        }

        return false;
    }
}
