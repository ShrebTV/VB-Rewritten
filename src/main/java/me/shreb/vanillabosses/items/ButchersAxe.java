package me.shreb.vanillabosses.items;

import me.shreb.vanillabosses.Vanillabosses;
import me.shreb.vanillabosses.items.utility.ItemCreationException;
import me.shreb.vanillabosses.items.utility.ItemDataRetriever;
import me.shreb.vanillabosses.logging.VBLogger;
import me.shreb.vanillabosses.utility.Utility;
import me.shreb.vanillabosses.utility.configFiles.FileCreator;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.logging.Level;

public class ButchersAxe extends VBItem implements BossWeapon{

    public static ButchersAxe instance = new ButchersAxe();

    public ButchersAxe() {
        this.pdcKey = new NamespacedKey(Vanillabosses.getInstance(), "ButchersAxe");
        this.configSection = "ButchersAxe";
        new FileCreator().createAndLoad(FileCreator.butchersAxePath, this.configuration);
        try {
            this.itemMaterial = Material.valueOf(this.configuration.getString("itemMaterial").toUpperCase());
        } catch (NullPointerException | IllegalArgumentException e) {
            new VBLogger(getClass().getName(), Level.SEVERE, "Unable to convert configuration of the Butchers Axe into an actual axe. Found: " + this.configuration.getString("itemMaterial")).logToFile();
            return;
        }
        this.lore = (ArrayList<String>) this.configuration.getStringList("Lore");
        this.itemName = Vanillabosses.getCurrentLanguage().itemButchersAxeName;
        this.itemGivenMessage = Vanillabosses.getCurrentLanguage().itemButchersAxeNameGivenMessage;
    }

    @Override
    public ItemStack makeItem() throws ItemCreationException {
        ItemStack axe = new ItemStack(this.itemMaterial);

        axe.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 5);
        axe.addUnsafeEnchantment(Enchantment.DURABILITY, 3);
        axe.addUnsafeEnchantment(Enchantment.FIRE_ASPECT, 2);

        ItemMeta meta = axe.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(this.pdcKey, PersistentDataType.STRING, "Bind II");
        container.set(VBItem.VBItemKey, PersistentDataType.STRING, "ButchersAxe");

        ArrayList<String> lore = new ArrayList<>();
        meta.setDisplayName(ChatColor.DARK_RED + Vanillabosses.getCurrentLanguage().itemButchersAxeName);
        lore.add("Bind II");
        lore.addAll(this.configuration.getStringList("Lore"));
        meta.setLore(lore);
        axe.setItemMeta(meta);

        return axe;
    }

    @Override
    public ItemStack makeItem(int amount) throws ItemCreationException {
        ItemStack axe = new ItemStack(this.itemMaterial, amount);

        axe.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 5);
        axe.addUnsafeEnchantment(Enchantment.DURABILITY, 3);
        axe.addUnsafeEnchantment(Enchantment.FIRE_ASPECT, 2);

        ItemMeta meta = axe.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(this.pdcKey, PersistentDataType.STRING, "Bind II");
        container.set(VBItem.VBItemKey, PersistentDataType.STRING, "ButchersAxe");

        ArrayList<String> lore = new ArrayList<>();
        meta.setDisplayName(ChatColor.DARK_RED + Vanillabosses.getCurrentLanguage().itemButchersAxeName);
        lore.add("Bind II");
        lore.addAll(this.configuration.getStringList("Lore"));
        meta.setLore(lore);
        axe.setItemMeta(meta);

        return axe;
    }

    @Override
    public void registerListener() {
        pluginManager.registerEvents(this, Vanillabosses.getInstance());
    }

    /**
     * Effect of this item: Slowness, "Bind II"
     *
     * @param entity The entity to apply the effect of the Butchers axe to
     */
    @Override
    public void itemAbility(LivingEntity entity) {
        if (Utility.roll(this.configuration.getDouble("ChanceToApplySlowness"))) {
            entity.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20 * this.configuration.getInt("SlownessDuration"), 2));
            Utility.spawnParticles(Particle.FLASH, entity.getWorld(), entity.getLocation(), 1, 1, 1, 5, 3);
        }
    }

    @EventHandler
    public void itemAbility(final EntityDamageByEntityEvent event) {

        boolean hasPluginItemInHand = event.getDamager() instanceof LivingEntity
                && ((LivingEntity) event.getDamager()).getEquipment() != null
                && ((LivingEntity) event.getDamager()).getEquipment().getItemInMainHand().getType() != Material.AIR
                && ((LivingEntity) event.getDamager()).getEquipment().getItemInMainHand().hasItemMeta()
                && ((LivingEntity) event.getDamager()).getEquipment().getItemInMainHand().getItemMeta().getPersistentDataContainer().has(this.pdcKey, PersistentDataType.STRING);

        if (hasPluginItemInHand) {
            ItemStack stack = ((LivingEntity) event.getDamager()).getEquipment().getItemInMainHand();

            if (!this.cooldownsetter.checkCooldown(stack)) return;

            try {
                new ItemDataRetriever(stack);
            } catch (ItemCreationException itemCreationException) {
                new VBLogger(getClass().getName(), Level.WARNING, "An Error has occurred. The item Butchers Axe was identified by PDC. But didn't match any Item materials. \n" +
                        "Item: " + stack).logToFile();
                return;
            }

            this.itemAbility((LivingEntity) event.getEntity());
        }
    }

    @Override
    public void equipWeapon(LivingEntity entity) {
        EntityEquipment equipment = entity.getEquipment();

        if(equipment == null) return;

        try {
            equipment.setItemInMainHand(makeItem());
        } catch (ItemCreationException e) {
            new VBLogger(getClass().getName(), Level.WARNING, "Could not equip Butchers Axe. Exception: " + e).logToFile();
        }
    }
}
