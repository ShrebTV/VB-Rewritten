package me.shreb.vanillabosses.items;

import me.shreb.vanillabosses.Vanillabosses;
import me.shreb.vanillabosses.bosses.utility.BossCreationException;
import me.shreb.vanillabosses.bosses.utility.BossDataRetriever;
import me.shreb.vanillabosses.bosses.utility.VBBossBar;
import me.shreb.vanillabosses.items.utility.ItemCreationException;
import me.shreb.vanillabosses.logging.VBLogger;
import me.shreb.vanillabosses.utility.configFiles.FileCreator;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.logging.Level;

public class BossEggs extends VBItem {

    public static BossEggs instance = new BossEggs();

    public EntityType type;

    {
        FileCreator.createAndLoad(FileCreator.bossEggsPath, configuration);
    }

    private BossEggs() {
        this.pdcKey = new NamespacedKey(Vanillabosses.getInstance(), "BossEgg");
        this.configSection = "BossEggs";
        this.lore = (ArrayList<String>) config.getStringList("Items." + this.configSection + ".Lore");
        this.itemName = Vanillabosses.getCurrentLanguage().itemBossEggName;
        this.itemGivenMessage = Vanillabosses.getCurrentLanguage().itemBossEggGivenMessage;
    }

    public BossEggs(EntityType type) {
        this();
        this.type = type;

        switch (type) {

            case BLAZE:
                this.itemMaterial = Material.BLAZE_SPAWN_EGG;
                break;

            case CREEPER:
                this.itemMaterial = Material.CREEPER_SPAWN_EGG;
                break;

            case ENDERMAN:
                this.itemMaterial = Material.ENDERMAN_SPAWN_EGG;
                break;

            case MAGMA_CUBE:
                this.itemMaterial = Material.MAGMA_CUBE_SPAWN_EGG;
                break;

            case SKELETON:
                this.itemMaterial = Material.SKELETON_SPAWN_EGG;
                break;

            case SLIME:
                this.itemMaterial = Material.SLIME_SPAWN_EGG;
                break;

            case SPIDER:
                this.itemMaterial = Material.SPIDER_SPAWN_EGG;
                break;

            case WITCH:
                this.itemMaterial = Material.WITCH_SPAWN_EGG;
                break;

            case WITHER:
                this.itemMaterial = Material.WITHER_SKELETON_SPAWN_EGG;
                break;

            case ZOMBIE:
                this.itemMaterial = Material.ZOMBIE_SPAWN_EGG;
                break;

            case ZOMBIFIED_PIGLIN:
                this.itemMaterial = Material.ZOMBIFIED_PIGLIN_SPAWN_EGG;
                break;
        }
    }

    /**
     * Has to be called on a custom BossEggs object made with an EntityType
     *
     * @return the ItemStack requested containing one boss egg
     * @throws ItemCreationException if the item could not be created
     */
    @Override
    public ItemStack makeItem() throws ItemCreationException {
        ItemStack stack;
        switch (this.type) {

            case BLAZE:
                stack = new ItemStack(Material.BLAZE_SPAWN_EGG);
                break;

            case CREEPER:
                stack = new ItemStack(Material.CREEPER_SPAWN_EGG);
                break;

            case ENDERMAN:
                stack = new ItemStack(Material.ENDERMAN_SPAWN_EGG);
                break;

            case MAGMA_CUBE:
                stack = new ItemStack(Material.MAGMA_CUBE_SPAWN_EGG);
                break;

            case SKELETON:
                stack = new ItemStack(Material.SKELETON_SPAWN_EGG);
                break;

            case SLIME:
                stack = new ItemStack(Material.SLIME_SPAWN_EGG);
                break;

            case SPIDER:
                stack = new ItemStack(Material.SPIDER_SPAWN_EGG);
                break;

            case WITCH:
                stack = new ItemStack(Material.WITCH_SPAWN_EGG);
                break;

            case WITHER:
                stack = new ItemStack(Material.WITHER_SKELETON_SPAWN_EGG);
                break;

            case ZOMBIE:
                stack = new ItemStack(Material.ZOMBIE_SPAWN_EGG);
                break;

            case ZOMBIFIED_PIGLIN:
                stack = new ItemStack(Material.ZOMBIFIED_PIGLIN_SPAWN_EGG);
                break;

            default:
                new VBLogger(getClass().getName(), Level.WARNING, "Error at Boss egg creation. Could not match EntityType passed in with a boss type!").logToFile();
                throw new ItemCreationException("Could not make Boss egg: Type mismatch. Type passed in: " + type);
        }
        ItemMeta meta = stack.getItemMeta();

        String name = type.toString().toLowerCase();
        name = name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();

        name = name + "Boss spawn egg";

        BossDataRetriever retriever = new BossDataRetriever(type);
        String colorString = config.getString("Bosses." + retriever.CONFIGSECTION + ".displayNameColor");

        if (colorString == null) {
            new VBLogger(getClass().getName(), Level.WARNING, "Color String could not be resolved for: " + type).logToFile();
            return stack;
        }

        ChatColor color = ChatColor.of(colorString);

        meta.setDisplayName(color + name);

        meta.getPersistentDataContainer().set(pdcKey, PersistentDataType.STRING, this.type.toString());
        meta.getPersistentDataContainer().set(VBItem.VBItemKey, PersistentDataType.STRING, "BossEggs");

        meta.setLore(config.getStringList("Items.BossEggs.lore"));

        stack.setItemMeta(meta);

        return stack;
    }

    @Override
    public ItemStack makeItem(int amount) throws ItemCreationException {

        ItemStack stack;
        switch (this.type) {

            case BLAZE:
                stack = new ItemStack(Material.BLAZE_SPAWN_EGG, amount);
                break;

            case CREEPER:
                stack = new ItemStack(Material.CREEPER_SPAWN_EGG, amount);
                break;

            case ENDERMAN:
                stack = new ItemStack(Material.ENDERMAN_SPAWN_EGG, amount);
                break;

            case MAGMA_CUBE:
                stack = new ItemStack(Material.MAGMA_CUBE_SPAWN_EGG, amount);
                break;

            case SKELETON:
                stack = new ItemStack(Material.SKELETON_SPAWN_EGG, amount);
                break;

            case SLIME:
                stack = new ItemStack(Material.SLIME_SPAWN_EGG, amount);
                break;

            case SPIDER:
                stack = new ItemStack(Material.SPIDER_SPAWN_EGG, amount);
                break;

            case WITCH:
                stack = new ItemStack(Material.WITCH_SPAWN_EGG, amount);
                break;

            case WITHER:
                stack = new ItemStack(Material.WITHER_SKELETON_SPAWN_EGG, amount);
                break;

            case ZOMBIE:
                stack = new ItemStack(Material.ZOMBIE_SPAWN_EGG, amount);
                break;

            case ZOMBIFIED_PIGLIN:
                stack = new ItemStack(Material.ZOMBIFIED_PIGLIN_SPAWN_EGG, amount);
                break;

            default:
                new VBLogger(getClass().getName(), Level.WARNING, "Error at Boss egg creation. Could not match EntityType passed in with a boss type!").logToFile();
                throw new ItemCreationException("Could not make Boss egg: Type mismatch. Type passed in: " + type);
        }

        ItemMeta meta = stack.getItemMeta();

        String name = type.toString().toLowerCase();
        name = name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();

        name = name + "Boss spawn egg";

        BossDataRetriever retriever = new BossDataRetriever(type);
        String colorString = config.getString("Bosses." + retriever.CONFIGSECTION + ".displayNameColor");

        if (colorString == null) {
            new VBLogger(getClass().getName(), Level.WARNING, "Color String could not be resolved for: " + type).logToFile();
            return stack;
        }

        ChatColor color = ChatColor.of(colorString);

        meta.setDisplayName(color + name);

        meta.getPersistentDataContainer().set(pdcKey, PersistentDataType.STRING, this.type.toString());
        meta.getPersistentDataContainer().set(VBItem.VBItemKey, PersistentDataType.STRING, "BossEggs");

        meta.setLore(config.getStringList("Items.BossEggs.lore"));

        stack.setItemMeta(meta);

        return stack;
    }

    @Override
    public void registerListener() {
        pluginManager.registerEvents(this, Vanillabosses.getInstance());
    }

    @Override
    public void itemAbility(LivingEntity entity) {

    }

    @EventHandler
    public void itemAbility(final PlayerInteractEvent event) {

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || !event.hasItem() || !event.hasBlock()) return;

        ItemStack itemStack = event.getItem();

        if (itemStack == null || !itemStack.hasItemMeta()) return;

        if (itemStack.getItemMeta().getPersistentDataContainer().has(pdcKey, PersistentDataType.STRING)) {

            EntityType type = EntityType.valueOf(itemStack.getItemMeta().getPersistentDataContainer().get(pdcKey, PersistentDataType.STRING));
            BossDataRetriever bossData;

            try {
                bossData = new BossDataRetriever(type);
            } catch (IllegalArgumentException exception) {
                new VBLogger(getClass().getName(), Level.WARNING, "Could not get Data for boss type:" + type + "\n Error:" + exception).logToFile();
                event.getPlayer().sendMessage("An error occurred! Error written to log file!");
                return;
            }

            if (event.getHand() == null
                    || event.getPlayer().getEquipment() == null
                    || event.getClickedBlock() == null) return;

            Location loc = event.getClickedBlock().getLocation();

            //Make it so the boss doesn't get stuck in blocks as much
            switch (event.getBlockFace()) {

                case UP:
                    loc = loc.add(0, 1, 0);
                    break;

                case DOWN:
                    loc = loc.add(0, -2, 0);
                    break;

                case EAST:
                    loc = loc.add(1, 0, 0);
                    break;

                case WEST:
                    loc = loc.add(-1, 0, 0);
                    break;

                case NORTH:
                    loc = loc.add(0, 0, 1);
                    break;

                case SOUTH:
                    loc = loc.add(0, 0, -1);
                    break;

                default:
                    event.getPlayer().sendMessage("This won't work...");
                    return;
            }

            try {
                LivingEntity entity = bossData.instance.makeBoss(loc);
                new VBBossBar(entity, Bukkit.createBossBar(entity.getName(), BarColor.YELLOW, BarStyle.SOLID, BarFlag.PLAY_BOSS_MUSIC));
            } catch (BossCreationException ex) {
                new VBLogger(getClass().getName(), Level.WARNING, "An error occurred while spawning a boss from an egg. Error:\n" + ex).logToFile();
                event.getPlayer().sendMessage("An error occurred! Error written to log file!");
                return;
            }

            event.getPlayer().getEquipment().getItem(event.getHand()).setAmount(event.getPlayer().getEquipment().getItem(event.getHand()).getAmount() - 1);
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.GREEN + "The Boss has been summoned! Good luck...");

        }
    }
}

