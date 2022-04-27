package me.shreb.vanillabosses.items;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;

public class Slingshot extends VBItem {

    public static Slingshot instance = new Slingshot();

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
