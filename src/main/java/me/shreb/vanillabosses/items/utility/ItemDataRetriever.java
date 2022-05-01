package me.shreb.vanillabosses.items.utility;

import me.shreb.vanillabosses.items.*;
import me.shreb.vanillabosses.logging.VBLogger;
import me.shreb.vanillabosses.utility.DataRetriever;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.logging.Level;

public class ItemDataRetriever extends DataRetriever {

    static HashMap<Material, VBItem> materialInstanceMap = new HashMap<>();

    VBItem instance; //The Instance of the item class referenced by the ItemDataRetriever object

    static {

        materialInstanceMap.put(BaseballBat.instance.itemMaterial, BaseballBat.instance);
        materialInstanceMap.put(Blazer.instance.itemMaterial, Blazer.instance);
        materialInstanceMap.put(BossEggs.instance.itemMaterial, BossEggs.instance);
        materialInstanceMap.put(ButchersAxe.instance.itemMaterial, ButchersAxe.instance);
        materialInstanceMap.put(HeatedMagmaCream.instance.itemMaterial, HeatedMagmaCream.instance);
        materialInstanceMap.put(InvisibilityCloak.instance.itemMaterial, InvisibilityCloak.instance);
        materialInstanceMap.put(Skeletor.instance.itemMaterial, Skeletor.instance);
        materialInstanceMap.put(SlimeBoots.instance.itemMaterial, SlimeBoots.instance);
        materialInstanceMap.put(Slingshot.instance.itemMaterial, Slingshot.instance);

        if ((long) materialInstanceMap.keySet().size() != materialInstanceMap.keySet().stream().distinct().count()) {
            new VBLogger("ItemDataRetriever", Level.SEVERE, "Duplicate Item materials ").logToFile();
        }
    }

    /**
     * A Method to make a new ItemDataRetriever using the Material to determine which item is to be referenced
     *
     * @param material The material required for the readout of data
     * @throws ItemCreationException if the Retriever could not be made. This happens if the Material passed in did not correspond to any plugin items.
     */
    public ItemDataRetriever(Material material) throws ItemCreationException {

        //TODO Test this

        if (!materialInstanceMap.containsKey(material))
            throw new ItemCreationException("Could not create ItemDataRetriever using the material " + material);

        this.instance = materialInstanceMap.get(material);

        if (this.instance == null) {
            new VBLogger(getClass().getName(), Level.WARNING, "Could not create ItemDataRetriever! Material: " + material).logToFile();
        }

        this.CONFIGSECTION = this.instance.configSection;

    }

    public ItemDataRetriever(ItemStack itemStack) throws ItemCreationException {

        if(itemStack == null) throw new ItemCreationException("Item passed to Data Retriever was null");

        //TODO Test this
        ItemDataRetriever retriever;

        if (itemStack.hasItemMeta() &&
                !itemStack.getItemMeta().getPersistentDataContainer().has(VBItem.VBItemKey, PersistentDataType.INTEGER)) {
            return;
        } else {

            Material mat = itemStack.getType();

            try {
                retriever = new ItemDataRetriever(mat);
            } catch (ItemCreationException e) {
                new VBLogger(getClass().getName(), Level.WARNING, "Bad Item input had a VB tag. Please let the author know about this and whether there are Vanilla Bosses extension plugins installed.").logToFile();
                return;
            }
        }

        this.instance = retriever.instance;
        this.CONFIGSECTION = retriever.CONFIGSECTION;

    }


    /**
     * Makes a new ItemStack with the Item specified by the DataRetriever
     *
     * @return a new Item as made by the respective Item class
     * @throws ItemCreationException if there was a problem creating the item
     */
    public ItemStack makeItem() throws ItemCreationException {
        return new ItemStack(this.instance.makeItem());
    }

    /**
     * @param amount the amount of the item to set the itemStack to
     * @return a new itemStack as made by the respective Item class
     * @throws ItemCreationException if there was a problem creating the item
     */
    public ItemStack makeItem(int amount) throws ItemCreationException {
        return new ItemStack(this.instance.makeItem(amount));
    }

    @Override
    public String toString() {
        return "Config Section: " + ChatColor.AQUA + this.CONFIGSECTION + "\n" +
                "NamespacedKey String: " + ChatColor.AQUA + this.instance.pdcKey.getKey() + "\n" +
                "Lore: " + ChatColor.AQUA + this.instance.lore + "\n";
    }
}
