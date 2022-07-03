package me.shreb.vanillabosses.bosses.bossRepresentation;

import com.google.gson.annotations.SerializedName;
import me.shreb.vanillabosses.bosses.utility.BossCommand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public abstract class Boss {

    protected static final ArrayList<String> BOSS_NAMES = new ArrayList<>();

    static {
        BOSS_NAMES.add("Blaze");
        BOSS_NAMES.add("Creeper");
        BOSS_NAMES.add("Enderman");
        BOSS_NAMES.add("Magma_Cube");
        BOSS_NAMES.add("Skeleton");
        BOSS_NAMES.add("Slime");
        BOSS_NAMES.add("Spider");
        BOSS_NAMES.add("Witch");
        BOSS_NAMES.add("Wither");
        BOSS_NAMES.add("Zombie");
        BOSS_NAMES.add("Zombified_Piglin");
    }

    @SerializedName("commands")
    public int[] commandIndexes;
    public EntityType type;

    /**
     * A Method to put the command PDC entry into the PDC of the entity specified
     *
     * @param entity the LivingEntity object to put the PDC entry on
     */
    public void putCommandsToPDC(LivingEntity entity) {
        NormalBoss boss = new NormalBoss(entity.getType());
        entity.getPersistentDataContainer().set(BossCommand.COMMAND_INDEX_KEY, PersistentDataType.INTEGER_ARRAY, boss.commandIndexes);
    }

    public static List<String> getBossNames() {
        return BOSS_NAMES;
    }

}