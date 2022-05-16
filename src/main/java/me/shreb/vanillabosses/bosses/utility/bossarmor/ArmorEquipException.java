package me.shreb.vanillabosses.bosses.utility.bossarmor;

/**
 * This exception indicates a problem of any kind related to equipping armor onto a Living Entity
 */
public class ArmorEquipException extends Exception{

    public ArmorEquipException(String s){
        super(s);
    }
}