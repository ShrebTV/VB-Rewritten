package me.shreb.vanillabosses.bosses.utility;

import me.shreb.vanillabosses.Vanillabosses;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class VBBossBar implements Listener {

    private LivingEntity assignedEntity;
    public BossBar bossBar;

    public static final HashMap<UUID, VBBossBar> bossBarMap = new HashMap<>();

    /**
     * private constructor for registering listeners in this class
     * <p>
     * This constructor should not be called anywhere else
     */
    private VBBossBar() {

    }

    /**
     * registers the Listeners of this class
     */
    public static void registerListeners() {
        Vanillabosses.getInstance().getServer().getPluginManager().registerEvents(new VBBossBar(), Vanillabosses.getInstance());
    }

    /**
     * Makes a new VBBossBar and puts it inside the bossBarMap automatically so that it is updated, shown and removed if the entity dies
     *
     * @param assignedEntity the entity this bossBar should be watching
     * @param bossBar        the bossBar for the assignedEntity
     */
    public VBBossBar(LivingEntity assignedEntity, BossBar bossBar) {
        this.assignedEntity = assignedEntity;
        this.bossBar = bossBar;

        bossBarMap.put(assignedEntity.getUniqueId(), this);
    }

    /**
     * Replaces the Entity assigned to the bossBar with a new one.
     * Also replaces the reference inside the bossBarMap to be the new one
     *
     * @param oldID the UUID of the old entity assigned to the bossBar referenced by this ID
     * @param newID the UUID of the new entity which will be assigned to the bossBar specified by the old ID
     */
    public static void replaceAssignedEntity(UUID oldID, UUID newID) {

        //TODO test boss bar support for respawning bosses
        //get the referenced bar with the old id
        VBBossBar bar = bossBarMap.get(oldID);

        //check for whether the map even contained that value
        if (bar == null) {
            //return if not
            return;
        }

        //remove the old bar from the map
        bossBarMap.remove(oldID);

        //get the entity with the new UUID
        Entity entity = Vanillabosses.getInstance().getServer().getEntity(newID);

        //check whether the entity is valid and a LivingEntity
        if (entity != null && !entity.isDead() && entity instanceof LivingEntity) {
            //If check successful, assign the new entity to the bar object and put into map
            bar.assignedEntity = (LivingEntity) entity;
            bossBarMap.put(newID, bar);
        }
    }

    /**
     * Used to update this bossBar progress.
     * removes the entity from the bossBarMap if it is dead or null
     */
    public void updateBossBar() {

        //check for valid assigned entity
        if (assignedEntity != null && !assignedEntity.isDead()) {
            //Update progress of the bar
            this.bossBar.setProgress(assignedEntity.getHealth() / assignedEntity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue());

        } else {
            //if not valid, kill the bossBar
            this.killBossBar();
        }
    }

    /**
     * shows this bossBar to every player within a certain radius
     * if a player goes out of range the player will no longer see the boss bar after this is called
     */
    public void showBossBar() {

        Location location;

        // check for valid assigned entity, kill the boss bar if not valid
        if (this.assignedEntity != null && !this.assignedEntity.isDead()) {
            location = this.assignedEntity.getLocation();
        } else {
            this.killBossBar();
            return;
        }

        //List containing all players within a 20x20 radius and 15 up and down.
        ArrayList<Entity> players = (ArrayList<Entity>) location.getWorld().getNearbyEntities(location, 20, 15, 20, n -> n instanceof Player);
        //remove all players from seeing the bossbar
        this.bossBar.removeAll();

        //add all players which were in the radius when checked
        for (Entity player : players) {
            Player p = (Player) player;
            this.bossBar.addPlayer(p);
        }
    }

    /**
     * This updates the boss bar linked to the damaged boss whenever the boss is damaged
     *
     * @param event
     */
    @EventHandler
    public void onBossDamaged(EntityDamageEvent event) {

        if (bossBarMap.containsKey(event.getEntity().getUniqueId())) {
            VBBossBar bar = bossBarMap.get(event.getEntity().getUniqueId());
            bar.updateBossBar();
        }
    }

    /**
     * When the entity associated with a bossBar dies, kill the boss bar
     *
     * @param event
     */
    @EventHandler
    public void onBossDeath(EntityDeathEvent event) {
        bossBarMap.get(event.getEntity().getUniqueId()).killBossBar();
    }

    /**
     * removes the bossBar from the bossBarMap and makes sure no player can see the bossBar anymore
     */
    private void killBossBar() {
        this.bossBar.removeAll();
        bossBarMap.remove(this.assignedEntity.getUniqueId());
    }

    /**
     * This method starts a timer to periodically update the list of players each bossBar from this plugin is shown to the players near the assigned entity
     * This should only be called once on startup
     */
    public static void startBarShowTimer() {

        Bukkit.getScheduler().runTaskTimer(Vanillabosses.getInstance(), () -> {

            for (VBBossBar bar : bossBarMap.values()) {

                if (bar.assignedEntity.isDead()) {
                    bar.killBossBar();
                } else {
                    bar.showBossBar();
                }
            }
        }, 80, 40);
    }
}