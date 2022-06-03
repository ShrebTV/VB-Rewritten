package me.shreb.vanillabosses.bosses;

import me.shreb.vanillabosses.Vanillabosses;
import me.shreb.vanillabosses.bosses.bossRepresentation.NormalBoss;
import me.shreb.vanillabosses.bosses.utility.BossCreationException;
import me.shreb.vanillabosses.logging.VBLogger;
import me.shreb.vanillabosses.utility.Utility;
import me.shreb.vanillabosses.utility.configFiles.FileCreator;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.EntityTeleportEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.logging.Level;

public class EndermanBoss extends VBBoss {

    public static EndermanBoss instance = new EndermanBoss();

    public static final String CONFIGSECTION = "EndermanBoss";
    public static final String SCOREBOARDTAG = "BossEnderman";

    public EndermanBoss() {
        new FileCreator().createAndLoad(FileCreator.endermanBossPath, this.config);
    }

    @Override
    public LivingEntity makeBoss(Location location) throws BossCreationException {

        LivingEntity entity;

        //Attempting to spawn in a new Entity which is to be edited to a boss. logging failures as warning.
        try {
            entity = (LivingEntity) location.getWorld().spawnEntity(location, EntityType.ENDERMAN);
        } catch (NullPointerException e) {
            new VBLogger(getClass().getName(), Level.WARNING, "Nullpointer Exception at Enderman Boss. World or location was null.\n" +
                    "Location: " + location.toString()).logToFile();
            new VBLogger(getClass().getName(), Level.WARNING, e.toString());
            throw new BossCreationException("Could not create Enderman Boss.");
        }

        //Attempting to edit the previously made entity into a boss. Logging failure as warning.
        try {
            makeBoss(entity);
        } catch (BossCreationException e) {
            new VBLogger(getClass().getName(), Level.WARNING, "Error creating a Enderman boss!").logToFile();
        }

        return entity;
    }

    @Override
    public LivingEntity makeBoss(LivingEntity entity) throws BossCreationException {

        if (!instance.config.getBoolean("enabled")) return entity;

        // checking wether the entity passed in is a Enderman. Logging as a warning and throwing an exception if not.
        if (!(entity instanceof Enderman)) {
            new VBLogger(getClass().getName(), Level.WARNING, "Attempted to make a Enderman boss out of an Entity.\n" +
                    "Entity passed in: " + entity.getType() + "\n" +
                    "Boss could not be created!").logToFile();

            throw new BossCreationException("Attempted to make a boss out of an Entity. Could not make Enderman Boss out of this Entity.");
        }

        Bukkit.getScheduler().runTaskLater(Vanillabosses.getInstance(), () -> {

            //getting the Boss Attributes from the config file
            double health = instance.config.getDouble("health");
            String nameColorString = instance.config.getString("displayNameColor");

            ChatColor nameColor;

            //If the String is null or empty set it to a standard String
            if (nameColorString == null || nameColorString.equals("")) {
                new VBLogger(getClass().getName(), Level.WARNING, "Could not get name Color String for Enderman boss! Defaulting to #000000").logToFile();
                nameColor = ChatColor.of("#000000");
            } else {
                try {
                    nameColor = ChatColor.of(nameColorString);
                } catch (IllegalArgumentException e) {
                    nameColor = ChatColor.of("#000000");
                }
            }

            String name = instance.config.getString("displayName");

            double speedMultiplier = instance.config.getDouble("SpeedModifier");
            if (speedMultiplier < 0.0001) speedMultiplier = 1;
            entity.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(speedMultiplier * entity.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).getBaseValue());

            //setting the entity Attributes. Logging failure as Warning.
            try {
                entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(health);
                entity.setHealth(health);
                entity.setCustomName(nameColor + name);
                entity.setCustomNameVisible(instance.config.getBoolean("showDisplayNameAlways"));

            } catch (Exception e) {
                new VBLogger(getClass().getName(), Level.WARNING, "Could not set Attributes on Enderman Boss\n" +
                        "Reason: " + e).logToFile();
            }

        }, 1);

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

    /**
     * This is what will happen once the enderman teleports.
     * It spawns a certain amount of endermites if enabled in the config
     *
     * @param event the event to check for enderman bosses in
     */
    @EventHandler
    public void onTeleport(EntityTeleportEvent event) {

        if (event.getEntity().getType().equals(EntityType.ENDERMAN) && event.getEntity().getScoreboardTags().contains(SCOREBOARDTAG)) {

            int amount = instance.config.getInt("onHitEvents.endermiteSpawnOnTeleport.amount");

            if (instance.config.getBoolean("onHitEvents.endermiteSpawnOnTeleport.enabled")) {
                for (int i = 0; i <= amount; i++) {
                    event.getEntity().getWorld().spawnEntity(event.getEntity().getLocation(), EntityType.ENDERMITE);
                }
            }
        }
    }

    /**
     * This is what will happen once an enderman boss tries to target an endermite.
     * It will not let the enderman boss target endermites.
     *
     * @param event
     */
    @EventHandler
    public void onEndermanTargetMite(EntityTargetEvent event) {

        if (event.getTarget() == null) return;

        if (event.getEntity().getScoreboardTags().contains("BossEnderman")) {

            if (event.getTarget().getType() == EntityType.ENDERMITE) {
                event.setCancelled(true);
                return;
            }
        }

        if (event.getEntityType() == EntityType.ENDERMITE && event.getTarget().getScoreboardTags().contains("BossEnderman")) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onHitEvent(EntityDamageByEntityEvent event) {

        if (event.getEntity().getScoreboardTags().contains("BossEnderman") && event.getEntityType() == EntityType.ENDERMAN) {

            if (!(event.getDamager() instanceof Player)) return;

            Enderman enderman = (Enderman) event.getEntity();

            for (String s : instance.config.getStringList("onHitEvents.potionEffects")) {
                String[] strings = s.split(":");
                PotionEffectType type = PotionEffectType.getByName(strings[0].toUpperCase());

                if (type != null && Utility.roll(Double.parseDouble(strings[3]))) {
                    enderman.addPotionEffect(new PotionEffect(type, Integer.parseInt(strings[2]) * 20, Integer.parseInt(strings[1])));
                }
            }

            if (Utility.roll(instance.config.getDouble("onHitEvents.teleportBehindPlayer.chance")) && instance.config.getBoolean("onHitEvents.teleportBehindPlayer.enabled")) {
                int x = event.getDamager().getLocation().getBlockX() - enderman.getLocation().getBlockX();
                int y = event.getDamager().getLocation().getBlockY() - enderman.getLocation().getBlockY();
                int z = event.getDamager().getLocation().getBlockZ() - enderman.getLocation().getBlockZ();
                Vector v = new Vector(x / 4, y / 3, z / 4);

                Location newLoc = event.getDamager().getLocation();
                Location originalLoc = enderman.getLocation();

                EnderPearl ep = (EnderPearl) enderman.getWorld().spawnEntity(enderman.getEyeLocation(), EntityType.ENDER_PEARL);
                ep.setVelocity(v);

                enderman.teleport(newLoc);

                if (instance.config.getBoolean("onHitEvents.teleportBehindPlayer.teleportBackToOldLocation")) {
                    Bukkit.getScheduler().scheduleSyncDelayedTask(Vanillabosses.getInstance(), () -> enderman.teleport(originalLoc), 20L * instance.config.getInt("onHitEvents.teleportBehindPlayer.teleportBackDelay"));

                }

                if (instance.config.getBoolean("onHitEvents.teleportBehindPlayer.invisibility")) {
                    enderman.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, instance.config.getInt("onHitEvents.teleportBehindPlayer.invisibilityDuration") * 20, 3));
                }
            }
        }
    }
}