package me.shreb.vanillabosses.bosses;

import me.shreb.vanillabosses.bosses.utility.BossCreationException;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

import java.net.http.WebSocket;

public abstract class VBBoss implements WebSocket.Listener {

    public static final String BOSSTAG = "VB-Boss";

    /**
     * creates a completely new Entity and makes it into a boss
     * @param location the location at which the boss should be spawned
     * @return the resulting boss
     * @throws BossCreationException if there is a problem creating the boss
     */
    abstract LivingEntity makeBoss(Location location) throws BossCreationException;

    /**
     * Edits an existing Entity into a Boss version of said entity
     * @param entity the entity which is to be turned into a boss
     * @return the resulting boss
     * @throws BossCreationException if there is a problem creating the boss
     */
    abstract LivingEntity makeBoss(LivingEntity entity) throws BossCreationException;

    private void registerListeners(){

    }

}
