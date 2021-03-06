package me.shreb.vanillabosses.items;

import me.shreb.vanillabosses.Vanillabosses;
import me.shreb.vanillabosses.items.utility.ItemAbilityNotFoundException;
import me.shreb.vanillabosses.items.utility.ItemCreationException;
import me.shreb.vanillabosses.logging.VBLogger;
import me.shreb.vanillabosses.utility.configFiles.FileCreator;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.LivingEntity;
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
        new FileCreator().createAndLoad(FileCreator.bouncySlimePath, this.configuration);
        this.itemMaterial = Material.SLIME_BALL;
        this.lore = (ArrayList<String>) this.configuration.getStringList("Lore");
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
        container.set(VBItem.VBItemKey, PersistentDataType.STRING, "BouncySlime");
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
        container.set(VBItem.VBItemKey, PersistentDataType.STRING, "BouncySlime");
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

    public static ItemStack replaceBouncySlime(ItemStack stack) {

        if (!stack.hasItemMeta() && !stack.getItemMeta().getPersistentDataContainer().has(instance.pdcKey, PersistentDataType.STRING))
            return stack;

        ItemStack helperStack;

        int amount = stack.getAmount();
        try {
            helperStack = instance.makeItem(amount);
        } catch (ItemCreationException e) {
            new VBLogger("BouncySlime", Level.SEVERE, "Could not create bouncy slime for some reason. Exception: " + e).logToFile();
            return stack;
        }
        return helperStack;

    }
}
