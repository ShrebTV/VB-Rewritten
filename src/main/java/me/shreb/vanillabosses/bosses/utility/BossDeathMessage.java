package me.shreb.vanillabosses.bosses.utility;

import me.shreb.vanillabosses.Vanillabosses;
import me.shreb.vanillabosses.logging.VBLogger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDeathEvent;

import java.util.UUID;
import java.util.logging.Level;

public class BossDeathMessage {

    static BossCommand.KillerPHReplacer killerReplacer = new BossCommand.KillerPHReplacer();

    public String rawMessage;
    public String processedMessage;


    public BossDeathMessage(String rawMessage, EntityDeathEvent event) {
        BossCommand helperCommand = new BossCommand(0);
        helperCommand.command = rawMessage;

        killerReplacer.replaceKiller(helperCommand, event);


        if (helperCommand.command.contains(BossCommand.PLACEHOLDER_MOST_DAMAGE)) {
            UUID id = BossCommand.MostDamagePHReplacer.getMostDamageUUID(event.getEntity().getUniqueId());

            if (id == null) {
                return;
            }

            Player player = Bukkit.getPlayer(id);

            if (player == null) {
                new VBLogger(getClass().getName(), Level.WARNING, "Could not replace <mostDamage> Placeholder. Player was null").logToFile();
                return;
            }

            helperCommand.command = helperCommand.command.replace(BossCommand.PLACEHOLDER_MOST_DAMAGE, player.getName());
        }

        helperCommand.replacePlaceholders(event);

        this.processedMessage = helperCommand.command;
        replaceKilledName(event);
    }


    public void replaceKilledName(EntityDeathEvent event) {

        if (!this.processedMessage.contains(BossCommand.PLACEHOLDER_KILLED_NAME)) return;

        if (event.getEntity().getCustomName() != null && !event.getEntity().getCustomName().isEmpty()) {
            this.processedMessage = this.processedMessage.replace(BossCommand.PLACEHOLDER_KILLED_NAME, event.getEntity().getCustomName());
        } else {
            this.processedMessage = this.processedMessage.replace(BossCommand.PLACEHOLDER_KILLED_NAME, event.getEntity().getName());
        }
    }

    public void sendMessage() {
        Vanillabosses.getInstance().getServer().broadcastMessage(this.processedMessage);
    }

}