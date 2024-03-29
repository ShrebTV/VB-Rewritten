package me.shreb.vanillabosses.items;

import me.shreb.vanillabosses.Vanillabosses;
import me.shreb.vanillabosses.items.utility.ItemCreationException;
import me.shreb.vanillabosses.logging.VBLogger;
import me.shreb.vanillabosses.utility.Utility;
import me.shreb.vanillabosses.utility.configFiles.FileCreator;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.logging.Level;
import java.util.stream.Stream;

public class Blazer extends VBItem {

    public static Blazer instance = new Blazer();

    public Blazer() {
        this.pdcKey = new NamespacedKey(Vanillabosses.getInstance(), "Blazer");
        this.configSection = "Blazer";
        new FileCreator().createAndLoad(FileCreator.blazerPath, this.configuration);
        try {
            this.itemMaterial = Material.valueOf(this.configuration.getString("Chestplate").toUpperCase());
        } catch (NullPointerException | IllegalArgumentException e) {
            new VBLogger(getClass().getName(), Level.SEVERE, "Unable to convert configuration of the Blazer chestplate into an actual chestplate. Found: " + this.configuration.getString("Chestplate")).logToFile();
            return;
        }
        this.lore = (ArrayList<String>) this.configuration.getStringList("Lore");
        this.itemName = Vanillabosses.getCurrentLanguage().itemBlazerName;
        this.itemGivenMessage = Vanillabosses.getCurrentLanguage().itemBlazerNameGivenMessage;
    }

    @Override
    public ItemStack makeItem() throws ItemCreationException {

        ItemStack blazer = new ItemStack(this.itemMaterial);

        if (this.itemMaterial == Material.LEATHER_CHESTPLATE) {
            LeatherArmorMeta leatherArmorMeta = (LeatherArmorMeta) blazer.getItemMeta();
            leatherArmorMeta.setColor(Color.BLUE);
            blazer.setItemMeta(leatherArmorMeta);
        }

        ItemMeta armorMeta = blazer.getItemMeta();
        PersistentDataContainer container = armorMeta.getPersistentDataContainer();
        container.set(this.pdcKey, PersistentDataType.INTEGER, 1);
        container.set(VBItem.VBItemKey, PersistentDataType.STRING, "Blazer");

        armorMeta.setDisplayName(ChatColor.DARK_BLUE + Vanillabosses.getCurrentLanguage().itemBlazerName);
        ArrayList<String> lore = new ArrayList<>(this.lore);

        armorMeta.setLore(lore);

        blazer.setItemMeta(armorMeta);

        return blazer;
    }

    @Override
    public ItemStack makeItem(int amount) throws ItemCreationException {

        ItemStack blazer = new ItemStack(this.itemMaterial, amount);

        if (blazer.getType() == Material.LEATHER_CHESTPLATE) {
            LeatherArmorMeta leatherArmorMeta = (LeatherArmorMeta) blazer.getItemMeta();
            leatherArmorMeta.setColor(Color.BLUE);
            blazer.setItemMeta(leatherArmorMeta);
        }

        ItemMeta armorMeta = blazer.getItemMeta();
        PersistentDataContainer container = armorMeta.getPersistentDataContainer();
        container.set(this.pdcKey, PersistentDataType.INTEGER, 1);
        container.set(VBItem.VBItemKey, PersistentDataType.STRING, "Blazer");

        armorMeta.setDisplayName(ChatColor.DARK_BLUE + Vanillabosses.getCurrentLanguage().itemBlazerName);
        ArrayList<String> lore = new ArrayList<>(this.lore);

        armorMeta.setLore(lore);

        blazer.setItemMeta(armorMeta);

        return blazer;
    }

    @Override
    public void registerListener() {
        pluginManager.registerEvents(this, Vanillabosses.getInstance());
    }

    @Override
    public void itemAbility(LivingEntity entity) {

        entity.setFireTicks(this.configuration.getInt("ticksOfFire"));

    }

    @EventHandler
    public void itemAbility(final EntityDamageByEntityEvent event) {

        Stream<ItemStack> stream = Arrays.stream(((LivingEntity) event.getEntity()).getEquipment().getArmorContents())
                .filter(n -> n != null && n.getType() != Material.AIR && n.hasItemMeta())
                .filter(n -> n.getItemMeta().getPersistentDataContainer().has(this.pdcKey, PersistentDataType.INTEGER));


        Optional<ItemStack> item = stream.findFirst();

        if (!item.isPresent()) return;

        ItemStack itemStack = item.get();

        boolean executeAbility =
                //check whether the entity is a living entity
                event.getEntity() instanceof LivingEntity
                        && Utility.roll(this.configuration.getDouble("chanceToCombust"))
                        && this.cooldownsetter.checkCooldown(itemStack);

        if (executeAbility) {

            itemAbility((LivingEntity) event.getDamager());

        }
    }
}
