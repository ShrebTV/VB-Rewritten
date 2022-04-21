package me.shreb.vanillabosses.bosses;

import me.shreb.vanillabosses.Vanillabosses;
import me.shreb.vanillabosses.bosses.utility.BossCreationException;
import me.shreb.vanillabosses.logging.VBLogger;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.entity.Witch;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.Random;
import java.util.logging.Level;

public class WitchBoss extends VBBoss {

    public static WitchBoss instance = new WitchBoss();

    public static final String CONFIGSECTION = "WitchBoss";
    public static final String SCOREBOARDTAG = "BossWitch";

    @Override
    public LivingEntity makeBoss(Location location) throws BossCreationException {

        LivingEntity entity;

        //Attempting to spawn in a new Entity which is to be edited to a boss. logging failures as warning.
        try {
            entity = (LivingEntity) location.getWorld().spawnEntity(location, EntityType.WITCH);
        } catch (NullPointerException e) {
            new VBLogger(getClass().getName(), Level.WARNING, "Nullpointer Exception at Witch Boss. World or location was null.\n" +
                    "Location: " + location.toString()).logToFile();
            new VBLogger(getClass().getName(), Level.WARNING, e.toString());
            throw new BossCreationException("Could not create Witch Boss.");
        }

        //Attempting to edit the previously made entity into a boss. Logging failure as warning.
        try {
            makeBoss(entity);
        } catch (BossCreationException e) {
            new VBLogger(getClass().getName(), Level.WARNING, "Error creating a Witch boss!").logToFile();
        }

        return entity;
    }

    @Override
    public LivingEntity makeBoss(LivingEntity entity) throws BossCreationException {

        FileConfiguration config = Vanillabosses.getInstance().getConfig();

        // checking wether the entity passed in is a Wither. Logging as a warning and throwing an exception if not.
        if (!(entity instanceof Witch)) {
            new VBLogger(getClass().getName(), Level.WARNING, "Attempted to make a Witch boss out of an Entity.\n" +
                    "Entity passed in: " + entity.getType() + "\n" +
                    "Boss could not be created!").logToFile();

            throw new BossCreationException("Attempted to make a boss out of an Entity. Could not make Witch Boss out of this Entity.");
        }

        //getting the Boss Attributes from the config file
        double health = config.getDouble("Bosses." + CONFIGSECTION + ".health");
        String nameColorString = config.getString("Bosses." + CONFIGSECTION + ".displayNameColor");

        ChatColor nameColor;

        //If the String is null or empty set it to a standard String
        if (nameColorString == null || nameColorString.equals("")) {
            new VBLogger(getClass().getName(), Level.WARNING, "Could not get name Color String for Witch boss! Defaulting to #000000").logToFile();
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
            new VBLogger(getClass().getName(), Level.WARNING, "Could not set Attributes on Witch Boss\n" +
                    "Reason: " + e).logToFile();
        }

        // Setting scoreboard tag so the boss can be recognised.
        entity.getScoreboardTags().add(SCOREBOARDTAG);
        entity.getScoreboardTags().add(VBBoss.BOSSTAG);

        //Putting glowing effect on bosses if config is set to do so.
        if (Vanillabosses.getInstance().getConfig().getBoolean("Bosses.bossesGetGlowingPotionEffect")) {
            entity.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, Integer.MAX_VALUE, 1));
        }

        return null;
    }

    String path = "Bosses.WitchBoss.customThrownPotions";
    FileConfiguration config = Vanillabosses.getInstance().getConfig();

    /**
     * A method to make a custom potion for the witch to throw and drop
     * @return the potion of the type specified in the method name
     */
    public ItemStack makeDamagePot() {
        ItemStack damagePot = new ItemStack(Material.SPLASH_POTION);
        PotionMeta meta = (PotionMeta) damagePot.getItemMeta();
        meta.addCustomEffect(new PotionEffect(PotionEffectType.HARM, 1, config.getInt(path + "Harm.amplifier") - 1), true);
        meta.setDisplayName(org.bukkit.ChatColor.RED + "Potion of Harming");
        damagePot.setItemMeta(meta);
        return damagePot;
    }

    /**
     * A method to make a custom potion for the witch to throw and drop
     * @return the potion of the type specified in the method name
     */
    public ItemStack makePoisonPot() {
        ItemStack poisonPot = new ItemStack(Material.SPLASH_POTION);
        PotionMeta meta = (PotionMeta) poisonPot.getItemMeta();
        meta.addCustomEffect(new PotionEffect(PotionEffectType.POISON, 20 * config.getInt(path + "Poison.duration"), config.getInt(path + "Poison.amplifier") - 1), true);
        meta.setDisplayName(org.bukkit.ChatColor.GREEN + "Potion of Poison");
        poisonPot.setItemMeta(meta);
        return poisonPot;
    }

    /**
     * A method to make a custom potion for the witch to throw and drop
     * @return the potion of the type specified in the method name
     */
    public ItemStack makeBlindnessPot() {
        ItemStack blindnessPot = new ItemStack(Material.SPLASH_POTION);
        PotionMeta meta = (PotionMeta) blindnessPot.getItemMeta();
        meta.addCustomEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20 * config.getInt(path + "Blindness.duration"), config.getInt(path + "Blindness.amplifier") - 1), true);
        meta.setDisplayName(org.bukkit.ChatColor.BLACK + "Potion of Blindness");
        blindnessPot.setItemMeta(meta);
        return blindnessPot;
    }

    /**
     * A method to make a custom potion for the witch to throw and drop
     * @return the potion of the type specified in the method name
     */
    public ItemStack makeWitherPot() {
        ItemStack poisonPot = new ItemStack(Material.SPLASH_POTION);
        PotionMeta meta = (PotionMeta) poisonPot.getItemMeta();
        meta.addCustomEffect(new PotionEffect(PotionEffectType.WITHER, 20 * config.getInt(path + "Wither.duration"), config.getInt(path + "Wither.amplifier") - 1), true);
        meta.setDisplayName(org.bukkit.ChatColor.GRAY + "Potion of Withering");
        poisonPot.setItemMeta(meta);
        return poisonPot;
    }

    /**
     * A method to make a custom potion for the witch to throw and drop
     * @return the potion of the type specified in the method name
     */
    public ItemStack makeHungerPot() {
        ItemStack hungerPot = new ItemStack(Material.SPLASH_POTION);
        PotionMeta meta = (PotionMeta) hungerPot.getItemMeta();
        meta.addCustomEffect(new PotionEffect(PotionEffectType.HUNGER, 20 * config.getInt(path + "Hunger.duration"), config.getInt(path + "Hunger.amplifier") - 1), true);
        meta.setDisplayName(org.bukkit.ChatColor.DARK_GREEN + "Potion of Hunger");
        hungerPot.setItemMeta(meta);
        return hungerPot;
    }

    /**
     * This is what happens when a Witch boss throws a potion.
     * The potion will be replaced by the custom potions added by this plugin.
     * @param event the event to check for a boss witch in and to change the thrown potion in.
     */
    public void onPotionThrow(ProjectileLaunchEvent event) {

        double harmChance = config.getDouble(path + "Harm.chance");
        double poisonChance = config.getDouble(path + "Poison.chance");
        double blindChance = config.getDouble(path + "Blindness.chance");
        double witherChance = config.getDouble(path + "Wither.chance");
        double hungerChance = config.getDouble(path + "Hunger.chance");

        //Don't want non living entities
        if(!(event.getEntity().getShooter() instanceof LivingEntity)) return;

        //Don't want entities which don't have the witch boss scoreboard tag
        if(!event.getEntity().getScoreboardTags().contains(SCOREBOARDTAG)) return;

        //Don't want any living entity which isn't a witch
        if(!((LivingEntity) event.getEntity().getShooter()).getType().equals(EntityType.WITCH)) return;

        //Don't want a projectile which isn't a splash potion
        if(!event.getEntity().getType().equals(EntityType.SPLASH_POTION)) return;

        Vector v = event.getEntity().getVelocity();
        Location loc = event.getLocation();

        double random = new Random().nextDouble();
        double currentChance = harmChance;

        if (random < currentChance) {

            event.getEntity().remove();

            ThrownPotion e = (ThrownPotion) event.getEntity().getWorld().spawnEntity(loc, EntityType.SPLASH_POTION);

            e.setItem(makeDamagePot());
            e.setVelocity(v);

            return;
        }

        currentChance += poisonChance;
        if (random < currentChance) {
            event.getEntity().remove();

            ThrownPotion e = (ThrownPotion) event.getEntity().getWorld().spawnEntity(loc, EntityType.SPLASH_POTION);

            e.setItem(makePoisonPot());
            e.setVelocity(v);

            return;
        }

        currentChance += blindChance;
        if (random < currentChance) {
            event.getEntity().remove();

            ThrownPotion e = (ThrownPotion) event.getEntity().getWorld().spawnEntity(loc, EntityType.SPLASH_POTION);

            e.setItem(makeBlindnessPot());
            e.setVelocity(v);

            return;
        }

        currentChance += witherChance;
        if (random < currentChance) {
            event.getEntity().remove();

            ThrownPotion e = (ThrownPotion) event.getEntity().getWorld().spawnEntity(loc, EntityType.SPLASH_POTION);

            e.setItem(makeWitherPot());
            e.setVelocity(v);

            return;
        }

        currentChance += hungerChance;
        if (random < currentChance) {
            event.getEntity().remove();

            ThrownPotion e = (ThrownPotion) event.getEntity().getWorld().spawnEntity(loc, EntityType.SPLASH_POTION);

            e.setItem(makeHungerPot());
            e.setVelocity(v);
        }
    }

}
