package me.shreb.vanillabosses.items;

import me.shreb.vanillabosses.Vanillabosses;
import me.shreb.vanillabosses.items.utility.ItemCreationException;
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

public class Skeletor extends VBItem {

    public static Skeletor instance = new Skeletor();

    public Skeletor(){
        this.pdcKey = new NamespacedKey(Vanillabosses.getInstance(), "Skeletor");
        this.configSection = "Skeletor";
        this.itemMaterial = Material.BOW;
        this.lore = (ArrayList<String>) config.getStringList("Items.Skeletor.Lore");
    }


    @Override
    public ItemStack makeItem() throws ItemCreationException {
        ItemStack bow = new ItemStack(this.itemMaterial);

        ItemMeta meta = bow.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD + "Skeletor");
        ArrayList<String> lore = new ArrayList<>(this.lore);

        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(this.pdcKey, PersistentDataType.STRING, "Shoots TNT");
        meta.setLore(lore);
        bow.setItemMeta(meta);

        return bow;
    }

    @Override
    public ItemStack makeItem(int amount) throws ItemCreationException {
        ItemStack bow = new ItemStack(this.itemMaterial, amount);

        ItemMeta meta = bow.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD + "Skeletor");
        ArrayList<String> lore = new ArrayList<>(this.lore);

        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(this.pdcKey, PersistentDataType.STRING, "Shoots TNT");
        meta.setLore(lore);
        bow.setItemMeta(meta);

        return bow;
    }

    @Override
    public void itemAbility(LivingEntity entity) {

    }

    @Override
    <T extends Event> void itemAbility(T e) {

    }
}
