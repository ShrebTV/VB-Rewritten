package me.shreb.vanillabosses.utility;

import me.shreb.vanillabosses.Vanillabosses;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;

import java.util.concurrent.ThreadLocalRandom;

public class Utility {


    public static void spawnParticles(Particle particle, World world, Location loc, double offsetX, double offsetY, double offsetZ, int amount, int repeats) {

        for (int i = 0; i < repeats; i++) {

            Bukkit.getScheduler().scheduleSyncDelayedTask(Vanillabosses.getInstance(), () -> world.spawnParticle(particle, loc, amount, offsetX, offsetY, offsetZ), 10L * i);

        }
    }

    /**
     * A Method to see whether an action should be executed
     * @param chance the chance to check against
     * @return true if the thing the chance applies to should be executed
     */
    public static boolean roll(double chance) {
        return ThreadLocalRandom.current().nextDouble() < chance;
    }
}
