package me.shreb.vanillabosses.bosses;

import me.shreb.vanillabosses.Vanillabosses;
import me.shreb.vanillabosses.bosses.bossRepresentation.NormalBoss;
import me.shreb.vanillabosses.bosses.utility.BossCreationException;
import me.shreb.vanillabosses.logging.VBLogger;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class WitherBoss extends VBBoss {

    public static WitherBoss instance = new WitherBoss();
    public static List<UUID> passiveWitherList = new ArrayList<>();

    public static final String CONFIGSECTION = "WitherBoss";
    public static final String SCOREBOARDTAG = "BossWither";

    @Override
    public LivingEntity makeBoss(Location location) throws BossCreationException {

        LivingEntity entity;

        //Attempting to spawn in a new Entity which is to be edited to a boss. logging failures as warning.
        try {
            entity = (LivingEntity) location.getWorld().spawnEntity(location, EntityType.WITHER);
        } catch (NullPointerException e) {
            new VBLogger(getClass().getName(), Level.WARNING, "Nullpointer Exception at Wither Boss. World or location was null.\n" +
                    "Location: " + location.toString()).logToFile();
            new VBLogger(getClass().getName(), Level.WARNING, e.toString());
            throw new BossCreationException("Could not create Wither Boss.");
        }

        //Attempting to edit the previously made entity into a boss. Logging failure as warning.
        try {
            makeBoss(entity);
        } catch (BossCreationException e) {
            new VBLogger(getClass().getName(), Level.WARNING, "Error creating a Wither boss!").logToFile();
        }

        return entity;
    }

    @Override
    public LivingEntity makeBoss(LivingEntity entity) throws BossCreationException {

        FileConfiguration config = Vanillabosses.getInstance().getConfig();

        // checking wether the entity passed in is a Wither. Logging as a warning and throwing an exception if not.
        if (!(entity instanceof Wither)) {
            new VBLogger(getClass().getName(), Level.WARNING, "Attempted to make a Wither boss out of an Entity.\n" +
                    "Entity passed in: " + entity.getType() + "\n" +
                    "Boss could not be created!").logToFile();

            throw new BossCreationException("Attempted to make a boss out of an Entity. Could not make Wither Boss out of this Entity.");
        }

        //getting the Boss Attributes from the config file
        double health = config.getDouble("Bosses." + CONFIGSECTION + ".health");
        String nameColorString = config.getString("Bosses." + CONFIGSECTION + ".displayNameColor");

        ChatColor nameColor;

        //If the String is null or empty set it to a standard String
        if (nameColorString == null || nameColorString.equals("")) {
            new VBLogger(getClass().getName(), Level.WARNING, "Could not get name Color String for Wither boss! Defaulting to #000000").logToFile();
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
            new VBLogger(getClass().getName(), Level.WARNING, "Could not set Attributes on Wither Boss\n" +
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

    public void onPlaceWitherEgg(BlockPlaceEvent event) {

        if (!(event.getBlock().getType().equals(Material.DRAGON_EGG))) return;
        if (!event.getItemInHand().getItemMeta().hasLore()) return;

        if (Objects.requireNonNull(event.getItemInHand().getItemMeta().getLore()).contains("What will hatch from this?")) {

            BlockData data = event.getBlock().getBlockData();

            if (event.getBlock().getWorld().getBlockAt(event.getBlock().getLocation().getBlockX(), event.getBlock().getLocation().getBlockY() - 1, event.getBlock().getLocation().getBlockZ()).getType().equals(Material.ANVIL)) {

                Location eggLoc = event.getBlock().getLocation();
                Location anvilLoc = event.getBlock().getWorld().getBlockAt(event.getBlock().getLocation().getBlockX(), event.getBlock().getLocation().getBlockY() - 1, event.getBlock().getLocation().getBlockZ()).getLocation();

                Bukkit.getScheduler().scheduleSyncDelayedTask(Vanillabosses.getInstance(), () -> {

                    if (eggLoc.getBlock().getType().equals(Material.DRAGON_EGG)
                            && eggLoc.getBlock().getBlockData().matches(data)
                            && anvilLoc.getBlock().getType().equals(Material.ANVIL)) {

                        Wither wither = (Wither) event.getPlayer().getWorld().spawnEntity(eggLoc, EntityType.WITHER);

                        wither.setCustomName(event.getPlayer().getName() + "s Pet Wither");

                        wither.setAI(false);

                        wither.addScoreboardTag("PassiveWither");

                        wither.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(Vanillabosses.getInstance().getConfig().getDouble("Items.WitherEgg.petWitherHP"));
                        wither.setHealth(Vanillabosses.getInstance().getConfig().getDouble("Items.WitherEgg.petWitherHP"));

                        eggLoc.getBlock().setType(Material.AIR);
                        anvilLoc.getBlock().setType(Material.AIR);

                        Location loc = wither.getLocation();
                        wither.getWorld().spawnParticle(Particle.CAMPFIRE_SIGNAL_SMOKE, loc, 150);
                        passiveWitherList.add(wither.getUniqueId());

                        Bukkit.getScheduler().runTaskTimer(Vanillabosses.getInstance(), () -> {

                            passiveWitherTarget(wither);

                        }, 20, 15);

                    } else if (eggLoc.getBlock().getType().equals(Material.DRAGON_EGG) && eggLoc.getBlock().getBlockData().matches(data)) {
                        eggLoc.getBlock().setType(Material.AIR);

                        ItemStack egg = new ItemStack(Material.DRAGON_EGG);

                        ItemMeta meta = egg.getItemMeta();
                        meta.setDisplayName("A Withers Egg");

                        ArrayList<String> lore = new ArrayList<>();
                        lore.add("What will hatch from this?");
                        lore.add(org.bukkit.ChatColor.BLACK + "Place on an Anvil to find out!");
                        meta.setLore(lore);

                        egg.setItemMeta(meta);
                        event.getBlock().getWorld().dropItem(eggLoc, egg);
                    }


                }, Vanillabosses.getInstance().getConfig().getInt("Items.WitherEgg.timeToHatch") * 20L);
            }
        }
    }


    /**
     *
     * @param wither
     */
    public static void passiveWitherTarget(Wither wither) {

        FileConfiguration config = Vanillabosses.getInstance().getConfig();

        if(wither.getHealth() < 0.001) return;

        int range = config.getInt("Items.WitherEgg.arrowRange");

        List<Entity> targetList = wither.getNearbyEntities(range, range, range)
                .stream()
                .filter(n -> n instanceof LivingEntity)
                .filter(n -> n instanceof Monster)
                .filter(wither::hasLineOfSight)
                .collect(Collectors.toList());

        if (targetList.isEmpty()) return;

        LivingEntity target = (LivingEntity) targetList.get(0);
        if(target.isDead()) return;

        Location witherLoc = wither.getEyeLocation();
        Location targetLoc = target.getEyeLocation();

        Arrow arrow = wither.getLocation().getWorld().spawnArrow(
                witherLoc,
                new Vector(targetLoc.getX() - witherLoc.getX(),
                        targetLoc.getY() - witherLoc.getY(),
                        targetLoc.getZ() - witherLoc.getZ()),
                5,
                1
        );
        arrow.setDamage(config.getDouble("Items.WitherEgg.arrowDamageMultiplier") * arrow.getDamage());
        arrow.setGravity(false);
        arrow.setPickupStatus(AbstractArrow.PickupStatus.CREATIVE_ONLY);

        arrow.getPersistentDataContainer().set(new NamespacedKey(Vanillabosses.getInstance(), "targetUUID"), PersistentDataType.STRING, target.getUniqueId().toString());
        arrow.getPersistentDataContainer().set(new NamespacedKey(Vanillabosses.getInstance(), "shooterUUID"), PersistentDataType.STRING, wither.getUniqueId().toString());

        Bukkit.getScheduler().runTaskLater(Vanillabosses.getInstance(), arrow::remove, 100);
    }

    /**
     * The method to make a wither egg with
     * @return the wither egg
     */
    public static ItemStack makeWitherEgg() {

        ItemStack witherEgg = new ItemStack(Material.DRAGON_EGG);

        ItemMeta meta = witherEgg.getItemMeta();
        meta.setDisplayName("A Withers Egg");
        ArrayList<String> lore = new ArrayList<>();
        lore.add("What will hatch from this?");
        lore.add(org.bukkit.ChatColor.BLACK + "Place on an Anvil to find out!");
        meta.setLore(lore);

        witherEgg.setItemMeta(meta);

        return witherEgg;
    }


    /**
     * This is what will happen once an Arrow from a Passive Wither hits a target.
     * @param event the event to check for arrows fired by passive withers in
     */
    public void onWitherArrowHit(EntityDamageByEntityEvent event){

        if(!(event.getDamager() instanceof Arrow)
                || !event.getDamager().getPersistentDataContainer().has(new NamespacedKey(Vanillabosses.getInstance(), "targetUUID"), PersistentDataType.STRING)){
            return;
        }

        if(event.getEntity() instanceof HumanEntity || !(event.getEntity() instanceof Monster)) event.getDamager().setGravity(true);

        if((!event.getEntity().getUniqueId().toString().equals(event.getDamager().getPersistentDataContainer().get(new NamespacedKey(Vanillabosses.getInstance(), "targetUUID"), PersistentDataType.STRING))
                && !(event.getEntity() instanceof Monster))
                || event.getEntity().getUniqueId().toString().equals(event.getDamager().getPersistentDataContainer().get(new NamespacedKey(Vanillabosses.getInstance(), "shooterUUID"), PersistentDataType.STRING))
        ) {
            event.setCancelled(true);
            event.setDamage(0.05 * ((Arrow) event.getDamager()).getDamage());
            event.getDamager().setGravity(true);
        }
    }

    /**
     * The method which is meant to edit a wither spawned over a netherite block into a boss wither.
     * @param event the event to check for a netherite block at the correct position in
     */
    public void onWitherSpawn(CreatureSpawnEvent event) {
        FileConfiguration config = Vanillabosses.getInstance().getConfig();

        if (event.getEntityType().equals(EntityType.WITHER)) {

            if (!config.getBoolean("Bosses.WitherBoss.enabled")) return;

            LivingEntity wither = event.getEntity();

            int x = wither.getLocation().getBlockX();
            int y = wither.getLocation().getBlockY();
            int z = wither.getLocation().getBlockZ();

            if (wither.getWorld().getBlockAt(x, y - 1, z).getType() == Material.NETHERITE_BLOCK) {

                try {
                    makeBoss(wither);

                    if (config.getBoolean("Bosses.WitherBoss.removeNetheriteBlockOnSpawn")) {
                        wither.getWorld().getBlockAt(x, y - 1, z).setType(Material.AIR);
                    }

                } catch (BossCreationException e) {
                    new VBLogger(getClass().getName(), Level.WARNING, "Could not spawn wither boss").logToFile();
                }
            }
        }
    }

    public static void initializePassiveWithers(){

        for(World w : Vanillabosses.getInstance().getServer().getWorlds()){
            w.getEntities().stream()
                    .filter(n -> n.getScoreboardTags().contains("PassiveWither"))
                    .forEach(n -> WitherBoss.passiveWitherList.add(n.getUniqueId()));
        }

        for(UUID uuid : WitherBoss.passiveWitherList){

            Entity entity = Vanillabosses.getInstance().getServer().getEntity(uuid);
            if(entity instanceof Wither && entity.getScoreboardTags().contains("PassiveWither")){
                Wither wither = (Wither) entity;

                Bukkit.getScheduler().runTaskTimer(Vanillabosses.getInstance(), () -> {

                    WitherBoss.passiveWitherTarget(wither);

                }, 20, 15);
            }
        }
    }

    public void onHitAbility(EntityDamageByEntityEvent event){

        FileConfiguration config = Vanillabosses.getInstance().getConfig();

        if (event.getEntity().getScoreboardTags().contains("BossWither")) {

            if (!(event.getEntityType().equals(EntityType.WITHER))) return;

            if (event.getDamager().getType().equals(EntityType.SPECTRAL_ARROW)) {
                event.setDamage(event.getDamage() * config.getDouble("Bosses.WitherBoss.onHitEvents.spectralArrowDamageMultiplier"));
            }
        }
    }

}
