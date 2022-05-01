package me.shreb.vanillabosses.bosses;

import me.shreb.vanillabosses.Vanillabosses;
import me.shreb.vanillabosses.bosses.bossRepresentation.NormalBoss;
import me.shreb.vanillabosses.bosses.utility.BossCreationException;
import me.shreb.vanillabosses.logging.VBLogger;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Spider;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Level;

public class SpiderBoss extends VBBoss {

    public static SpiderBoss instance = new SpiderBoss();

    public static final String CONFIGSECTION = "SpiderBoss";
    public static final String SCOREBOARDTAG = "BossSpider";

    @Override
    public LivingEntity makeBoss(Location location) throws BossCreationException {

        LivingEntity entity;

        //Attempting to spawn in a new Entity which is to be edited to a boss. logging failures as warning.
        try {
            entity = (LivingEntity) location.getWorld().spawnEntity(location, EntityType.SPIDER);
        } catch (NullPointerException e) {
            new VBLogger(getClass().getName(), Level.WARNING, "Nullpointer Exception at Spider Boss. World or location was null.\n" +
                    "Location: " + location.toString()).logToFile();
            new VBLogger(getClass().getName(), Level.WARNING, e.toString());
            throw new BossCreationException("Could not create Spider Boss.");
        }

        //Attempting to edit the previously made entity into a boss. Logging failure as warning.
        try {
            makeBoss(entity);
        } catch (BossCreationException e) {
            new VBLogger(getClass().getName(), Level.WARNING, "Error creating a Spider boss!").logToFile();
        }

        return entity;
    }

    @Override
    public LivingEntity makeBoss(LivingEntity entity) throws BossCreationException {

        FileConfiguration config = Vanillabosses.getInstance().getConfig();

        // checking wether the entity passed in is a Spider. Logging as a warning and throwing an exception if not.
        if (!(entity instanceof Spider)) {
            new VBLogger(getClass().getName(), Level.WARNING, "Attempted to make a Spider boss out of an Entity.\n" +
                    "Entity passed in: " + entity.getType() + "\n" +
                    "Boss could not be created!").logToFile();

            throw new BossCreationException("Attempted to make a boss out of an Entity. Could not make Spider Boss out of this Entity.");
        }

        //getting the Boss Attributes from the config file
        double health = config.getDouble("Bosses." + CONFIGSECTION + ".health");
        String nameColorString = config.getString("Bosses." + CONFIGSECTION + ".displayNameColor");

        ChatColor nameColor;

        //If the String is null or empty set it to a standard String
        if (nameColorString == null || nameColorString.equals("")) {
            new VBLogger(getClass().getName(), Level.WARNING, "Could not get name Color String for Spider boss! Defaulting to #000000").logToFile();
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
            new VBLogger(getClass().getName(), Level.WARNING, "Could not set Attributes on Spider Boss\n" +
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
    public void onHitAbility(EntityDamageByEntityEvent event){

        FileConfiguration config = Vanillabosses.getInstance().getConfig();

        if (event.getEntity().getScoreboardTags().contains("BossSpider") && event.getEntityType() == EntityType.SPIDER) {

            if (!(event.getDamager() instanceof Player)) return;

            Spider spider = (Spider) event.getEntity();

            Location originalLoc = spider.getLocation();
            Location tempLoc = event.getDamager().getLocation();

            double chanceInvisibility;
            double chanceLeap;

            double rn = new Random().nextDouble();

            chanceInvisibility = config.getInt("Bosses.SpiderBoss.onHitEvents.invisibility.chance");
            chanceLeap = config.getInt("Bosses.SpiderBoss.onHitEvents.leap.chance");

            int currentChance = 0;

            currentChance += chanceInvisibility;

            if (rn <= currentChance) {

                long duration = 20L * config.getInt("Bosses.SpiderBoss.onHitEvents.invisibility.duration");

                ArrayList<PotionEffect> effects = (ArrayList<PotionEffect>) spider.getActivePotionEffects();

                if (config.getBoolean("Bosses.SpiderBoss.onHitEvents.invisibility.teleportToPlayer")) {

                    if (spider.getScoreboardTags().contains("isInvis")) return;
                    spider.addScoreboardTag("isInvis");

                    ArrayList<PotionEffect> tempEffects = new ArrayList<>();
                    tempEffects.add(new PotionEffect(PotionEffectType.ABSORPTION, Integer.MAX_VALUE, 5));
                    tempEffects.add(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 3));
                    spider.addPotionEffects(tempEffects);

                    spider.teleport(tempLoc);

                    if (config.getBoolean("Bosses.SpiderBoss.onHitEvents.invisibility.teleportBack")) {
                        Bukkit.getScheduler().scheduleSyncDelayedTask(Vanillabosses.getInstance(), () -> spider.teleport(originalLoc), 20L * duration);
                    }
                }

                spider.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, (int) duration, 1, false));

                Bukkit.getScheduler().scheduleSyncDelayedTask(Vanillabosses.getInstance(), () -> {
                    spider.getScoreboardTags().remove("isInvis");

                    for (PotionEffect p : spider.getActivePotionEffects()
                    ) {
                        spider.removePotionEffect(p.getType());
                    }
                    spider.addPotionEffects(effects);


                }, 20L * config.getInt("Bosses.SpiderBoss.onHitEvents.invisibility.duration"));

                return;
            }

            currentChance += chanceLeap;

            if (rn <= currentChance) {

                if (spider.getScoreboardTags().contains("preparingToJump")) return;

                spider.addScoreboardTag("preparingToJump");

                int delay = config.getInt("Bosses.SpiderBoss.onHitEvents.leap.maxDelayAfterHit");

                if(delay < 0) {

                    return;
                }

                int randomInt = new Random().nextInt(delay + 1);

                Bukkit.getScheduler().scheduleSyncDelayedTask(Vanillabosses.getInstance(), () -> {

                    int playerX = event.getDamager().getLocation().getBlockX();
                    int playerY = event.getDamager().getLocation().getBlockY();
                    int playerZ = event.getDamager().getLocation().getBlockZ();

                    int spiderX = spider.getLocation().getBlockX();
                    int spiderY = spider.getLocation().getBlockY();
                    int spiderZ = spider.getLocation().getBlockZ();

                    Vector v = new Vector((playerX - spiderX) / 2, (playerY - spiderY) / 2, (playerZ - spiderZ) / 2);

                    spider.setVelocity(v);

                    spider.removeScoreboardTag("preparingToJump");

                }, 20L * randomInt);
            }
        }
    }
}
