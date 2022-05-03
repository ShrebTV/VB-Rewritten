package me.shreb.vanillabosses.items;

import me.shreb.vanillabosses.Vanillabosses;
import me.shreb.vanillabosses.items.utility.ItemCreationException;
import me.shreb.vanillabosses.logging.VBLogger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;

public class InvisibilityCloak extends VBItem {

    public static InvisibilityCloak instance = new InvisibilityCloak();

    public InvisibilityCloak() {
        this.pdcKey = new NamespacedKey(Vanillabosses.getInstance(), "cloakOfInvisibility");
        this.configSection = "cloakOfInvisibility";
        try {
            this.itemMaterial = Material.valueOf(config.getString("Items.cloakOfInvisibility.itemMaterial").toUpperCase());
        } catch (NullPointerException | IllegalArgumentException e) {
            new VBLogger(getClass().getName(), Level.SEVERE, "Unable to convert configuration of the Invisibility cloak into an actual chestplate. Found: " + config.getString("Items.cloakOfInvisibility.itemMaterial")).logToFile();
            return;
        }
        this.lore = (ArrayList<String>) config.getStringList("Items.cloakOfInvisibility.Lore");
    }

    @Override
    public ItemStack makeItem() throws ItemCreationException {
        ItemStack cloak = new ItemStack(this.itemMaterial);
        ItemMeta meta = cloak.getItemMeta();
        ArrayList<String> lore = new ArrayList<>(this.lore);
        meta.setDisplayName(ChatColor.GRAY + Vanillabosses.getCurrentLanguage().itemInvisibilityCloakName);
        meta.setLore(lore);
        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(this.pdcKey, PersistentDataType.INTEGER, 1);

        cloak.setItemMeta(meta);

        return cloak;
    }

    @Override
    public ItemStack makeItem(int amount) throws ItemCreationException {
        ItemStack cloak = new ItemStack(this.itemMaterial, amount);
        ItemMeta meta = cloak.getItemMeta();
        ArrayList<String> lore = new ArrayList<>(this.lore);
        meta.setDisplayName(ChatColor.GRAY + Vanillabosses.getCurrentLanguage().itemInvisibilityCloakName);
        meta.setLore(lore);
        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(this.pdcKey, PersistentDataType.INTEGER, 1);

        cloak.setItemMeta(meta);

        return cloak;
    }

    //saves the Entity UUID along with the task id of the task timer assigned to them

    @Override
    public void itemAbility(LivingEntity entity) {

        int time = config.getInt("Items.cloakOfInvisibility.delayBetweenChecks");

        if (time < 2) {
            new VBLogger(getClass().getName(), Level.WARNING, "Invisibility time is too short. Please set it to 2 or more seconds").logToFile();
            return;
        }

        entity.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, (time * 20) + 10, 1));

    }

    @Override
    <T extends Event> void itemAbility(T e) {

        new VBLogger(getClass().getName(), Level.WARNING, "tried to invoke item Ability of Invisibility cloak. Event: " + e.getEventName()).logToFile();

    }

    BukkitTask checkTask = null;

    public void initializeChecks() {

        if (checkTask != null && !checkTask.isCancelled()) {
            checkTask.cancel();
        }

        checkTask = Bukkit.getScheduler().runTaskTimer(Vanillabosses.getInstance(), () -> {
            for (Player player : Vanillabosses.getInstance().getServer().getOnlinePlayers()) {

                if (player.getEquipment() != null
                        && Arrays.stream(player.getEquipment().getArmorContents())
                        .filter(n -> n.getType() != Material.AIR && n.hasItemMeta())
                        .anyMatch(n -> n.getItemMeta().getPersistentDataContainer().has(pdcKey, PersistentDataType.INTEGER))) {

                    itemAbility(player);

                }
            }
        }, 10, config.getInt("Items.cloakOfInvisibility.delayBetweenChecks") * 20L);
    }
}