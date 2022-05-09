package me.shreb.vanillabosses.items;

import me.shreb.vanillabosses.Vanillabosses;
import me.shreb.vanillabosses.items.utility.ItemCreationException;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;

import java.util.ArrayList;

public abstract class VBItem implements Listener {

    public static NamespacedKey VBItemKey = new NamespacedKey(Vanillabosses.getInstance(), "VB-Item");
    static final PluginManager pluginManager = Vanillabosses.getInstance().getServer().getPluginManager();

    public YamlConfiguration configuration = new YamlConfiguration();
    public NamespacedKey pdcKey; //The PDCKey which identifies this item as the specific special item it is
    public String configSection;
    public Material itemMaterial;
    public String itemName;
    public String itemGivenMessage;

    public ArrayList<String> lore;

    /**
     * This method is used to activate the ability of the item.
     */
    public abstract void itemAbility(LivingEntity entity);

    public abstract ItemStack makeItem() throws ItemCreationException;

    public abstract ItemStack makeItem(int amount) throws ItemCreationException;

    public abstract void registerListener();

}
