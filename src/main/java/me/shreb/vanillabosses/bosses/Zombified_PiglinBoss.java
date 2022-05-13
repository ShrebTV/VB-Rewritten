package me.shreb.vanillabosses.bosses;

import me.shreb.vanillabosses.Vanillabosses;
import me.shreb.vanillabosses.bosses.bossRepresentation.NormalBoss;
import me.shreb.vanillabosses.bosses.utility.BossCreationException;
import me.shreb.vanillabosses.items.ButchersAxe;
import me.shreb.vanillabosses.items.utility.ItemCreationException;
import me.shreb.vanillabosses.logging.VBLogger;
import me.shreb.vanillabosses.utility.Utility;
import me.shreb.vanillabosses.utility.configFiles.FileCreator;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.UUID;
import java.util.logging.Level;

public class Zombified_PiglinBoss extends VBBoss {

    public static Zombified_PiglinBoss instance = new Zombified_PiglinBoss();

    public static LinkedList<UUID> piglinBossList = new LinkedList<>();

    public static final String CONFIGSECTION = "Zombified_PiglinBoss";
    public static final String SCOREBOARDTAG = "BossZombified_Piglin";

    public Zombified_PiglinBoss() {
        new FileCreator().createAndLoad(FileCreator.zombified_PiglinBossPath, this.config);
    }

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

        if (!config.getBoolean("enabled")) return entity;

        // checking wether the entity passed in is a pig zombie. Logging as a warning and throwing an exception if not.
        if (!(entity instanceof PigZombie)) {
            new VBLogger(getClass().getName(), Level.WARNING, "Attempted to make a Zombified Piglin boss out of an Entity.\n" +
                    "Entity passed in: " + entity.getType() + "\n" +
                    "Boss could not be created!").logToFile();

            throw new BossCreationException("Attempted to make a boss out of an Entity. Could not make Zombified Piglin Boss out of this Entity.");
        }

        //getting the Boss Attributes from the config file
        double health = config.getDouble("health");
        String nameColorString = config.getString("displayNameColor");

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

        String name = config.getString("displayName");

        double speedMultiplier = config.getDouble("SpeedModifier");
        if (speedMultiplier < 0.0001) speedMultiplier = 1;
        entity.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(speedMultiplier * entity.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).getBaseValue());

        //setting the entity Attributes. Logging failure as Warning.
        try {
            entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(health);
            entity.setHealth(health);
            entity.setCustomName(nameColor + name);
            entity.setCustomNameVisible(config.getBoolean("showDisplayNameAlways"));

        } catch (Exception e) {
            new VBLogger(getClass().getName(), Level.WARNING, "Could not set Attributes on Zombified Piglin Boss\n" +
                    "Reason: " + e).logToFile();
        }

        // Setting scoreboard tag so the boss can be recognised.
        entity.getScoreboardTags().add(SCOREBOARDTAG);
        entity.getScoreboardTags().add(BOSSTAG);
        entity.getScoreboardTags().add(REMOVE_ON_DISABLE_TAG);

        new NormalBoss(entity.getType()).putCommandsToPDC(entity);

        //Putting equipment on the boss, throwing Exception if failed.
        if (!putOnEquipment((PigZombie) entity)) {
            throw new BossCreationException("Could not put Armor on Zombified Piglin boss");
        }

        //Putting glowing effect on bosses if config is set to do so.
        if (Vanillabosses.getInstance().getConfig().getBoolean("Bosses.bossesGetGlowingPotionEffect")) {
            entity.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, Integer.MAX_VALUE, 1));
        }

        piglinBossList.add(entity.getUniqueId());

        return null;
    }

    /**
     * A method to put the boss armor and the boss weapon on the zombie passed into the method.
     *
     * @param pigZombie the Piglin to put the boss armor on
     * @return true if successful, false if not successful
     */
    private boolean putOnEquipment(PigZombie pigZombie) {

        //Creating a new ItemStack array and filling it with the needed armor.
        ItemStack[] armor = new ItemStack[]{
                new ItemStack(Material.GOLDEN_BOOTS),
                new ItemStack(Material.GOLDEN_LEGGINGS),
                new ItemStack(Material.GOLDEN_CHESTPLATE),
                new ItemStack(Material.GOLDEN_HELMET)
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

        try {
            pigZombie.getEquipment().setItemInMainHand(ButchersAxe.instance.makeItem());
        } catch (ItemCreationException e) {
            new VBLogger(getClass().getName(), Level.WARNING, "Could not create Weapon for Zombified Piglin boss. Exception: " + e).logToFile();
        }
        pigZombie.getEquipment().setItemInMainHandDropChance((float) ButchersAxe.instance.configuration.getDouble("dropChance"));

        return true;
    }

    @EventHandler
    public void ability(EntityDamageByEntityEvent event) {

        double jumpChance = this.config.getDouble("onHitAbilities.Jump.Chance");
        double pullChance = this.config.getDouble("onHitAbilities.Pull.Chance");
        double pushChance = this.config.getDouble("onHitAbilities.Push.Chance");

        double jumpStrengthMultiplier = config.getDouble("onHitAbilities.Jump.JumpStrengthMultiplier");
        double pullStrengthMultiplier = config.getDouble("onHitAbilities.Pull.PullStrengthMultiplier");
        double pushStrengthMultiplier = config.getDouble("onHitAbilities.Push.PushStrengthMultiplier");

        if (event.getEntity().getScoreboardTags().contains(SCOREBOARDTAG)
                && event.getEntityType() == EntityType.ZOMBIFIED_PIGLIN
                && (event.getDamager() instanceof Player || (event.getDamager() instanceof Projectile && ((Projectile) event.getDamager()).getShooter() instanceof Player))) {

            PigZombie entity = (PigZombie) event.getEntity();
            Player player = null;

            if (event.getDamager() instanceof Player) {
                player = (Player) event.getDamager();
            } else if (event.getDamager() instanceof Projectile && ((Projectile) event.getDamager()).getShooter() instanceof Player) {
                player = (Player) ((Projectile) event.getDamager()).getShooter();
            }

            if (player == null) return;

            if (Utility.roll(jumpChance)) {

                entity.setVelocity(new Vector(0, 1 * jumpStrengthMultiplier, 0));

            }

            if (Utility.roll(pullChance)) {

                Vector towardsBoss = new Vector(entity.getLocation().getX() - player.getLocation().getX(),
                        entity.getLocation().getY() - player.getLocation().getY(),
                        entity.getLocation().getZ() - player.getLocation().getZ());

                towardsBoss.multiply(pullStrengthMultiplier);

                player.setVelocity(towardsBoss);

            }

            if (Utility.roll(pushChance)) {

                Vector awayFromBoss = new Vector(player.getLocation().getX() - entity.getLocation().getX(),
                        player.getLocation().getY() - entity.getLocation().getY(),
                        player.getLocation().getZ() - entity.getLocation().getZ());

                awayFromBoss.multiply(pushStrengthMultiplier);

                player.setVelocity(awayFromBoss);

            }
        }
    }

    /**
     * Runs a timer in order to make all registered Zombie piglin bosses agressive to players without being hit
     * This method should only be run once after startup of the plugin
     */
    public static void aggressionTimer() {

        Bukkit.getScheduler().runTaskTimer(Vanillabosses.getInstance(), () -> {

            for (UUID uuid : new LinkedList<>(piglinBossList)) {

                Entity entity = Bukkit.getEntity(uuid);
                if (entity == null || entity.isDead()) {

                    piglinBossList.remove(uuid);
                } else if (entity.getType() == EntityType.ZOMBIFIED_PIGLIN) {

                    ArrayList<Entity> playerList = (ArrayList<Entity>) entity.getWorld().getNearbyEntities(entity.getLocation(), 30, 20, 30, n -> n instanceof Player && ((Player) n).getGameMode() == GameMode.SURVIVAL);
                    if (playerList.isEmpty()) return;

                    Location bossLoc = entity.getLocation();
                    double latestDistance = 1000;
                    Player latestPlayer = null;
                    for (Entity e : playerList) {
                        double thisDistance = bossLoc.distance(e.getLocation());
                        if (thisDistance < latestDistance) {
                            latestDistance = thisDistance;
                            latestPlayer = (Player) e;
                        }
                    }

                    ((PigZombie) entity).setTarget(latestPlayer);
                }
            }
        }, 100, 30);
    }
}