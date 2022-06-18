package me.shreb.vanillabosses.bosses.utility;

import me.shreb.vanillabosses.Vanillabosses;
import me.shreb.vanillabosses.bosses.VBBoss;
import me.shreb.vanillabosses.logging.VBLogger;
import me.shreb.vanillabosses.utility.Utility;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.plugin.PluginManager;

import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class BossCommand implements Listener {

    static HashMap<Integer, String> commandMap = new HashMap<>();

    public static final NamespacedKey COMMAND_INDEX_KEY = new NamespacedKey(Vanillabosses.getInstance(), "CommandIndexes");

    public int index;
    public int delay;
    public String command;
    public int radius;
    public double chance = 1.0;
    public ArrayList<UUID> damagers = new ArrayList<>();
    public ArrayList<UUID> playersToExecuteFor = new ArrayList<>();

    public static final String PLACEHOLDER_KILLER = "<killer>";
    public static final String PLACEHOLDER_DAMAGER = "<damager>";
    public static final String PLACEHOLDER_MOST_DAMAGE = "<mostDamage>";

    static {
        DamagerPHReplacer.cleanUp();

        FileConfiguration config = Vanillabosses.getInstance().getConfig();
        List<String> commandList = new ArrayList<>(config.getStringList("Bosses.CommandsExecutedOnBossDeath"));
        for (String string : commandList) {
            int i = commandList.indexOf(string);
            commandMap.put(i, string);
        }
    }

    public static HashMap<Integer, String> getCommandMap() {
        return commandMap;
    }

    /**
     * registers all Listeners inside this class
     */
    public static void registerListeners() {
        PluginManager pm = Vanillabosses.getInstance().getServer().getPluginManager();
        Vanillabosses instance = Vanillabosses.getInstance();
        pm.registerEvents(new BossCommand(), instance);
        pm.registerEvents(new BossCommand.DamagerPHReplacer(), instance);
        pm.registerEvents(new BossCommand.MostDamagePHReplacer(), instance);
    }

    private BossCommand() {
    }

    public BossCommand(int index) {
        this.index = index;
        this.command = commandMap.get(index);
        setDelay();
    }

    //Gets the intended delay for this command from this.command and sets it. sets this.command to only have the things before 'DELAY:'
    private void setDelay() {

        if (this.command == null) return;

        if (this.command.contains("CHANCE:")) {

            this.chance = Double.parseDouble(this.command.split("CHANCE:")[1]);

            this.command = this.command.replace("CHANCE:", "");
        }

        if (!this.command.contains("DELAY:")) {
            delay = 0;
            return;
        }

        String[] strings = this.command.split("DELAY:");

        //make sure the strings array has 2 objects. If it doesn't the Command string was faulty
        if (strings.length != 2) {
            new VBLogger(getClass().getName(), Level.WARNING, "Bad Command, 'DELAY:' was found more than one time or not at all. \n" +
                    "If you do not want to have a delay to the command, please use 'DELAY: 0'\n" +
                    "Command: " + this.command).logToFile();
            //go to next command, which may not be faulty, thus not break;
            return;
        }

        //Parse the second object inside the String array to get the intended delay
        int delay;
        try {
            delay = Integer.parseInt(strings[1]);
        } catch (NumberFormatException e) {
            //log to the file and default to the value '0' in case the delay cannot be read
            new VBLogger(getClass().getName(), Level.WARNING, "Could not read delay from command string. Defaulting to 0. Command: " + command).logToFile();
            delay = 0;
        }

        //additional check for negative values
        if (delay < 0) {
            delay = 0;
            new VBLogger(getClass().getName(), Level.WARNING, "Read a negative value for Delay of the command. Defaulting to 0. Command: " + command).logToFile();
        }
        this.delay = delay;
        this.command = strings[0];
    }

    /**
     * has to call replacePlaceholders before actually executing
     */
    public void executeBossCommand(EntityDeathEvent event) {
        replacePlaceholders(event);

        this.playersToExecuteFor.addAll(this.damagers);

        if (this.radius > 0) {
            playersToExecuteFor
                    .addAll(event.getEntity().getWorld()
                            .getNearbyEntities(event.getEntity().getLocation(), radius, radius, radius)
                            .stream()
                            .map(Entity::getUniqueId)
                            .collect(Collectors.toList()));
        }

        if (playersToExecuteFor.isEmpty()) return;

        for (UUID uuid : playersToExecuteFor.stream().distinct().collect(Collectors.toList())) {

            Player player = Bukkit.getPlayer(uuid);
            if (player == null) continue;

            if (!Utility.roll(this.chance)) continue;

            if (this.command.contains("%name%")) {
                this.command = this.command.replace("%name%", player.getName());
            }

            try {
                Bukkit.getScheduler().scheduleSyncDelayedTask(Vanillabosses.getInstance(), () -> {
                    Bukkit.getServer().dispatchCommand(Vanillabosses.getInstance().getServer().getConsoleSender(), this.command);

                }, (long) delay * 20 + 1);
            } catch (CommandException e) {
                new VBLogger("BossCommand", Level.WARNING, "Attempted to execute a command. Execution failed. Command: " + command).logToFile();
            }
        }
    }

    /**
     * replaces all placeholders which are in the command String.
     * Has to call replaceRadiusPlaceHolder() as last call
     */
    private void replacePlaceholders(EntityDeathEvent event) {

        if (!this.command.contains("<") || !this.command.contains(">")) {
            return;
        }

        if (this.command.contains(PLACEHOLDER_KILLER)) {

            new KillerPHReplacer().replaceKiller(this, event);

        }

        if (this.command.contains(PLACEHOLDER_DAMAGER)) {

            new DamagerPHReplacer().setDamagers(this, event);

        }
        if (this.command.contains(PLACEHOLDER_MOST_DAMAGE)) {
            UUID id = MostDamagePHReplacer.getMostDamageUUID(event.getEntity().getUniqueId());

            Player player = Bukkit.getPlayer(id);
            playersToExecuteFor.add(id);
            this.command = this.command.replace(PLACEHOLDER_MOST_DAMAGE, "%name%");
            if (player == null)
                new VBLogger(getClass().getName(), Level.WARNING, "Could not replace <mostDamage> Placeholder. Player was null").logToFile();
        }
        replaceRadiusPlaceHolder();
    }

    /**
     * Has to be called last inside the replacePlaceHolders() method, so that it does not wreck any other correct placeHolders.
     * Sets the BossCommand objects radius attribute.
     * Removes the "<>" if it exists
     */
    private void replaceRadiusPlaceHolder() {

        int radius;

        if (!this.command.contains("<")) return;

        String[] strings = this.command.split("<");

        if (strings.length != 2) {
            new VBLogger("PlaceHolders", Level.WARNING, "An Error occurred while replacing radius placeholder within command: " + this.command).logToFile();
            return;
        }

        String[] strings1 = new String[3];
        try {
            strings1[0] = strings[0];
            strings1[1] = strings[1].split(">")[0];
            strings1[2] = strings[1].split(">")[1];
        } catch (ArrayIndexOutOfBoundsException e) {
            new VBLogger("PlaceHolders", Level.WARNING, "An Error occurred while replacing radius placeholder within command: " + this.command).logToFile();
            return;
        }

        if (Arrays.stream(strings1).anyMatch(Objects::isNull)) {
            new VBLogger("PlaceHolders", Level.WARNING, "Null! An Error occurred while replacing radius placeholder within command: " + this.command).logToFile();
            return;
        }

        try {
            radius = Integer.parseInt(strings1[1]);
        } catch (NumberFormatException e) {
            new VBLogger("PlaceHolders", Level.WARNING, "Could not find a number inside the <> radius placeholder. Could not execute command: " + this.command).logToFile();
            return;
        }

        if (radius <= 0) {
            new VBLogger("PlaceHolders", Level.WARNING, "Radius cannot be 0 or less than 0. Could not execute command: " + this.command).logToFile();
            return;
        }

        this.command = strings[0] + "%name%" + strings1[2];

        this.radius = radius;
    }


    /**
     * A Method in order to replace a UUID from the damager map and the mostDamage map with a new one without losing any data
     *
     * @param oldID the UUID the data will be retrieved from
     * @param newID the UUID the data will be set to on both maps
     */
    public static void replaceMappedUUIDs(UUID oldID, UUID newID) {

        ArrayList<UUID> damagerList = DamagerPHReplacer.damagerHashMap.get(oldID);
        MostDamagePHReplacer replacer = MostDamagePHReplacer.damagePHReplacerHashMap.get(oldID);

        if (damagerList != null) {
            DamagerPHReplacer.damagerHashMap.put(newID, damagerList);
            DamagerPHReplacer.damagerHashMap.remove(oldID);
        }

        if (replacer != null) {
            MostDamagePHReplacer.damagePHReplacerHashMap.put(newID, replacer);
            MostDamagePHReplacer.damagePHReplacerHashMap.remove(oldID);
        }
    }


    /**
     * A Way to replace the <killer> placeholder
     */
    static class KillerPHReplacer {

        public KillerPHReplacer() {
        }

        /**
         * Replaces the Placeholder "<killer>" with the name of the killer inside the command String of the BossCommand
         *
         * @param bossCommand the command to replace the killer string inside
         * @param event       the event to get the killer name from
         */
        public void replaceKiller(BossCommand bossCommand, EntityDeathEvent event) {

            if (event.getEntity().getKiller() == null) return;

            Player killer = event.getEntity().getKiller();

            if (killer == null) return;

            String killerName = killer.getName();

            bossCommand.playersToExecuteFor.add(killer.getUniqueId());

            bossCommand.command = bossCommand.command.replace(PLACEHOLDER_KILLER, killerName);
        }
    }

    static class DamagerPHReplacer implements Listener {

        //This HashMap saves the UUID of the damaged boss along with a set of player UUIDs which were recognized as damagers
        static HashMap<UUID, ArrayList<UUID>> damagerHashMap = new HashMap<>();

        public DamagerPHReplacer() {
        }

        public void setDamagers(BossCommand bossCommand, EntityDeathEvent event) {
            bossCommand.damagers = new ArrayList<>();

            if (!bossCommand.command.contains(PLACEHOLDER_DAMAGER)) return;
            if (!damagerHashMap.containsKey(event.getEntity().getUniqueId())) return;

            bossCommand.damagers.addAll(damagerHashMap.get(event.getEntity().getUniqueId()));
            bossCommand.command = bossCommand.command.replace(PLACEHOLDER_DAMAGER, "%name%");
        }

        //A way to keep track of bosses which are damaged by players.
        @EventHandler
        public void updateDamagerHashMap(EntityDamageByEntityEvent event) {

            Entity damagedBoss = event.getEntity();
            Entity damager = event.getDamager();

            if (!(damagedBoss instanceof LivingEntity)
                    || !damagedBoss.getScoreboardTags().contains(VBBoss.BOSSTAG)
                    || !(damager instanceof Player)) return;

            if (!damagerHashMap.containsKey(damagedBoss.getUniqueId())) {
                damagerHashMap.put(damagedBoss.getUniqueId(), new ArrayList<>());
            }

            damagerHashMap.get(damagedBoss.getUniqueId()).add(damager.getUniqueId());
        }

        /**
         * A method in order to not let the damagerHashMap grow out of proportion on big servers
         * Starts a repeating task for every 10 minutes in order to run through the damagerHashMap and remove dead entity UUIDs
         */
        public static void cleanUp() {

            Bukkit.getScheduler().scheduleSyncRepeatingTask(Vanillabosses.getInstance(), () -> {

                try {
                    HashMap<UUID, ArrayList<UUID>> tempMap = new HashMap<>(damagerHashMap);

                    tempMap.entrySet().stream().filter(n -> Bukkit.getEntity(n.getKey()) != null && Bukkit.getEntity(n.getKey()).isDead()).forEach(n -> damagerHashMap.remove(n.getKey()));
                    new VBLogger("DamagerPHReplacer", Level.INFO, "Cleaned up Boss damager map").logToFile();
                } catch (NullPointerException e) {
                    new VBLogger("DamagerReplacer", Level.WARNING, "An Error occurred while cleaning up the Boss Damager map. Please report this to the Author! \n Error: " + e).logToFile();
                }
            }, 500, 20 * 60 * 10);
        }
    }

    static class MostDamagePHReplacer implements Listener {

        // saves the boss UUID along with a MostDamagePHReplacer Object
        static HashMap<UUID, MostDamagePHReplacer> damagePHReplacerHashMap = new HashMap<>();

        private UUID bossUUID;
        private EntityType type;
        private final HashMap<UUID, Double> playerDamageMap = new HashMap<>(); // saves the Player uuids and the damage they have dealt to the boss
        private double bossHealth;
        private int registeredHits = 0; //How often this boss has been hit by players

        MostDamagePHReplacer() {
        }

        public MostDamagePHReplacer(UUID bossUUID) {
            this.bossUUID = bossUUID;

            try {
                this.type = Bukkit.getEntity(bossUUID).getType();
                this.bossHealth = new BossDataRetriever(this.type).health;
            } catch (NullPointerException ignored) {
            } catch (IllegalArgumentException e) {
                new VBLogger(getClass().getName(), Level.WARNING, "Unexpected EntityType entered into a Damage Replacer. EntityType: " + this.type).logToFile();
            }
        }

        @EventHandler
        public void onBossIsHit(EntityDamageByEntityEvent event) {

            if (!event.getEntity().getScoreboardTags().contains(VBBoss.BOSSTAG)
                    || !(event.getDamager() instanceof Player) || !(event.getEntity() instanceof LivingEntity)) {
                return;
            }

            UUID bossUUID = event.getEntity().getUniqueId();
            try {
                new BossDataRetriever((LivingEntity) event.getEntity());
            } catch (IllegalArgumentException e) {
                new VBLogger(getClass().getName(), Level.WARNING, "An error occurred while registering damage dealt to boss. Error: " + e).logToFile();
            }

            if (!damagePHReplacerHashMap.containsKey(bossUUID)) {
                damagePHReplacerHashMap.put(bossUUID, new MostDamagePHReplacer(bossUUID));
            }

            UUID playerUUID = event.getDamager().getUniqueId();
            double damageDealt = event.getFinalDamage();

            damagePHReplacerHashMap.get(bossUUID).registeredHits++;

            if (!damagePHReplacerHashMap.get(bossUUID).playerDamageMap.containsKey(playerUUID)) {
                damagePHReplacerHashMap.get(bossUUID).playerDamageMap.put(playerUUID, 0.0);
            }

            double prevDamage = damagePHReplacerHashMap.get(bossUUID).playerDamageMap.get(playerUUID);

            damagePHReplacerHashMap.get(bossUUID).playerDamageMap.put(playerUUID, prevDamage + damageDealt);
        }

        public double getDamagePercentage(UUID playerUUID) {
            double health = this.bossHealth;
            double damageByPlayer = damagePHReplacerHashMap.get(bossUUID).playerDamageMap.get(playerUUID);

            return damageByPlayer / health;
        }

        public int getRegisteredHits() {
            return this.registeredHits;
        }

        /**
         * A Method to get the UUID of the player who did the most damage to the boss specified by this objects bossUUID
         */
        public static UUID getMostDamageUUID(UUID bossUUID) {

            MostDamagePHReplacer replacer = damagePHReplacerHashMap.get(bossUUID);
            Map.Entry<UUID, Double> entry = null;

            for (Map.Entry<UUID, Double> tmpEntry : replacer.playerDamageMap.entrySet()) {
                if (entry == null || entry.getValue() < tmpEntry.getValue()) {
                    entry = tmpEntry;
                }
            }

            if (entry == null) return null;

            UUID id = entry.getKey();
            Player player = Bukkit.getPlayer(id);
            if (player == null) {
                new VBLogger("BossCommands", Level.WARNING, "Player gotten from replaceMostDamage was null.\n" +
                        "Information:\n" +
                        "UUID: " + id + "\n" +
                        "Entry: " + entry).logToFile();
            }
            return id;
        }
    }
}
