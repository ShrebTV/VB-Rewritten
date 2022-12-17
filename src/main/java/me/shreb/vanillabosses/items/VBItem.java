package me.shreb.vanillabosses.items;

import me.shreb.vanillabosses.Vanillabosses;
import me.shreb.vanillabosses.items.utility.Cooldownsetter;
import me.shreb.vanillabosses.items.utility.ItemCreationException;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;

import java.util.ArrayList;
import java.util.List;

public abstract class VBItem implements Listener {

    public static NamespacedKey VBItemKey = new NamespacedKey(Vanillabosses.getInstance(), "VB-Item");
    static final PluginManager pluginManager = Vanillabosses.getInstance().getServer().getPluginManager();

    private static final ArrayList<String> ITEM_NAMES = new ArrayList<>();
    private static final ArrayList<String> SPECIAL_ITEM_NAMES = new ArrayList<>();

    public YamlConfiguration configuration = new YamlConfiguration();
    public NamespacedKey pdcKey; //The PDCKey which identifies this item as the specific special item it is
    public String configSection;
    public Material itemMaterial;
    public String itemName;
    public String itemGivenMessage;

    public ArrayList<String> lore;

    protected Cooldownsetter cooldownsetter = new Cooldownsetter();

    static {
        ITEM_NAMES.add("BaseballBat");
        ITEM_NAMES.add("Blazer");
        ITEM_NAMES.add("BouncySlime");
        ITEM_NAMES.add("ButchersAxe");
        ITEM_NAMES.add("InvisibilityCloak");
        ITEM_NAMES.add("Skeletor");
        ITEM_NAMES.add("SlimeBoots");
        ITEM_NAMES.add("Slingshot");
        ITEM_NAMES.add("WitherEgg");

        SPECIAL_ITEM_NAMES.add("bossegg");
        SPECIAL_ITEM_NAMES.add("hmc");
    }

    public static List<String> getItemNames() {
        return ITEM_NAMES;
    }

    public static List<String> getSpecialItemNames() {
        return SPECIAL_ITEM_NAMES;
    }

    /**
     * This method is used to activate the ability of the item.
     */
    public abstract void itemAbility(LivingEntity entity);

    public abstract ItemStack makeItem() throws ItemCreationException;

    public abstract ItemStack makeItem(int amount) throws ItemCreationException;

    public abstract void registerListener();

}
