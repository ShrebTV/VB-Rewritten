package me.shreb.vanillabosses.items;

import me.shreb.vanillabosses.Vanillabosses;
import me.shreb.vanillabosses.bosses.utility.BossCreationException;
import me.shreb.vanillabosses.bosses.utility.BossDataRetriever;
import me.shreb.vanillabosses.items.utility.ItemCreationException;
import me.shreb.vanillabosses.logging.VBLogger;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.logging.Level;

public class BossEggs extends VBItem {

    public static BossEggs instance = new BossEggs();

    public EntityType type;

    private BossEggs() {
        this.PDCKEY = new NamespacedKey(Vanillabosses.getInstance(), "BossEgg");
        this.CONFIGSECTION = "BossEggs";
    }

    public BossEggs(EntityType type){
        this.type = type;
    }

    @Override
    public ItemStack makeItem() throws ItemCreationException {
        ItemStack stack;
        switch(this.type){

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

//TODO edit PDC of the item
        return null;
    }

    @Override
    public ItemStack makeItem(int amount) throws ItemCreationException {

        ItemStack stack;
        switch(this.type){

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

//TODO edit PDC of the item
        return null;

    }

    @Override
    public void itemAbility(LivingEntity entity) {

    }

    @Override
    @EventHandler
    <T extends Event> void itemAbility(T e) {
        if (!(e instanceof PlayerInteractEvent)) return;

        PlayerInteractEvent event = (PlayerInteractEvent) e;

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || !event.hasItem() || !event.hasBlock()) return;

        ItemStack itemStack = event.getItem();

        if (itemStack == null || !itemStack.hasItemMeta()) return;

        if (itemStack.getItemMeta().getPersistentDataContainer().has(PDCKEY, PersistentDataType.STRING)) {

            EntityType type = EntityType.valueOf(itemStack.getItemMeta().getPersistentDataContainer().get(PDCKEY, PersistentDataType.STRING));
            BossDataRetriever bossData;

            try {
                bossData = new BossDataRetriever(type);
            } catch(IllegalArgumentException exception){
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
                bossData.instance.makeBoss(loc);
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

