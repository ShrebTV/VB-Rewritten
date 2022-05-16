package me.shreb.vanillabosses.bosses.utility.bossarmor;

import org.bukkit.Material;

/**
 * An Enum to list all valid Materials armor can be made of in Vanilla Minecraft.
 * Basically a more narrow Material Enum
 */
public enum ArmorType {

    LEATHER_BOOTS(),
    LEATHER_LEGGINGS(),
    LEATHER_CHESTPLATE(),
    LEATHER_HELMET(),

    GOLD_BOOTS(),
    GOLD_LEGGINGS(),
    GOLD_CHESTPLATE(),
    GOLD_HELMET(),

    IRON_BOOTS(),
    IRON_LEGGINGS(),
    IRON_CHESTPLATE(),
    IRON_HELMET(),

    DIAMOND_BOOTS(),
    DIAMOND_LEGGINGS(),
    DIAMOND_CHESTPLATE(),
    DIAMOND_HELMET(),

    NETHERITE_BOOTS(),
    NETHERITE_LEGGINGS(),
    NETHERITE_CHESTPLATE(),
    NETHERITE_HELMET();

    /**
     * Converts this ArmorType object into a Material object for use with ItemStacks
     * @return the corresponding Material Enum object
     */
    public Material toMaterial(){
        return Material.valueOf(this.name());
    }
}