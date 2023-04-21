package me.shreb.vanillabosses.bosses;

import me.shreb.vanillabosses.Vanillabosses;
import me.shreb.vanillabosses.bosses.bossRepresentation.NormalBoss;
import me.shreb.vanillabosses.bosses.utility.BossCreationException;
import me.shreb.vanillabosses.bosses.utility.bossarmor.ArmorEquipException;
import me.shreb.vanillabosses.bosses.utility.bossarmor.ArmorSet;
import me.shreb.vanillabosses.bosses.utility.bossarmor.ArmorSetType;
import me.shreb.vanillabosses.items.Skeletor;
import me.shreb.vanillabosses.items.utility.ItemCreationException;
import me.shreb.vanillabosses.listeners.SpawnEvent;
import me.shreb.vanillabosses.logging.VBLogger;
import me.shreb.vanillabosses.utility.configFiles.FileCreator;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;

public class SkeletonBoss extends VBBoss {

    public static SkeletonBoss instance = new SkeletonBoss();

    public static final String CONFIGSECTION = "SkeletonBoss";
    public static final String SCOREBOARDTAG = "BossSkeleton";

    public SkeletonBoss() {
        new FileCreator().createAndLoad(FileCreator.skeletonBossPath, this.config);
    }

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

        if (!instance.config.getBoolean("enabled")) return entity;

        if (!(entity instanceof Skeleton)) {
            new VBLogger(getClass().getName(), Level.WARNING, "Attempted to make a Skeleton boss out of an Entity.\n" +
                    "Entity passed in: " + entity.getType() + "\n" +
                    "Boss could not be created!").logToFile();

            throw new BossCreationException("Attempted to make a boss out of an Entity. Could not make Skeleton Boss out of this Entity.");
        }

        Bukkit.getScheduler().runTaskLater(Vanillabosses.getInstance(), () -> {

            //getting the Boss Attributes from the config file
            double health = instance.config.getDouble("health");
            String nameColorString = instance.config.getString("displayNameColor");

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

            String name = instance.config.getString("displayName");

            double speedMultiplier = instance.config.getDouble("SpeedModifier");
            if (speedMultiplier < 0.0001) speedMultiplier = 1;
            entity.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(speedMultiplier * entity.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).getBaseValue());

            //setting the Attributes. Logging to file if it fails at any point.
            try {
                entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(health);
                entity.setHealth(health);
                entity.setCustomName(nameColor + name);
                entity.setCustomNameVisible(instance.config.getBoolean("showDisplayNameAlways"));

            } catch (Exception e) {
                new VBLogger(getClass().getName(), Level.WARNING, "Could not set Attributes on Skeleton Boss\n" +
                        "Reason: " + e).logToFile();
            }

        }, 1);

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

        String armorType = config.getString("ArmorMaterial");

        if (armorType == null) armorType = "IRON";

        armorType = armorType.toUpperCase();

        ArmorSetType type;

        try {
            type = ArmorSetType.valueOf(armorType);
        } catch (IllegalArgumentException e) {
            type = ArmorSetType.DIAMOND;
        }

        ArmorSet set = new ArmorSet(type);

        int protMin = config.getInt("ProtectionMin");
        int protMax = config.getInt("ProtectionMax");

        set.enchantAllArmor(Enchantment.DURABILITY, 3);
        set.enchantAllArmor(Enchantment.PROTECTION_ENVIRONMENTAL, protMin, protMax);

        try {
            set.equipArmor(skeleton);
        } catch (ArmorEquipException e) {
            new VBLogger(getClass().getName(), Level.WARNING, "Could not put armor on the Skeleton boss. ArmorEquipException at SkeletonBoss.putOnArmor().\n" +
                    "Exception: " + e).logToFile();
            return false;
        }

        skeleton.getEquipment().setHelmetDropChance(0);
        skeleton.getEquipment().setChestplateDropChance(0);
        skeleton.getEquipment().setLeggingsDropChance(0);
        skeleton.getEquipment().setBootsDropChance(0);

        try {

            int powerMin = config.getInt("BowEnchants.Power.min");
            int powerMax = config.getInt("BowEnchants.Power.max");

            int punchMin = config.getInt("BowEnchants.Punch.min");
            int punchMax = config.getInt("BowEnchants.Punch.max");

            int unbreakingMin = config.getInt("BowEnchants.Unbreaking.min");
            int unbreakingMax = config.getInt("BowEnchants.Unbreaking.max");

            int flameMin = config.getInt("BowEnchants.Flame.min");
            int flameMax = config.getInt("BowEnchants.Flame.max");

            ItemStack weapon = Skeletor.instance.makeItem();

            weapon.addUnsafeEnchantment(Enchantment.ARROW_DAMAGE, ThreadLocalRandom.current().nextInt(powerMin, powerMax + 1));
            weapon.addUnsafeEnchantment(Enchantment.ARROW_KNOCKBACK, ThreadLocalRandom.current().nextInt(punchMin, punchMax + 1));
            weapon.addUnsafeEnchantment(Enchantment.DURABILITY, ThreadLocalRandom.current().nextInt(unbreakingMin, unbreakingMax + 1));
            weapon.addUnsafeEnchantment(Enchantment.ARROW_FIRE, ThreadLocalRandom.current().nextInt(flameMin, flameMax + 1));

            skeleton.getEquipment().setItemInMainHand(weapon);
        } catch (ItemCreationException e) {
            new VBLogger(getClass().getName(), Level.WARNING, "Could not create Weapon for Skeleton boss. Exception: " + e).logToFile();
        }
        skeleton.getEquipment().setItemInMainHandDropChance((float) Skeletor.instance.configuration.getDouble("dropChance"));

        return true;
    }

    @EventHandler
    @SuppressWarnings("ConstantConditions")
    public void onHitAbility(EntityDamageByEntityEvent event) {

        if (event.getEntity().getScoreboardTags().contains(SCOREBOARDTAG) && event.getEntityType() == EntityType.SKELETON) {

            if (!(event.getDamager() instanceof Player)
                    && !(event.getDamager() instanceof Arrow)) return;

            Entity entity = event.getEntity();

            double rn = new Random().nextDouble();

            double chanceReflectDamage = config.getDouble("onHitEvents.reflectDamage.chance");
            double chanceSpawnMinions = config.getDouble("onHitEvents.spawnMinions.chance");
            double chanceInvulnerability = config.getDouble("onHitEvents.invulnerability.chance");

            double currentChance = chanceReflectDamage;

            if (rn <= currentChance) {
                //Reflecting damage

                event.getEntity().getWorld().playSound(event.getEntity().getLocation(), Sound.BLOCK_ANVIL_FALL, 1F, 2F);

                double damage = event.getDamage();

                damage *= config.getDouble("onHitEvents.reflectDamage.damageMultiplier");

                Player playerToDamage;

                if (event.getDamager() instanceof Arrow && ((Arrow) event.getDamager()).getShooter() instanceof Player) {

                    playerToDamage = (Player) ((Arrow) event.getDamager()).getShooter();

                } else if (event.getDamager() instanceof Player) {

                    playerToDamage = (Player) event.getDamager();

                } else return;

                (playerToDamage).damage(damage);

                event.setCancelled(true);

                return;
            }

            currentChance += chanceInvulnerability;

            if (rn <= currentChance) {
                //invulnerablility


                int seconds = config.getInt("onHitEvents.invulnerability.durationInSeconds");

                entity.setInvulnerable(true);
                entity.getWorld().spawnParticle(Particle.FLAME, entity.getLocation(), 25);

                Bukkit.getScheduler().scheduleSyncDelayedTask(Vanillabosses.getInstance(), () -> {

                    entity.setInvulnerable(false);
                    entity.getWorld().spawnParticle(Particle.HEART, entity.getLocation(), 25);
                    ((LivingEntity) entity).addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 10 * 20, 5));

                }, seconds * 20L);

                return;
            }

            currentChance += chanceSpawnMinions;

            if (rn <= currentChance) {
                //Minions

                World w = entity.getWorld();

                w.playSound(entity.getLocation(), Sound.BLOCK_LAVA_EXTINGUISH, 1F, 1F);

                int x = entity.getLocation().getBlockX();
                int y = entity.getLocation().getBlockY();
                int z = entity.getLocation().getBlockZ();

                ArrayList<Entity> minions = new ArrayList<>();

                if (config.getBoolean("onHitEvents.spawnMinions.abilityRemovesBlocks")) {

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

                    EntityEquipment equipment = ((LivingEntity) e).getEquipment();

                    e.removeScoreboardTag("BossSkeleton");
                    e.setCustomName("The Kings Minion");
                    e.setCustomNameVisible(false);
                    equipment.setBoots(null);
                    equipment.setLeggings(null);
                    equipment.setChestplate(null);
                    equipment.setItemInOffHand(null);
                    equipment.setItemInMainHand(new ItemStack(Material.BOW));
                }
            }
        }
    }
}
