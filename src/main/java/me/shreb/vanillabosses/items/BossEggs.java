package me.shreb.vanillabosses.items;

import me.shreb.vanillabosses.Vanillabosses;
import me.shreb.vanillabosses.bosses.utility.BossCreationException;
import me.shreb.vanillabosses.bosses.utility.BossDataRetriever;
import me.shreb.vanillabosses.logging.VBLogger;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Location;
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

    public static NamespacedKey bossEggPDCKey = new NamespacedKey(Vanillabosses.getInstance(), "BossEgg");

    @Override
    public ItemStack makeItem() {
        return null;
    }

    @Override
    void itemAbility(LivingEntity entity) {

    }

    @Override
    @EventHandler
    <T extends Event> void itemAbility(T e) {
        if (!(e instanceof PlayerInteractEvent)) return;

        PlayerInteractEvent event = (PlayerInteractEvent) e;

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || !event.hasItem() || !event.hasBlock()) return;

        ItemStack itemStack = event.getItem();

        if (itemStack == null || !itemStack.hasItemMeta()) return;

        if (itemStack.getItemMeta().getPersistentDataContainer().has(bossEggPDCKey, PersistentDataType.STRING)) {

            EntityType type = EntityType.valueOf(itemStack.getItemMeta().getPersistentDataContainer().get(bossEggPDCKey, PersistentDataType.STRING));
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

