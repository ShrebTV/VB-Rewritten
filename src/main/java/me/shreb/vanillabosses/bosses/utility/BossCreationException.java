package me.shreb.vanillabosses.bosses.utility;

/**
 * thrown when the creation of a Boss is unsuccessful for any reason
 */
public class BossCreationException extends Exception{
    public BossCreationException(String s){super(s);}
}
