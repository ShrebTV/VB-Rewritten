package me.shreb.vanillabosses.bosses;

import me.shreb.vanillabosses.Vanillabosses;
import me.shreb.vanillabosses.bosses.bossRepresentation.NormalBoss;
import me.shreb.vanillabosses.bosses.utility.BossCreationException;
import me.shreb.vanillabosses.logging.VBLogger;
import me.shreb.vanillabosses.utility.configFiles.FileCreator;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Wither;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.logging.Level;

public class WitherBoss extends VBBoss {

    public static WitherBoss instance = new WitherBoss();

    public static final String CONFIGSECTION = "WitherBoss";
    public static final String SCOREBOARDTAG = "BossWither";

    public WitherBoss() {
        new FileCreator().createAndLoad(FileCreator.witherBossPath, this.config);
    }

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

        if (!config.getBoolean("enabled")) return entity;

        // checking wether the entity passed in is a Wither. Logging as a warning and throwing an exception if not.
        if (!(entity instanceof Wither)) {
            new VBLogger(getClass().getName(), Level.WARNING, "Attempted to make a Wither boss out of an Entity.\n" +
                    "Entity passed in: " + entity.getType() + "\n" +
                    "Boss could not be created!").logToFile();

            throw new BossCreationException("Attempted to make a boss out of an Entity. Could not make Wither Boss out of this Entity.");
        }

        Bukkit.getScheduler().runTaskLater(Vanillabosses.getInstance(), () -> {

            //getting the Boss Attributes from the config file
            double health = config.getDouble("health");
            String nameColorString = config.getString("displayNameColor");

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
                new VBLogger(getClass().getName(), Level.WARNING, "Could not set Attributes on Wither Boss\n" +
                        "Reason: " + e).logToFile();
            }

        }, 1);

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

    /**
     * The method to make a wither egg with
     *
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
     * The method which is meant to edit a wither spawned over a netherite block into a boss wither.
     *
     * @param event the event to check for a netherite block at the correct position in
     */
    @EventHandler
    public void onWitherSpawn(CreatureSpawnEvent event) {

        if (event.getEntityType().equals(EntityType.WITHER)) {

            if (!config.getBoolean("enabled")) return;

            LivingEntity wither = event.getEntity();

            int x = wither.getLocation().getBlockX();
            int y = wither.getLocation().getBlockY();
            int z = wither.getLocation().getBlockZ();

            if (wither.getWorld().getBlockAt(x, y - 1, z).getType() == Material.NETHERITE_BLOCK) {

                try {
                    makeBoss(wither);

                    if (config.getBoolean("removeNetheriteBlockOnSpawn")) {
                        wither.getWorld().getBlockAt(x, y - 1, z).setType(Material.AIR);
                    }

                } catch (BossCreationException e) {
                    new VBLogger(getClass().getName(), Level.WARNING, "Could not spawn wither boss").logToFile();
                }
            }
        }
    }

    @EventHandler
    public void onHitAbility(EntityDamageByEntityEvent event) {

        if (event.getEntity().getScoreboardTags().contains(SCOREBOARDTAG)) {

            if (!(event.getEntityType().equals(EntityType.WITHER))) return;

            if (event.getDamager().getType().equals(EntityType.SPECTRAL_ARROW)) {
                event.setDamage(event.getDamage() * config.getDouble("onHitEvents.spectralArrowDamageMultiplier"));
            }
        }
    }
}
