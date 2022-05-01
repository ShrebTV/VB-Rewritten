package me.shreb.vanillabosses.listeners;

import me.shreb.vanillabosses.Vanillabosses;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class AFKChecker implements Listener {

    //TODO Test this
    @EventHandler
    public void onBossIsHit(EntityDamageByEntityEvent event){
        if (event.getDamager() instanceof Player) {

            if (!prevloc.containsKey(event.getDamager().getUniqueId())) {
                prevloc.put(event.getDamager().getUniqueId(), event.getDamager().getLocation());
            }
            if (!counter.containsKey(event.getDamager().getUniqueId())) {
                counter.put(event.getDamager().getUniqueId(), 0);
            }
            afkCheck(event);
        }
    }


    static Configuration config = Vanillabosses.getInstance().getConfig();
    public static HashMap<UUID, Location> prevloc = new HashMap<>();
    public static HashMap<UUID, Integer> counter = new HashMap<>();
    static HashMap<UUID, Integer> timer1= new HashMap<>();
    static HashMap<UUID, Integer> timer2= new HashMap<>();
    /**
     * This method is meant to check whether a player is actually killing monsters legit or grinding them in a mobfarm with an auto clicker while afk
     *
     * @param event the event to be checked for afk players hitting mobs
     */
    public static void afkCheck(EntityDamageByEntityEvent event) {
        //AFKCHECK START

        if (config.getBoolean("Bosses.enableAntiAFKTeleports")) {

            if(!timer1.containsKey(event.getDamager().getUniqueId())){timer1.put(event.getDamager().getUniqueId(), 0);}
            if(!timer2.containsKey(event.getDamager().getUniqueId())){timer2.put(event.getDamager().getUniqueId(), 0);}

            if (prevloc.get(event.getDamager().getUniqueId()).equals(event.getDamager().getLocation())) { //check wether a players location or rotation is the same as the last time they hit a boss
                if(timer1.get(event.getDamager().getUniqueId()) == 0) {
                    counter.put(event.getDamager().getUniqueId(), counter.get(event.getDamager().getUniqueId()) + 1); // counter++ if the location hasnt changed
                    int taskID = Bukkit.getScheduler().scheduleSyncDelayedTask(Vanillabosses.getInstance(), () -> timer1.put(event.getDamager().getUniqueId(), 0), 5);
                    timer1.put(event.getDamager().getUniqueId(), taskID);
                }

                if(counter.get(event.getDamager().getUniqueId()) > config.getInt("Bosses.AntiAFKHitLimit") - 3){
                    if(timer2.get(event.getDamager().getUniqueId()) == 0){
                        event.getDamager().sendMessage(ChatColor.GRAY + config.getString("Bosses.AntiAFKWarningMessage"));
                        int taskID = Bukkit.getScheduler().scheduleSyncDelayedTask(Vanillabosses.getInstance(), () -> timer2.put(event.getDamager().getUniqueId(), 0), 100);
                        timer2.put(event.getDamager().getUniqueId(), taskID);
                    }
                }

            } else {
                counter.put(event.getDamager().getUniqueId(), 0);
                prevloc.put(event.getDamager().getUniqueId(), event.getDamager().getLocation()); //set the location to compare the next time the player hits a boss
            }

            if (counter.get(event.getDamager().getUniqueId()) > config.getInt("Bosses.AntiAFKHitLimit") &&
                    prevloc.get(event.getDamager().getUniqueId()).equals(event.getDamager().getLocation())) {
                //check wether the player has hit bosses more than the limit without changing location/moving their mouse
                int afkCheck = ThreadLocalRandom.current().nextInt(-1, 1000);
                //make a random number to compare against the chance in Bosses.AntiAFKChance

                if (afkCheck < config.getInt("Bosses.AntiAFKChance")) { //check wether it should teleport the boss onto the player
                    if(event.getEntity().getType() != EntityType.CREEPER){
                        event.getEntity().teleport(event.getDamager()); //teleport the mob onto the player who is afk grinding bosses
                        event.setCancelled(true);
                    }
                }
            }
        }
    }
}
