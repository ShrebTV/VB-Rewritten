package me.shreb.vanillabosses.bosses;

import me.shreb.vanillabosses.Vanillabosses;
import me.shreb.vanillabosses.bosses.bossRepresentation.NormalBoss;
import me.shreb.vanillabosses.bosses.bossRepresentation.RespawningBoss;
import me.shreb.vanillabosses.bosses.utility.BossCommand;
import me.shreb.vanillabosses.bosses.utility.BossCreationException;
import me.shreb.vanillabosses.bosses.utility.VBBossBar;
import me.shreb.vanillabosses.logging.VBLogger;
import me.shreb.vanillabosses.utility.ConfigVerification;
import me.shreb.vanillabosses.utility.Utility;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.Configuration;
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

public class CreeperBoss extends VBBoss implements ConfigVerification {

    public static CreeperBoss instance = new CreeperBoss();

    public static final String CONFIGSECTION = "CreeperBoss";
    public static final String SCOREBOARDTAG = "BossCreeper";

    public static final String EXPLODINGTAG = "ExplodingATM";
    public static final String CANCEL_EXPLOSION = "CancelOnExplode";
    public static final String CANCEL_BLOWUP_ITEMS = "dontBlowUpItems";

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

        if (!config.getBoolean("Bosses." + CONFIGSECTION + ".enabled")) return entity;

        // checking whether the entity passed in is a Creeper. Logging as a warning and throwing an exception if not.
        if (!(entity instanceof Creeper)) {
            new VBLogger(getClass().getName(), Level.WARNING, "Attempted to make a Creeper boss out of an Entity.\n" +
                    "Entity passed in: " + entity.getType() + "\n" +
                    "Boss could not be created!").logToFile();

            throw new BossCreationException("Attempted to make a boss out of an Entity. Could not make Creeper Boss out of this Entity.");
        }

        //getting the Boss Attributes from the config file
        double health = config.getDouble("Bosses." + CONFIGSECTION + ".health");
        String nameColorString = config.getString("Bosses." + CONFIGSECTION + ".displayNameColor");

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

        String name = config.getString("Bosses." + CONFIGSECTION + ".displayName");

        double speedMultiplier = config.getDouble("Bosses." + CONFIGSECTION + ".SpeedModifier");
        if (speedMultiplier < 0.0001) speedMultiplier = 1;
        entity.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(speedMultiplier * entity.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).getBaseValue());

        //setting the entity Attributes. Logging failure as Warning.
        try {
            entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(health);
            entity.setHealth(health);
            entity.setCustomName(nameColor + name);
            entity.setCustomNameVisible(config.getBoolean("Bosses." + CONFIGSECTION + ".showDisplayNameAlways"));

        } catch (Exception e) {
            new VBLogger(getClass().getName(), Level.WARNING, "Could not set Attributes on Creeper Boss\n" +
                    "Reason: " + e).logToFile();
        }

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

            int fuseTime = Vanillabosses.getInstance().getConfig().getInt("Bosses.CreeperBoss.thrownTNT.TNTFuse");

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

            Configuration config = Vanillabosses.getInstance().getConfig();

            //get the entity from the event after verifying that it is a boss creeper above
            Creeper creeper = (Creeper) event.getEntity();

            //Cancel the event so nothing actually explodes. have to replace the sound
            event.setCancelled(true);
            event.getEntity().getWorld().playSound(event.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1F, 1F);

            //If the creeper is almost or pretty much dead, let it die
            if (creeper.getHealth() < 0.0001) {
                return;
            }

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

                creeperNew.setHealth(creeper.getHealth());
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

                creeperNew.setHealth(creeper.getHealth());

                creeperNew.addScoreboardTag("ExplodingATM");
                Creeper finalCreeper1 = (Creeper) creeperNew;
                Bukkit.getScheduler().scheduleSyncDelayedTask(Vanillabosses.getInstance(), () -> {
                    finalCreeper1.removeScoreboardTag("ExplodingATM");
                }, 20L * config.getInt("Bosses.CreeperBoss.thrownTNT.TNTFuse"));

                //always have the same explosion radius
            }

            ((Creeper) creeperNew).setExplosionRadius(creeper.getExplosionRadius());

            VBBossBar.replaceAssignedEntity(creeper.getUniqueId(), creeperNew.getUniqueId());

            BossCommand.replaceMappedUUIDs(creeper.getUniqueId(), creeperNew.getUniqueId());

            if (config.getBoolean("Bosses.CreeperBoss.thrownTNT.throwTNTEnable")) {

                Entity[] TNTArry = {
                        creeperNew.getWorld().spawnEntity(creeperNew.getLocation(), EntityType.PRIMED_TNT),
                        creeperNew.getWorld().spawnEntity(creeperNew.getLocation(), EntityType.PRIMED_TNT),
                        creeperNew.getWorld().spawnEntity(creeperNew.getLocation(), EntityType.PRIMED_TNT),
                        creeperNew.getWorld().spawnEntity(creeperNew.getLocation(), EntityType.PRIMED_TNT),
                        creeperNew.getWorld().spawnEntity(creeperNew.getLocation(), EntityType.PRIMED_TNT),
                        creeperNew.getWorld().spawnEntity(creeperNew.getLocation(), EntityType.PRIMED_TNT),
                        creeperNew.getWorld().spawnEntity(creeperNew.getLocation(), EntityType.PRIMED_TNT),
                        creeperNew.getWorld().spawnEntity(creeperNew.getLocation(), EntityType.PRIMED_TNT),
                };

                for (Entity e : TNTArry
                ) {
                    ((TNTPrimed) e).setYield(config.getInt("Bosses.CreeperBoss.thrownTNT.TNTYield"));
                    ((TNTPrimed) e).setFuseTicks(20 * config.getInt("Bosses.CreeperBoss.thrownTNT.TNTFuse"));
                }

                double multiplier = config.getDouble("Bosses.CreeperBoss.thrownTNT.TNTSpreadMultiplier");

                TNTArry[0].setVelocity(new Vector(0.25 * multiplier, 0.5, 0));
                TNTArry[1].setVelocity(new Vector(-0.25 * multiplier, 0.5, 0));
                TNTArry[2].setVelocity(new Vector(0, 0.5, 0.25 * multiplier));
                TNTArry[3].setVelocity(new Vector(0, 0.5, -0.25 * multiplier));
                TNTArry[4].setVelocity(new Vector(0.25 * multiplier, 0.5, 0.25 * multiplier));
                TNTArry[5].setVelocity(new Vector(-0.25 * multiplier, 0.5, 0.25 * multiplier));
                TNTArry[6].setVelocity(new Vector(0.25 * multiplier, 0.5, -0.25 * multiplier));
                TNTArry[7].setVelocity(new Vector(-0.25 * multiplier, 0.5, -0.25 * multiplier));

                if (config.getBoolean("Bosses.CreeperBoss.thrownTNT.TNTDoesNoBlockDamage")) {
                    for (Entity e : TNTArry) {
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