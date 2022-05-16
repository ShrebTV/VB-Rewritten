package me.shreb.vanillabosses.bosses.utility.bossarmor;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.ThreadLocalRandom;

/**
 * This class defines a set of armor to be put on a boss.
 * The armor can be enchanted using methods provided in this class
 * Putting armor on an entity is done by using the provided equipArmor() method
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
    public void enchantAllArmor(Enchantment enchantment, int minLevel, int maxLevel) {

        int actualLevel = ThreadLocalRandom.current().nextInt(minLevel, maxLevel + 1);

        this.enchantAllArmor(enchantment, actualLevel);

    }

    /**
     * Enchants the specified piece of armor inside this object with the enchantment of the specified level
     *
     * @param enchantment The enchantment to put on the armor piece
     * @param level       The level to put the enchantment at
     * @param slot        the slot of armor which is supposed to be enchanted
     */
    public void enchantArmorPiece(Enchantment enchantment, int level, ArmorSlot slot) {

        this.armorSet[slot.getIndex()].addUnsafeEnchantment(enchantment, level);

    }

    /**
     * Enchants the specified piece of armor inside this object with the enchantment of the specified level
     *
     * @param enchantment The enchantment to put on the armor piece
     * @param minLevel    The minimum level to put the enchantment at
     * @param maxLevel    The maximum level to put the enchantment at.
     *                    The level will be randomly chosen between the min and max
     * @param slot        the slot of armor which is supposed to be enchanted
     */
    public void enchantArmorPiece(Enchantment enchantment, int minLevel, int maxLevel, ArmorSlot slot) {

        int actualLevel = ThreadLocalRandom.current().nextInt(minLevel, maxLevel + 1);

        enchantArmorPiece(enchantment, actualLevel, slot);

    }

    /**
     * @return the Array of ItemStacks containing the armor of this object
     */
    public ItemStack[] getArmorSet() {
        return this.armorSet;
    }

}
