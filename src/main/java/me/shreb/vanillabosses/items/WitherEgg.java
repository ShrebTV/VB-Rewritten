package me.shreb.vanillabosses.items;

import me.shreb.vanillabosses.Vanillabosses;
import me.shreb.vanillabosses.bosses.VBBoss;
import me.shreb.vanillabosses.bosses.WitherBoss;
import me.shreb.vanillabosses.items.utility.ItemAbilityNotFoundException;
import me.shreb.vanillabosses.items.utility.ItemCreationException;
import me.shreb.vanillabosses.logging.VBLogger;
import me.shreb.vanillabosses.utility.configFiles.FileCreator;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.data.BlockData;
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
import java.util.UUID;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class WitherEgg extends VBItem {

    public static WitherEgg instance = new WitherEgg();
    public static List<UUID> passiveWitherList = new ArrayList<>();

    public static NamespacedKey PASSIVE_WITHER_PDC_KEY = new NamespacedKey(Vanillabosses.getInstance(), "PassiveWither");

    public WitherEgg() {
        this.pdcKey = new NamespacedKey(Vanillabosses.getInstance(), "WitherEgg");
        this.configSection = "WitherEgg";
        new FileCreator().createAndLoad(FileCreator.witherEggPath, this.configuration);
        try {
            this.itemMaterial = Material.valueOf(this.configuration.getString("itemMaterial").toUpperCase());
        } catch (NullPointerException | IllegalArgumentException e) {
            new VBLogger(getClass().getName(), Level.SEVERE, "Unable to convert configuration of the Witheregg to a Material. Found: " + this.configuration.getString("itemMaterial")).logToFile();
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

        if (WitherBoss.checkForWitherEgg(event.getItemInHand())) {

            BlockData data = event.getBlock().getBlockData();

            ArrayList<Material> allowedBlocksToPlaceOn = new ArrayList<>();
            allowedBlocksToPlaceOn.add(Material.ANVIL);
            allowedBlocksToPlaceOn.add(Material.CHIPPED_ANVIL);
            allowedBlocksToPlaceOn.add(Material.DAMAGED_ANVIL);
            Material targetMaterial = event.getBlock().getWorld().getBlockAt(event.getBlock().getLocation().getBlockX(), event.getBlock().getLocation().getBlockY() - 1, event.getBlock().getLocation().getBlockZ()).getType();

            Location eggLoc = event.getBlock().getLocation();
            Location anvilLoc = event.getBlock().getWorld().getBlockAt(event.getBlock().getLocation().getBlockX(), event.getBlock().getLocation().getBlockY() - 1, event.getBlock().getLocation().getBlockZ()).getLocation();

            if (allowedBlocksToPlaceOn.contains(targetMaterial)) {

                Bukkit.getScheduler().scheduleSyncDelayedTask(Vanillabosses.getInstance(), () -> {

                    if (eggLoc.getBlock().getType().equals(Material.DRAGON_EGG)
                            && eggLoc.getBlock().getBlockData().matches(data)
                            && allowedBlocksToPlaceOn.contains(anvilLoc.getBlock().getType())) {

                        Wither wither = (Wither) event.getPlayer().getWorld().spawnEntity(eggLoc, EntityType.WITHER);

                        wither.setCustomName(event.getPlayer().getName() + "s Pet Wither");

                        wither.setAI(false);

                        wither.addScoreboardTag("PassiveWither");

                        wither.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(this.configuration.getDouble("petWitherHP"));
                        wither.setHealth(this.configuration.getDouble("petWitherHP"));

                        eggLoc.getBlock().setType(Material.AIR);
                        anvilLoc.getBlock().setType(Material.AIR);

                        Location loc = wither.getLocation();
                        wither.getWorld().spawnParticle(Particle.CAMPFIRE_SIGNAL_SMOKE, loc, 150);
                        passiveWitherList.add(wither.getUniqueId());

                        Bukkit.getScheduler().runTaskTimer(Vanillabosses.getInstance(), () -> {

                            passiveWitherTarget(wither);

                        }, 20, 15);

                    } else if (eggLoc.getBlock().getType().equals(Material.DRAGON_EGG) && eggLoc.getBlock().getBlockData().matches(data)) {
                        dropWitherEgg(event.getBlock().getWorld(), eggLoc);
                    }

                }, this.configuration.getInt("timeToHatch") * 20L);
            } else if (eggLoc.getBlock().getType().equals(Material.DRAGON_EGG) && eggLoc.getBlock().getBlockData().matches(data)) {
                dropWitherEgg(event.getBlock().getWorld(), eggLoc);
            }
        }
    }

    public static void dropWitherEgg(World world, Location eggLocation) {
        eggLocation.getBlock().setType(Material.AIR);
        ItemStack egg = WitherBoss.makeWitherEgg();
        world.dropItem(eggLocation, egg);
    }


    public static void passiveWitherTarget(Wither wither) {

        if (wither.getHealth() < 0.001) return;

        int range = instance.configuration.getInt("arrowRange");

        List<Entity> targetList = wither.getNearbyEntities(range, range, range)
                .stream()
                .filter(n -> n instanceof LivingEntity)
                .filter(n -> n instanceof Monster)
                .filter(wither::hasLineOfSight)
                .filter(n -> n.getType() != EntityType.WITHER)
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
        arrow.setDamage(instance.configuration.getDouble("arrowDamageMultiplier") * arrow.getDamage());
        arrow.setGravity(false);
        arrow.setPickupStatus(AbstractArrow.PickupStatus.CREATIVE_ONLY);
        arrow.getScoreboardTags().add(VBBoss.REMOVE_ON_DISABLE_TAG);

        arrow.getPersistentDataContainer().set(new NamespacedKey(Vanillabosses.getInstance(), "targetUUID"), PersistentDataType.STRING, target.getUniqueId().toString());
        arrow.getPersistentDataContainer().set(new NamespacedKey(Vanillabosses.getInstance(), "shooterUUID"), PersistentDataType.STRING, wither.getUniqueId().toString());

        Bukkit.getScheduler().runTaskLater(Vanillabosses.getInstance(), arrow::remove, 100);
    }

    public static void initializePassiveWithers() {

        for (World w : Bukkit.getWorlds()) {
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