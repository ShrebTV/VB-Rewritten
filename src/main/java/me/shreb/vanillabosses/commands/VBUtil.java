package me.shreb.vanillabosses.commands;

import me.shreb.vanillabosses.Vanillabosses;
import me.shreb.vanillabosses.items.BouncySlime;
import me.shreb.vanillabosses.items.HeatedMagmaCream;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * /vb replaceItems
 */
public class VBUtil extends VBCommands {

    private static final VBUtil INSTANCE = new VBUtil();

    public static VBUtil getInstance() {
        return INSTANCE;
    }

    @Override
    public void registerCommand() {
        Vanillabosses.getInstance().getCommand("vbUtil").setExecutor(new VBUtil());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (sender instanceof Player && args.length == 1 && args[0].equalsIgnoreCase("replaceitems")) {
            ArrayList<ItemStack> replaceableItems = (ArrayList<ItemStack>) Arrays.stream(((Player) sender).getInventory().getContents())
                    .filter(Objects::nonNull)
                    .filter(n -> n.getType() != Material.AIR)
                    //filtering out Air because of itemMeta nullpointers
                    .filter(n -> n.getItemMeta().getPersistentDataContainer().has(HeatedMagmaCream.instance.pdcKey, PersistentDataType.INTEGER)
                            || n.getItemMeta().getPersistentDataContainer().has(BouncySlime.instance.pdcKey, PersistentDataType.STRING))
                    //filtering for all replaceable items!
                    .collect(Collectors.toList());

            if (replaceableItems.isEmpty()) {
                sender.sendMessage("You do not have any items to replace!");
                return true;
            }

            for (ItemStack stack : replaceableItems) {

                switch (stack.getType()) {

                    case SLIME_BALL:
                        ((Player) sender).getInventory().remove(stack);
                        ((Player) sender).getInventory().addItem(BouncySlime.replaceBouncySlime(stack));
                        break;

                    case MAGMA_CREAM:
                        ((Player) sender).getInventory().remove(stack);
                        ((Player) sender).getInventory().addItem(HeatedMagmaCream.replaceHMC(stack));
                        break;

                    default:
                        sender.sendMessage("Weird Error 102");

                }
            }
            return true;
        }
        return false;
    }
}
