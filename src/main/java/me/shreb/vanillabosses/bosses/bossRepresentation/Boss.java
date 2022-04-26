package me.shreb.vanillabosses.bosses.bossRepresentation;

import me.shreb.vanillabosses.bosses.utility.BossCommand;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.persistence.PersistentDataType;

/**
 *
 */
public abstract class Boss {

    public int[] commandIndexes;
    public EntityType type;

    abstract void spawnBoss(Location location);

    public static void putCommandsToPDC(LivingEntity entity){
        NormalBoss boss = new NormalBoss(entity.getType());
        entity.getPersistentDataContainer().set(BossCommand.COMMAND_INDEX_KEY, PersistentDataType.INTEGER_ARRAY, boss.commandIndexes);
    }


}
