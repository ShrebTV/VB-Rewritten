package me.shreb.vanillabosses.items;

import me.shreb.vanillabosses.Vanillabosses;
import me.shreb.vanillabosses.items.utility.ItemCreationException;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;

public abstract class VBItem implements Listener {

    public static NamespacedKey VBItemKey = new NamespacedKey(Vanillabosses.getInstance(), "VB-Item");
    public static FileConfiguration config = Vanillabosses.getInstance().getConfig();

    public NamespacedKey PDCKEY; //The PDCKey which identifies this item as the specific special item it is
    public String CONFIGSECTION;
    public Material itemMaterial;

    public ArrayList<String> lore;

    /**
     * This method is used to activate the ability of the item.
     */
    public abstract void itemAbility(LivingEntity entity);

    public abstract ItemStack makeItem() throws ItemCreationException;

    public abstract ItemStack makeItem(int amount) throws ItemCreationException;

    /**
     * This method is used to activate the ability of the item.
     * @param <T> the event which is used to activate the ability
     */
    @EventHandler
    abstract <T extends Event> void itemAbility(T e);

}
