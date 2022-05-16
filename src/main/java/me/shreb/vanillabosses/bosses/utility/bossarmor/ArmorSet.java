package me.shreb.vanillabosses.bosses.utility.bossarmor;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.ThreadLocalRandom;

/**
 * This class defines a set of armor to be put on a boss.
 */
public class ArmorSet {

    private final ItemStack[] armorSet; // An array of itemstacks, to be ordered in a way that makes Equipment.setArmorContents accept it properly

    /**
     * Creates a new ArmorSet object from the material specified
     * @param material The material the armor should be made of
     */
    public ArmorSet(ArmorSetType material){
        this.armorSet = material.toFullSet();
    }

    /**
     * Puts the armor from this object onto the LivingEntity specified
     * @param entity The entity to put the armor on
     */
    public void equipArmor(LivingEntity entity) throws ArmorEquipException{

        EntityEquipment equipment = entity.getEquipment();

        if(equipment == null){
            throw new ArmorEquipException("Could not put Armor on Entity. Equipment was null.");
        }

        equipment.setArmorContents(this.armorSet);
    }
    
    /**
     * Enchants all armor with the specified enchantment of the level given
     * @param enchantment the enchantment to put on all armor in this object
     * @param level the level the enchantment should be of
     */
    public void enchantAllArmor(Enchantment enchantment, int level){

        for(ItemStack stack : this.armorSet){
            stack.addUnsafeEnchantment(enchantment, level);
        }
    }

    /**
     * Enchants all armor with the specified enchantment of the level given
     * The level of the enchantment will be a random number between the minLevel and maxLevel
     *
     * @param enchantment the enchantment to put on all armor in this object
     * @param minLevel the minimum level of the enchantment
     * @param maxLevel the maximum level of the enchantment
     */
    public void enchantAllArmor(Enchantment enchantment, int minLevel, int maxLevel){

        int actualLevel = ThreadLocalRandom.current().nextInt(minLevel, maxLevel + 1);

        this.enchantAllArmor(enchantment, actualLevel);

    }

    /**
     * @return the Array of ItemStacks containing the armor of this object
     */
    public ItemStack[] getArmorSet(){
        return this.armorSet;
    }

}
