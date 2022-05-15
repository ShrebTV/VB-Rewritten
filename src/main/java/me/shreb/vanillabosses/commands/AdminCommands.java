package me.shreb.vanillabosses.commands;

import me.shreb.vanillabosses.Vanillabosses;
import me.shreb.vanillabosses.bosses.WitherBoss;
import me.shreb.vanillabosses.bosses.bossRepresentation.RespawningBoss;
import me.shreb.vanillabosses.bosses.utility.BossCreationException;
import me.shreb.vanillabosses.bosses.utility.BossDataRetriever;
import me.shreb.vanillabosses.bosses.utility.VBBossBar;
import me.shreb.vanillabosses.items.*;
import me.shreb.vanillabosses.items.utility.ItemCreationException;
import me.shreb.vanillabosses.items.utility.ItemDataRetriever;
import me.shreb.vanillabosses.logging.VBLogger;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.logging.Level;

/**
 * Command name -> "vbAdmin"
 * possible commands:   "vbAdmin respawningBoss (type) (respawnTime) [commands]"
 * "bossInfo (type)"
 * "Item (current item material/name of the item in the currently active language)"
 */
public class AdminCommands extends VBCommands {

    private static final AdminCommands INSTANCE = new AdminCommands();

    public static AdminCommands getInstance() {
        return INSTANCE;
    }

    /**
     * A command for admins to see what they have to put in the config file to make a working respawning Boss requiring them to enter as little information as possible
     */
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!sender.isOp()) sender.sendMessage(ChatColor.RED + Vanillabosses.getCurrentLanguage().badPermissions);

        if (args.length == 0) return false;

        if (sender instanceof Player && args[0].equalsIgnoreCase("readme")) {

            ComponentBuilder builder = new ComponentBuilder(ChatColor.BLUE + "" + ChatColor.UNDERLINE + "Click here for the README");

            BaseComponent[] component = builder.event(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://github.com/ShrebTV/VB-Rewritten/blob/master/README.md")).create();

            sender.spigot().sendMessage(component);

            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(Vanillabosses.getCurrentLanguage().notEnoughArguments);
            return true;
        }

        if (args[0].equalsIgnoreCase("respawningBoss")) {

            return respawningBossCommandReaction(sender, args);

        } else if (args[0].equalsIgnoreCase("bossInfo")) {

            return bossInfoCommandReaction(sender, args);

        } else if (args[0].equalsIgnoreCase("giveItem")) {

            return giveItemCommandReaction(sender, args);

        } else if (args[0].equalsIgnoreCase("spawnBoss")) {

            return spawnBossCommandReaction(sender, args);

        } else if (args[0].equalsIgnoreCase("specialItem")) {

            return hmcBossEggsReaction(sender, args);

        } else {
            return false;
        }
    }

    @Override
    void registerCommand() {
        try {
            Vanillabosses.getInstance().getCommand("vbAdmin").setExecutor(getInstance());
            new VBLogger("AdminCommands", Level.INFO, "Successfully registered command \"vbAdmin\"").logToFile();
        } catch (NullPointerException e) {
            new VBLogger("AdminCommands", Level.SEVERE, "Could not register command \"vbAdmin\"").logToFile();
        }
    }

    /**
     * A command for admins to easily find out what to write into the config file in order to get the respawning boss they want where they want it.
     *
     * @param sender CommandSender which sent the command
     * @param args   command arguments
     * @return true if there was no problem executing the command
     */
    boolean respawningBossCommandReaction(CommandSender sender, String[] args) {

        //Check whether the sender is a player who has op permissions
        if (!(sender instanceof Player) || !sender.isOp()) {
            sender.sendMessage("User of this command has to be a Player and OP");
            return true;
        }

        if (args.length < 4) {
            sender.sendMessage("Not enough arguments");
            return true;
        }

        Location location = ((Player) sender).getLocation();
        String world;
        EntityType type;
        int respawnTime;
        int[] commands = new int[args[3].length() / 2 + 1];

        try {
            world = location.getWorld().getName();
            type = EntityType.valueOf(args[1].toUpperCase());
            new BossDataRetriever(type); //checking whether the boss type exists
            respawnTime = Integer.parseInt(args[2]);
        } catch (NullPointerException e) {
            sender.sendMessage("An Error has occurred. Please look at the log file for more details.");
            new VBLogger("RespawningBossCommand", Level.WARNING, "Nullpointer: " + e).logToFile();
            return false;
        } catch (NumberFormatException e) { // respawn time was not a number
            sender.sendMessage("Expected Integer number. Found: " + args[2]);
            return true;
        } catch (IllegalArgumentException e) { // Entity type not found or couldn't make type into a boss data retriever
            sender.sendMessage("Could not resolve Boss type " + args[1]);
            return true;
        }

        if (args[3] != null) {
            String[] indexes = args[3].split(",");
            //adding the expected command indexes to a new arrray in order to set the commands array in the respawningBoss object
            for (int i = 0; i < indexes.length; i++) {
                try {

                    commands[i] = Integer.parseInt(indexes[i]);

                } catch (NumberFormatException e) {
                    sender.sendMessage("Commands have to be referred to by numbers. Found: " + args[i]);
                    return false;
                }
            }
        }

        RespawningBoss bossObject = new RespawningBoss(type, world, (int) location.getX(), (int) location.getY(), (int) location.getZ(), respawnTime, commands, true);
        sender.sendMessage(bossObject.serialize());
        return true;
    }

    boolean bossInfoCommandReaction(CommandSender sender, String[] args) {

        // Display some of the boss config to the sender in a readable fashion for the boss requested.
        if (args.length < 2) return false;

        EntityType type;

        try {
            type = EntityType.valueOf(args[1].toUpperCase());
            BossDataRetriever retriever = new BossDataRetriever(type);
            sender.sendMessage(retriever.toString());
            return true;
        } catch (IllegalArgumentException e) {
            sender.sendMessage("Could not resolve boss type \"" + args[1] + "\"");
            return true;
        }
    }

    boolean giveItemCommandReaction(CommandSender sender, String[] args) {

        //first argument: giveItem /static
        //second argument: Item identifier /static
        //third argument: specified player, if there is no argument for this, default to the sender, parse for amount
        //fourth argument: amount, if there is no argument for this and third wasn't an amount, default to 1

        //possible commands I want to allow:
        // /vbAdmin giveItem slingshot //gives the sender a slingshot if possible
        // /vbAdmin giveItem slingshot <playerName> //gives the player with the playerName a slingshot
        // /vbAdmin giveItem slingshot <playerName> <amount> //gives the player with the playerName the specified amount of slingshots

        //commands I want it to reject and quit out of in an orderly fashion:
        // /vbAdmin giveItem gabbagoo
        // /vbAdmin giveItem slingshot FFFFFF <- not a player name
        // /vbAdmin giveItem slingshot Shreb DDDDD <- not a number

        String itemGetterString;
        Player receivingPlayer;

        int amount = 1;

        if (args.length < 2) {

            sender.sendMessage(Vanillabosses.getCurrentLanguage().notEnoughArguments);
            return false;

        } else if (args.length == 2) {

            if (sender instanceof Player) {
                receivingPlayer = (Player) sender;
                itemGetterString = args[1];
            } else {
                sender.sendMessage(Vanillabosses.getCurrentLanguage().notEnoughArguments);
                return true;
            }

        } else {

            itemGetterString = args[1];
            String receivingPlayerName = args[2];

            //get the receiving player from the third argument or get an amount
            receivingPlayer = Vanillabosses.getInstance().getServer().getPlayer(receivingPlayerName);
            if (receivingPlayer == null) {

                //player could not be found, telling the sender something went wrong
                sender.sendMessage(Vanillabosses.getCurrentLanguage().badArgument + ": " + receivingPlayerName);
                return true;
            }

            if (args.length >= 4) {
                try {
                    amount = Integer.parseInt(args[3]);
                } catch (NumberFormatException ignored) {
                    sender.sendMessage(Vanillabosses.getCurrentLanguage().badArgument);
                    return true;
                }
            }

            if (1 > amount || amount > 64) {
                sender.sendMessage(Vanillabosses.getCurrentLanguage().badAmount);
                return true;
            }
        }

        ItemStack itemToGive = parseForItem(itemGetterString, amount);

        ItemDataRetriever retriever;
        try {
            retriever = new ItemDataRetriever(itemToGive.getType());
        } catch (ItemCreationException e) {
            new VBLogger(getClass().getName(), Level.WARNING, "Bad Item from parseForItem() in Admin Commands.\n" +
                    "Item: " + itemToGive + "\n" +
                    "Exception: " + e).logToFile();
            sender.sendMessage(Vanillabosses.getCurrentLanguage().errorMessage);
            return true;
        }

        giveItemToPlayer(sender, receivingPlayer, itemToGive, retriever.instance);
        return true;
    }

    /**
     * Parses a String in order to try and get a plugin item from it
     *
     * @param toParse the string to parse for a plugin item reference
     * @param amount  the amount of the item the itemStack should have
     * @return the itemstack gotten from the input string or null if it couldn't be parsed or it was HMC or boss eggs
     * for HMC and boss eggs, a different method should be used
     */
    ItemStack parseForItem(String toParse, int amount) {
        ItemStack itemStack = null;

        try {
            //attempt to get the item by parsing the string for a material
            Material itemMaterial = Material.valueOf(toParse.toUpperCase());
            itemStack = new ItemDataRetriever(itemMaterial).makeItem(amount);

        } catch (IllegalArgumentException | ItemCreationException e) {

            //Attempt to get the itemStack from the name of the item
            try {
                if (toParse.equalsIgnoreCase("baseballBat")) {

                    itemStack = BaseballBat.instance.makeItem(amount);

                } else if (toParse.equalsIgnoreCase("blazer")) {

                    itemStack = Blazer.instance.makeItem(amount);

                } else if (toParse.equalsIgnoreCase("BouncySlime")) {

                    itemStack = BouncySlime.instance.makeItem(amount);

                } else if (toParse.equalsIgnoreCase("ButchersAxe")) {

                    itemStack = ButchersAxe.instance.makeItem(amount);

                } else if (toParse.equalsIgnoreCase("invisibilityCloak")) {

                    itemStack = InvisibilityCloak.instance.makeItem(amount);

                } else if (toParse.equalsIgnoreCase("skeletor")) {

                    itemStack = Skeletor.instance.makeItem(amount);

                } else if (toParse.equalsIgnoreCase("SlimeBoots")) {

                    itemStack = SlimeBoots.instance.makeItem(amount);

                } else if (toParse.equalsIgnoreCase("Slingshot")) {

                    itemStack = Slingshot.instance.makeItem(amount);

                } else if (toParse.equalsIgnoreCase("witherEgg")) {

                    itemStack = WitherBoss.makeWitherEgg();

                }

            } catch (ItemCreationException ex) {
                new VBLogger(getClass().getName(), Level.WARNING, "An Error has occurred while trying to create an Item. Exception: " + ex);
            }
        }
        return itemStack;
    }


    /**
     * Gives the item to the specified player
     *
     * @param sender    the command sender
     * @param p         the player to give the item to
     * @param itemStack the item to give the player
     * @param instance  the instance of the item
     */
    void giveItemToPlayer(CommandSender sender, Player p, ItemStack itemStack, VBItem instance) {
        if (p.getInventory().firstEmpty() != -1) {
            p.getInventory().addItem(itemStack);

            if (instance != null) {
                p.sendMessage(ChatColor.AQUA + instance.itemGivenMessage);
            }

        } else {
            sender.sendMessage(ChatColor.RED + Vanillabosses.getCurrentLanguage().inventoryFull);
        }
    }

    boolean spawnBossCommandReaction(CommandSender sender, String[] args) {

        //not enough arguments
        if (args.length < 2) {
            return false;
        }

        String type = args[1];
        BossDataRetriever retriever;

        try {
            retriever = new BossDataRetriever(EntityType.valueOf(type.toUpperCase()));
        } catch (IllegalArgumentException ignored) {
            return false;
        }

        //the location to spawn the boss at
        Location locationToSpawn = null;
        //optionally the player to spawn the boss on
        Player player = null;

        //if the sender is a player I have more information from them, like the location and direction they're looking
        if (sender instanceof Player) {
            player = (Player) sender;

            //try to get a player from the third argument
            Player fromString = null;
            if (args.length > 2) {
                fromString = Vanillabosses.getInstance().getServer().getPlayer(args[2]);
            }

            //in case the third argument contained a player name, set player to that one.
            if (fromString != null) {
                player = fromString;

            } else if (args.length > 2) {
                //in case the third argument was not a player, try and get a location from the third argument instead

                String[] strings = args[2].split(";");

                //see whether the location argument has enough parts to it
                if (strings.length != 4) {
                    return false;
                }

                //attempt to get a valid location from the 3rd argument, setting the locationToSpawn to the new location in case it was successfully read
                try {
                    World world = Vanillabosses.getInstance().getServer().getWorld(strings[0]);
                    double x = Double.parseDouble(strings[1]);
                    double y = Double.parseDouble(strings[2]);
                    double z = Double.parseDouble(strings[3]);

                    if (world == null) {
                        world = ((Player) sender).getWorld();
                    }

                    locationToSpawn = new Location(world, x, y, z);

                } catch (NumberFormatException ignored) {
                    //Coordinates could not be read correctly
                    return false;
                }
            }

            //Just in case the locationToSpawn is still null, try to get it from the player, which was specified or set to be the sender
            if (locationToSpawn == null) {

                int i = 8;
                Block block = player.getTargetBlock(null, i);
                locationToSpawn = block.getLocation();

            }

            try {
                LivingEntity entity = retriever.instance.makeBoss(locationToSpawn);
                if (Vanillabosses.getInstance().getConfig().getBoolean("Bosses.CommandBossesHaveBossBars")) {
                    new VBBossBar(entity, Bukkit.createBossBar(entity.getName(), BarColor.GREEN, BarStyle.SOLID, BarFlag.PLAY_BOSS_MUSIC));
                }
            } catch (BossCreationException e) {
                new VBLogger(getClass().getName(), Level.WARNING, "Something went wrong while spawning a boss via command. Exception: " + e).logToFile();
                sender.sendMessage(Vanillabosses.getCurrentLanguage().errorMessage);
            }

            return true;

        } else if (args.length < 3) {
            //command via console and not enough arguments for that
            sender.sendMessage(Vanillabosses.getCurrentLanguage().notEnoughArguments);
            return true;
        } else {
//command via console and 3 arguments

            //try to get a player from the third argument
            Player fromString = null;

            fromString = Vanillabosses.getInstance().getServer().getPlayer(args[1]);


            //in case the third argument contained a player name, set player to that one.
            if (fromString != null) {
                player = fromString;

            } else {
                //in case the third argument was not a player, try and get a location from the third argument instead

                String[] strings = args[2].split(";");

                //see whether the location argument has enough parts to it
                if (strings.length != 4) {
                    return false;
                }

                //attempt to get a valid location from the 3rd argument, setting the locationToSpawn to the new location in case it was successfully read
                try {
                    World world = Vanillabosses.getInstance().getServer().getWorld(strings[0]);
                    double x = Double.parseDouble(strings[1]);
                    double y = Double.parseDouble(strings[2]);
                    double z = Double.parseDouble(strings[3]);

                    if (world == null) {
                        sender.sendMessage(Vanillabosses.getCurrentLanguage().badArgument + ": " + strings[0]);
                        return true;
                    }

                    locationToSpawn = new Location(world, x, y, z);

                } catch (NumberFormatException ignored) {
                    //Coordinates could not be read correctly
                    return false;
                }
            }

            if (locationToSpawn == null) {

                int i = 5;
                Block block = player.getTargetBlock(null, i);

                while (block.getType() == Material.AIR && i > 0) {
                    block = player.getTargetBlock(null, --i);
                    locationToSpawn = block.getLocation();
                }
            }

            try {
                LivingEntity entity = retriever.instance.makeBoss(locationToSpawn);
                if (Vanillabosses.getInstance().getConfig().getBoolean("Bosses.CommandBossesHaveBossBars")) {
                    new VBBossBar(entity, Bukkit.createBossBar(entity.getName(), BarColor.GREEN, BarStyle.SOLID, BarFlag.PLAY_BOSS_MUSIC));
                }
            } catch (BossCreationException e) {
                new VBLogger(getClass().getName(), Level.WARNING, "Something went wrong while spawning a boss via command. Exception: " + e).logToFile();
                sender.sendMessage(Vanillabosses.getCurrentLanguage().errorMessage);
                return true;
            }

        }
        return false;
    }

    public boolean hmcBossEggsReaction(CommandSender sender, String[] args) {

        String itemGetterString;
        Player receivingPlayer;

        int amount = 1;

        if (args.length < 2) {

            sender.sendMessage(Vanillabosses.getCurrentLanguage().notEnoughArguments);
            return false;

        } else if (args.length == 2) {

            if (sender instanceof Player) {
                receivingPlayer = (Player) sender;
                itemGetterString = args[1];
            } else {
                sender.sendMessage(Vanillabosses.getCurrentLanguage().notEnoughArguments);
                return true;
            }

        } else {

            itemGetterString = args[1];
            String receivingPlayerName = args[2];

            //get the receiving player from the third argument or get an amount
            receivingPlayer = Vanillabosses.getInstance().getServer().getPlayer(receivingPlayerName);
            if (receivingPlayer == null) {

                //player could not be found, telling the sender something went wrong
                sender.sendMessage(Vanillabosses.getCurrentLanguage().badArgument + ": " + receivingPlayerName);
                return true;
            }

            if (args.length >= 4) {
                try {
                    amount = Integer.parseInt(args[3]);
                } catch (NumberFormatException ignored) {
                    sender.sendMessage(Vanillabosses.getCurrentLanguage().badArgument);
                    return true;
                }
            }

            if (1 > amount || amount > 64) {
                sender.sendMessage(Vanillabosses.getCurrentLanguage().badAmount);
                return true;
            }
        }

        ItemStack itemToGive = null;

        if (itemGetterString.equalsIgnoreCase("hmc")) {
            int level;
            if (args.length >= 5) {
                try {
                    level = Integer.parseInt(args[4]);
                } catch (NumberFormatException ignored) {
                    sender.sendMessage(Vanillabosses.getCurrentLanguage().badArgument);
                    return true;
                }
                try {
                    itemToGive = new HeatedMagmaCream(level).makeItem(amount);
                } catch (ItemCreationException e) {
                    new VBLogger(getClass().getName(), Level.WARNING, "Could not make Heated magma cream. Exception: " + e).logToFile();
                } catch (IllegalArgumentException ignored) {
                    return false;
                }
            } else {
                return false;
            }

        } else if (itemGetterString.equalsIgnoreCase("bossegg")) {

            EntityType type;
            if (args.length >= 5) {
                try {
                    type = EntityType.valueOf(args[4].toUpperCase());
                } catch (NumberFormatException ignored) {
                    sender.sendMessage(Vanillabosses.getCurrentLanguage().badArgument);
                    return true;
                } catch (IllegalArgumentException ignored) {
                    return false;
                }
                try {
                    itemToGive = new BossEggs(type).makeItem(amount);
                } catch (ItemCreationException | NullPointerException e) {
                    return false;
                }
            } else {
                return false;
            }

        } else {
            return false;
        }

        if (itemToGive == null) return false;

        ItemDataRetriever retriever;
        try {
            retriever = new ItemDataRetriever(itemToGive);
        } catch (ItemCreationException e) {
            new VBLogger(getClass().getName(), Level.WARNING, "Bad Item from parseForItem() in Admin Commands.\n" +
                    "Item: " + itemToGive + "\n" +
                    "Exception: " + e).logToFile();
            sender.sendMessage(Vanillabosses.getCurrentLanguage().errorMessage);
            return true;
        }

        giveItemToPlayer(sender, receivingPlayer, itemToGive, retriever.instance);
        return true;
    }
}
