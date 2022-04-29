package me.shreb.vanillabosses.items;

import me.shreb.vanillabosses.Vanillabosses;
import me.shreb.vanillabosses.items.utility.ItemCreationException;
import me.shreb.vanillabosses.logging.VBLogger;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.logging.Level;

public class SlimeBoots extends VBItem {

    public static SlimeBoots instance = new SlimeBoots();

    public SlimeBoots(){
        this.pdcKey = new NamespacedKey(Vanillabosses.getInstance(), "SlimeBoots");
        this.configSection = "SlimeBoots";
        try {
            this.itemMaterial = Material.valueOf(config.getString("Items.SlimeBoots.itemMaterial").toUpperCase());
        } catch (NullPointerException | IllegalArgumentException e) {
            new VBLogger(getClass().getName(), Level.SEVERE, "Unable to convert configuration of the Slime boots to actual boots. Found: " + config.getString("Items.SlimeBoots.itemMaterial")).logToFile();
            return;
        }
        this.lore = (ArrayList<String>) config.getStringList("Items." + this.configSection + ".Lore");
    }

    @Override
    public ItemStack makeItem() throws ItemCreationException {
        ItemStack slimeBoots = new ItemStack(this.itemMaterial);

        if(itemMaterial == Material.LEATHER_BOOTS){
            LeatherArmorMeta meta = (LeatherArmorMeta) slimeBoots.getItemMeta();
            meta.setColor(Color.GREEN);
            slimeBoots.setItemMeta(meta);
        }

        ItemMeta meta = slimeBoots.getItemMeta();
        meta.setDisplayName(ChatColor.DARK_GREEN + Vanillabosses.getCurrentLanguage().itemSlimeBootsName);
        ArrayList<String> lore = new ArrayList<>(this.lore);
        meta.setLore(lore);
        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(this.pdcKey, PersistentDataType.STRING, "negateFallDamage");
        slimeBoots.setItemMeta(meta);

        return slimeBoots;
    }

    @Override
    public ItemStack makeItem(int amount) throws ItemCreationException {
        ItemStack slimeBoots = new ItemStack(this.itemMaterial, amount);

        if(itemMaterial == Material.LEATHER_BOOTS){
            LeatherArmorMeta meta = (LeatherArmorMeta) slimeBoots.getItemMeta();
            meta.setColor(Color.GREEN);
            slimeBoots.setItemMeta(meta);
        }

        ItemMeta meta = slimeBoots.getItemMeta();
        meta.setDisplayName(ChatColor.DARK_GREEN + Vanillabosses.getCurrentLanguage().itemSlimeBootsName);
        ArrayList<String> lore = new ArrayList<>(this.lore);
        meta.setLore(lore);
        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(this.pdcKey, PersistentDataType.STRING, "negateFallDamage");
        slimeBoots.setItemMeta(meta);

        return slimeBoots;
    }

    @Override
    public void itemAbility(LivingEntity entity) {

    }

    @Override
    <T extends Event> void itemAbility(T e) {

    }
}
