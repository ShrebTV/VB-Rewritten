package me.shreb.vanillabosses.bosses.utility;

import me.shreb.vanillabosses.Vanillabosses;
import me.shreb.vanillabosses.bosses.VBBoss;
import me.shreb.vanillabosses.logging.VBLogger;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandException;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.PluginManager;

import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class BossCommand implements Listener {

    public static final NamespacedKey COMMAND_INDEX_KEY = new NamespacedKey(Vanillabosses.getInstance(), "CommandIndexes");

    int index;
    int delay;
    String command;
    int radius;
    ArrayList<UUID> damagers;
    ArrayList<UUID> playersToExecuteFor = new ArrayList<>();

    public static final String PLACEHOLDER_KILLER = "<killer>";
    public static final String PLACEHOLDER_DAMAGER = "<damager>";
    public static final String PLACEHOLDER_MOST_DAMAGE = "<mostDamage>";

    static{
        DamagerPHReplacer.cleanUp();
    }

    /**
     * registers all Listeners inside this class
     */
    public static void registerListeners(){
        PluginManager pm = Vanillabosses.getInstance().getServer().getPluginManager();
        Vanillabosses instance = Vanillabosses.getInstance();
        pm.registerEvents(new BossCommand(), instance);
        pm.registerEvents(new BossCommand.DamagerPHReplacer(), instance);
        pm.registerEvents(new BossCommand.MostDamagePHReplacer(), instance);
    }

    private BossCommand(){}

    public BossCommand(int index, int delay, String command) {
        this.index = index;
        this.delay = delay;
        this.command = command;
    }

    /**
     * has to call replacePlaceholders before actually executing
     */
    public void executeBossCommand(EntityDeathEvent event) {

        replacePlaceholders(event);

        this.playersToExecuteFor.addAll(this.damagers);

        if(this.radius > 0){
            playersToExecuteFor
                    .addAll(event.getEntity().getWorld()
                            .getNearbyEntities(event.getEntity().getLocation(), radius, radius, radius)
                            .stream()
                            .map(Entity::getUniqueId)
                            .collect(Collectors.toList()));
        }

        try{
            Bukkit.getServer().dispatchCommand(Vanillabosses.getInstance().getServer().getConsoleSender(), this.command);
        } catch(CommandException e){
            new VBLogger("BossCommand", Level.WARNING, "Attempted to execute a command. Execution failed. Command: " + command).logToFile();
        }
    }

    /**
     * replaces all placeholders which are in the command String.
     * Has to call replaceRadiusPlaceHolder() as last call
     */
    public void replacePlaceholders(EntityDeathEvent event) {

        UUID uuid = event.getEntity().getUniqueId();

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
           UUID playerUUID = new MostDamagePHReplacer(uuid).getMostDamagePlayer();
           Player player = Bukkit.getPlayer(playerUUID);
           if(player != null){
               this.command = this.command.replace(PLACEHOLDER_MOST_DAMAGE, player.getName());
           } else {
               new VBLogger("BossCommands", Level.WARNING, "Could not replace <mostDamage> Placeholder. Replacing with null. Command: " + this.command).logToFile();
           }
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

        this.command = strings1[0] + strings1[1] + strings1[2];

        this.radius = radius;
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

            bossCommand.command = bossCommand.command.replace(PLACEHOLDER_KILLER, killerName);
        }
    }

    static class DamagerPHReplacer implements Listener{

        //This HashMap saves the UUID of the damaged boss along with a set of player UUIDs which were recognized as damagers
        static HashMap<UUID, Set<UUID>> damagerHashMap = new HashMap<>();

        private DamagerPHReplacer() {}

        public void setDamagers(BossCommand bossCommand, EntityDeathEvent event) {

            if(!damagerHashMap.containsKey(event.getEntity().getUniqueId())) return;

            bossCommand.damagers = new ArrayList<>(damagerHashMap.get(event.getEntity().getUniqueId()));

        }

        //A way to keep track of bosses which are damaged by players.
        @EventHandler
        public void updateDamagerHashMap(EntityDamageByEntityEvent event) {

            Entity damagedBoss = event.getEntity();
            Entity damager = event.getDamager();

            if (!(damagedBoss instanceof LivingEntity)
                    || !damagedBoss.getScoreboardTags().contains(VBBoss.BOSSTAG)
                    || !(damager instanceof Player)) return;

            if(!damagerHashMap.containsKey(damagedBoss.getUniqueId())){
                damagerHashMap.put(damagedBoss.getUniqueId(), new HashSet<>());
            }

            damagerHashMap.get(damagedBoss.getUniqueId()).add(damager.getUniqueId());

        }

        /**
         * A method in order to not let the damagerHashMap grow out of proportion on big servers
         * Starts a repeating task for every 10 minutes in order to run through the damagerHashMap and remove dead entity UUIDs
         */
        public static void cleanUp(){

            Bukkit.getScheduler().scheduleSyncRepeatingTask(Vanillabosses.getInstance(), ()-> {

                try {
                    damagerHashMap.entrySet().stream().filter(n -> Bukkit.getEntity(n.getKey()) != null &&Bukkit.getEntity(n.getKey()).isDead()).forEach(n -> damagerHashMap.remove(n.getKey()));
                    new VBLogger("DamagerPHReplacer", Level.INFO, "Cleaned up Boss damager map").logToFile();
                } catch(NullPointerException e){
                    new VBLogger("DamagerReplacer", Level.WARNING, "An Error occurred while cleaning up the Boss Damager map. Please report this to the Author! \n Error: " + e).logToFile();
                }
            }, 500, 20 * 60 * 10);
        }
    }

    static class MostDamagePHReplacer implements Listener{

        // saves the boss UUID along with a MostDamagePHReplacer Object
        static HashMap<UUID, MostDamagePHReplacer> damagePHReplacerHashMap = new HashMap<>();

        private UUID bossUUID;
        private EntityType type;
        private final HashMap<UUID, Double> playerDamageMap = new HashMap<>(); // saves the Player uuids and the damage they have dealt to the boss
        private double bossHealth;
        private int registeredHits = 0; //How often this boss has been hit by players

        private MostDamagePHReplacer(){}

        public MostDamagePHReplacer(UUID bossUUID) {
            this.bossUUID = bossUUID;

            try {
                this.type = Bukkit.getEntity(bossUUID).getType();
                this.bossHealth = new BossDataRetriever(this.type).health;
            } catch(NullPointerException ignored){
            } catch(IllegalArgumentException e){
                new VBLogger(getClass().getName(), Level.WARNING, "Unexpected EntityType entered into a Damage Replacer. EntityType: " + this.type).logToFile();
            }
        }

        @EventHandler
        public void onBossIsHit(EntityDamageByEntityEvent event){

            if(!event.getEntity().getScoreboardTags().contains(VBBoss.BOSSTAG)
            || !(event.getDamager() instanceof Player)) {
                return;
            }

            UUID bossUUID = event.getEntity().getUniqueId();

            if(!damagePHReplacerHashMap.containsKey(bossUUID)){
                damagePHReplacerHashMap.put(bossUUID, new MostDamagePHReplacer(bossUUID));
            }

            UUID playerUUID = event.getDamager().getUniqueId();
            double damageDealt = event.getFinalDamage();

            damagePHReplacerHashMap.get(bossUUID).registeredHits++;

            if(!damagePHReplacerHashMap.get(bossUUID).playerDamageMap.containsKey(playerUUID)){
                damagePHReplacerHashMap.get(bossUUID).playerDamageMap.put(playerUUID, 0.0);
            }

            double prevDamage = damagePHReplacerHashMap.get(bossUUID).playerDamageMap.get(playerUUID);

            damagePHReplacerHashMap.get(bossUUID).playerDamageMap.put(playerUUID, prevDamage + damageDealt);
        }

        public double getDamagePercentage(UUID playerUUID){
            double health = this.bossHealth;
            double damageByPlayer = damagePHReplacerHashMap.get(bossUUID).playerDamageMap.get(playerUUID);

            return damageByPlayer/health;
        }

        public int getRegisteredHits(){
            return this.registeredHits;
        }

        /**
         *  A Method to get the UUID of the player who did the most damage to the boss specified by this objects bossUUID
         * @return The UUID of the player who has done the most damage to the boss.
         */
        public UUID getMostDamagePlayer(){
            Map.Entry<UUID, Double> entry = this.playerDamageMap.entrySet().stream().max(Map.Entry.comparingByValue()).orElse(null);
            if(entry == null) return null;
            return entry.getKey();
        }
    }
}
