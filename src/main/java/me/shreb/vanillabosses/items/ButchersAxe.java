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

public class ButchersAxe extends VBItem {

    public static ButchersAxe instance = new ButchersAxe();

    public ButchersAxe() {
        this.pdcKey = new NamespacedKey(Vanillabosses.getInstance(), "ButchersAxe");
        this.configSection = "ButchersAxe";
        try {
            this.itemMaterial = Material.valueOf(config.getString("Items.ButchersAxe.itemMaterial").toUpperCase());
        } catch (NullPointerException | IllegalArgumentException e) {
            new VBLogger(getClass().getName(), Level.SEVERE, "Unable to convert configuration of the Butchers Axe into an actual axe. Found: " + config.getString("Items.ButchersAxe.itemMaterial")).logToFile();
            return;
        }
        this.lore = (ArrayList<String>) config.getStringList("Items." + this.configSection + ".Lore");
    }

    @Override
    public ItemStack makeItem() throws ItemCreationException {
        ItemStack axe = new ItemStack(this.itemMaterial);

        axe.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 5);
        axe.addUnsafeEnchantment(Enchantment.DURABILITY, 3);
        axe.addUnsafeEnchantment(Enchantment.FIRE_ASPECT, 2);

        ItemMeta meta = axe.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(this.pdcKey, PersistentDataType.STRING, "Bind II");

        ArrayList<String> lore = new ArrayList<>();
        meta.setDisplayName(ChatColor.DARK_RED + Vanillabosses.getCurrentLanguage().itemButchersAxeName);
        lore.add("Bind II");
        lore.addAll(config.getStringList("Items.ButchersAxe.Lore"));
        meta.setLore(lore);
        axe.setItemMeta(meta);

        return axe;
    }

    @Override
    public ItemStack makeItem(int amount) throws ItemCreationException {
        ItemStack axe = new ItemStack(this.itemMaterial, amount);

        axe.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 5);
        axe.addUnsafeEnchantment(Enchantment.DURABILITY, 3);
        axe.addUnsafeEnchantment(Enchantment.FIRE_ASPECT, 2);

        ItemMeta meta = axe.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(this.pdcKey, PersistentDataType.STRING, "Bind II");

        ArrayList<String> lore = new ArrayList<>();
        meta.setDisplayName(ChatColor.DARK_RED + Vanillabosses.getCurrentLanguage().itemButchersAxeName);
        lore.add("Bind II");
        lore.addAll(config.getStringList("Items.ButchersAxe.Lore"));
        meta.setLore(lore);
        axe.setItemMeta(meta);

        return axe;
    }

    @Override
    public void itemAbility(LivingEntity entity) {

    }

    @Override
    <T extends Event> void itemAbility(T e) {

    }
}
