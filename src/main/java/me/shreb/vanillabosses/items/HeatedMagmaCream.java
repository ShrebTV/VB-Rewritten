package me.shreb.vanillabosses.items;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;

public class HeatedMagmaCream extends VBItem {

    public static HeatedMagmaCream instance = new HeatedMagmaCream();

    @Override
    public ItemStack makeItem() {
        return null;
    }

    @Override
    void itemAbility(LivingEntity entity) {

    }

    @Override
    <T extends Event> void itemAbility(T e) {

    }
}
