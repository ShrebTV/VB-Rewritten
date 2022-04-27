package me.shreb.vanillabosses.items;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;

public class BaseballBat extends VBItem {

    public static BaseballBat instance = new BaseballBat();

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
