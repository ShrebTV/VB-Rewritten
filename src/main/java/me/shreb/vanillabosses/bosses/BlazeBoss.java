package me.shreb.vanillabosses.bosses;

import me.shreb.vanillabosses.Vanillabosses;
import me.shreb.vanillabosses.bosses.utility.BossCreationException;
import me.shreb.vanillabosses.logging.VBLogger;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Zombie;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.logging.Level;

public class BlazeBoss extends VBBoss {

    public static final String CONFIGSECTION = "ZombieBoss";
    public static final String SCOREBOARDTAG = "BossZombie";

    @Override
    public LivingEntity makeBoss(Location location) throws BossCreationException {

        LivingEntity entity;
        try {
            entity = (LivingEntity) location.getWorld().spawnEntity(location, EntityType.ZOMBIE);
        } catch (NullPointerException e) {
            new VBLogger(getClass().getName(), Level.WARNING, "Nullpointer Exception at Zombie Boss. World or location was null.\n" +
                    "Location: " + location.toString()).logToFile();
            new VBLogger(getClass().getName(), Level.WARNING, e.toString());
            throw new BossCreationException("Could not create Zombie Boss.");
        }

        try {
            makeBoss(entity);
        } catch (BossCreationException e) {
            new VBLogger(getClass().getName(), Level.WARNING, "Error creating a Zombie boss!").logToFile();
        }

        return entity;
    }

    @Override
    public LivingEntity makeBoss(LivingEntity entity) throws BossCreationException {

        FileConfiguration config = Vanillabosses.getInstance().getConfig();

        if (!(entity instanceof Zombie)) {
            new VBLogger(getClass().getName(), Level.WARNING, "Attempted to make a Zombie boss out of an Entity.\n" +
                    "Entity passed in: " + entity.getType() + "\n" +
                    "Boss could not be created!").logToFile();

            throw new BossCreationException("Attempted to make a boss out of an Entity. Could not make Zombie Boss out of this Entity.");
        }

        //getting the Boss Attributes from the config file
        double health = config.getDouble("Bosses.ZombieBoss.health");
        String nameColorString = config.getString("Bosses.ZombieBoss.displayNameColor");

        ChatColor nameColor = null;

        //If the String is null or empty set it to a standard String
        if (nameColorString == null || nameColorString.equals("")) {
            new VBLogger(getClass().getName(), Level.WARNING, "Could not get name Color String for Zombie boss! Defaulting to #000000").logToFile();
        } else {
            try {
                nameColor = ChatColor.of(nameColorString);
            } catch (IllegalArgumentException e) {
                nameColor = ChatColor.of("#000000");
            }
        }

        String name = config.getString("Bosses.ZombieBoss.displayName");

        //setting the Attributes. Logging to file if it fails at any point.
        try {
            entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(health);
            entity.setHealth(health);
            entity.setCustomName(nameColor + name);
            entity.setCustomNameVisible(config.getBoolean("Bosses.ZombieBoss.showDisplayNameAlways"));

        } catch (Exception e) {
            new VBLogger(getClass().getName(), Level.WARNING, "Could not set Attributes on Zombie Boss\n" +
                    "Reason: " + e).logToFile();
        }

        // Setting scoreboard tag so the boss can be recognised.
        entity.getScoreboardTags().add(SCOREBOARDTAG);

        if (!putOnEquipment((Zombie) entity)) {
            throw new BossCreationException("Could not put Armor on Zombie boss");
        }

        if (Vanillabosses.getInstance().getConfig().getBoolean("Bosses.bossesGetGlowingPotionEffect")) {
            entity.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, Integer.MAX_VALUE, 1));
        }

        return null;
    }
}

