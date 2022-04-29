package me.shreb.vanillabosses.items;

import me.shreb.vanillabosses.Vanillabosses;
import me.shreb.vanillabosses.items.utility.ItemCreationException;
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

public class BaseballBat extends VBItem {

    public static BaseballBat instance = new BaseballBat();

    public BaseballBat(){
        this.pdcKey = new NamespacedKey(Vanillabosses.getInstance(), "BaseballBat");
        this.configSection = "BaseballBat";
        this.itemMaterial = Material.WOODEN_SWORD;
        this.lore = (ArrayList<String>) config.getStringList("Items." + this.configSection + ".Lore");
    }

    @Override
    public ItemStack makeItem() throws ItemCreationException {
        ItemStack baseballBat = new ItemStack(Material.WOODEN_SWORD);
        ItemMeta meta = baseballBat.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        meta.setDisplayName(ChatColor.LIGHT_PURPLE + Vanillabosses.getCurrentLanguage().itemBaseballBatName);

        PersistentDataContainer container = meta.getPersistentDataContainer();

        container.set(this.pdcKey, PersistentDataType.STRING, "Concuss I");

        lore.add("Concuss I");
        lore.addAll(this.lore);

        meta.setLore(lore);

        baseballBat.setItemMeta(meta);

        baseballBat.addUnsafeEnchantment(Enchantment.KNOCKBACK, 4);
        baseballBat.addUnsafeEnchantment(Enchantment.DURABILITY, 3);
        baseballBat.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 3);

        return baseballBat;
    }

    @Override
    public ItemStack makeItem(int amount) throws ItemCreationException{

        ItemStack baseballBat = new ItemStack(Material.WOODEN_SWORD);
        ItemMeta meta = baseballBat.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        meta.setDisplayName(ChatColor.LIGHT_PURPLE + Vanillabosses.getCurrentLanguage().itemBaseballBatName);

        PersistentDataContainer container = meta.getPersistentDataContainer();

        container.set(this.pdcKey, PersistentDataType.STRING, "Concuss I");

        lore.add("Concuss I");
        lore.addAll(this.lore);

        meta.setLore(lore);

        baseballBat.setItemMeta(meta);

        baseballBat.addUnsafeEnchantment(Enchantment.KNOCKBACK, 4);
        baseballBat.addUnsafeEnchantment(Enchantment.DURABILITY, 3);
        baseballBat.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 3);

        return baseballBat;
    }

    @Override
    public void itemAbility(LivingEntity entity) {

    }

    @Override
    <T extends Event> void itemAbility(T e) {

    }

}
