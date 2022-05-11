package me.shreb.vanillabosses.items.configDataReader;

import me.shreb.vanillabosses.bosses.utility.BossDataRetriever;
import me.shreb.vanillabosses.items.BossEggs;
import me.shreb.vanillabosses.logging.VBLogger;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

/**
 * A class in order to properly read data from the BossEgg config file.
 * The data is put into a Hashmap containing EntityType and a Set of Materials which the Boss eggs may be placed on.
 */
public class BossEggDataReader {

    private final YamlConfiguration config;
    private final HashMap<EntityType, Set<Material>> allowedBlocks = new HashMap<>();

    /**
     * Automatically reads the data from the config file for the boss eggs
     * Call the checkAllowed() method in order to find out whether the EntityType put in is allowed to be placed on the specified material.
     */
    public BossEggDataReader(){

        this.config = BossEggs.instance.configuration;

        readData();

    }

    /**
     * A private method in order to properly read the data from the config. Used inside the constructor to set the allowed blocks
     * Initializes and correctly reads the entries from the config.
     */
    private void readData(){

        ArrayList<String> entries = (ArrayList<String>) this.config.getStringList("CanSpawnOnBlocks");

        for(String s : entries){

            if(s == null || s.equals("")){
                continue;
            }

            String[] strings = s.split(":");

            if(strings.length != 2){
                new VBLogger(getClass().getName(), Level.WARNING, "Bad String entered for 'CanSpawnOnBlocks' in Boss Eggs.yml. Bad String in question: " + s + "\n" +
                        "Has to contain exactly one ':' in order to separate the Entity type and Block material.").logToFile();
                continue;
            }

            EntityType type;
            Material mat;

            try{

                type = EntityType.valueOf(strings[0].toUpperCase());
                mat = Material.valueOf(strings[1].toUpperCase());

            } catch (IllegalArgumentException ignored) {
                new VBLogger(getClass().getName(), Level.WARNING, "Bad String entered for 'CanSpawnOnBlocks' in Boss Eggs.yml. Bad String in question: " + s + "\n" +
                        "One of the 2 values could not be read into an EntityType and a Material type respectively. Make sure there are no typos.").logToFile();
                continue;
            }

            try{
                new BossDataRetriever(type);
            } catch (IllegalArgumentException ignored) {
                new VBLogger(getClass().getName(), Level.WARNING, "Bad String entered for 'CanSpawnOnBlocks' in Boss Eggs.yml. Bad String in question: " + s + "\n" +
                        "The Type passed in was not recognised as a Boss type. Please make sure there are no typos").logToFile();
                continue;
            }

            if(!mat.isBlock()){
                new VBLogger(getClass().getName(), Level.WARNING, "Bad String entered for 'CanSpawnOnBlocks' in Boss Eggs.yml. Bad String in question: " + s + "\n" +
                        "The material passed in was not a block. Make sure this is a block and not something like an Egg.").logToFile();
                continue;
            }

            if(this.allowedBlocks.containsKey(type)){

                this.allowedBlocks.computeIfAbsent(type, k -> new HashSet<>());

            } else {

                this.allowedBlocks.put(type, new HashSet<>());

            }
            this.allowedBlocks.get(type).add(mat);
        }
    }

    /**
     * Checks, whether the specified EntityType is allowed to be spawned on the specified Material
     * @param type the type to use as a key
     * @param mat the material to check against
     * @return true if the map did not contain the type or the value mapped to the type contained the Material specified.
     *         false if the map did contain the key, but the material was not present in the data mapped to the key.
     *         Also false if the value mapped to the key was null.
     */
    public boolean checkAllowed(EntityType type, Material mat){

        if(!this.allowedBlocks.containsKey(type)){
            return true;
        }

        return this.allowedBlocks.get(type) != null && this.allowedBlocks.get(type).contains(mat);
    }
}