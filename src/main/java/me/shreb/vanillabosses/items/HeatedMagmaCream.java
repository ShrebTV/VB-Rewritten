package me.shreb.vanillabosses.items;

import me.shreb.vanillabosses.Vanillabosses;
import me.shreb.vanillabosses.items.utility.ItemCreationException;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;

public class HeatedMagmaCream extends VBItem {

    public static HeatedMagmaCream instance = new HeatedMagmaCream();

    public HeatedMagmaCream(){
        this.PDCKEY = new NamespacedKey(Vanillabosses.getInstance(), "HeatedMagmaCream");
        this.CONFIGSECTION = "HeatedMagmaCream";
    }

    @Override
    public ItemStack makeItem() throws ItemCreationException {
        return null;
    }

    @Override
    public ItemStack makeItem(int amount) throws ItemCreationException {
        return null;
    }

    @Override
    public void itemAbility(LivingEntity entity) {

    }

    @Override
    <T extends Event> void itemAbility(T e) {

    }
}
