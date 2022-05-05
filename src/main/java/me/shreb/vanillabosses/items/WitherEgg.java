package me.shreb.vanillabosses.items;

import me.shreb.vanillabosses.Vanillabosses;
import me.shreb.vanillabosses.items.utility.ItemAbilityNotFoundException;
import me.shreb.vanillabosses.items.utility.ItemCreationException;
import me.shreb.vanillabosses.logging.VBLogger;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class WitherEgg extends VBItem {

    public static WitherEgg instance = new WitherEgg();
    public static List<UUID> passiveWitherList = new ArrayList<>();

    public static NamespacedKey PASSIVE_WITHER_PDC_KEY = new NamespacedKey(Vanillabosses.getInstance(), "PassiveWither");

    //TODO Test the passive wither spawning and abilities

    public WitherEgg() {
        this.pdcKey = new NamespacedKey(Vanillabosses.getInstance(), "WitherEgg");
        this.configSection = "WitherEgg";
        try {
            this.itemMaterial = Material.valueOf(config.getString("Items." + this.configSection + ".itemMaterial").toUpperCase());
        } catch (NullPointerException | IllegalArgumentException e) {
            new VBLogger(getClass().getName(), Level.SEVERE, "Unable to convert configuration of the Witheregg to a Material. Found: " + config.getString("Items." + this.configSection + ".itemMaterial")).logToFile();
            return;
        }
        this.lore = new ArrayList<>();
        this.lore.add("What will hatch from this?");
        this.lore.add("Place on an Anvil to find out!");

        this.itemName = Vanillabosses.getCurrentLanguage().itemWitherEggName;
        this.itemGivenMessage = Vanillabosses.getCurrentLanguage().itemWitherEggGivenMessage;
    }


    @Override
    public ItemStack makeItem() throws ItemCreationException {
        ItemStack witherEgg = new ItemStack(this.itemMaterial);
        ItemMeta meta = witherEgg.getItemMeta();
        ArrayList<String> lore = new ArrayList<>(this.lore);
        meta.setDisplayName(ChatColor.DARK_GRAY + this.itemName);
        meta.setLore(lore);
        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(this.pdcKey, PersistentDataType.STRING, "WitherEgg");
        container.set(VBItem.VBItemKey, PersistentDataType.STRING, "WitherEgg");
        witherEgg.setItemMeta(meta);

        return witherEgg;
    }

    @Override
    public ItemStack makeItem(int amount) throws ItemCreationException {
        ItemStack witherEgg = new ItemStack(this.itemMaterial);
        ItemMeta meta = witherEgg.getItemMeta();
        ArrayList<String> lore = new ArrayList<>(this.lore);
        meta.setDisplayName(ChatColor.DARK_GRAY + this.itemName);
        meta.setLore(lore);
        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(this.pdcKey, PersistentDataType.STRING, "WitherEgg");
        container.set(VBItem.VBItemKey, PersistentDataType.STRING, "WitherEgg");
        witherEgg.setItemMeta(meta);

        return witherEgg;
    }

    @Override
    public void registerListener() {
        pluginManager.registerEvents(this, Vanillabosses.getInstance());
    }

    @Override
    public void itemAbility(LivingEntity entity) {

        throw new ItemAbilityNotFoundException("Could not invoke itemAbility on LivingEntity for Slingshot.");

    }

    @EventHandler
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


    public static void passiveWitherTarget(Wither wither) {

        FileConfiguration config = Vanillabosses.getInstance().getConfig();

        if (wither.getHealth() < 0.001) return;

        int range = config.getInt("Items.WitherEgg.arrowRange");

        List<Entity> targetList = wither.getNearbyEntities(range, range, range)
                .stream()
                .filter(n -> n instanceof LivingEntity)
                .filter(n -> n instanceof Monster)
                .filter(wither::hasLineOfSight)
                .collect(Collectors.toList());

        if (targetList.isEmpty()) return;

        LivingEntity target = (LivingEntity) targetList.get(0);
        if (target.isDead()) return;

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

    public static void initializePassiveWithers() {

        for (World w : Vanillabosses.getInstance().getServer().getWorlds()) {
            w.getEntities().stream()
                    .filter(n -> n.getScoreboardTags().contains("PassiveWither") || n.getPersistentDataContainer().has(WitherEgg.PASSIVE_WITHER_PDC_KEY, PersistentDataType.STRING))
                    .forEach(n -> passiveWitherList.add(n.getUniqueId()));
        }

        for (UUID uuid : passiveWitherList) {

            Entity entity = Vanillabosses.getInstance().getServer().getEntity(uuid);
            if (entity instanceof Wither && entity.getScoreboardTags().contains("PassiveWither")) {
                Wither wither = (Wither) entity;

                Bukkit.getScheduler().runTaskTimer(Vanillabosses.getInstance(), () -> {

                    passiveWitherTarget(wither);

                }, 20, 15);
            }
        }
    }

    /**
     * This is what will happen once an Arrow from a Passive Wither hits a target.
     *
     * @param event the event to check for arrows fired by passive withers in
     */
    @EventHandler
    public void onWitherArrowHit(EntityDamageByEntityEvent event) {

        if (!(event.getDamager() instanceof Arrow)
                || !event.getDamager().getPersistentDataContainer().has(new NamespacedKey(Vanillabosses.getInstance(), "targetUUID"), PersistentDataType.STRING)) {
            return;
        }

        if (event.getEntity() instanceof HumanEntity || !(event.getEntity() instanceof Monster))
            event.getDamager().setGravity(true);

        if ((!event.getEntity().getUniqueId().toString().equals(event.getDamager().getPersistentDataContainer().get(new NamespacedKey(Vanillabosses.getInstance(), "targetUUID"), PersistentDataType.STRING))
                && !(event.getEntity() instanceof Monster))
                || event.getEntity().getUniqueId().toString().equals(event.getDamager().getPersistentDataContainer().get(new NamespacedKey(Vanillabosses.getInstance(), "shooterUUID"), PersistentDataType.STRING))
        ) {
            event.setCancelled(true);
            event.setDamage(0.05 * ((Arrow) event.getDamager()).getDamage());
            event.getDamager().setGravity(true);
        }
    }
}