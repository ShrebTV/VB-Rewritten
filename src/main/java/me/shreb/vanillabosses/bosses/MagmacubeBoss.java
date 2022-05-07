package me.shreb.vanillabosses.bosses;

import me.shreb.vanillabosses.Vanillabosses;
import me.shreb.vanillabosses.bosses.bossRepresentation.NormalBoss;
import me.shreb.vanillabosses.bosses.utility.BossCreationException;
import me.shreb.vanillabosses.logging.VBLogger;
import me.shreb.vanillabosses.utility.ConfigVerification;
import me.shreb.vanillabosses.utility.Utility;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.logging.Level;

public class MagmacubeBoss extends VBBoss implements ConfigVerification {

    public static MagmacubeBoss instance = new MagmacubeBoss();

    public static final String CONFIGSECTION = "Magma_cubeBoss";
    public static final String SCOREBOARDTAG = "BossMagmacube";

    @Override
    public LivingEntity makeBoss(Location location) throws BossCreationException {

        LivingEntity entity;

        //Attempting to spawn in a new Entity which is to be edited to a boss. logging failures as warning.
        try {
            entity = (LivingEntity) location.getWorld().spawnEntity(location, EntityType.MAGMA_CUBE);
        } catch (NullPointerException e) {
            new VBLogger(getClass().getName(), Level.WARNING, "Nullpointer Exception at Magmacube Boss. World or location was null.\n" +
                    "Location: " + location.toString()).logToFile();
            new VBLogger(getClass().getName(), Level.WARNING, e.toString());
            throw new BossCreationException("Could not create Magmacube Boss.");
        }

        //Attempting to edit the previously made entity into a boss. Logging failure as warning.
        try {
            makeBoss(entity);
        } catch (BossCreationException e) {
            new VBLogger(getClass().getName(), Level.WARNING, "Error creating a Magmacube boss!").logToFile();
        }

        return entity;
    }

    @Override
    public LivingEntity makeBoss(LivingEntity entity) throws BossCreationException {

        FileConfiguration config = Vanillabosses.getInstance().getConfig();

        // checking wether the entity passed in is a Magmacube. Logging as a warning and throwing an exception if not.
        if (!(entity instanceof MagmaCube)) {
            new VBLogger(getClass().getName(), Level.WARNING, "Attempted to make a Magmacube boss out of an Entity.\n" +
                    "Entity passed in: " + entity.getType() + "\n" +
                    "Boss could not be created!").logToFile();

            throw new BossCreationException("Attempted to make a boss out of an Entity. Could not make Magmacube Boss out of this Entity.");
        }

        //getting the Boss Attributes from the config file
        double health = config.getDouble("Bosses." + CONFIGSECTION + ".health");
        String nameColorString = config.getString("Bosses." + CONFIGSECTION + ".displayNameColor");

        ChatColor nameColor;

        //If the String is null or empty set it to a standard String
        if (nameColorString == null || nameColorString.equals("")) {
            new VBLogger(getClass().getName(), Level.WARNING, "Could not get name Color String for Magmacube boss! Defaulting to #000000").logToFile();
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
            new VBLogger(getClass().getName(), Level.WARNING, "Could not set Attributes on Magmacube Boss\n" +
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
    public void onBossHitByPlayer(EntityDamageByEntityEvent event) {
        FileConfiguration config = Vanillabosses.getInstance().getConfig();

        if (!(event.getDamager() instanceof Player)) return;
        if (!event.getEntity().getScoreboardTags().contains(SCOREBOARDTAG)) return;
        if (event.getEntity().getType() != EntityType.MAGMA_CUBE) return;

        Player player = (Player) event.getDamager();
        MagmaCube magma = (MagmaCube) event.getEntity();
        Location magmaLoc = magma.getLocation();
        int radius = config.getInt("Bosses.Magma_cubeBoss.onHitEvents.BurningAir.range");
        int time = config.getInt("Bosses.Magma_cubeBoss.onHitEvents.BurningAir.time");

        double chance = config.getInt("Bosses.Magma_cubeBoss.onHitEvents.BurningAir.chance");

        if (config.getBoolean("Bosses.Magma_cubeBoss.onHitEvents.BurningAir.enabled")
                && Utility.roll(chance)
                && magma.getHealth() > 0) {

            Utility.spawnParticles(Particle.FIREWORKS_SPARK, magma.getWorld(), magmaLoc, radius, radius, radius, 150, 3);
            player.getWorld().playSound(magmaLoc, Sound.ENTITY_SLIME_SQUISH, 1.0f, 1.0f);

            Bukkit.getScheduler().scheduleSyncDelayedTask(Vanillabosses.getInstance(), () -> {

                if (magma.getHealth() < 0) return;

                magma.getWorld().spawnParticle(Particle.FLAME, magma.getLocation(), 100, radius, radius, radius);

                for (Entity e : magma.getLocation().getWorld().getNearbyEntities(magma.getLocation(), radius, radius, radius, n -> n instanceof LivingEntity)) {
                    e.setFireTicks(20 * time);
                }

            }, 60L);
        }
    }

    @Override
    public boolean verifyConfig() {
        String fullConfig = "Bosses." + CONFIGSECTION + ".";

        VBLogger logger = new VBLogger("BlazeBoss", Level.WARNING, "");

        if (!verifyBoolean(fullConfig + "enabled")) {
            logger.setStringToLog("Config Error at '" + fullConfig + "enabled, has to be true or false");
            logger.logToFile();
        }

        if (!verifyString(config.getString(fullConfig + "displayName"))) {
            logger.setStringToLog("Config Error at '" + fullConfig + "displayName, cannot be empty");
            logger.logToFile();
        }

        if (!verifyColorCode(config.getString(fullConfig + "displayNameColor"))) {
            logger.setStringToLog("Config Error at '" + fullConfig + "displayNameColor, has to be a hexCode");
            logger.logToFile();
        }

        if (!verifyBoolean(fullConfig + "showDisplayNameAlways")) {
            logger.setStringToLog("Config Error at '" + fullConfig + "showDisplayNameAlways, has to be true or false");
            logger.logToFile();
        }

        return true;
    }
}