package me.shreb.vanillabosses.bosses;

import me.shreb.vanillabosses.Vanillabosses;
import me.shreb.vanillabosses.bosses.bossRepresentation.NormalBoss;
import me.shreb.vanillabosses.bosses.utility.BossCreationException;
import me.shreb.vanillabosses.logging.VBLogger;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Level;

public class SlimeBoss extends VBBoss {

    public static SlimeBoss instance = new SlimeBoss();

    public static final String CONFIGSECTION = "SlimeBoss";
    public static final String SCOREBOARDTAG = "BossSlime";

    @Override
    public LivingEntity makeBoss(Location location) throws BossCreationException {

        LivingEntity entity;

        //Attempting to spawn in a new Entity which is to be edited to a boss. logging failures as warning.
        try {
            entity = (LivingEntity) location.getWorld().spawnEntity(location, EntityType.SLIME);
        } catch (NullPointerException e) {
            new VBLogger(getClass().getName(), Level.WARNING, "Nullpointer Exception at Slime Boss. World or location was null.\n" +
                    "Location: " + location.toString()).logToFile();
            new VBLogger(getClass().getName(), Level.WARNING, e.toString());
            throw new BossCreationException("Could not create Slime Boss.");
        }

        //Attempting to edit the previously made entity into a boss. Logging failure as warning.
        try {
            makeBoss(entity);
        } catch (BossCreationException e) {
            new VBLogger(getClass().getName(), Level.WARNING, "Error creating a Slime boss!").logToFile();
        }

        return entity;
    }

    @Override
    public LivingEntity makeBoss(LivingEntity entity) throws BossCreationException {

        FileConfiguration config = Vanillabosses.getInstance().getConfig();

        // checking wether the entity passed in is a Slime. Logging as a warning and throwing an exception if not.
        if (!(entity instanceof Slime)) {
            new VBLogger(getClass().getName(), Level.WARNING, "Attempted to make a Slime boss out of an Entity.\n" +
                    "Entity passed in: " + entity.getType() + "\n" +
                    "Boss could not be created!").logToFile();

            throw new BossCreationException("Attempted to make a boss out of an Entity. Could not make Slime Boss out of this Entity.");
        }

        //getting the Boss Attributes from the config file
        double health = config.getDouble("Bosses." + CONFIGSECTION + ".health");
        String nameColorString = config.getString("Bosses." + CONFIGSECTION + ".displayNameColor");

        ChatColor nameColor;

        //If the String is null or empty set it to a standard String
        if (nameColorString == null || nameColorString.equals("")) {
            new VBLogger(getClass().getName(), Level.WARNING, "Could not get name Color String for Slime boss! Defaulting to #000000").logToFile();
            nameColor = ChatColor.of("#000000");
        } else {
            try {
                nameColor = ChatColor.of(nameColorString);
            } catch (IllegalArgumentException e) {
                nameColor = ChatColor.of("#000000");
            }
        }

        String name = config.getString("Bosses." + CONFIGSECTION + ".displayName");

        //setting the entity Attributes. Logging failure as Warning.
        try {
            entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(health);
            entity.setHealth(health);
            entity.setCustomName(nameColor + name);
            entity.setCustomNameVisible(config.getBoolean("Bosses." + CONFIGSECTION + ".showDisplayNameAlways"));

        } catch (Exception e) {
            new VBLogger(getClass().getName(), Level.WARNING, "Could not set Attributes on Slime Boss\n" +
                    "Reason: " + e).logToFile();
        }

        // Setting scoreboard tag so the boss can be recognised.
        entity.getScoreboardTags().add(SCOREBOARDTAG);
        entity.getScoreboardTags().add(BOSSTAG);
        entity.getScoreboardTags().add(REMOVE_ON_DISABLE_TAG);

        new NormalBoss(entity.getType()).putCommandsToPDC(entity);

        //Putting glowing effect on bosses if config is set to do so.
        if (Vanillabosses.getInstance().getConfig().getBoolean("Bosses.bossesGetGlowingPotionEffect")) {
            entity.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, Integer.MAX_VALUE, 1));
        }

        return null;
    }

    @EventHandler
    public void onSlimeBossFallDMG(EntityDamageEvent event) {

        if (event.getCause() != EntityDamageEvent.DamageCause.FALL) return;
        if (event.getEntity().getScoreboardTags().contains("NoFallDMG") && event.getEntity().getScoreboardTags().contains(SCOREBOARDTAG)) {
            event.setCancelled(true);
            event.getEntity().getScoreboardTags().removeIf(n -> n.equals("NoFallDMG"));
        }
    }


    static boolean isJumping = false;

    @EventHandler
    public void onHitAbility(EntityDamageByEntityEvent event) {

        if (event.getEntity().getScoreboardTags().contains(SCOREBOARDTAG) && event.getEntityType() == EntityType.SLIME) {

            if (!(event.getDamager() instanceof Player)) return;
            double jumpSlamChance = Vanillabosses.getInstance().getConfig().getDouble("Bosses.SlimeBoss.onHitEvents.JumpSlam.chance");

            if (new Random().nextDouble() < jumpSlamChance) {

                if (event.getFinalDamage() > ((LivingEntity) event.getEntity()).getHealth()) return;

                if (isJumping) return;
                isJumping = true;
                Slime slime = (Slime) event.getEntity();

                ArrayList<Entity> list = (ArrayList<Entity>) event.getEntity().getWorld().getNearbyEntities(event.getEntity().getLocation(), 15, 10, 15, n -> (n instanceof Player));

                Bukkit.getScheduler().scheduleSyncDelayedTask(Vanillabosses.getInstance(), () -> {
                    slime.setVelocity(new Vector(0, 2, 0));
                }, 1);

                Bukkit.getScheduler().scheduleSyncDelayedTask(Vanillabosses.getInstance(), () -> {

                    slime.setVelocity(new Vector(0, -5, 0));
                    slime.getScoreboardTags().add("NoFallDMG");


                    Bukkit.getScheduler().scheduleSyncDelayedTask(Vanillabosses.getInstance(), () -> {

                        for (Entity e : list) {
                            if (e.getWorld().getBlockAt(e.getLocation().subtract(0, 0.1, 0)).getType() != Material.AIR
                                    && e.getWorld().getBlockAt(e.getLocation().subtract(0, 0.1, 0)).getType() != Material.LAVA
                                    && e.getWorld().getBlockAt(e.getLocation().subtract(0, 0.1, 0)).getType() != Material.GRASS
                                    && e.getWorld().getBlockAt(e.getLocation().subtract(0, 0.1, 0)).getType() != Material.TALL_GRASS
                                    && e.isOnGround()
                            ) {
                                int x = e.getLocation().getBlockX() - slime.getLocation().getBlockX();
                                int z = e.getLocation().getBlockZ() - slime.getLocation().getBlockZ();
                                Vector v = new Vector(x, 2, z);
                                e.setVelocity(v);
                            }
                        }

                        event.getEntity().getWorld().playSound(event.getEntity().getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1, 1);
                        isJumping = false;

                    }, 8);

                }, 20);
            }
        }
    }
}