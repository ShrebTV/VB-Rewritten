package me.shreb.vanillabosses.items;

import me.shreb.vanillabosses.Vanillabosses;
import me.shreb.vanillabosses.items.utility.ItemCreationException;
import me.shreb.vanillabosses.logging.VBLogger;
import org.bukkit.*;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.logging.Level;

public class SlimeBoots extends VBItem {

    public static SlimeBoots instance = new SlimeBoots();

    public SlimeBoots(){
        this.pdcKey = new NamespacedKey(Vanillabosses.getInstance(), "SlimeBoots");
        this.configSection = "SlimeBoots";
        try {
            this.itemMaterial = Material.valueOf(config.getString("Items.SlimeBoots.itemMaterial").toUpperCase());
        } catch (NullPointerException | IllegalArgumentException e) {
            new VBLogger(getClass().getName(), Level.SEVERE, "Unable to convert configuration of the Slime boots to actual boots. Found: " + config.getString("Items.SlimeBoots.itemMaterial")).logToFile();
            return;
        }
        this.lore = (ArrayList<String>) config.getStringList("Items." + this.configSection + ".Lore");
        this.itemName = Vanillabosses.getCurrentLanguage().itemSlimeBootsName;
        this.itemGivenMessage = Vanillabosses.getCurrentLanguage().itemSlimeBootsGivenMessage;
    }

    @Override
    public ItemStack makeItem() throws ItemCreationException {
        ItemStack slimeBoots = new ItemStack(this.itemMaterial);

        if(itemMaterial == Material.LEATHER_BOOTS){
            LeatherArmorMeta meta = (LeatherArmorMeta) slimeBoots.getItemMeta();
            meta.setColor(Color.GREEN);
            slimeBoots.setItemMeta(meta);
        }

        ItemMeta meta = slimeBoots.getItemMeta();
        meta.setDisplayName(ChatColor.DARK_GREEN + Vanillabosses.getCurrentLanguage().itemSlimeBootsName);
        ArrayList<String> lore = new ArrayList<>(this.lore);
        meta.setLore(lore);
        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(this.pdcKey, PersistentDataType.STRING, "negateFallDamage");
        slimeBoots.setItemMeta(meta);

        return slimeBoots;
    }

    @Override
    public ItemStack makeItem(int amount) throws ItemCreationException {
        ItemStack slimeBoots = new ItemStack(this.itemMaterial, amount);

        if(itemMaterial == Material.LEATHER_BOOTS){
            LeatherArmorMeta meta = (LeatherArmorMeta) slimeBoots.getItemMeta();
            meta.setColor(Color.GREEN);
            slimeBoots.setItemMeta(meta);
        }

        ItemMeta meta = slimeBoots.getItemMeta();
        meta.setDisplayName(ChatColor.DARK_GREEN + Vanillabosses.getCurrentLanguage().itemSlimeBootsName);
        ArrayList<String> lore = new ArrayList<>(this.lore);
        meta.setLore(lore);
        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(this.pdcKey, PersistentDataType.STRING, "negateFallDamage");
        slimeBoots.setItemMeta(meta);

        return slimeBoots;
    }

    @Override
    public void registerListener() {
        pluginManager.registerEvents(this, Vanillabosses.getInstance());
    }

    @Override
    public void itemAbility(LivingEntity entity) {

    }

    @EventHandler
    void itemAbility(final EntityDamageEvent event) {

        if(!(event.getEntity() instanceof Player)) return;

        if(((Player)event.getEntity()).getEquipment().getBoots() != null){
            if(((Player)event.getEntity()).getEquipment().getBoots().getItemMeta().getPersistentDataContainer().has(pdcKey, PersistentDataType.STRING)){

                double fallDamageMultiplier = config.getDouble("Items.SlimeBoots.fallDamageMultiplier");

                if(fallDamageMultiplier > 1 || fallDamageMultiplier < 0){
                    new VBLogger(getClass().getName(),
                            Level.WARNING,
                            "Fall damage multiplier had a bad value for the Slime boots ability. Please keep it between 0 and 1. Current multiplier: " + fallDamageMultiplier)
                            .logToFile();
                }

                event.setDamage(event.getFinalDamage() * fallDamageMultiplier);

                int damageOnUse = (int)(event.getDamage() * Vanillabosses.getInstance().getConfig().getInt("Items.SlimeBoots.damageOnUseMultiplier"));

                ItemMeta meta = ((Player)event.getEntity()).getEquipment().getBoots().getItemMeta();
                ((Damageable)meta).setDamage(((Damageable)meta).getDamage() + damageOnUse);
                ((Player)event.getEntity()).getEquipment().getBoots().setItemMeta(meta);

                ((Player) event.getEntity()).playSound(event.getEntity().getLocation(), Sound.ENTITY_SLIME_SQUISH, 1f, 1f);
                ((Player) event.getEntity()).spawnParticle(Particle.SLIME, event.getEntity().getLocation(), 100, 2, 1, 2);

                if(((Damageable) meta).getDamage() > this.itemMaterial.getMaxDurability()){
                    ((Player)event.getEntity()).getEquipment().setBoots(new ItemStack(Material.AIR));
                    event.getEntity().getWorld().playSound(event.getEntity().getLocation(), Sound.ENTITY_ITEM_BREAK, 1F, 1F);
                }
            }
        }
    }
}
