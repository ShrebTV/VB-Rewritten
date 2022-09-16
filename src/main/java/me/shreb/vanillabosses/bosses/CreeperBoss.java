package me.shreb.vanillabosses.bosses;

import me.shreb.vanillabosses.Vanillabosses;
import me.shreb.vanillabosses.bosses.bossRepresentation.NormalBoss;
import me.shreb.vanillabosses.bosses.bossRepresentation.RespawningBoss;
import me.shreb.vanillabosses.bosses.utility.BossCommand;
import me.shreb.vanillabosses.bosses.utility.BossCreationException;
import me.shreb.vanillabosses.bosses.utility.VBBossBar;
import me.shreb.vanillabosses.logging.VBLogger;
import me.shreb.vanillabosses.utility.Utility;
import me.shreb.vanillabosses.utility.configFiles.FileCreator;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Map;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class CreeperBoss extends VBBoss {

    public static CreeperBoss instance = new CreeperBoss();

    public static final String CONFIGSECTION = "CreeperBoss";
    public static final String SCOREBOARDTAG = "BossCreeper";

    public static final String EXPLODINGTAG = "ExplodingATM";
    public static final String CANCEL_EXPLOSION = "CancelOnExplode";
    public static final String CANCEL_BLOWUP_ITEMS = "dontBlowUpItems";

    public CreeperBoss(){
        new FileCreator().createAndLoad(FileCreator.creeperBossPath, this.config);
    }

    @Override
    public LivingEntity makeBoss(Location location) throws BossCreationException {

        LivingEntity entity;

        //Attempting to spawn in a new Entity which is to be edited to a boss. logging failures as warning.
        try {
            entity = (LivingEntity) location.getWorld().spawnEntity(location, EntityType.CREEPER);
        } catch (NullPointerException e) {
            new VBLogger(getClass().getName(), Level.WARNING, "Nullpointer Exception at Creeper Boss. World or location was null.\n" +
                    "Location: " + location.toString()).logToFile();
            new VBLogger(getClass().getName(), Level.WARNING, e.toString());
            throw new BossCreationException("Could not create Creeper Boss.");
        }

        //Attempting to edit the previously made entity into a boss. Logging failure as warning.
        try {
            makeBoss(entity);
        } catch (BossCreationException e) {
            new VBLogger(getClass().getName(), Level.WARNING, "Error creating a Creeper boss!").logToFile();
        }

        return entity;
    }

    @Override
    public LivingEntity makeBoss(LivingEntity entity) throws BossCreationException {

        if (!instance.config.getBoolean("enabled")) return entity;

        // checking whether the entity passed in is a Creeper. Logging as a warning and throwing an exception if not.
        if (!(entity instanceof Creeper)) {
            new VBLogger(getClass().getName(), Level.WARNING, "Attempted to make a Creeper boss out of an Entity.\n" +
                    "Entity passed in: " + entity.getType() + "\n" +
                    "Boss could not be created!").logToFile();

            throw new BossCreationException("Attempted to make a boss out of an Entity. Could not make Creeper Boss out of this Entity.");
        }

        Bukkit.getScheduler().runTaskLater(Vanillabosses.getInstance(), () -> {

            //getting the Boss Attributes from the config file
            double health = instance.config.getDouble("health");
            String nameColorString = instance.config.getString("displayNameColor");

            ChatColor nameColor;

            //If the String is null or empty set it to a standard String
            if (nameColorString == null || nameColorString.equals("")) {
                new VBLogger(getClass().getName(), Level.WARNING, "Could not get name Color String for Creeper boss! Defaulting to #000000").logToFile();
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
                new VBLogger(getClass().getName(), Level.WARNING, "Could not set Attributes on Creeper Boss\n" +
                        "Reason: " + e).logToFile();
            }

        }, 1);

        // Setting scoreboard tag so the boss can be recognised.
        entity.getScoreboardTags().add(SCOREBOARDTAG);
        entity.getScoreboardTags().add(BOSSTAG);
        entity.getScoreboardTags().add(REMOVE_ON_DISABLE_TAG);
        entity.getScoreboardTags().add(CANCEL_BLOWUP_ITEMS);
        entity.addScoreboardTag(CANCEL_EXPLOSION);

        new NormalBoss(entity.getType()).putCommandsToPDC(entity);

        //Putting glowing effect on bosses if config is set to do so.
        if (Vanillabosses.getInstance().getConfig().getBoolean("Bosses.bossesGetGlowingPotionEffect")) {
            entity.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, Integer.MAX_VALUE, 1));
        }
        return null;
    }

    /**
     * This is what will happen once the Creeper boss is lit on fire.
     * it is supposed to explode after being lit on fire and not take damage from the fire itself.
     *
     * @param event the EntityCombustEvent to check for the boss creeper in.
     */
    @EventHandler
    public void bossLitOnFire(EntityCombustEvent event) {

        if (event.getEntityType().equals(EntityType.CREEPER) && event.getEntity().getScoreboardTags().contains(CreeperBoss.SCOREBOARDTAG)) {

            int fuseTime = instance.config.getInt("thrownTNT.TNTFuse");

            event.setCancelled(true);

            Creeper creeper = (Creeper) event.getEntity();

            if (!(creeper.getScoreboardTags().contains(EXPLODINGTAG))) {
                creeper.ignite();
                creeper.addScoreboardTag(EXPLODINGTAG);
            }
            Bukkit.getScheduler().scheduleSyncDelayedTask(Vanillabosses.getInstance(), () -> {

                creeper.removeScoreboardTag(EXPLODINGTAG);

            }, 20L * fuseTime + 5);
        }
    }

    @EventHandler
    public void onEntityExplode(org.bukkit.event.entity.EntityExplodeEvent event) {
        //BossCreeper

        if (event.getEntity().getScoreboardTags().contains(SCOREBOARDTAG) && event.getEntityType() == EntityType.CREEPER) {

            //get the entity from the event after verifying that it is a boss creeper above
            Creeper creeper = (Creeper) event.getEntity();

            //Cancel the event so nothing actually explodes. have to replace the sound
            event.setCancelled(true);
            event.getEntity().getWorld().playSound(event.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1F, 1F);

            //If the creeper is almost or pretty much dead, let it die
            if (creeper.getHealth() < 0.0001) {
                return;
            }

            double health = creeper.getHealth();
            double maxHealth = creeper.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();

            //new boolean to check whether the respawning boss map contains this specific boss and the boss has the respawning boss scoreboard tag
            boolean isRespawningBoss = RespawningBoss.livingRespawningBossesMap.entrySet()
                    .stream()
                    .anyMatch(n -> n.getValue() == creeper.getUniqueId()
                            &&
                            creeper.getScoreboardTags().contains(RespawningBoss.RESPAWNING_BOSS_TAG));

            LivingEntity creeperNew;

            if (isRespawningBoss) {

                //make a new Array list in order to save all bosses which were returned by the stream
                ArrayList<RespawningBoss> respawningBosses = (ArrayList<RespawningBoss>) RespawningBoss.livingRespawningBossesMap.entrySet()
                        .stream()
                        .filter(n -> n.getValue() == creeper.getUniqueId())
                        .map(Map.Entry::getKey).collect(Collectors.toList());

                //check whether there was only one value in there. log if there was 0 or more than one, return.
                if (respawningBosses.size() != 1) {
                    new VBLogger(getClass().getName(), Level.WARNING, "More than one Respawning Boss mapped with the same entity or irregularly none matched. List: " + respawningBosses).logToFile();
                    return;
                }

                //get the respawning boss corresponding to the UUID of the creeper from the event
                RespawningBoss respawningBoss = RespawningBoss.livingRespawningBossesMap.entrySet()
                        .stream()
                        .filter(n -> n.getValue() == creeper.getUniqueId())
                        .map(Map.Entry::getKey)
                        .collect(Collectors.toList()).get(0);

                //remake the creeper boss which just blew up
                try {
                    creeperNew = respawningBoss.spawnBoss();
                } catch (BossCreationException e) {
                    new VBLogger(getClass().getName(), Level.WARNING, "Could not respawn Creeper boss. If you cannot fix this using the config, please let the author know.").logToFile();
                    return;
                }

                //check whether the new entity is actually a creeper, supposed to catch some coding and logic errors on my end
                if (!(creeperNew instanceof Creeper)) {
                    new VBLogger(getClass().getName(), Level.WARNING, "Unexpected Respawning boss type inside Respawning Boss list. Expected: Creeper. Found: " + respawningBoss.getType()).logToFile();
                    return;
                }

            } else {

                //else, the boss is not a respawning boss. gotta have some stuff for that here

                //Attempt to spawn a new Creeper to replace the old one.
                try {
                    creeperNew = new NormalBoss(EntityType.CREEPER).spawnBoss(creeper.getLocation());
                } catch (BossCreationException e) {
                    new VBLogger(getClass().getName(), Level.WARNING, "Unable to respawn Creeper for some reason. Exception: " + e).logToFile();
                    return;
                }

                Utility.spawnParticles(Particle.FLAME, event.getEntity().getWorld(), event.getLocation(), 4, 2, 4, 30, 3);

                creeperNew.addScoreboardTag("ExplodingATM");
                Creeper finalCreeper1 = (Creeper) creeperNew;
                Bukkit.getScheduler().scheduleSyncDelayedTask(Vanillabosses.getInstance(), () -> {
                    finalCreeper1.removeScoreboardTag("ExplodingATM");
                }, 20L * instance.config.getInt("thrownTNT.TNTFuse"));

                //always have the same explosion radius
            }

            try {
                creeperNew.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(this.config.getDouble("health"));
                creeperNew.setHealth(health);
            } catch (IllegalArgumentException e) {
                new VBLogger(getClass().getName(), Level.WARNING, "Could not properly set Creeper health after exploding. \n" +
                        "This is most likely caused by another plugin setting the hp of the boss or An old creeper exploding when you changed its settings").logToFile();
            }

            ((Creeper) creeperNew).setExplosionRadius(creeper.getExplosionRadius());

            VBBossBar.replaceAssignedEntity(creeper.getUniqueId(), creeperNew.getUniqueId());
            creeperNew.setRemoveWhenFarAway(creeper.getRemoveWhenFarAway());

            BossCommand.replaceMappedUUIDs(creeper.getUniqueId(), creeperNew.getUniqueId());

            if (instance.config.getBoolean("thrownTNT.throwTNTEnable")) {

                Entity[] tntArray = {
                        creeperNew.getWorld().spawnEntity(creeperNew.getLocation(), EntityType.PRIMED_TNT),
                        creeperNew.getWorld().spawnEntity(creeperNew.getLocation(), EntityType.PRIMED_TNT),
                        creeperNew.getWorld().spawnEntity(creeperNew.getLocation(), EntityType.PRIMED_TNT),
                        creeperNew.getWorld().spawnEntity(creeperNew.getLocation(), EntityType.PRIMED_TNT),
                        creeperNew.getWorld().spawnEntity(creeperNew.getLocation(), EntityType.PRIMED_TNT),
                        creeperNew.getWorld().spawnEntity(creeperNew.getLocation(), EntityType.PRIMED_TNT),
                        creeperNew.getWorld().spawnEntity(creeperNew.getLocation(), EntityType.PRIMED_TNT),
                        creeperNew.getWorld().spawnEntity(creeperNew.getLocation(), EntityType.PRIMED_TNT),
                };

                for (Entity e : tntArray
                ) {
                    ((TNTPrimed) e).setYield(instance.config.getInt("thrownTNT.TNTYield"));
                    ((TNTPrimed) e).setFuseTicks(20 * instance.config.getInt("thrownTNT.TNTFuse"));
                }

                double multiplier = instance.config.getDouble("thrownTNT.TNTSpreadMultiplier");

                tntArray[0].setVelocity(new Vector(0.25 * multiplier, 0.5, 0));
                tntArray[1].setVelocity(new Vector(-0.25 * multiplier, 0.5, 0));
                tntArray[2].setVelocity(new Vector(0, 0.5, 0.25 * multiplier));
                tntArray[3].setVelocity(new Vector(0, 0.5, -0.25 * multiplier));
                tntArray[4].setVelocity(new Vector(0.25 * multiplier, 0.5, 0.25 * multiplier));
                tntArray[5].setVelocity(new Vector(-0.25 * multiplier, 0.5, 0.25 * multiplier));
                tntArray[6].setVelocity(new Vector(0.25 * multiplier, 0.5, -0.25 * multiplier));
                tntArray[7].setVelocity(new Vector(-0.25 * multiplier, 0.5, -0.25 * multiplier));

                if (instance.config.getBoolean("thrownTNT.TNTDoesNoBlockDamage")) {
                    for (Entity e : tntArray) {
                        e.addScoreboardTag(CANCEL_EXPLOSION);
                        e.getScoreboardTags().add(CANCEL_BLOWUP_ITEMS);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onTNTExplode(EntityExplodeEvent event) {
        //cancel explosions with the CANCEL_EXPLOSION tag
        if (event.getEntity().getScoreboardTags().contains(CANCEL_EXPLOSION)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onTNTBlowUpItem(EntityDamageByEntityEvent event) {
        //cancel blowing up items
        if (event.getDamager().getScoreboardTags().contains(CANCEL_BLOWUP_ITEMS) && event.getEntity().getType() == EntityType.DROPPED_ITEM) {
            event.setCancelled(true);
        }
        //cancel boss creepers taking damage from explosions
        if (event.getCause() == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION && event.getEntity().getScoreboardTags().contains(SCOREBOARDTAG)) {
            event.setCancelled(true);
        }
    }
}