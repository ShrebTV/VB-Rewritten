package me.shreb.vanillabosses.bosses;

import me.shreb.vanillabosses.Vanillabosses;
import me.shreb.vanillabosses.bosses.bossRepresentation.NormalBoss;
import me.shreb.vanillabosses.bosses.utility.BossCreationException;
import me.shreb.vanillabosses.items.BaseballBat;
import me.shreb.vanillabosses.items.utility.ItemCreationException;
import me.shreb.vanillabosses.listeners.SpawnEvent;
import me.shreb.vanillabosses.logging.VBLogger;
import me.shreb.vanillabosses.utility.ConfigVerification;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.Objects;
import java.util.logging.Level;

public class ZombieBoss extends VBBoss implements ConfigVerification {

    public static ZombieBoss instance = new ZombieBoss();

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
        double health = config.getDouble("Bosses." + CONFIGSECTION + ".health");
        String nameColorString = config.getString("Bosses." + CONFIGSECTION + ".displayNameColor");

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
            entity.setCustomNameVisible(config.getBoolean("Bosses." + CONFIGSECTION + ".showDisplayNameAlways"));

        } catch (Exception e) {
            new VBLogger(getClass().getName(), Level.WARNING, "Could not set Attributes on Zombie Boss\n" +
                    "Reason: " + e).logToFile();
        }

        // Setting scoreboard tag so the boss can be recognised.
        entity.getScoreboardTags().add(SCOREBOARDTAG);
        entity.getScoreboardTags().add(BOSSTAG);
        entity.getScoreboardTags().add(REMOVE_ON_DISABLE_TAG);

        new NormalBoss(entity.getType()).putCommandsToPDC(entity);

        if (!putOnEquipment((Zombie) entity)) {
            throw new BossCreationException("Could not put Armor on Zombie boss");
        }

        if (Vanillabosses.getInstance().getConfig().getBoolean("Bosses.bossesGetGlowingPotionEffect")) {
            entity.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, Integer.MAX_VALUE, 1));
        }

        return null;
    }

    /**
     * A method to put the boss armor and the boss weapon on the zombie passed into the method.
     *
     * @param zombie the zombie to put the boss armor on
     * @return true if successful, false if not successful
     */
    private boolean putOnEquipment(Zombie zombie) {
        FileConfiguration config = Vanillabosses.getInstance().getConfig();

        ItemStack[] armor = new ItemStack[]{
                new ItemStack(Material.IRON_BOOTS),
                new ItemStack(Material.IRON_LEGGINGS),
                new ItemStack(Material.IRON_CHESTPLATE),
                new ItemStack(Material.IRON_HELMET)
        };

        for (ItemStack itemStack : armor) {
            itemStack.addUnsafeEnchantment(Enchantment.DURABILITY, 3);
            itemStack.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 4);
        }

        try {
            zombie.getEquipment().setArmorContents(armor);
        } catch (NullPointerException e) {
            new VBLogger(getClass().getName(), Level.WARNING, "Could not put armor on the zombie boss. Nullpointer exception at ZombieBoss.putOnArmor()").logToFile();
            return false;
        }

        zombie.getEquipment().setHelmetDropChance(0);
        zombie.getEquipment().setChestplateDropChance(0);
        zombie.getEquipment().setLeggingsDropChance(0);
        zombie.getEquipment().setBootsDropChance(0);

        try {
            zombie.getEquipment().setItemInMainHand(BaseballBat.instance.makeItem());
        } catch (ItemCreationException e) {
            new VBLogger(getClass().getName(), Level.WARNING, "Could not create Weapon for Zombie boss. Exception: " + e).logToFile();

        }
        zombie.getEquipment().setItemInMainHandDropChance((float) config.getDouble("Items.BaseballBat.dropChance"));

        return true;
    }


    public static void zombieHorde(int radius, int amountOfZombies, Location center) {

        Vector v = new Vector(radius, 0, 0);  //set the starting vector for spawning

        double degrees = 360.0 / amountOfZombies; //get the degrees every spawnpoint has to be different by to make a circle

        SpawnEvent.spawn = false;

        for (int i = 0; i < amountOfZombies; i++) {

            center.add(v);

            Zombie zombie = (Zombie) Objects.requireNonNull(center.getWorld()).spawnEntity(center.add(0, 2, 0), EntityType.ZOMBIE);

            center.subtract(0, 2, 0);

            zombie.getScoreboardTags().add("NotABoss");

            zombie.setBaby(false);

            zombie.getEquipment().setHelmet(new ItemStack(Material.LEATHER_HELMET));
            zombie.getEquipment().setHelmetDropChance(0);

            center.subtract(v);

            v.rotateAroundY(degrees);

        }
        SpawnEvent.spawn = true;
    }

    /**
     * The ability of the zombie boss. Idea: The more zombies are around the boss the stronger it gets
     * This method contains checks only for whether the entity is not null and alive.
     * Checks for whether the ability should be applied have to be made before calling this method
     */
    @EventHandler
    private void zombieAbility(EntityDamageByEntityEvent event) {

        Entity damagedEntity = event.getEntity();
        Entity damager = event.getDamager();

        int zombiesAround;

        double maxDamageMultiplier = config.getDouble("Bosses.ZombieBoss.zombieAbility.maxDamageModifier");
        double maxArmorMultiplier = config.getDouble("Bosses.ZombieBoss.zombieAbility.maxArmorModifier");

        double bossMultiplier = config.getDouble("Bosses.ZombieBoss.zombieAbility.modifierPerZombie");

        if (damager.getScoreboardTags().contains(SCOREBOARDTAG)) {
            //damageMultiplier apply

            //Get the zombies around the damaged boss
            zombiesAround = damager.getWorld().getNearbyEntities(damager.getLocation(), 8, 5, 8, n -> n instanceof Zombie).size() - 1;

            if (zombiesAround < 1) return;

            //multiplier is 1 + the multiplier per zombie * zombies
            double actualMultiplier = 1 + zombiesAround * bossMultiplier;
            //Apply max multiplier
            if (actualMultiplier > maxDamageMultiplier) actualMultiplier = maxDamageMultiplier;

            event.setDamage(event.getDamage() * actualMultiplier);
        }

        if (damagedEntity.getScoreboardTags().contains(SCOREBOARDTAG)) {
            //damage taken multiplier apply

            zombiesAround = damagedEntity.getWorld().getNearbyEntities(damagedEntity.getLocation(), 8, 5, 8, n -> n instanceof Zombie).size() - 1;
            //multiplier is 1 + the multiplier per zombie * zombies

            if (zombiesAround < 1) return;

            double actualMultiplier = 1 + zombiesAround * bossMultiplier;
            //Apply max multiplier
            if (actualMultiplier > maxArmorMultiplier) actualMultiplier = maxArmorMultiplier;

            if (actualMultiplier == 0) actualMultiplier = 1;

            //get a value between 0 and 1 indirectly proportional to the actualMultiplier
            double modifier = 1 / actualMultiplier;

            event.setDamage(event.getDamage() * modifier);
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