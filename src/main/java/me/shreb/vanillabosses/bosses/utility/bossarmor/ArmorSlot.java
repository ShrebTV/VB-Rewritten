package me.shreb.vanillabosses.bosses.utility.bossarmor;

/**
 * An Enum to list the armor slots and the indexes within a properly sorted Array for putting onto an entity directly
 * Basically an Enum of Armor slots
 */
public enum ArmorSlot {

    BOOTS(0),
    LEGGINGS(1),
    CHESTPLATE(2),
    HELMET(3);

    private final int index;

    ArmorSlot(int index) {
        this.index = index;
    }

    /**
     * @return the index of the Slot in a properly sorted ArmorContents
     */
    public int getIndex() {
        return this.index;
    }

}