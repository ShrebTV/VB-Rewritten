package me.shreb.vanillabosses.items.utility;

import me.shreb.vanillabosses.Vanillabosses;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.Objects;

/**
 * Manages the cooldowns of an items ability
 */
public class Cooldownsetter {

    private static final String COOLDOWN_KEY = "AbilityCooldown";
    private static final PersistentDataType<Long, Long> COOLDOWN_DATA_TYPE = PersistentDataType.LONG;
    private final NamespacedKey cooldownKey = new NamespacedKey(Vanillabosses.getInstance(), COOLDOWN_KEY);

    /**
     * @param itemStack the stack to check for an active cooldown
     * @return true if the cooldown has passed, false if it has not or the item was not a custom item
     */
    public boolean checkCooldown(ItemStack itemStack) {

        long currentTime = System.currentTimeMillis();
        long cooldownSetTime = itemStack.getItemMeta().getPersistentDataContainer().get(cooldownKey, COOLDOWN_DATA_TYPE);

        long cooldown;
        try {
            cooldown = getRelatedCooldown(itemStack);
        } catch (ItemCreationException ignored) {
            return false;
            //Only happens if the item passed in was not a custom item from this plugin
        }

        long passedTime = currentTime - cooldownSetTime;

        passedTime = Math.max(passedTime, 0);

        if (passedTime >= cooldown) {
            initCooldown(itemStack);
            return true;
        } else {
            return false;
        }
    }

    private void initCooldown(ItemStack itemStack) {
        Objects.requireNonNull(itemStack.getItemMeta()).getPersistentDataContainer().set(this.cooldownKey, COOLDOWN_DATA_TYPE, System.currentTimeMillis());
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
}
