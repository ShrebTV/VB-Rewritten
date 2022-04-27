package me.shreb.vanillabosses.items;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

public abstract class VBItem implements Listener {

    /**
     * makes an ItemStack of the special item
     * @return the created ItemStack
     */
     abstract ItemStack makeItem();

    /**
     * This method is used to activate the ability of the item.
     */
    abstract void itemAbility(LivingEntity entity);

    /**
     * This method is used to activate the ability of the item.
     * @param <T> the event which is used to activate the ability
     */
    @EventHandler
    abstract <T extends Event> void itemAbility(T e);

}
