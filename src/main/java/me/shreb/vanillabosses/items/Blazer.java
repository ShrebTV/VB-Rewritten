package me.shreb.vanillabosses.items;

import me.shreb.vanillabosses.Vanillabosses;
import me.shreb.vanillabosses.items.utility.ItemCreationException;
import me.shreb.vanillabosses.logging.VBLogger;
import me.shreb.vanillabosses.utility.Utility;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;

public class Blazer extends VBItem {

    public static Blazer instance = new Blazer();

    public Blazer() {
        this.pdcKey = new NamespacedKey(Vanillabosses.getInstance(), "Blazer");
        this.configSection = "Blazer";
        try {
            this.itemMaterial = Material.valueOf(config.getString("Items.Blazer.Chestplate").toUpperCase());
        } catch (NullPointerException | IllegalArgumentException e) {
            new VBLogger(getClass().getName(), Level.SEVERE, "Unable to convert configuration of the Blazer chestplate into an actual chestplate. Found: " + config.getString("Items.Blazer.Chestplate")).logToFile();
            return;
        }
        this.lore = (ArrayList<String>) config.getStringList("Items." + this.configSection + ".Lore");
    }

    @Override
    public ItemStack makeItem() throws ItemCreationException {

        ItemStack blazer = new ItemStack(this.itemMaterial);

        if(this.itemMaterial == Material.LEATHER_CHESTPLATE){
            LeatherArmorMeta leatherArmorMeta = (LeatherArmorMeta) blazer.getItemMeta();
            leatherArmorMeta.setColor(Color.BLUE);
            blazer.setItemMeta(leatherArmorMeta);
        }

        ItemMeta armorMeta = blazer.getItemMeta();
        PersistentDataContainer container = armorMeta.getPersistentDataContainer();
        container.set(this.pdcKey, PersistentDataType.INTEGER, 1);

        armorMeta.setDisplayName(ChatColor.DARK_BLUE + Vanillabosses.getCurrentLanguage().itemBlazerName);
        ArrayList<String> lore = new ArrayList<>(this.lore);

        armorMeta.setLore(lore);

        blazer.setItemMeta(armorMeta);

        return blazer;
    }

    @Override
    public ItemStack makeItem(int amount) throws ItemCreationException {

        ItemStack blazer = new ItemStack(this.itemMaterial, amount);

        if(blazer.getType() == Material.LEATHER_CHESTPLATE){
            LeatherArmorMeta leatherArmorMeta = (LeatherArmorMeta) blazer.getItemMeta();
            leatherArmorMeta.setColor(Color.BLUE);
            blazer.setItemMeta(leatherArmorMeta);
        }

        ItemMeta armorMeta = blazer.getItemMeta();
        PersistentDataContainer container = armorMeta.getPersistentDataContainer();
        container.set(this.pdcKey, PersistentDataType.INTEGER, 1);

        armorMeta.setDisplayName(ChatColor.DARK_BLUE + Vanillabosses.getCurrentLanguage().itemBlazerName);
        ArrayList<String> lore = new ArrayList<>(this.lore);

        armorMeta.setLore(lore);

        blazer.setItemMeta(armorMeta);

        return blazer;
    }

    @Override
    public void itemAbility(LivingEntity entity) {

        entity.setFireTicks(config.getInt("Items.Blazer.ticksOfFire"));

    }

    @Override
    <T extends Event> void itemAbility(T e) {

        if(!(e instanceof EntityDamageByEntityEvent)) return;

        EntityDamageByEntityEvent event = (EntityDamageByEntityEvent) e;

        boolean executeAbility =
                //check whether the entity is a living entity
                event.getEntity() instanceof LivingEntity
                //checking whether the attacked entity is wearing a blazer. could theoretically be set to be any armor
                && Arrays.stream(((LivingEntity) event.getEntity()).getEquipment().getArmorContents())
                .filter(n -> n.getType() != Material.AIR && n.hasItemMeta())
                .anyMatch(n -> n.getItemMeta().getPersistentDataContainer().has(this.pdcKey, PersistentDataType.INTEGER))
                //check whether the chance applies
                && Utility.roll(config.getDouble("Items.Blazer.chanceToCombust"));

        if(executeAbility){

            itemAbility((LivingEntity) event.getEntity());

        }
    }
}
