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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class VBBossBar implements Listener {

    private LivingEntity assignedEntity;
    private final BossBar bossBar;

    public static final HashMap<UUID, VBBossBar> bossBarMap = new HashMap<>();

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

        //TODO implement boss bar support for respawning bosses
        //TODO implement the creeper boss uuid being replaced with this method when it explodes
        VBBossBar bar = bossBarMap.get(oldID);

        if (bar == null) {
            return;
        }

        bossBarMap.remove(oldID);

        Entity entity = Vanillabosses.getInstance().getServer().getEntity(newID);

        if (entity != null && !entity.isDead() && entity instanceof LivingEntity) {

            bar.assignedEntity = (LivingEntity) entity;
            bossBarMap.put(newID, bar);

        }
    }

    /**
     * Used to update this bossBar progress.
     * removes the entity from the bossBarMap if it is dead or null
     */
    public void updateBossBar() {

        if (assignedEntity != null && !assignedEntity.isDead()) {

            this.bossBar.setProgress(assignedEntity.getHealth() / assignedEntity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue());

        } else {
            bossBarMap.remove(assignedEntity.getUniqueId());
        }
    }

    /**
     * shows this bossBar to every player within a certain radius
     * if a player goes out of range the player will no longer see the boss bar after this is called
     */
    public void showBossBar() {

        Location location;

        if (this.assignedEntity != null && !this.assignedEntity.isDead()) {
            location = this.assignedEntity.getLocation();
        } else {
            return;
        }

        ArrayList<Entity> players = (ArrayList<Entity>) location.getWorld().getNearbyEntities(location, 35, 20, 35, n -> n instanceof Player);
        ArrayList<Player> onlinePlayers = new ArrayList<>(Vanillabosses.getInstance().getServer().getOnlinePlayers());

        for (Entity player : players) {

            Player p = (Player) player;
            onlinePlayers.remove(p);

            this.bossBar.addPlayer(p);
        }

        for (Player player : onlinePlayers) {
            this.bossBar.removePlayer(player);
        }
    }

    /**
     * This updates the boss bar linked to the damaged boss whenever the boss is damaged
     * @param event
     */
    @EventHandler
    public void onBossDamaged(EntityDamageEvent event){

        if(bossBarMap.containsKey(event.getEntity().getUniqueId())){

            VBBossBar bar = bossBarMap.get(event.getEntity().getUniqueId());

            bar.updateBossBar();
        }
    }

    /**
     * This method starts a timer to periodically update the list of players each bossBar from this plugin is shown to
     *
     * This should only be called once on startup
     */
    public static void startBarShowTimer(){

        Bukkit.getScheduler().runTaskTimer(Vanillabosses.getInstance(), () -> {

            for(VBBossBar bar : bossBarMap.values()){
                bar.showBossBar();
            }

        }, 80, 40);
    }
}