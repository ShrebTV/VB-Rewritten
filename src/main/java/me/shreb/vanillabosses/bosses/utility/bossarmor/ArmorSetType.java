package me.shreb.vanillabosses.bosses.utility.bossarmor;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public enum ArmorSetType {

    LEATHER(),
    GOLD(),
    IRON(),
    DIAMOND(),
    NETHERITE();

    /**
     * Turns this type of armor into a full set of armor ready to be put on a LivingEntity
     * The armor is in the order of Boots->Leggings->Chestplate->Helmet from index 0 to 3
     *
     * @return The Armor as an ItemStack array of the size 4 in the correct order for putting on LivingEntities
     */
    public ItemStack[] toFullSet() {

        ItemStack[] armor = new ItemStack[4];

        switch (this) {

            case LEATHER:
                armor[0] = new ItemStack(Material.LEATHER_BOOTS);
                armor[1] = new ItemStack(Material.LEATHER_LEGGINGS);
                armor[2] = new ItemStack(Material.LEATHER_CHESTPLATE);
                armor[3] = new ItemStack(Material.LEATHER_HELMET);
                break;

            case GOLD:
                armor[0] = new ItemStack(Material.GOLDEN_BOOTS);
                armor[1] = new ItemStack(Material.GOLDEN_LEGGINGS);
                armor[2] = new ItemStack(Material.GOLDEN_CHESTPLATE);
                armor[3] = new ItemStack(Material.GOLDEN_HELMET);
                break;

            case IRON:
                armor[0] = new ItemStack(Material.IRON_BOOTS);
                armor[1] = new ItemStack(Material.IRON_LEGGINGS);
                armor[2] = new ItemStack(Material.IRON_CHESTPLATE);
                armor[3] = new ItemStack(Material.IRON_HELMET);
                break;

            case DIAMOND:
                armor[0] = new ItemStack(Material.DIAMOND_BOOTS);
                armor[1] = new ItemStack(Material.DIAMOND_LEGGINGS);
                armor[2] = new ItemStack(Material.DIAMOND_CHESTPLATE);
                armor[3] = new ItemStack(Material.DIAMOND_HELMET);
                break;

            case NETHERITE:
                armor[0] = new ItemStack(Material.NETHERITE_BOOTS);
                armor[1] = new ItemStack(Material.NETHERITE_LEGGINGS);
                armor[2] = new ItemStack(Material.NETHERITE_CHESTPLATE);
                armor[3] = new ItemStack(Material.NETHERITE_HELMET);
                break;
        }
        return armor;
    }
}