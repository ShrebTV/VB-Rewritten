package me.shreb.vanillabosses.bosses;

import me.shreb.vanillabosses.Vanillabosses;
import me.shreb.vanillabosses.bosses.utility.BossCreationException;
import me.shreb.vanillabosses.logging.VBLogger;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;
import java.util.logging.Level;

public class BlazeBoss extends VBBoss {

    public static BlazeBoss instance = new BlazeBoss();

    public static final String CONFIGSECTION = "BlazeBoss";
    public static final String SCOREBOARDTAG = "BossBlaze";

    Random abilityRandom = new Random();

    @Override
    public LivingEntity makeBoss(Location location) throws BossCreationException {

        LivingEntity entity;

        //Attempting to spawn in a new Entity which is to be edited to a boss. logging failures as warning.
        try {
            entity = (LivingEntity) location.getWorld().spawnEntity(location, EntityType.BLAZE);
        } catch (NullPointerException e) {
            new VBLogger(getClass().getName(), Level.WARNING, "Nullpointer Exception at Blaze Boss. World or location was null.\n" +
                    "Location: " + location.toString()).logToFile();
            new VBLogger(getClass().getName(), Level.WARNING, e.toString());
            throw new BossCreationException("Could not create Blaze Boss.");
        }

        //Attempting to edit the previously made entity into a boss. Logging failure as warning.
        try {
            makeBoss(entity);
        } catch (BossCreationException e) {
            new VBLogger(getClass().getName(), Level.WARNING, "Error creating a Blaze boss!").logToFile();
        }

        return entity;
    }

    @Override
    public LivingEntity makeBoss(LivingEntity entity) throws BossCreationException {

        FileConfiguration config = Vanillabosses.getInstance().getConfig();

        // checking wether the entity passed in is a Blaze. Logging as a warning and throwing an exception if not.
        if (!(entity instanceof Blaze)) {
            new VBLogger(getClass().getName(), Level.WARNING, "Attempted to make a Blaze boss out of an Entity.\n" +
                    "Entity passed in: " + entity.getType() + "\n" +
                    "Boss could not be created!").logToFile();

            throw new BossCreationException("Attempted to make a boss out of an Entity. Could not make Blaze Boss out of this Entity.");
        }

        //getting the Boss Attributes from the config file
        double health = config.getDouble("Bosses." + CONFIGSECTION + ".health");
        String nameColorString = config.getString("Bosses." + CONFIGSECTION + ".displayNameColor");

        ChatColor nameColor;

        //If the String is null or empty set it to a standard String
        if (nameColorString == null || nameColorString.equals("")) {
            new VBLogger(getClass().getName(), Level.WARNING, "Could not get name Color String for Blaze boss! Defaulting to #000000").logToFile();
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
            new VBLogger(getClass().getName(), Level.WARNING, "Could not set Attributes on Blaze Boss\n" +
                    "Reason: " + e).logToFile();
        }

        // Setting scoreboard tags so the boss can be recognised.
        entity.getScoreboardTags().add(SCOREBOARDTAG);
        entity.getScoreboardTags().add(VBBoss.BOSSTAG);

        //Putting glowing effect on bosses if config is set to do so.
        if (Vanillabosses.getInstance().getConfig().getBoolean("Bosses.bossesGetGlowingPotionEffect")) {
            entity.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, Integer.MAX_VALUE, 1));
        }

        return null;
    }


    public static HashMap<Integer, UUID> bossBlazeTargetMap = new HashMap<>();
    /**
     * The ability of the blaze boss to transform its projectiles into different projectiles.
     * @param event the ProjectileLaunchEvent to check for a boss blaze in
     */
    public void blazeAbility(ProjectileLaunchEvent event) {

        FileConfiguration config = Vanillabosses.getInstance().getConfig();

        if (!checkForBlazeBoss(event.getEntity())) return;

        Vector v = null;
        Entity projectile = event.getEntity();
        World w = event.getEntity().getWorld();

        if (bossBlazeTargetMap.containsKey(((Entity) event.getEntity().getShooter()).getEntityId())) {

            v = Objects.requireNonNull(Vanillabosses.getInstance().getServer()
                    .getPlayer(bossBlazeTargetMap.get(((Entity) event.getEntity().getShooter()).getEntityId())))
                    .getLocation().subtract(event.getLocation()).toVector();

            v.divide(new Vector(8, 15, 8));
        }

        double random = abilityRandom.nextDouble();

        double chanceWither = config.getDouble("Bosses.BlazeBoss.blazeShootEventsChances.witherSkull");
        double chanceEnder = config.getDouble("Bosses.BlazeBoss.blazeShootEventsChances.enderDragonFireBall");
        double chanceLarge = config.getDouble("Bosses.BlazeBoss.blazeShootEventsChances.largeFireBall");

        double currentChance = chanceWither;
        if(currentChance < random){

            WitherSkull entity = (WitherSkull) w.spawnEntity(event.getEntity().getLocation(), EntityType.WITHER_SKULL);

            entity.setVelocity(Objects.requireNonNullElseGet(v, projectile::getVelocity));
            event.getEntity().remove();

            Vector finalV = v;
            for(int i=0; i<6; i++){
                Bukkit.getScheduler().scheduleSyncDelayedTask(Vanillabosses.getInstance(), () -> {
                    entity.setVelocity(Objects.requireNonNullElseGet(finalV, projectile::getVelocity));
                }, 15 * i);
            }

        } else if((currentChance += chanceEnder) < random){

            DragonFireball entity = (DragonFireball) w.spawnEntity(event.getEntity().getLocation(), EntityType.DRAGON_FIREBALL);

            entity.setVelocity(Objects.requireNonNullElseGet(v, projectile::getVelocity));
            event.getEntity().remove();

            Vector finalV = v;
            for(int i=0; i<6; i++){
                Bukkit.getScheduler().scheduleSyncDelayedTask(Vanillabosses.getInstance(), () -> {
                    entity.setVelocity(Objects.requireNonNullElseGet(finalV, projectile::getVelocity));
                }, 15 * i);
            }

        } else if((currentChance + chanceLarge) < random){

            Fireball entity = (Fireball) w.spawnEntity(event.getEntity().getLocation(), EntityType.FIREBALL);

            entity.setVelocity(Objects.requireNonNullElseGet(v, projectile::getVelocity));
            event.getEntity().remove();

            Vector finalV = v;

            for(int i=0; i<6; i++){
                Bukkit.getScheduler().scheduleSyncDelayedTask(Vanillabosses.getInstance(), () -> {
                    entity.setVelocity(Objects.requireNonNullElseGet(finalV, projectile::getVelocity));
                }, 15 * i);
            }
        }
    }

    /**
     * This is how the projectiles from the blazeAbility() method get their vector. It did not work any other way sadly.
     * @param event The EntityTargetLivingEntityEvent to check for a blaze boss targeting a player in
     */
    public void blazeTargeting(EntityTargetLivingEntityEvent event) {

        if (event.getEntityType() != EntityType.BLAZE || !(event.getEntity().getScoreboardTags().contains(BlazeBoss.SCOREBOARDTAG)))
            return;
        if (!(event.getTarget() instanceof Player)) return;

        bossBlazeTargetMap.put(event.getEntity().getEntityId(), event.getTarget().getUniqueId());

    }

    private boolean checkForBlazeBoss(Entity entity) {
        return entity instanceof Blaze
                && entity.getScoreboardTags().contains(BlazeBoss.SCOREBOARDTAG)
                && entity.getScoreboardTags().contains(VBBoss.BOSSTAG);
    }
}

