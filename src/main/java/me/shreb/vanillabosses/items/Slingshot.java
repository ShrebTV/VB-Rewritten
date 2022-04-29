package me.shreb.vanillabosses.items;

import me.shreb.vanillabosses.Vanillabosses;
import me.shreb.vanillabosses.items.utility.ItemCreationException;
import me.shreb.vanillabosses.logging.VBLogger;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Objects;
import java.util.logging.Level;

public class Slingshot extends VBItem {

    public static Slingshot instance = new Slingshot();

    public Slingshot(){
        this.pdcKey = new NamespacedKey(Vanillabosses.getInstance(), "Slingshot");
        this.configSection = "Slingshot";
        try {
            this.itemMaterial = Material.valueOf(config.getString("Items.Slingshot.itemMaterial").toUpperCase());
        } catch (NullPointerException | IllegalArgumentException e) {
            new VBLogger(getClass().getName(), Level.SEVERE, "Unable to convert configuration of the Slingshot to a hoe. Found: " + config.getString("Items.Slingshot.itemMaterial")).logToFile();
            return;
        }
        this.lore = (ArrayList<String>) config.getStringList("Items." + this.configSection + ".Lore");
    }


    @Override
    public ItemStack makeItem() throws ItemCreationException {
        ItemStack slingshot = new ItemStack(this.itemMaterial);
        ItemMeta meta = slingshot.getItemMeta();
        ArrayList<String> lore = new ArrayList<>(this.lore);
        meta.setDisplayName(ChatColor.DARK_RED + Vanillabosses.getCurrentLanguage().itemSlingshotName);
        meta.setLore(lore);
        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(this.pdcKey, PersistentDataType.INTEGER,  1);
        slingshot.setItemMeta(meta);

        return slingshot;
    }

    @Override
    public ItemStack makeItem(int amount) throws ItemCreationException {
        ItemStack slingshot = new ItemStack(this.itemMaterial);
        ItemMeta meta = slingshot.getItemMeta();
        ArrayList<String> lore = new ArrayList<>(this.lore);
        meta.setDisplayName(ChatColor.DARK_RED + Vanillabosses.getCurrentLanguage().itemSlingshotName);
        meta.setLore(lore);
        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(this.pdcKey, PersistentDataType.INTEGER,  1);
        slingshot.setItemMeta(meta);

        return slingshot;
    }

    @Override
    public void itemAbility(LivingEntity entity) {

    }

    @Override
    <T extends Event> void itemAbility(T e) {

    }
}
