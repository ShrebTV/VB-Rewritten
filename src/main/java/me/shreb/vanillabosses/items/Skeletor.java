package me.shreb.vanillabosses.items;

import me.shreb.vanillabosses.Vanillabosses;
import me.shreb.vanillabosses.bosses.CreeperBoss;
import me.shreb.vanillabosses.items.utility.ItemCreationException;
import me.shreb.vanillabosses.logging.VBLogger;
import me.shreb.vanillabosses.utility.Utility;
import me.shreb.vanillabosses.utility.configFiles.FileCreator;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.logging.Level;

public class Skeletor extends VBItem implements BossWeapon {

    public static Skeletor instance = new Skeletor();

    public Skeletor() {
        this.pdcKey = new NamespacedKey(Vanillabosses.getInstance(), "Skeletor");
        this.configSection = "Skeletor";
        new FileCreator().createAndLoad(FileCreator.skeletorPath, this.configuration);
        this.itemMaterial = Material.BOW;
        this.lore = (ArrayList<String>) this.configuration.getStringList("Lore");
        this.itemName = Vanillabosses.getCurrentLanguage().itemSkeletorName;
        this.itemGivenMessage = Vanillabosses.getCurrentLanguage().itemSkeletorGivenMessage;
    }


    @Override
    public ItemStack makeItem() throws ItemCreationException {
        ItemStack bow = new ItemStack(this.itemMaterial);

        ItemMeta meta = bow.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD + "Skeletor");
        ArrayList<String> lore = new ArrayList<>(this.lore);

        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(this.pdcKey, PersistentDataType.STRING, "Shoots TNT");
        container.set(VBItem.VBItemKey, PersistentDataType.STRING, "Skeletor");
        meta.setLore(lore);
        bow.setItemMeta(meta);

        return bow;
    }

    @Override
    public ItemStack makeItem(int amount) throws ItemCreationException {
        ItemStack bow = new ItemStack(this.itemMaterial, amount);

        ItemMeta meta = bow.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD + "Skeletor");
        ArrayList<String> lore = new ArrayList<>(this.lore);

        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(this.pdcKey, PersistentDataType.STRING, "Shoots TNT");
        container.set(VBItem.VBItemKey, PersistentDataType.STRING, "Skeletor");
        meta.setLore(lore);
        bow.setItemMeta(meta);

        return bow;
    }

    @Override
    public void registerListener() {
        pluginManager.registerEvents(this, Vanillabosses.getInstance());
        pluginManager.registerEvents(new SkeletorArrow(), Vanillabosses.getInstance());
    }

    @Override
    public void itemAbility(LivingEntity entity) {

    }

    @Override
    public void equipWeapon(LivingEntity entity) {
        EntityEquipment equipment = entity.getEquipment();

        if(equipment == null) return;

        try {
            equipment.setItemInMainHand(makeItem());
        } catch (ItemCreationException e) {
            new VBLogger(getClass().getName(), Level.WARNING, "Could not equip Skeletor. Exception: " + e).logToFile();
        }

    }

    static class SkeletorArrow implements Listener {

        public static final String EXPLODING_ARROW = "Vanilla Bosses - Exploding Arrow";
        public static final String CANCEL_TNT_EXPLOSION = "Vanilla Bosses - Cancel Explosion";

        /**
         * Edits a normal arrow to have the tag needed for the Skeletor ability
         *
         * @param event the event to edit the arrow inside of
         */
        @EventHandler
        public void makeSkeltorArrow(ProjectileLaunchEvent event) {

            if (!(event.getEntity().getShooter() instanceof LivingEntity)) return;

            LivingEntity shooter = (LivingEntity) event.getEntity().getShooter();
            Projectile projectile = event.getEntity();

            //make sure the projectile is an arrow
            boolean isArrow = event.getEntity() instanceof Arrow;

            //make sure the shooter of the projectile is a Living Entity and has a skeletor in their main hand
            boolean shouldEdit = shooter != null
                    && shooter.getEquipment() != null
                    && shooter.getEquipment().getItemInMainHand().getType() != Material.AIR
                    && shooter.getEquipment().getItemInMainHand().getItemMeta() != null
                    && shooter.getEquipment().getItemInMainHand().getItemMeta().getPersistentDataContainer().has(Skeletor.instance.pdcKey, PersistentDataType.STRING);

            if (isArrow) {
                Arrow arrow = (Arrow) projectile;

                if (shouldEdit) {
                    arrow.getScoreboardTags().add(EXPLODING_ARROW);
                }
            }
        }

        /**
         * Edits an arrow from a projectile launch event to a primed tnt block with the same vector and velocity
         *
         * @param event the event to edit the arrow inside of
         */
        @EventHandler
        public void editArrowToTNT(ProjectileLaunchEvent event) {

            if (!Skeletor.instance.configuration.getBoolean("ShootTNTFromOffHand.enabled")) return;

            boolean isArrow = event.getEntity() instanceof Arrow;

            boolean shooterIsAlive = event.getEntity().getShooter() instanceof LivingEntity;

            if (isArrow && shooterIsAlive) {

                Arrow arrow = (Arrow) event.getEntity();
                LivingEntity shooter = (LivingEntity) event.getEntity().getShooter();

                boolean shooterHoldingSkeletorMain = shooter != null
                        && shooter.getEquipment() != null
                        && shooter.getEquipment().getItemInMainHand().getType() != Material.AIR
                        && shooter.getEquipment().getItemInMainHand().getItemMeta() != null
                        && shooter.getEquipment().getItemInMainHand().getItemMeta().getPersistentDataContainer().has(Skeletor.instance.pdcKey, PersistentDataType.STRING);

                if (!shooterHoldingSkeletorMain) return;

                boolean shooterHoldingTNTOffHand = (shooter.getEquipment().getItemInOffHand().getType() == Material.TNT);

                if (shooterHoldingTNTOffHand) {

                    if (shooter instanceof Player && ((Player) shooter).getGameMode() == GameMode.SURVIVAL) {
                        //decrease amount of TNT in off hand only if the shooter is a player and they are in survival mode
                        shooter.getEquipment().getItemInOffHand().setAmount(shooter.getEquipment().getItemInOffHand().getAmount() - 1);
                    }

                    int fuseTicks = Skeletor.instance.configuration.getInt("ShootTNTFromOffHand.TNTTimer");
                    double tntYield = Skeletor.instance.configuration.getInt("ShootTNTFromOffHand.TNTYield");
                    boolean cancelExplosion = Skeletor.instance.configuration.getBoolean("ShootTNTFromOffHand.TNTDoesNoBlockDamage");

                    Vector vector = arrow.getVelocity();

                    arrow.remove();

                    TNTPrimed tnt = (TNTPrimed) arrow.getLocation().getWorld().spawnEntity(arrow.getLocation(), EntityType.PRIMED_TNT);

                    if (fuseTicks < 1) {
                        new VBLogger(getClass().getName(), Level.WARNING, "Skeletor TNT Fuse ticks were set too low. 20 ticks is one second, cannot be less than 1 tick. Ticks: " + fuseTicks).logToFile();
                    } else {
                        tnt.setFuseTicks(fuseTicks);
                    }

                    if (tntYield < 0) {
                        new VBLogger(getClass().getName(), Level.WARNING, "Skeletor TNT yield was set too low. Cannot be less than 0. Yield: " + tntYield).logToFile();
                    } else {
                        tnt.setYield((float) tntYield);
                    }

                    if (cancelExplosion) {
                        tnt.getScoreboardTags().add(CANCEL_TNT_EXPLOSION);
                        tnt.getScoreboardTags().add(CreeperBoss.CANCEL_BLOWUP_ITEMS);
                    }

                    tnt.setVelocity(vector);
                }
            }
        }

        /**
         * An event to listen to the ProjectileHitEvent in order to summon tnt on entities hit by a SkeletorArrow
         * and edit it to the correct Attributes from the config
         *
         * @param event the event to spawn the tnt inside of
         */
        @EventHandler
        public void onExplodingArrowHit(EntityDamageByEntityEvent event) {

            if (!Skeletor.instance.configuration.getBoolean("TNTOnArrowHit.enable")
                    || !(event.getDamager() instanceof Arrow)) return;

            Arrow arrow = (Arrow) event.getDamager();

            boolean isSkeletorArrow = arrow.getScoreboardTags().contains(EXPLODING_ARROW);

            boolean hitLivingEntity = event.getEntity() instanceof LivingEntity;

            if (isSkeletorArrow && hitLivingEntity) {

                arrow.setDamage(0);
                arrow.setFireTicks(1);
                arrow.remove();

                Location location = arrow.getLocation();

                if (location.getWorld() == null) return;

                TNTPrimed tnt = (TNTPrimed) location.getWorld().spawnEntity(location, EntityType.PRIMED_TNT);

                int fuseTicks = Skeletor.instance.configuration.getInt("TNTOnArrowHit.TNTTimer");
                double tntYield = Skeletor.instance.configuration.getDouble("TNTOnArrowHit.TNTYield");
                boolean cancelTNT = Skeletor.instance.configuration.getBoolean("TNTOnArrowHit.TNTDoesNoBlockDamage");

                if (fuseTicks < 1) {
                    new VBLogger(getClass().getName(), Level.WARNING, "Skeletor TNT Fuse ticks were set too low. 20 ticks is one second, cannot be less than 1 tick. Ticks: " + fuseTicks).logToFile();
                } else {
                    tnt.setFuseTicks(fuseTicks);
                }

                if (tntYield < 0) {
                    new VBLogger(getClass().getName(), Level.WARNING, "Skeletor TNT yield was set too low. Cannot be less than 0. Yield: " + tntYield).logToFile();
                } else {
                    tnt.setYield((float) tntYield);
                }

                Utility.spawnParticles(Particle.FLAME, event.getEntity().getWorld(), event.getEntity().getLocation(), 2, 1, 2, 5, 2);

                if (cancelTNT) {
                    tnt.getScoreboardTags().add(CANCEL_TNT_EXPLOSION);
                    tnt.getScoreboardTags().add(CreeperBoss.CANCEL_BLOWUP_ITEMS);
                }
            }
        }

        /**
         * Cancels the explosion of primed tnt if it has the scoreboardTag intended for this
         *
         * @param event the event to cancel the explosion in
         */
        @EventHandler
        public void onTNTExplosion(EntityExplodeEvent event) {

            boolean isCancelledTNT = event.getEntity() instanceof TNTPrimed
                    && event.getEntity().getScoreboardTags().contains(CANCEL_TNT_EXPLOSION);

            if (isCancelledTNT) {
                event.setCancelled(true);
                event.getEntity().getWorld().playSound(event.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 2f, 1f);
            }
        }
    }
}