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

public class BaseballBat extends VBItem implements BossWeapon {

    public static BaseballBat instance = new BaseballBat();

    public BaseballBat() {
        this.pdcKey = new NamespacedKey(Vanillabosses.getInstance(), "BaseballBat");
        new FileCreator().createAndLoad(FileCreator.baseballBatPath, this.configuration);
        this.configSection = "BaseballBat";
        this.itemMaterial = Material.WOODEN_SWORD;
        this.lore = (ArrayList<String>) this.configuration.getStringList("Lore");
        this.itemName = Vanillabosses.getCurrentLanguage().itemBaseballBatName;
        this.itemGivenMessage = Vanillabosses.getCurrentLanguage().itemBaseballBatGivenMessage;
    }

    @Override
    public ItemStack makeItem() throws ItemCreationException {
        ItemStack baseballBat = new ItemStack(Material.WOODEN_SWORD);
        ItemMeta meta = baseballBat.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        meta.setDisplayName(ChatColor.LIGHT_PURPLE + Vanillabosses.getCurrentLanguage().itemBaseballBatName);

        PersistentDataContainer container = meta.getPersistentDataContainer();

        container.set(this.pdcKey, PersistentDataType.STRING, "Concuss I");
        container.set(VBItem.VBItemKey, PersistentDataType.STRING, "BaseballBat");

        lore.add("Concuss I");
        lore.addAll(this.lore);

        meta.setLore(lore);

        baseballBat.setItemMeta(meta);

        baseballBat.addUnsafeEnchantment(Enchantment.KNOCKBACK, 4);
        baseballBat.addUnsafeEnchantment(Enchantment.DURABILITY, 3);
        baseballBat.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 3);

        return baseballBat;
    }

    @Override
    public ItemStack makeItem(int amount) throws ItemCreationException{

        ItemStack baseballBat = new ItemStack(Material.WOODEN_SWORD);
        ItemMeta meta = baseballBat.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        meta.setDisplayName(ChatColor.LIGHT_PURPLE + Vanillabosses.getCurrentLanguage().itemBaseballBatName);

        PersistentDataContainer container = meta.getPersistentDataContainer();

        container.set(this.pdcKey, PersistentDataType.STRING, "Concuss I");
        container.set(VBItem.VBItemKey, PersistentDataType.STRING, "BaseballBat");

        lore.add("Concuss I");
        lore.addAll(this.lore);

        meta.setLore(lore);

        baseballBat.setItemMeta(meta);

        baseballBat.addUnsafeEnchantment(Enchantment.KNOCKBACK, 4);
        baseballBat.addUnsafeEnchantment(Enchantment.DURABILITY, 3);
        baseballBat.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 3);

        return baseballBat;
    }

    @Override
    public void registerListener() {
        pluginManager.registerEvents(new BaseballBat(), Vanillabosses.getInstance());
    }

    /**
     *
     * Effect of this item: Blindness, "Concuss"
     *
     * @param entity The entity to apply the effect of the Baseball bat to
     */
    @Override
    public void itemAbility(LivingEntity entity) {
        if (Utility.roll(configuration.getDouble("chanceToConcuss"))) {
            entity.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20 * configuration.getInt("concussionDuration"), 2));
            Utility.spawnParticles(Particle.FIREWORKS_SPARK, entity.getWorld(), entity.getLocation(), 1, 1, 1, 20, 3);
        }
    }

    @EventHandler
    public void itemAbility(final EntityDamageByEntityEvent event) {

        boolean hasPluginItemInHand = event.getDamager() instanceof LivingEntity
                && ((LivingEntity) event.getDamager()).getEquipment() != null
                && ((LivingEntity) event.getDamager()).getEquipment().getItemInMainHand().getType() != Material.AIR
                && ((LivingEntity) event.getDamager()).getEquipment().getItemInMainHand().hasItemMeta()
                && ((LivingEntity) event.getDamager()).getEquipment().getItemInMainHand().getItemMeta().getPersistentDataContainer().has(this.pdcKey, PersistentDataType.STRING);

        if(hasPluginItemInHand) {

            ItemStack stack = ((LivingEntity) event.getDamager()).getEquipment().getItemInMainHand();

            if (!this.cooldownsetter.checkCooldown(stack)) return;

            try {
                new ItemDataRetriever(stack);
            } catch (ItemCreationException itemCreationException) {
                new VBLogger(getClass().getName(), Level.WARNING, "An Error has occurred. The item Baseball bat was identified by PDC. But didn't match any Item materials. \n" +
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
            new VBLogger(getClass().getName(), Level.WARNING, "Could not equip BaseBallBat. Exception: " + e).logToFile();
        }

    }
}
