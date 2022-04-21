package me.shreb.vanillabosses;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;

public class Utility {

    public static void spawnParticles(Particle particle, World world, Location loc, double offsetX, double offsetY, double offsetZ, int amount, int repeats) {

        for (int i = 0; i < repeats; i++) {

            Bukkit.getScheduler().scheduleSyncDelayedTask(Vanillabosses.getInstance(), () -> world.spawnParticle(particle, loc, amount, offsetX, offsetY, offsetZ), 10L * i);

        }
    }

}
