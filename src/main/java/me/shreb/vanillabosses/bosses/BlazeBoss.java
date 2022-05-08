package me.shreb.vanillabosses.bosses;

import me.shreb.vanillabosses.Vanillabosses;
import me.shreb.vanillabosses.bosses.bossRepresentation.NormalBoss;
import me.shreb.vanillabosses.bosses.utility.BossCreationException;
import me.shreb.vanillabosses.logging.VBLogger;
import me.shreb.vanillabosses.utility.ConfigVerification;
import me.shreb.vanillabosses.utility.configFiles.FileCreator;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.logging.Level;

public class BlazeBoss extends VBBoss implements ConfigVerification {

    public static BlazeBoss instance = new BlazeBoss();

    public static final String CONFIGSECTION = "BlazeBoss";
    public static final String SCOREBOARDTAG = "BossBlaze";
    Random abilityRandom = new Random();

    static {
        FileCreator.createAndLoad(FileCreator.blazeBossPath, instance.configuration);
    }

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

        if (!instance.configuration.getBoolean("enabled")) return entity;

        // checking wether the entity passed in is a Blaze. Logging as a warning and throwing an exception if not.
        if (!(entity instanceof Blaze)) {
            new VBLogger(getClass().getName(), Level.WARNING, "Attempted to make a Blaze boss out of an Entity.\n" +
                    "Entity passed in: " + entity.getType() + "\n" +
                    "Boss could not be created!").logToFile();

            throw new BossCreationException("Attempted to make a boss out of an Entity. Could not make Blaze Boss out of this Entity.");
        }

        //getting the Boss Attributes from the config file
        double health = instance.configuration.getDouble("health");
        String nameColorString = instance.configuration.getString("displayNameColor");

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

        double speedMultiplier = instance.configuration.getDouble("SpeedModifier");
        if (speedMultiplier < 0.0001) speedMultiplier = 1;
        entity.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(speedMultiplier * entity.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).getBaseValue());

        String name = instance.configuration.getString("displayName");

        //setting the entity Attributes. Logging failure as Warning.
        try {
            entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(health);
            entity.setHealth(health);
            entity.setCustomName(nameColor + name);
            entity.setCustomNameVisible(instance.configuration.getBoolean("showDisplayNameAlways"));

        } catch (Exception e) {
            new VBLogger(getClass().getName(), Level.WARNING, "Could not set Attributes on Blaze Boss\n" +
                    "Reason: " + e).logToFile();
        }

        // Setting scoreboard tags so the boss can be recognised.
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


    public static HashMap<Integer, UUID> bossBlazeTargetMap = new HashMap<>();

    /**
     * The ability of the blaze boss to transform its projectiles into different projectiles.
     *
     * @param event the ProjectileLaunchEvent to check for a boss blaze in
     */
    @EventHandler
    public void blazeAbility(ProjectileLaunchEvent event) {

        if (!checkForBlazeBoss(event.getEntity().getShooter())) return;

        System.out.println("isBlazeBoss");

        Vector v = null;
        Entity projectile = event.getEntity();
        World w = event.getEntity().getWorld();

        if (bossBlazeTargetMap.containsKey(((Entity) event.getEntity().getShooter()).getEntityId())) {

            System.out.println("containsKey");

            v = Objects.requireNonNull(Vanillabosses.getInstance().getServer()
                            .getPlayer(bossBlazeTargetMap.get(((Entity) event.getEntity().getShooter()).getEntityId())))
                    .getLocation().subtract(event.getLocation()).toVector();

            v.divide(new Vector(8, 15, 8));
        }

        double random = abilityRandom.nextDouble();

        double chanceWither = instance.configuration.getDouble("Bosses.BlazeBoss.blazeShootEventsChances.witherSkull");
        double chanceEnder = instance.configuration.getDouble("Bosses.BlazeBoss.blazeShootEventsChances.enderDragonFireBall");
        double chanceLarge = instance.configuration.getDouble("Bosses.BlazeBoss.blazeShootEventsChances.largeFireBall");

        double currentChance = chanceWither;
        if (currentChance > random) {

            System.out.println("wither");

            WitherSkull entity = (WitherSkull) w.spawnEntity(event.getEntity().getLocation(), EntityType.WITHER_SKULL);

            entity.setVelocity(Objects.requireNonNullElseGet(v, projectile::getVelocity));
            event.getEntity().remove();
            entity.setShooter(event.getEntity().getShooter());

            Vector finalV = v;
            for (int i = 0; i < 6; i++) {
                Bukkit.getScheduler().scheduleSyncDelayedTask(Vanillabosses.getInstance(), () -> {
                    entity.setVelocity(Objects.requireNonNullElseGet(finalV, projectile::getVelocity));
                }, 15 * i);
            }

        } else if ((currentChance += chanceEnder) > random) {

            System.out.println("dragon");

            DragonFireball entity = (DragonFireball) w.spawnEntity(event.getEntity().getLocation(), EntityType.DRAGON_FIREBALL);

            entity.setVelocity(Objects.requireNonNullElseGet(v, projectile::getVelocity));
            event.getEntity().remove();
            entity.setShooter(event.getEntity().getShooter());

            Vector finalV = v;
            for (int i = 0; i < 6; i++) {
                Bukkit.getScheduler().scheduleSyncDelayedTask(Vanillabosses.getInstance(), () -> {
                    entity.setVelocity(Objects.requireNonNullElseGet(finalV, projectile::getVelocity));
                }, 15 * i);
            }

        } else if ((currentChance + chanceLarge) > random) {

            System.out.println("fireball");

            Fireball entity = (Fireball) w.spawnEntity(event.getEntity().getLocation(), EntityType.FIREBALL);

            entity.setVelocity(Objects.requireNonNullElseGet(v, projectile::getVelocity));
            event.getEntity().remove();
            entity.setShooter(event.getEntity().getShooter());

            Vector finalV = v;

            for (int i = 0; i < 6; i++) {
                Bukkit.getScheduler().scheduleSyncDelayedTask(Vanillabosses.getInstance(), () -> {
                    entity.setVelocity(Objects.requireNonNullElseGet(finalV, projectile::getVelocity));
                }, 15 * i);
            }
        }
    }

    /**
     * This is how the projectiles from the blazeAbility() method get their vector. It did not work any other way sadly.
     *
     * @param event The EntityTargetLivingEntityEvent to check for a blaze boss targeting a player in
     */
    @EventHandler
    public void blazeTargeting(EntityTargetLivingEntityEvent event) {

        if (event.getEntityType() != EntityType.BLAZE || !(event.getEntity().getScoreboardTags().contains(BlazeBoss.SCOREBOARDTAG)))
            return;
        if (!(event.getTarget() instanceof Player)) return;

        bossBlazeTargetMap.put(event.getEntity().getEntityId(), event.getTarget().getUniqueId());

    }

    private boolean checkForBlazeBoss(ProjectileSource entity) {
        return entity instanceof Blaze
                && ((Blaze) entity).getScoreboardTags().contains(BlazeBoss.SCOREBOARDTAG)
                && ((Blaze) entity).getScoreboardTags().contains(VBBoss.BOSSTAG);
    }

    @EventHandler
    public void onHitEvent(EntityDamageByEntityEvent event) {

        boolean useDamageModifier = event.getEntity().getScoreboardTags().contains(SCOREBOARDTAG)
                && event.getEntityType() == EntityType.BLAZE
                && event.getDamager().getType().equals(EntityType.SPECTRAL_ARROW);

        if (useDamageModifier) {
            event.setDamage(event.getDamage() * instance.configuration.getDouble("onHitEvents.spectralArrowDamageMultiplier"));
        }
    }

    @Override
    public boolean verifyConfig() {

        VBLogger logger = new VBLogger("BlazeBoss", Level.WARNING, "");

        if (!verifyBoolean(instance.configuration.getString("enabled"), "BlazeBoss.enabled")) {
            logger.setStringToLog("Blaze Boss: Config Error at enabled, has to be true or false");
            logger.logToFile();
        }

        if (!verifyString(instance.configuration.getString("displayName"), "BlazeBoss.displayName")) {
            logger.setStringToLog("Blaze Boss: Config Error at displayName, cannot be empty");
            logger.logToFile();
        }

        if (!verifyColorCode(instance.configuration.getString("displayNameColor"), "BlazeBoss.displayNameColor")) {
            logger.setStringToLog("Blaze Boss: Config Error at displayNameColor, has to be a hexCode");
            logger.logToFile();
        }

        if (!verifyBoolean(instance.configuration.getString("showDisplayNameAlways"), "BlazeBoss.showDisplayNameAlways")) {
            logger.setStringToLog("Blaze Boss: Config Error at showDisplayNameAlways, has to be true or false");
            logger.logToFile();
        }

        if (!verifyDouble(instance.configuration.getString("DamageModifier"), "BlazeBoss.DamageModifier", 0.001, 100)) {
            logger.setStringToLog("Blaze Boss: Config Warning/Error at DamageModifier, has to be a value above 0.0, recommended not to put to 100, close to it or even above :P. Has to be a number");
            logger.logToFile();
        }

        if (!verifyDouble(instance.configuration.getString("SpeedModifier"), "BlazeBoss.SpeedModifier", 0.001, 100)) {
            logger.setStringToLog("Blaze Boss: Config Warning/Error at SpeedModifier, has to be a value above 0.0, recommended not to put to 100, close to it or even above :P. Has to be a number");
            logger.logToFile();
        }

        if (!verifyInt(instance.configuration.getString("health"), "BlazeBoss.health", 0, Integer.MAX_VALUE)) {
            logger.setStringToLog("Blaze Boss: Config Warning/Error at health, has to be a value above 0, cannot be more than 2147483647, has to be a number");
            logger.logToFile();
        }

        if (!verifyDouble(instance.configuration.getString("spawnChance"), "BlazeBoss.spawnChance", 0.0, 1.0)) {
            logger.setStringToLog("Blaze Boss: Config Warning/Error at spawnChance, has to be a value between 0 and 1, has to be a number");
            logger.logToFile();
        }

        if (!verifyString(instance.configuration.getString("killedMessage"), "BlazeBoss.killedMessage")) {
            logger.setStringToLog("Blaze Boss: Config Error at killedMessage, cannot be empty");
            logger.logToFile();
        }

        if (!verifySpawnWorlds((ArrayList<String>) instance.configuration.getStringList("spawnWorlds"))) {
            logger.setStringToLog("Blaze Boss: Config Error at killedMessage, cannot be empty");
            logger.logToFile();
        }

        if (!verifyDrops(EntityType.BLAZE)) {
            logger.setStringToLog("Blaze Boss: Could not verify Blaze boss drops.");
            logger.logToFile();
        }

        if (!verifyInt(instance.configuration.getString("droppedXP"), "BlazeBoss.droppedXP", 0, Integer.MAX_VALUE)) {
            logger.setStringToLog("Blaze Boss: Config Warning/Error at droppedXP, has to be a value above 0, cannot be more than 2147483647, has to be a number");
            logger.logToFile();
        }

        return true;
    }
}