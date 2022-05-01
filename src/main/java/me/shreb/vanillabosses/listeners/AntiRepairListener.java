package me.shreb.vanillabosses.listeners;

import me.shreb.vanillabosses.Vanillabosses;
import me.shreb.vanillabosses.items.utility.ItemCreationException;
import me.shreb.vanillabosses.items.utility.ItemDataRetriever;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.ItemStack;

public class AntiRepairListener implements Listener {

    @EventHandler
    public void onAnvilRepairPrep(PrepareAnvilEvent event) {

        //TODO Test this listener, register it

        ItemStack item1 = event.getInventory().getContents()[0];
        ItemStack item2 = event.getInventory().getContents()[1];

        if (item1 == null && item2 == null) return;

//if item1 and item2 aren't null
        if (item1 != null && item1.hasItemMeta() && item2 != null && item2.getType() != Material.ENCHANTED_BOOK) {

            //first item can't be null. has to be a plugin item
            //second item can't be null. can be an enchanted book or a plugin item

            try {
                new ItemDataRetriever(item1);
                //item1 is a plugin item

                if (item2.getType() != Material.ENCHANTED_BOOK) {
                    //item2 is not a book
                    //if it's not a book, check for plugin items
                    try {
                        new ItemDataRetriever(item2);
                        //if it gets here, item2 is a plugin item
                        return;
                        //return in order not to cancel the event
                    } catch (ItemCreationException ignored) {
                        //if in here, item2 is not a plugin item, should cancel
                    }
                    //if the above throws the exception, it should cancel the event
                } else {
                    return;
                    //item2 is a book, shouldn't cancel
                }
            } catch (ItemCreationException ignored) {
                //item1 is not a plugin item
                return;
                //return in order to not prevent the event
            }

            // prevent anvil action if the first item is a plugin item, but the second one is not an enchanted book or another plugin item
            event.getInventory().setRepairCost(0);
            event.setResult(null);
        }

        if (Vanillabosses.getInstance().getConfig().getBoolean("Items.DisableRepairAndEnchant") && item1 != null && item1.hasItemMeta()) {

            //If item2 is not empty or null, see if item1 is a plugin Item
            //If the retriever throws, the first item is not a plugin item. Continue
            //If the retriever does not throw, the first item is a plugin item.
            if (item2 != null && item2.getType() != Material.AIR) {
                try {
                    new ItemDataRetriever(item1);
                } catch (ItemCreationException e) {
                    return;
                }
            }

            // prevent anvil action
            event.getInventory().setRepairCost(0);
            event.setResult(null);
        }
    }
}
