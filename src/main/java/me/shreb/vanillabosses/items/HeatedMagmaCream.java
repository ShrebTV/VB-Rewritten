package me.shreb.vanillabosses.items;

import me.shreb.vanillabosses.Vanillabosses;
import me.shreb.vanillabosses.items.utility.ItemCreationException;
import me.shreb.vanillabosses.logging.VBLogger;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.logging.Level;

public class HeatedMagmaCream extends VBItem {

    public static HeatedMagmaCream instance = new HeatedMagmaCream();

    public int level;

    public HeatedMagmaCream() {
        this.pdcKey = new NamespacedKey(Vanillabosses.getInstance(), "HeatedMagmaCream");
        this.configSection = "HeatedMagmaCream";
        this.itemMaterial = Material.MAGMA_CREAM;
        this.lore = (ArrayList<String>) config.getStringList("Items." + this.configSection + ".Lore");
    }

    public HeatedMagmaCream(int level) {
        this();
        if (level > 3 || level < 1) {
            new VBLogger(getClass().getName(), Level.SEVERE, "Tried to create Heated Magma Cream with a wrong level. Please let the Author know about this.");
            return;
        }
        this.level = level;
    }

    @Override
    public ItemStack makeItem() throws ItemCreationException {
        ItemStack cream = new ItemStack(Material.MAGMA_CREAM);

        switch (level) {

            case 1:
                cream.addUnsafeEnchantment(Enchantment.ARROW_FIRE, 1);
                break;

            case 2:
                cream.addUnsafeEnchantment(Enchantment.ARROW_FIRE, 2);
                break;

            case 3:
                cream.addUnsafeEnchantment(Enchantment.ARROW_FIRE, 3);
                break;

            default:
                throw new IllegalArgumentException(ChatColor.RED + "VanillaBosses: Error with HeatedMagmaCream. Please notify the Author of the plugin about this Error.");
        }
        ItemMeta meta = cream.getItemMeta();
        meta.setDisplayName(Vanillabosses.getCurrentLanguage().itemHMCName + " Lv." + level);
        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(this.pdcKey, PersistentDataType.INTEGER, level);
        cream.setItemMeta(meta);

        return cream;
    }

    @Override
    public ItemStack makeItem(int amount) throws ItemCreationException {
        ItemStack cream = new ItemStack(Material.MAGMA_CREAM, amount);

        switch (level) {

            case 1:
                cream.addUnsafeEnchantment(Enchantment.ARROW_FIRE, 1);
                break;

            case 2:
                cream.addUnsafeEnchantment(Enchantment.ARROW_FIRE, 2);
                break;

            case 3:
                cream.addUnsafeEnchantment(Enchantment.ARROW_FIRE, 3);
                break;

            default:
                throw new IllegalArgumentException(ChatColor.RED + "VanillaBosses: Error with HeatedMagmaCream. Please notify the Author of the plugin about this Error.");
        }
        ItemMeta meta = cream.getItemMeta();
        meta.setDisplayName(Vanillabosses.getCurrentLanguage().itemHMCName + " Lv." + level);
        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(this.pdcKey, PersistentDataType.INTEGER, level);
        cream.setItemMeta(meta);

        return cream;
    }

    @Override
    public void itemAbility(LivingEntity entity) {

    }

    @Override
    <T extends Event> void itemAbility(T e) {

    }
}
