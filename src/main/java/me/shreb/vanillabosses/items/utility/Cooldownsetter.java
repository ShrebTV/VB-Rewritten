package me.shreb.vanillabosses.items.utility;

import me.shreb.vanillabosses.Vanillabosses;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.Objects;

/**
 * Manages the cooldowns of an items ability
 */
public class Cooldownsetter {

    private static final String COOLDOWN_KEY = "AbilityCooldown";
    private final NamespacedKey cooldownKey = new NamespacedKey(Vanillabosses.getInstance(), COOLDOWN_KEY);

    private static final String COOLDOWN_MESSAGE = Vanillabosses.getInstance().getConfig().getString("Items.CooldownMessage");

    /**
     * @param itemStack the stack to check for an active cooldown
     * @return true if the cooldown has passed, false if it has not or the item was not a custom item
     */
    public boolean checkCooldown(ItemStack itemStack) {

        if (!itemStack.hasItemMeta()) return false;

        if (!itemStack.getItemMeta().getPersistentDataContainer().has(cooldownKey, PersistentDataType.LONG)) {
            firstInit(itemStack);
        }

        long cooldown;
        try {
            cooldown = getRelatedCooldown(itemStack);
        } catch (ItemCreationException ignored) {
            return false;
            //Only happens if the item passed in was not a custom item from this plugin or not recognized
        }

        long passedTime = getTimePassedOnCooldown(itemStack);

        if (passedTime >= cooldown) {
            initCooldown(itemStack);
            return true;
        } else {
            return false;
        }
    }

    private void firstInit(ItemStack itemStack) {
        ItemMeta meta = itemStack.getItemMeta();
        itemStack.getItemMeta().getPersistentDataContainer().set(this.cooldownKey, PersistentDataType.LONG, 0L);
        itemStack.setItemMeta(meta);
    }

    private void initCooldown(ItemStack itemStack) {
        ItemMeta meta = itemStack.getItemMeta();
        Objects.requireNonNull(meta).getPersistentDataContainer().set(this.cooldownKey, PersistentDataType.LONG, System.currentTimeMillis());
        itemStack.setItemMeta(meta);
    }

    /**
     * Gets the cooldown in ticks related to the passed in item
     *
     * @param itemStack the item to check the cooldown of
     * @return the time in ticks the cooldown is set to
     */
    public long getRelatedCooldown(ItemStack itemStack) throws ItemCreationException {
        ItemDataRetriever retriever = new ItemDataRetriever(itemStack);

        if (!retriever.instance.configuration.contains(COOLDOWN_KEY))
            return 0; // in case the item does not have a cooldown section

        long cooldown = retriever.instance.configuration.getLong(COOLDOWN_KEY);

        cooldown = Math.max(cooldown, 0);

        return cooldown;
    }

    /**
     * @param itemStack
     * @return
     */
    public long getTimePassedOnCooldown(ItemStack itemStack) {

        if (!itemStack.hasItemMeta()) return Long.MAX_VALUE;

        if (!itemStack.getItemMeta().getPersistentDataContainer().has(cooldownKey, PersistentDataType.LONG)) {
            firstInit(itemStack);
        }

        long currentTime = System.currentTimeMillis();

        //The time the cooldown was set at
        long cooldownSetTime = itemStack.getItemMeta().getPersistentDataContainer().getOrDefault(cooldownKey, PersistentDataType.LONG, 0L);

        long passedTime = currentTime - cooldownSetTime;

        return Math.max(passedTime, 0);
    }

    public long calculateCooldownLeft(ItemStack itemStack) {

        long passedTime = getTimePassedOnCooldown(itemStack);
        long cooldown;

        try {
            cooldown = getRelatedCooldown(itemStack);
        } catch (ItemCreationException e) {
            return -1L;
        }
        long cooldownLeftInMillis = cooldown - passedTime;

        cooldownLeftInMillis = Math.max(cooldownLeftInMillis, 0L);

        return Math.floorDiv(cooldownLeftInMillis, 1000);
    }

    public void sendCooldownMessage(Player player, ItemStack itemStack) {

        if (COOLDOWN_MESSAGE == null || COOLDOWN_MESSAGE.isEmpty()) return;

        long timeLeft = calculateCooldownLeft(itemStack);
        if (timeLeft < 0) return;
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(replacePlaceholderOnCooldownMessage(timeLeft), ChatColor.RED));
    }

    private String replacePlaceholderOnCooldownMessage(long timeLeftInSeconds) {
        if (COOLDOWN_MESSAGE == null || COOLDOWN_MESSAGE.isEmpty()) return "";
        return COOLDOWN_MESSAGE.replace("?", String.valueOf(timeLeftInSeconds));
    }

}
