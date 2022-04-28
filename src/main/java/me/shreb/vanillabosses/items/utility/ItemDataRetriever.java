package me.shreb.vanillabosses.items.utility;

import me.shreb.vanillabosses.items.*;
import me.shreb.vanillabosses.logging.VBLogger;
import me.shreb.vanillabosses.utility.DataRetriever;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

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

        this.CONFIGSECTION = this.instance.CONFIGSECTION;

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
        //TODO implement toString()
        return null;
    }
}
