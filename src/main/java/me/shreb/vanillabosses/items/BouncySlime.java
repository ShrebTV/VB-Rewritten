package me.shreb.vanillabosses.items;

import me.shreb.vanillabosses.Vanillabosses;
import me.shreb.vanillabosses.items.utility.ItemAbilityNotFoundException;
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
import java.util.logging.Level;

public class BouncySlime extends VBItem {

    public static BouncySlime instance = new BouncySlime();

    public BouncySlime() {
        this.pdcKey = new NamespacedKey(Vanillabosses.getInstance(), "BouncySlime");
        this.configSection = "BouncySlime";
        this.itemMaterial = Material.SLIME_BALL;
        this.lore = (ArrayList<String>) config.getStringList("Items." + this.configSection + ".Lore");
        this.itemName = Vanillabosses.getCurrentLanguage().itemBouncySlimeName;
        this.itemGivenMessage = Vanillabosses.getCurrentLanguage().itemBouncySlimeGivenMessage;
    }

    @Override
    public ItemStack makeItem() throws ItemCreationException {

        ItemStack bouncySlime = new ItemStack(this.itemMaterial);
        ItemMeta meta = bouncySlime.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        ArrayList<String> lore = new ArrayList<>(this.lore);
        meta.setLore(lore);
        container.set(this.pdcKey, PersistentDataType.STRING, "BouncySlime");
        meta.setDisplayName(ChatColor.DARK_GREEN + Vanillabosses.getCurrentLanguage().itemBouncySlimeName);
        bouncySlime.setItemMeta(meta);

        return bouncySlime;
    }

    @Override
    public ItemStack makeItem(int amount) throws ItemCreationException {
        ItemStack bouncySlime = new ItemStack(this.itemMaterial, amount);
        ItemMeta meta = bouncySlime.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        ArrayList<String> lore = new ArrayList<>(this.lore);
        meta.setLore(lore);
        container.set(this.pdcKey, PersistentDataType.STRING, "BouncySlime");
        meta.setDisplayName(ChatColor.DARK_GREEN + Vanillabosses.getCurrentLanguage().itemBouncySlimeName);
        bouncySlime.setItemMeta(meta);

        return bouncySlime;
    }

    @Override
    public void registerListener() {
        pluginManager.registerEvents(this, Vanillabosses.getInstance());
    }

    @Override
    public void itemAbility(LivingEntity entity) {
        new VBLogger(getClass().getName(), Level.WARNING, "Attempted to invoke itemAbility(LivingEntity entity) on Bouncy slime. The Author has to fix this, please report this error.").logToFile();
        throw new ItemAbilityNotFoundException("Could not find ability for Bouncy Slime item");

    }

    @Override
    <T extends Event> void itemAbility(T e) {
        new VBLogger(getClass().getName(), Level.WARNING, "Attempted to invoke itemAbility(T e) on Bouncy slime. The Author has to fix this, please report this error.").logToFile();
        throw new ItemAbilityNotFoundException("Could not find ability for Bouncy Slime item");
    }
}
