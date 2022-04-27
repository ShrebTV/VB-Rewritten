package me.shreb.vanillabosses.bosses;

import me.shreb.vanillabosses.Vanillabosses;
import me.shreb.vanillabosses.bosses.bossRepresentation.Boss;
import me.shreb.vanillabosses.bosses.bossRepresentation.NormalBoss;
import me.shreb.vanillabosses.bosses.utility.BossCreationException;
import me.shreb.vanillabosses.logging.VBLogger;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.logging.Level;

public class CreeperBoss extends VBBoss {

    public static CreeperBoss instance = new CreeperBoss();

    public static final String CONFIGSECTION = "CreeperBoss";
    public static final String SCOREBOARDTAG = "BossCreeper";

    public static final String EXPLODINGTAG = "ExplodingATM";

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

        FileConfiguration config = Vanillabosses.getInstance().getConfig();

        // checking wether the entity passed in is a Creeper. Logging as a warning and throwing an exception if not.
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
        entity.getScoreboardTags().add(VBBoss.BOSSTAG);

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
     * @param event the EntityCombustEvent to check for the boss creeper in.
     */
    public void bossLitOnFire(EntityCombustEvent event){

        if(event.getEntityType().equals(EntityType.CREEPER) && event.getEntity().getScoreboardTags().contains(CreeperBoss.SCOREBOARDTAG)){

            int fuseTime = Vanillabosses.getInstance().getConfig().getInt("Bosses.CreeperBoss.thrownTNT.TNTFuse");

            event.setCancelled(true);

            Creeper creeper = (Creeper) event.getEntity();

            if(!(creeper.getScoreboardTags().contains(EXPLODINGTAG))) {
                creeper.ignite();
                creeper.addScoreboardTag(EXPLODINGTAG);
            }
            Bukkit.getScheduler().scheduleSyncDelayedTask(Vanillabosses.getInstance(), () ->{

                creeper.removeScoreboardTag(EXPLODINGTAG);

            },20L * fuseTime +5 );
        }
    }
}
