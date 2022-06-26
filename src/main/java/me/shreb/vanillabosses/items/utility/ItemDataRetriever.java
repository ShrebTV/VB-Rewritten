package me.shreb.vanillabosses.items.utility;

import me.shreb.vanillabosses.items.*;
import me.shreb.vanillabosses.logging.VBLogger;
import me.shreb.vanillabosses.utility.DataRetriever;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.logging.Level;

public class ItemDataRetriever extends DataRetriever {

    static HashMap<Material, VBItem> materialInstanceMap = new HashMap<>();

    public VBItem instance; //The Instance of the item class referenced by the ItemDataRetriever object

    static {

        materialInstanceMap.put(BaseballBat.instance.itemMaterial, BaseballBat.instance);
        materialInstanceMap.put(Blazer.instance.itemMaterial, Blazer.instance);
        materialInstanceMap.put(BossEggs.instance.itemMaterial, BossEggs.instance);
        materialInstanceMap.put(ButchersAxe.instance.itemMaterial, ButchersAxe.instance);
        materialInstanceMap.put(HeatedMagmaCream.instance.itemMaterial, HeatedMagmaCream.instance);
        materialInstanceMap.put(InvisibilityCloak.instance.itemMaterial, InvisibilityCloak.instance);
        materialInstanceMap.put(Skeletor.instance.itemMaterial, Skeletor.instance);
        materialInstanceMap.put(SlimeBoots.instance.itemMaterial, SlimeBoots.instance);
        materialInstanceMap.put(BouncySlime.instance.itemMaterial, BouncySlime.instance);
        materialInstanceMap.put(Slingshot.instance.itemMaterial, Slingshot.instance);
        materialInstanceMap.put(WitherEgg.instance.itemMaterial, WitherEgg.instance);

        materialInstanceMap.put(Material.BLAZE_SPAWN_EGG, new BossEggs(EntityType.BLAZE));
        materialInstanceMap.put(Material.CREEPER_SPAWN_EGG, new BossEggs(EntityType.CREEPER));
        materialInstanceMap.put(Material.ENDERMAN_SPAWN_EGG, new BossEggs(EntityType.ENDERMAN));
        materialInstanceMap.put(Material.MAGMA_CUBE_SPAWN_EGG, new BossEggs(EntityType.MAGMA_CUBE));
        materialInstanceMap.put(Material.SKELETON_SPAWN_EGG, new BossEggs(EntityType.SKELETON));
        materialInstanceMap.put(Material.SLIME_SPAWN_EGG, new BossEggs(EntityType.SLIME));
        materialInstanceMap.put(Material.SPIDER_SPAWN_EGG, new BossEggs(EntityType.SPIDER));
        materialInstanceMap.put(Material.WITCH_SPAWN_EGG, new BossEggs(EntityType.WITCH));
        materialInstanceMap.put(Material.WITHER_SKELETON_SPAWN_EGG, new BossEggs(EntityType.WITHER));
        materialInstanceMap.put(Material.ZOMBIE_SPAWN_EGG, new BossEggs(EntityType.ZOMBIE));
        materialInstanceMap.put(Material.ZOMBIFIED_PIGLIN_SPAWN_EGG, new BossEggs(EntityType.ZOMBIFIED_PIGLIN));

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

        if (!materialInstanceMap.containsKey(material))
            throw new ItemCreationException("Could not create ItemDataRetriever using the material " + material);

        this.instance = materialInstanceMap.get(material);

        if (this.instance == null) {
            new VBLogger(getClass().getName(), Level.WARNING, "Could not create ItemDataRetriever! Material: " + material).logToFile();
        }

        this.CONFIGSECTION = this.instance.configSection;
    }

    /**
     * Use this to check whether an ItemStack is actually a plugin item.
     * Will retrieve the data for the corresponding plugin item in case there is one
     * @param itemStack the itemStack to check for plugin item characteristics
     * @throws ItemCreationException if the itemStack passed in was null or the item Material was not registered as a plugin item material
     */
    public ItemDataRetriever(ItemStack itemStack) throws ItemCreationException {

        if (itemStack == null) throw new ItemCreationException("Item passed to Data Retriever was null");

        ItemDataRetriever retriever;

        if (!itemStack.getItemMeta().getPersistentDataContainer().has(VBItem.VBItemKey, PersistentDataType.STRING)) {
            throw new ItemCreationException("Item is not a plugin item");
        } else {

            Material mat = itemStack.getType();

            String pdcOutput = itemStack.getItemMeta().getPersistentDataContainer().get(VBItem.VBItemKey, PersistentDataType.STRING);
            String bossEggType = null;
            if (itemStack.getItemMeta().getPersistentDataContainer().has(BossEggs.instance.pdcKey, PersistentDataType.STRING)) {
                bossEggType = itemStack.getItemMeta().getPersistentDataContainer().get(BossEggs.instance.pdcKey, PersistentDataType.STRING);
            }

            if (pdcOutput == null) {
                return;
            }

            switch (pdcOutput) {
                case "BaseballBat":
                    mat = BaseballBat.instance.itemMaterial;
                    break;
                case "Blazer":
                    mat = Blazer.instance.itemMaterial;
                    break;
                case "BossEggs":
                    mat = new BossEggs(EntityType.valueOf(bossEggType)).itemMaterial;
                    break;
                case "BouncySlime":
                    mat = BouncySlime.instance.itemMaterial;
                    break;
                case "ButchersAxe":
                    mat = ButchersAxe.instance.itemMaterial;
                    break;
                case "HMC":
                    mat = HeatedMagmaCream.instance.itemMaterial;
                    break;
                case "InvisibilityCloak":
                    mat = InvisibilityCloak.instance.itemMaterial;
                    break;
                case "Skeletor":
                    mat = Skeletor.instance.itemMaterial;
                    break;
                case "SlimeBoots":
                    mat = SlimeBoots.instance.itemMaterial;
                    break;
                case "Slingshot":
                    mat = Slingshot.instance.itemMaterial;
                    break;
            }

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
