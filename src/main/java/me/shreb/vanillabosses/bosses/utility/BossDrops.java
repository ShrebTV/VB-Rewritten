package me.shreb.vanillabosses.bosses.utility;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import me.shreb.vanillabosses.logging.VBLogger;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Level;

/**
 * A class to make it easier to retrieve the drops of a bosstype from the config.
 * Instantiate a new BossDrops object, call the toItemStacks method in order to get a List of ItemStacks to drop on boss death.
 */
public class BossDrops {

    public ArrayList<SingleDrop> drops;

    /**
     * Constructor to make the BossDrops object corresponding to the passed in dataRetriever object
     *
     * @param retriever A DataRetriever in order to make sure the things specified have a config section and so the Config section is readily available and correct
     */
    public BossDrops(BossDataRetriever retriever) {
        FileConfiguration config = retriever.instance.config;
        //a String containing the full config path to get the StringList from
        String fullSection = "Bosses." + retriever.CONFIGSECTION + ".droppedItems";
        //Making a new list of Strings with the values of the StringList in the config
        ArrayList<String> dropStrings = (ArrayList<String>) config.getStringList(fullSection);

        //new empty list of drops
        this.drops = new ArrayList<>();

        // If there are no drops specified in the config, return with the empty List
        if (dropStrings.isEmpty()) {
            return;
        }

        //Iterate over all strings inside the dropStrings in order to add all of them to the drops list as SingleDrop objects
        for (String string : dropStrings) {
            try {
                //put the deserialized drop into the drops list of this new object
                this.drops.add(SingleDrop.deserialize(string));
            } catch (JsonSyntaxException e) {
                //if the parsing of the json fails, log that to the file so the user can find out what went wrong.
                new VBLogger(getClass().getName(), Level.WARNING, "Could not add Drop to BossDrops. Error at: " + string).logToFile();
            }
        }
    }

    /**
     * A Method to turn all Drops inside the drops list into droppable itemStacks
     *
     * @return A list of the corresponding itemStacks made from the SingleDrop objects inside the drops list of this object. Returns an empty list if the config does not specify any drops
     */
    public ArrayList<ItemStack> toItemStacks() {
        ArrayList<ItemStack> itemStacks = new ArrayList<>();
        if (this.drops.isEmpty()) return itemStacks;

        for (SingleDrop singleDrop : this.drops) {
            itemStacks.add(singleDrop.toItemStack());
        }
        return itemStacks;
    }

    /**
     * Drops the items specified in the specified location
     *
     * @param stacks   The list of ItemStacks to drop
     * @param location The Location to drop the ItemStacks at
     */
    public static void dropItemStacks(ArrayList<ItemStack> stacks, Location location) {

        for (ItemStack stack : stacks) {
            if (location.getWorld() == null) break;
            if (stack.getAmount() < 1) {
                continue;
            }
            location.getWorld().dropItemNaturally(location, stack);
        }
    }


    /**
     * A Single drop specified inside the config in one line
     */
    public static class SingleDrop {
        public final String materialName;
        public final int minAmount;
        public int maxAmount;

        public SingleDrop(String materialName, int minAmount, int maxAmount) {
            this.materialName = materialName;
            this.minAmount = minAmount;
            this.maxAmount = maxAmount;
        }

        /**
         * Deserialization from the config into a valid object
         *
         * @param json The Json to be deserialized into a SingleDrop Object
         * @return a new SingleDrop object made from the
         * @throws JsonSyntaxException if the parsing of the json is not successful
         */
        public static SingleDrop deserialize(String json) throws JsonSyntaxException, IllegalArgumentException {
            SingleDrop drop = new Gson().fromJson(json, SingleDrop.class);

            //Verifying that the Material name is valid
            Material.valueOf(drop.materialName.toUpperCase());

            //Verifying that the amounts are valid and properly entered.
            if (drop.maxAmount < drop.minAmount
                    || drop.maxAmount < 1 || drop.maxAmount > 64
                    || drop.minAmount < 0) {
                new VBLogger("SingleDrop", Level.WARNING, "A bad number error occurred while reading the drop String: " + json).logToFile();
                throw new IllegalArgumentException("Max amount cannot be less than 1, min amount cannot be less than 0, max amount cannot be less than min amount. No amount can be larger than 64");
            }
            return drop;
        }

        /**
         * A Method to get an ItemStack with a randomized amount of the item using the SingleDrops' minAmount and maxAmount variables.
         *
         * @return an ItemStack within the bounds of the object this method is called on
         */
        public ItemStack toItemStack() {
            int difference = this.maxAmount - this.minAmount + 1;
            int amount = new Random().nextInt(difference);
            amount += this.minAmount;

            return new ItemStack(Material.valueOf(this.materialName.toUpperCase()), amount);
        }
    }
}
