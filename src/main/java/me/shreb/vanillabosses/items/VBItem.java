package me.shreb.vanillabosses.items;

import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;

public abstract class VBItem {

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
     */
    abstract void itemAbility();

}
