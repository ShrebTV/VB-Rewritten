package me.shreb.vanillabosses.items;

import me.shreb.vanillabosses.Vanillabosses;
import me.shreb.vanillabosses.items.utility.ItemAbilityNotFoundException;
import me.shreb.vanillabosses.items.utility.ItemCreationException;
import me.shreb.vanillabosses.logging.VBLogger;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Level;

public class Slingshot extends VBItem {

    public static Slingshot instance = new Slingshot();
    public static HashMap<UUID, Long> fallDamageTags = new HashMap<>();
    public static long FALL_DAMAGE_TAG_TIMEOUT = config.getInt("Items.Slingshot.antiFallDamageTime");

    public Slingshot() {
        this.pdcKey = new NamespacedKey(Vanillabosses.getInstance(), "Slingshot");
        this.configSection = "Slingshot";
        try {
            this.itemMaterial = Material.valueOf(config.getString("Items.Slingshot.itemMaterial").toUpperCase());
        } catch (NullPointerException | IllegalArgumentException e) {
            new VBLogger(getClass().getName(), Level.SEVERE, "Unable to convert configuration of the Slingshot to a hoe. Found: " + config.getString("Items.Slingshot.itemMaterial")).logToFile();
            return;
        }
        this.lore = (ArrayList<String>) config.getStringList("Items." + this.configSection + ".Lore");
        this.itemName = Vanillabosses.getCurrentLanguage().itemSlingshotName;
        this.itemGivenMessage = Vanillabosses.getCurrentLanguage().itemSlingshotGivenMessage;
    }


    @Override
    public ItemStack makeItem() throws ItemCreationException {
        ItemStack slingshot = new ItemStack(this.itemMaterial);
        ItemMeta meta = slingshot.getItemMeta();
        ArrayList<String> lore = new ArrayList<>(this.lore);
        meta.setDisplayName(ChatColor.DARK_RED + Vanillabosses.getCurrentLanguage().itemSlingshotName);
        meta.setLore(lore);
        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(this.pdcKey, PersistentDataType.INTEGER, 1);
        slingshot.setItemMeta(meta);

        return slingshot;
    }

    @Override
    public ItemStack makeItem(int amount) throws ItemCreationException {
        ItemStack slingshot = new ItemStack(this.itemMaterial);
        ItemMeta meta = slingshot.getItemMeta();
        ArrayList<String> lore = new ArrayList<>(this.lore);
        meta.setDisplayName(ChatColor.DARK_RED + Vanillabosses.getCurrentLanguage().itemSlingshotName);
        meta.setLore(lore);
        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(this.pdcKey, PersistentDataType.INTEGER, 1);
        slingshot.setItemMeta(meta);

        return slingshot;
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
    @Override
    <T extends Event> void itemAbility(T e) {
        if (!(e instanceof PlayerInteractEvent)) return;

        PlayerInteractEvent event = (PlayerInteractEvent) e;

        boolean ret = (!event.getAction().equals(Action.RIGHT_CLICK_AIR) && !event.getAction().equals(Action.RIGHT_CLICK_BLOCK))
                || event.getItem() == null
                || !event.getItem().hasItemMeta()
                || !event.getPlayer().isSneaking();

        if (ret) return;

        if (event.getItem().getItemMeta().getPersistentDataContainer().has(this.pdcKey, PersistentDataType.INTEGER)) {
            event.getPlayer().getScoreboardTags().add("NoFallDMG");
            fallDamageTags.put(event.getPlayer().getUniqueId(), System.currentTimeMillis());

            double multiplier = Vanillabosses.getInstance().getConfig().getDouble("Items.slingshot.thrustMultiplier");
            Vector v = event.getPlayer().getLocation().getDirection();
            double x = v.getX() * multiplier;
            double y = v.getY() * multiplier;
            double z = v.getZ() * multiplier;
            v = new Vector(x, y, z);
            event.getPlayer().setVelocity(v);
            if (Vanillabosses.getInstance().getConfig().getBoolean("Items.slingshot.enableBoostSound")) {
                event.getPlayer().getWorld().playSound(event.getPlayer().getLocation(), Sound.ENTITY_PARROT_IMITATE_SPIDER, (float) Vanillabosses.getInstance().getConfig().getDouble("Items.slingshot.boostSoundVolume"), 0.5F);
            }
            if (Vanillabosses.getInstance().getConfig().getBoolean("Items.slingshot.enableDamagingOnUse")) {
                ItemStack item = event.getItem();
                ItemMeta meta = item.getItemMeta();

                if (((Damageable) meta).getDamage() + Vanillabosses.getInstance().getConfig().getInt("Items.slingshot.damageOnUseAmount") > item.getType().getMaxDurability()) {      //Item breaking upon reaching 0 Durability
                    event.getPlayer().getEquipment().getItem(event.getHand()).setAmount(event.getPlayer().getEquipment().getItem(event.getHand()).getAmount() - 1);
                    event.getPlayer().getWorld().playSound(event.getPlayer().getLocation(), Sound.ENTITY_ITEM_BREAK, 1F, 1F);
                    return;
                }
                ((Damageable) meta).setDamage(((Damageable) meta).getDamage() + Vanillabosses.getInstance().getConfig().getInt("Items.slingshot.damageOnUseAmount"));
                item.setItemMeta(meta);
            }
        }
    }


    /**
     * Cancels the event if the Player in question has used a Slingshot within the configurable amount of time.
     * If they have, their fall damage tag is removed so that they will take fall damage again.
     * If they have not the tag is left inside the map and fall damage is not cancelled
     *
     * @param event The event to check the player for a tag in
     */
    @EventHandler
    public void onFallDamage(EntityDamageEvent event) {

        if (!(event.getEntity() instanceof Player)
                || !event.getCause().equals(EntityDamageEvent.DamageCause.FALL)) return;

        UUID entityID = event.getEntity().getUniqueId();

        if (fallDamageTags.containsKey(entityID)) {

            long currentTime = System.currentTimeMillis();
            long tagTime = fallDamageTags.get(entityID);

            if ((currentTime - tagTime) > 1000 * FALL_DAMAGE_TAG_TIMEOUT) {
                return;
            }

            fallDamageTags.remove(entityID);

            event.setCancelled(true);
        }
    }
}
