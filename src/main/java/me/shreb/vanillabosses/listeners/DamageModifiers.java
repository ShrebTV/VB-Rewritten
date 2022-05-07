package me.shreb.vanillabosses.listeners;

import me.shreb.vanillabosses.bosses.VBBoss;
import me.shreb.vanillabosses.bosses.utility.BossDataRetriever;
import me.shreb.vanillabosses.logging.VBLogger;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.HashMap;
import java.util.logging.Level;

/**
 * This class is a simple Listener created to check whether damage dealt by bosses should be changed
 */
public class DamageModifiers implements Listener {

    //A HashMap in order to keep retriever objects cached. This is a performance saving feature as otherwise every time a boss is damaged a new Retriever object would be created
    private static final HashMap<EntityType, RetrieverCacheObject> retrieverCache = new HashMap<>();

    /**
     * Event handler in order to modify damage dealt by bosses
     *
     * @param event the event to check for the damager being a boss in.
     */
    @EventHandler
    public void onBossDamageEntity(EntityDamageByEntityEvent event) {

        //set the entity to the event entity
        Entity entity = event.getDamager();

        //In case the entity is a Projectile, set the Entity to the shooter. Check if the shooter is a livingEntity
        if (event.getDamager() instanceof Projectile
                && ((Projectile) event.getDamager()).getShooter() != null
                && ((Projectile) event.getDamager()).getShooter() instanceof LivingEntity) {

            //set the entity to the shooter of the projectile
            try {
                entity = (Entity) ((Arrow) event.getDamager()).getShooter();
                if (entity == null) return;
            } catch (ClassCastException ignored) {
                //If a classcast exception is thrown the shooter is not an entity, return
                return;
            }
        }

        //Set the entityType to the type of the possibly corrected entity
        EntityType type = entity.getType();

        //timeout the retriever
        if (retrieverCache.containsKey(type) && retrieverCache.get(type).checkTimeOut()) {
            retrieverCache.remove(type);
        }

        //check whether the Entity is a boss
        if (!event.getDamager().getScoreboardTags().contains(VBBoss.BOSSTAG)) return;

        BossDataRetriever retriever;

        if (retrieverCache.containsKey(type)) {
            retriever = retrieverCache.get(type).getRetriever();

        } else {
            try {
                //get the data for the boss
                retriever = new BossDataRetriever(type);
                //cache it
                retrieverCache.put(type, new RetrieverCacheObject(retriever, retriever.SCOREBOARDTAG));
            } catch (IllegalArgumentException e) {
                new VBLogger(getClass().getName(), Level.SEVERE, "An Error occurred while retrieving the data for a boss. Type of the entity: " + type).logToFile();
                return;
            }
        }

        if(!entity.getScoreboardTags().contains(retrieverCache.get(type).getSpecificTag())) return;

        //get damage modifier from the retriever
        double modifier = retriever.damageModifier;
        //Apply it
        event.setDamage(event.getDamage() * modifier);

    }

    /**
     * A small inner class in order to create objects which contain a BossDataRetriever and a long value containing the millis of the creation of the object.
     * Using the checkTimeOut() method it is possible to check, whether the object saved in the cache should be updated.
     */
    static class RetrieverCacheObject {

        private final BossDataRetriever retriever;
        private final long millis;
        private final String specificTag;

        public RetrieverCacheObject(BossDataRetriever retriever, String specificTag) {
            this.retriever = retriever;
            this.specificTag = specificTag;
            this.millis = System.currentTimeMillis();
        }

        public BossDataRetriever getRetriever() {
            return retriever;
        }

        public String getSpecificTag(){
            return specificTag;
        }

        /**
         * Checks whether the Cache object should be removed or not. timeout = 30 seconds
         *
         * @return true if the object should be removed or updated, false otherwise
         */
        public boolean checkTimeOut() {
            return (System.currentTimeMillis() - this.millis) > 30000;
        }
    }
}
