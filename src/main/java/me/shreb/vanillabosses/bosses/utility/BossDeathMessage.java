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
        this.rawMessage = rawMessage;
        BossCommand helperCommand = new BossCommand(0);
        helperCommand.command = rawMessage;

        killerReplacer.replaceKiller(helperCommand, event);
        if (helperCommand.command.contains(BossCommand.PLACEHOLDER_MOST_DAMAGE)) {
            UUID id = BossCommand.MostDamagePHReplacer.getMostDamageUUID(event.getEntity().getUniqueId());

            if (id == null) {
                new VBLogger(getClass().getName(), Level.WARNING, "Could not replace <mostDamage> Placeholder. UUID was null. Did the player leave before the boss was killed?").logToFile();
                return;
            }

            Player player = Bukkit.getPlayer(id);

            if (player == null) {
                new VBLogger(getClass().getName(), Level.WARNING, "Could not replace <mostDamage> Placeholder. Player was null").logToFile();
                return;
            }

            helperCommand.command = helperCommand.command.replace(BossCommand.PLACEHOLDER_MOST_DAMAGE, player.getName());
        }
        this.processedMessage = helperCommand.command;

        replaceKilledName(event);
    }


    private void replaceKilledName(EntityDeathEvent event) {
        this.processedMessage = this.processedMessage.replace("<killedName>", event.getEntity().getName());
    }

    public void sendMessage() {
        Vanillabosses.getInstance().getServer().broadcastMessage(this.processedMessage);
    }

}