package me.shreb.vanillabosses.items;

import me.shreb.vanillabosses.Vanillabosses;
import me.shreb.vanillabosses.items.utility.ItemCreationException;
import me.shreb.vanillabosses.logging.VBLogger;
import me.shreb.vanillabosses.utility.Utility;
import me.shreb.vanillabosses.utility.configFiles.FileCreator;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.logging.Level;

public class HeatedMagmaCream extends VBItem {

    public static HeatedMagmaCream instance = new HeatedMagmaCream();

    public int level;

    public HeatedMagmaCream() {
        this.pdcKey = new NamespacedKey(Vanillabosses.getInstance(), "HeatedMagmaCream");
        this.configSection = "HeatedMagmaCream";
        new FileCreator().createAndLoad(FileCreator.hmcPath, this.configuration);
        this.itemMaterial = Material.MAGMA_CREAM;
        this.lore = (ArrayList<String>) this.configuration.getStringList("Lore");
        this.itemName = Vanillabosses.getCurrentLanguage().itemHMCName;
        this.itemGivenMessage = Vanillabosses.getCurrentLanguage().itemHMCNameGivenMessage;
    }

    public HeatedMagmaCream(int level) {
        this();
        if (level > 3 || level < 1) {
            new VBLogger(getClass().getName(), Level.SEVERE, "Tried to create Heated Magma Cream with a wrong level. Please let the Author know about this.");
            return;
        }
        this.level = level;
    }

    @Override
    public ItemStack makeItem() throws ItemCreationException {
        ItemStack cream = new ItemStack(Material.MAGMA_CREAM);

        switch (level) {

            case 1:
                cream.addUnsafeEnchantment(Enchantment.ARROW_FIRE, 1);
                break;

            case 2:
                cream.addUnsafeEnchantment(Enchantment.ARROW_FIRE, 2);
                break;

            case 3:
                cream.addUnsafeEnchantment(Enchantment.ARROW_FIRE, 3);
                break;

            default:
                throw new IllegalArgumentException(ChatColor.RED + "VanillaBosses: Error with HeatedMagmaCream. Please notify the Author of the plugin about this Error.");
        }
        ItemMeta meta = cream.getItemMeta();
        meta.setDisplayName(Vanillabosses.getCurrentLanguage().itemHMCName + " Lv." + level);
        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(this.pdcKey, PersistentDataType.INTEGER, level);
        container.set(VBItem.VBItemKey, PersistentDataType.STRING, "HMC");

        cream.setItemMeta(meta);

        return cream;
    }

    @Override
    public ItemStack makeItem(int amount) throws ItemCreationException {
        ItemStack cream = new ItemStack(Material.MAGMA_CREAM, amount);

        switch (level) {

            case 1:
                cream.addUnsafeEnchantment(Enchantment.ARROW_FIRE, 1);
                break;

            case 2:
                cream.addUnsafeEnchantment(Enchantment.ARROW_FIRE, 2);
                break;

            case 3:
                cream.addUnsafeEnchantment(Enchantment.ARROW_FIRE, 3);
                break;

            default:
                throw new IllegalArgumentException(ChatColor.RED + "VanillaBosses: Error with HeatedMagmaCream. Please notify the Author of the plugin about this Error.");
        }
        ItemMeta meta = cream.getItemMeta();
        meta.setDisplayName(Vanillabosses.getCurrentLanguage().itemHMCName + " Lv." + level);
        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(this.pdcKey, PersistentDataType.INTEGER, level);
        container.set(VBItem.VBItemKey, PersistentDataType.STRING, "HMC");
        cream.setItemMeta(meta);

        return cream;
    }

    @Override
    public void registerListener() {
        pluginManager.registerEvents(this, Vanillabosses.getInstance());
    }

    @Override
    public void itemAbility(LivingEntity entity) {

    }

    @EventHandler
    void itemAbility(final PlayerInteractEvent event) {

        boolean ret = event.getItem() == null
                || event.getHand() == null
                || (event.getAction() != Action.RIGHT_CLICK_AIR && ((PlayerInteractEvent) event).getAction() != Action.RIGHT_CLICK_BLOCK);

        if (ret) return;

        if (event.getItem().getType() == Material.MAGMA_CREAM
                && (event.getItem().getItemMeta().getPersistentDataContainer().has(this.pdcKey, PersistentDataType.INTEGER))) {

            ItemStack cream = event.getItem();
            Player player = event.getPlayer();
            Location loc = event.getPlayer().getLocation();
            int radius;
            int time;
            int level;


            if (cream.getItemMeta().getPersistentDataContainer().has(pdcKey, PersistentDataType.INTEGER)) {
                level = cream.getItemMeta().getPersistentDataContainer().get(pdcKey, PersistentDataType.INTEGER);
            } else {
                return;
            }

            switch (level) {

                case 1:
                    radius = this.configuration.getInt("Level1.radius");
                    time = this.configuration.getInt("Level1.burnTime");
                    break;

                case 2:
                    radius = this.configuration.getInt("Level2.radius");
                    time = this.configuration.getInt("Level2.burnTime");
                    break;

                case 3:
                    radius = this.configuration.getInt("Level3.radius");
                    time = this.configuration.getInt("Level3.burnTime");
                    break;

                default:
                    new VBLogger(getClass().getName(), Level.WARNING, "Noticed weird level of Heated magma cream. Level: " + level + "\n" +
                            "Cream: " + cream).logToFile();
                    return;
            }

            Utility.spawnParticles(Particle.FLAME, player.getWorld(), loc, radius, radius, radius, 150, 3);

            player.getWorld().playSound(loc, Sound.ENTITY_SLIME_SQUISH, 1.0f, 1.0f);

            for (Entity entity : player.getWorld().getNearbyEntities(loc, radius, radius, radius, n -> n instanceof LivingEntity && n != player)) {
                entity.setFireTicks(20 * time);
            }

            if (player.getGameMode() == GameMode.SURVIVAL) {
                event.getItem().setAmount(event.getItem().getAmount() - 1);
            }
        }
    }

    public static ItemStack replaceHMC(ItemStack stack) {

        if (!stack.hasItemMeta()
                && !stack.getItemMeta().getPersistentDataContainer().has(instance.pdcKey, PersistentDataType.INTEGER))
            return stack;

        int level = stack.getEnchantmentLevel(Enchantment.ARROW_FIRE);

        ItemStack helperStack;

        if (level == 0) return stack;

        int amount = stack.getAmount();
        try {
            helperStack = new HeatedMagmaCream(level).makeItem(amount);
        } catch (ItemCreationException e) {
            new VBLogger("HeatedMagmaCream", Level.SEVERE, "Could not create HMC for some reason. Exception: " + e).logToFile();
            return stack;
        }
        return helperStack;

    }

}
