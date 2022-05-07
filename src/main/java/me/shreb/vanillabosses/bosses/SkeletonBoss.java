package me.shreb.vanillabosses.bosses;

import me.shreb.vanillabosses.Vanillabosses;
import me.shreb.vanillabosses.bosses.bossRepresentation.NormalBoss;
import me.shreb.vanillabosses.bosses.utility.BossCreationException;
import me.shreb.vanillabosses.items.Skeletor;
import me.shreb.vanillabosses.items.utility.ItemCreationException;
import me.shreb.vanillabosses.listeners.SpawnEvent;
import me.shreb.vanillabosses.logging.VBLogger;
import me.shreb.vanillabosses.utility.ConfigVerification;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;
import java.util.logging.Level;

public class SkeletonBoss extends VBBoss implements ConfigVerification {

    public static SkeletonBoss instance = new SkeletonBoss();

    public static final String CONFIGSECTION = "SkeletonBoss";
    public static final String SCOREBOARDTAG = "BossSkeleton";

    @Override
    public LivingEntity makeBoss(Location location) throws BossCreationException {

        LivingEntity entity;
        try {
            entity = (LivingEntity) location.getWorld().spawnEntity(location, EntityType.SKELETON);
        } catch (NullPointerException e) {
            new VBLogger(getClass().getName(), Level.WARNING, "Nullpointer Exception at Skeleton Boss. World or location was null.\n" +
                    "Location: " + location.toString()).logToFile();
            new VBLogger(getClass().getName(), Level.WARNING, e.toString());
            throw new BossCreationException("Could not create Skeleton Boss.");
        }

        try {
            makeBoss(entity);
        } catch (BossCreationException e) {
            new VBLogger(getClass().getName(), Level.WARNING, "Error creating a Skeleton boss!").logToFile();
        }

        return entity;
    }

    @Override
    public LivingEntity makeBoss(LivingEntity entity) throws BossCreationException {

        if (!config.getBoolean("Bosses." + CONFIGSECTION + ".enabled")) return entity;

        if (!(entity instanceof Skeleton)) {
            new VBLogger(getClass().getName(), Level.WARNING, "Attempted to make a Skeleton boss out of an Entity.\n" +
                    "Entity passed in: " + entity.getType() + "\n" +
                    "Boss could not be created!").logToFile();

            throw new BossCreationException("Attempted to make a boss out of an Entity. Could not make Skeleton Boss out of this Entity.");
        }

        //getting the Boss Attributes from the config file
        double health = config.getDouble("Bosses." + CONFIGSECTION + ".health");
        String nameColorString = config.getString("Bosses." + CONFIGSECTION + ".displayNameColor");

        ChatColor nameColor = null;

        //If the String is null or empty set it to a standard String
        if (nameColorString == null || nameColorString.equals("")) {
            new VBLogger(getClass().getName(), Level.WARNING, "Could not get name Color String for Skeleton boss! Defaulting to #000000").logToFile();
        } else {
            try {
                nameColor = ChatColor.of(nameColorString);
            } catch (IllegalArgumentException e) {
                nameColor = ChatColor.of("#000000");
            }
        }

        String name = config.getString("Bosses." + CONFIGSECTION + ".displayName");

        //setting the Attributes. Logging to file if it fails at any point.
        try {
            entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(health);
            entity.setHealth(health);
            entity.setCustomName(nameColor + name);
            entity.setCustomNameVisible(config.getBoolean("Bosses." + CONFIGSECTION + ".showDisplayNameAlways"));

        } catch (Exception e) {
            new VBLogger(getClass().getName(), Level.WARNING, "Could not set Attributes on Skeleton Boss\n" +
                    "Reason: " + e).logToFile();
        }

        // Setting scoreboard tag so the boss can be recognised.
        entity.getScoreboardTags().add(SCOREBOARDTAG);
        entity.getScoreboardTags().add(BOSSTAG);
        entity.getScoreboardTags().add(REMOVE_ON_DISABLE_TAG);

        new NormalBoss(entity.getType()).putCommandsToPDC(entity);

        if (!putOnEquipment((Skeleton) entity)) {
            throw new BossCreationException("Could not put Armor on Skeleton boss");
        }

        if (Vanillabosses.getInstance().getConfig().getBoolean("Bosses.bossesGetGlowingPotionEffect")) {
            entity.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, Integer.MAX_VALUE, 1));
        }

        return null;
    }

    private boolean putOnEquipment(Skeleton skeleton) {
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
            skeleton.getEquipment().setArmorContents(armor);
        } catch (NullPointerException e) {
            new VBLogger(getClass().getName(), Level.WARNING, "Could not put armor on the Skeleton boss. Nullpointer exception at SkeletonBoss.putOnArmor()").logToFile();
            return false;
        }

        skeleton.getEquipment().setHelmetDropChance(0);
        skeleton.getEquipment().setChestplateDropChance(0);
        skeleton.getEquipment().setLeggingsDropChance(0);
        skeleton.getEquipment().setBootsDropChance(0);

        try {
            skeleton.getEquipment().setItemInMainHand(Skeletor.instance.makeItem());
        } catch (ItemCreationException e) {
            new VBLogger(getClass().getName(), Level.WARNING, "Could not create Weapon for Skeleton boss. Exception: " + e).logToFile();

        }
        skeleton.getEquipment().setItemInMainHandDropChance((float) config.getDouble("Items.Skeletor.dropChance"));

        return true;
    }

    @EventHandler
    public void onHitAbility(EntityDamageByEntityEvent event){

        FileConfiguration config = Vanillabosses.getInstance().getConfig();

        if (event.getEntity().getScoreboardTags().contains("BossSkeleton") && event.getEntityType() == EntityType.SKELETON) {

            if (!(event.getDamager() instanceof Player)) return;

            Entity entity = event.getEntity();

            double rn = new Random().nextDouble();

            double chanceReflectDamage = config.getDouble("Bosses.SkeletonBoss.onHitEvents.reflectDamage.chance");
            double chanceSpawnMinions = config.getDouble("Bosses.SkeletonBoss.onHitEvents.spawnMinions.chance");
            double chanceInvulnerability = config.getDouble("Bosses.SkeletonBoss.onHitEvents.invulnerability.chance");

            double currentChance = chanceReflectDamage;

            if (rn <= currentChance) {

                event.getEntity().getWorld().playSound(event.getEntity().getLocation(), Sound.BLOCK_ANVIL_FALL, 1F, 2F);

                double damage = event.getDamage();

                damage *= config.getDouble("Bosses.SkeletonBoss.onHitEvents.reflectDamage.damageMultiplier");

                ((Player) event.getDamager()).damage(damage);

                event.setCancelled(true);

                return;
            }

            currentChance += chanceInvulnerability;

            if (rn <= currentChance) {

                if (!(entity.isInvulnerable())) {

                    int seconds = config.getInt("Bosses.SkeletonBoss.onHitEvents.invulnerability.durationInSeconds");

                    entity.setInvulnerable(true);
                    entity.getWorld().spawnParticle(Particle.FLAME, entity.getLocation(), 25);

                    Bukkit.getScheduler().scheduleSyncDelayedTask(Vanillabosses.getInstance(), () -> {

                        entity.setInvulnerable(false);
                        entity.getWorld().spawnParticle(Particle.HEART, entity.getLocation(), 25);
                        ((LivingEntity) entity).addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 10 * 20, 5));

                    }, seconds * 20L);
                }
                return;
            }

            currentChance += chanceSpawnMinions;

            if (rn <= currentChance) {

                World w = entity.getWorld();

                w.playSound(entity.getLocation(), Sound.BLOCK_LAVA_EXTINGUISH, 1F, 1F);

                int x = entity.getLocation().getBlockX();
                int y = entity.getLocation().getBlockY();
                int z = entity.getLocation().getBlockZ();

                ArrayList<Entity> minions = new ArrayList<>();

                if (config.getBoolean("Bosses.SkeletonBoss.onHitEvents.spawnMinions.abilityRemovesBlocks")) {

                    Location[] blocks = {
                            w.getBlockAt(x, y, z).getLocation(),
                            w.getBlockAt(x, y + 1, z).getLocation(),
                            w.getBlockAt(x - 1, y, z).getLocation(),
                            w.getBlockAt(x - 1, y + 1, z).getLocation(),
                            w.getBlockAt(x + 1, y, z).getLocation(),
                            w.getBlockAt(x + 1, y + 1, z).getLocation(),
                            w.getBlockAt(x, y, z - 1).getLocation(),
                            w.getBlockAt(x, y + 1, z - 1).getLocation(),
                            w.getBlockAt(x, y, z + 1).getLocation(),
                            w.getBlockAt(x, y + 1, z + 1).getLocation(),
                            w.getBlockAt(x, y + 2, z).getLocation()
                    };

                    for (Location loc : blocks
                    ) {

                        if (!(loc.getBlock().getType().equals(Material.OBSIDIAN)) && !(loc.getBlock().getType().equals(Material.BEDROCK)) && !(loc.getBlock().getType().equals(Material.BEACON))) {

                            loc.getBlock().setType(Material.AIR);

                        }
                    }
                }
                SpawnEvent.spawn = false;

                Entity tempEnt = w.spawnEntity(w.getBlockAt(x + 1, y, z).getLocation(), EntityType.SKELETON);
                minions.add(tempEnt);
                tempEnt = w.spawnEntity(w.getBlockAt(x - 1, y, z).getLocation(), EntityType.SKELETON);
                minions.add(tempEnt);
                tempEnt = w.spawnEntity(w.getBlockAt(x, y, z + 1).getLocation(), EntityType.SKELETON);
                minions.add(tempEnt);
                tempEnt = w.spawnEntity(w.getBlockAt(x, y, z - 1).getLocation(), EntityType.SKELETON);
                minions.add(tempEnt);

                SpawnEvent.spawn = true;

                for (Entity e : minions
                ) {
                    ((LivingEntity) e).addPotionEffect(new PotionEffect(PotionEffectType.POISON, 30 * 20, 1));
                    Objects.requireNonNull(((LivingEntity) e).getEquipment()).setHelmet(new ItemStack(Material.IRON_HELMET));

                    if (((LivingEntity) e).getEquipment() == null) return;

                    e.removeScoreboardTag("BossSkeleton");
                    e.setCustomName("The Kings Minion");
                    e.setCustomNameVisible(false);
                    ((LivingEntity) e).getEquipment().setBoots(null);
                    ((LivingEntity) e).getEquipment().setLeggings(null);
                    ((LivingEntity) e).getEquipment().setChestplate(null);
                    ((LivingEntity) e).getEquipment().setItemInOffHand(null);
                    ((LivingEntity) e).getEquipment().setItemInMainHand(new ItemStack(Material.BOW));
                }
            }
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
