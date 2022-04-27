package me.shreb.vanillabosses.bosses;

import me.shreb.vanillabosses.Vanillabosses;
import me.shreb.vanillabosses.bosses.bossRepresentation.Boss;
import me.shreb.vanillabosses.bosses.bossRepresentation.NormalBoss;
import me.shreb.vanillabosses.bosses.utility.BossCreationException;
import me.shreb.vanillabosses.items.ButchersAxe;
import me.shreb.vanillabosses.logging.VBLogger;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.PigZombie;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.logging.Level;

public class Zombified_PiglinBoss extends VBBoss {

    public static Zombified_PiglinBoss instance = new Zombified_PiglinBoss();

    public static final String CONFIGSECTION = "Zombified_PiglinBoss";
    public static final String SCOREBOARDTAG = "BossZombified_Piglin";

    @Override
    public LivingEntity makeBoss(Location location) throws BossCreationException {

        LivingEntity entity;

        //Attempting to spawn in a new Entity which is to be edited to a boss. logging failures as warning.
        try {
            entity = (LivingEntity) location.getWorld().spawnEntity(location, EntityType.ZOMBIFIED_PIGLIN);
        } catch (NullPointerException e) {
            new VBLogger(getClass().getName(), Level.WARNING, "Nullpointer Exception at Zombified Piglin Boss. World or location was null.\n" +
                    "Location: " + location.toString()).logToFile();
            new VBLogger(getClass().getName(), Level.WARNING, e.toString());
            throw new BossCreationException("Could not create Zombified Piglin Boss.");
        }

        //Attempting to edit the previously made entity into a boss. Logging failure as warning.
        try {
            makeBoss(entity);
        } catch (BossCreationException e) {
            new VBLogger(getClass().getName(), Level.WARNING, "Error creating a Zombified Piglin boss!").logToFile();
        }

        return entity;
    }

    @Override
    public LivingEntity makeBoss(LivingEntity entity) throws BossCreationException {

        FileConfiguration config = Vanillabosses.getInstance().getConfig();

        // checking wether the entity passed in is a pig zombie. Logging as a warning and throwing an exception if not.
        if (!(entity instanceof PigZombie)) {
            new VBLogger(getClass().getName(), Level.WARNING, "Attempted to make a Zombified Piglin boss out of an Entity.\n" +
                    "Entity passed in: " + entity.getType() + "\n" +
                    "Boss could not be created!").logToFile();

            throw new BossCreationException("Attempted to make a boss out of an Entity. Could not make Zombified Piglin Boss out of this Entity.");
        }

        //getting the Boss Attributes from the config file
        double health = config.getDouble("Bosses." + CONFIGSECTION + ".health");
        String nameColorString = config.getString("Bosses." + CONFIGSECTION + ".displayNameColor");

        ChatColor nameColor;

        //If the String is null or empty set it to a standard String
        if (nameColorString == null || nameColorString.equals("")) {
            new VBLogger(getClass().getName(), Level.WARNING, "Could not get name Color String for Zombified Piglin boss! Defaulting to #000000").logToFile();
            nameColor = ChatColor.of("#000000");
        } else {
            try {
                nameColor = ChatColor.of(nameColorString);
            } catch (IllegalArgumentException e) {
                nameColor = ChatColor.of("#000000");
            }
        }

        //Setting the pigZombie to be angry all the time every time. might need tweaking.
        //Attempted tweaking: delayed the task by 2 seconds.
        Bukkit.getScheduler().scheduleSyncDelayedTask(Vanillabosses.getInstance(), () -> {
            ((PigZombie) entity).setAngry(true);
            ((PigZombie) entity).setAnger(Integer.MAX_VALUE);
        }, 40);


        String name = config.getString("Bosses." + CONFIGSECTION + ".displayName");

        //setting the entity Attributes. Logging failure as Warning.
        try {
            entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(health);
            entity.setHealth(health);
            entity.setCustomName(nameColor + name);
            entity.setCustomNameVisible(config.getBoolean("Bosses." + CONFIGSECTION + ".showDisplayNameAlways"));

        } catch (Exception e) {
            new VBLogger(getClass().getName(), Level.WARNING, "Could not set Attributes on Zombified Piglin Boss\n" +
                    "Reason: " + e).logToFile();
        }

        // Setting scoreboard tag so the boss can be recognised.
        entity.getScoreboardTags().add(SCOREBOARDTAG);
        entity.getScoreboardTags().add(VBBoss.BOSSTAG);

        new NormalBoss(entity.getType()).putCommandsToPDC(entity);

        //Putting equipment on the boss, throwing Exception if failed.
        if (!putOnEquipment((PigZombie) entity)) {
            throw new BossCreationException("Could not put Armor on Zombified Piglin boss");
        }

        //Putting glowing effect on bosses if config is set to do so.
        if (Vanillabosses.getInstance().getConfig().getBoolean("Bosses.bossesGetGlowingPotionEffect")) {
            entity.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, Integer.MAX_VALUE, 1));
        }

        return null;
    }

    /**
     * A method to put the boss armor and the boss weapon on the zombie passed into the method.
     *
     * @param pigZombie the Piglin to put the boss armor on
     * @return true if successful, false if not successful
     */
    private boolean putOnEquipment(PigZombie pigZombie) {
        FileConfiguration config = Vanillabosses.getInstance().getConfig();

        //Creating a new ItemStack array and filling it with the needed armor.
        ItemStack[] armor = new ItemStack[]{
                new ItemStack(Material.GOLDEN_HELMET),
                new ItemStack(Material.GOLDEN_CHESTPLATE),
                new ItemStack(Material.GOLDEN_LEGGINGS),
                new ItemStack(Material.GOLDEN_BOOTS)
        };

        //Enchanting armor
        for (ItemStack itemStack : armor) {
            itemStack.addUnsafeEnchantment(Enchantment.DURABILITY, 5);
            itemStack.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 3);
        }

        //Attempting to put the armor on the boss, Logging failure as warning and returning false
        try {
            pigZombie.getEquipment().setArmorContents(armor);
        } catch (NullPointerException e) {
            new VBLogger(getClass().getName(), Level.WARNING, "Could not put armor on the zombie boss. Nullpointer exception at ZombieBoss.putOnArmor()").logToFile();
            return false;
        }

        //Making sure the equipment does not drop
        pigZombie.getEquipment().setHelmetDropChance(0);
        pigZombie.getEquipment().setChestplateDropChance(0);
        pigZombie.getEquipment().setLeggingsDropChance(0);
        pigZombie.getEquipment().setBootsDropChance(0);

        pigZombie.getEquipment().setItemInMainHand(ButchersAxe.instance.makeItem());
        pigZombie.getEquipment().setItemInMainHandDropChance((float) config.getDouble("Items.ButchersAxe.dropChance"));

        return true;
    }

}
