package me.shreb.vanillabosses.items;

import me.shreb.vanillabosses.Vanillabosses;
import me.shreb.vanillabosses.items.utility.ItemCreationException;
import me.shreb.vanillabosses.logging.VBLogger;
import me.shreb.vanillabosses.utility.configFiles.FileCreator;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class InvisibilityCloak extends VBItem {

    public static InvisibilityCloak instance = new InvisibilityCloak();

    public InvisibilityCloak() {
        this.pdcKey = new NamespacedKey(Vanillabosses.getInstance(), "cloakOfInvisibility");
        this.configSection = "cloakOfInvisibility";
        new FileCreator().createAndLoad(FileCreator.invisibilityCloakPath, this.configuration);
        try {
            this.itemMaterial = Material.valueOf(this.configuration.getString("itemMaterial").toUpperCase());
        } catch (NullPointerException | IllegalArgumentException e) {
            new VBLogger(getClass().getName(), Level.SEVERE, "Unable to convert configuration of the Invisibility cloak into an actual chestplate. Found: " + this.configuration.getString("itemMaterial")).logToFile();
            return;
        }
        this.lore = (ArrayList<String>) this.configuration.getStringList("Lore");
        this.itemName = Vanillabosses.getCurrentLanguage().itemInvisibilityCloakName;
        this.itemGivenMessage = Vanillabosses.getCurrentLanguage().itemInvisibilityCloakNameGivenMessage;
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
        container.set(VBItem.VBItemKey, PersistentDataType.STRING, "InvisibilityCloak");

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
        container.set(VBItem.VBItemKey, PersistentDataType.STRING, "InvisibilityCloak");

        cloak.setItemMeta(meta);

        return cloak;
    }

    @Override
    public void registerListener() {
        pluginManager.registerEvents(this, Vanillabosses.getInstance());
    }

    //saves the Entity UUID along with the task id of the task timer assigned to them

    @Override
    public void itemAbility(LivingEntity entity) {

        int time = this.configuration.getInt("delayBetweenChecks");

        if (time < 2) {
            new VBLogger(getClass().getName(), Level.WARNING, "Invisibility time is too short. Please set it to 2 or more seconds").logToFile();
            return;
        }

        entity.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, (time * 20) + 10, 1));

    }

    BukkitTask checkTask = null;

    public void initializeChecks() {

        if (checkTask != null && !checkTask.isCancelled()) {
            checkTask.cancel();
        }

        checkTask = Bukkit.getScheduler().runTaskTimer(Vanillabosses.getInstance(), () -> {
            for (Player player : Vanillabosses.getInstance().getServer().getOnlinePlayers()) {

                //check whether the player has at least one Invisibility cloak item on
                if (player.getEquipment() != null
                        && Arrays.stream(player.getEquipment().getArmorContents())
                        .filter(n -> n != null && n.getType() != Material.AIR && n.hasItemMeta())
                        .anyMatch(n -> n.getItemMeta().getPersistentDataContainer().has(pdcKey, PersistentDataType.INTEGER))) {

                    //get the armor contents from that player
                    List<ItemStack> items = Arrays.stream(player.getEquipment().getArmorContents())
                            .filter(n -> n != null && n.getType() != Material.AIR
                                    && n.hasItemMeta()
                                    && n.getItemMeta().getPersistentDataContainer().has(pdcKey, PersistentDataType.INTEGER))
                            .collect(Collectors.toList());

                    //another check for whether the list contains at least one item which matches the invisbility cloak pdc
                    if (items.size() < 1) return;

                    if (((items.get(0).getType().getMaxDurability() - ((Damageable) items.get(0).getItemMeta()).getDamage()) < 1)) {
                        items.get(0).setAmount(items.get(0).getAmount() - 1);
                        return;
                    }

                    ItemMeta meta = items.get(0).getItemMeta();
                    if (!(meta instanceof Damageable)) {
                        return;
                    }

                    ((Damageable) meta).setDamage(((Damageable) items.get(0).getItemMeta()).getDamage() + 1);

                    items.get(0).setItemMeta(meta);

                    //execute the invisibility ability for the player
                    itemAbility(player);
                }
            }
        }, 10, this.configuration.getInt("delayBetweenChecks") * 20L);
    }
}