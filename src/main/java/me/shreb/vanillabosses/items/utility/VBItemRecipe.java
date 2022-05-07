package me.shreb.vanillabosses.items.utility;

import me.shreb.vanillabosses.Vanillabosses;
import me.shreb.vanillabosses.items.*;
import me.shreb.vanillabosses.logging.VBLogger;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;

public class VBItemRecipe {

    //A map in order to
    private static final HashMap<String, ItemStack> specialItemMap = new HashMap<>();
    private static final List<ShapedRecipe> recipeList = new LinkedList<>();

    static {
        try {

            //make a new splash potion of invisibility 8:00
            ItemStack potion = new ItemStack(Material.SPLASH_POTION);
            ItemMeta meta = potion.getItemMeta();
            PotionMeta potMeta = (PotionMeta) meta;
            PotionData data = new PotionData(PotionType.INVISIBILITY, true, false);
            potMeta.setBasePotionData(data);
            potion.setItemMeta(potMeta);

            specialItemMap.put("BaseballBat", BaseballBat.instance.makeItem());
            specialItemMap.put("Blazer", Blazer.instance.makeItem());
            specialItemMap.put("BouncySlime", BouncySlime.instance.makeItem());
            specialItemMap.put("ButchersAxe", ButchersAxe.instance.makeItem());
            specialItemMap.put("HMC1", new HeatedMagmaCream(1).makeItem());
            specialItemMap.put("HMC2", new HeatedMagmaCream(2).makeItem());
            specialItemMap.put("HMC3", new HeatedMagmaCream(3).makeItem());
            specialItemMap.put("InvisibilityCloak", InvisibilityCloak.instance.makeItem());
            specialItemMap.put("Skeletor", Skeletor.instance.makeItem());
            specialItemMap.put("SlimeBoots", SlimeBoots.instance.makeItem());
            specialItemMap.put("Slingshot", Slingshot.instance.makeItem());
            specialItemMap.put("WitherEgg", WitherEgg.instance.makeItem());
            specialItemMap.put("Pot", potion);

        } catch (ItemCreationException e) {
            e.printStackTrace();
        }
    }

    private final String firstLine;
    private final String secondLine;
    private final String thirdLine;
    private final VBItemEnum itemEnum;

    /**
     * @param e the Enum item to make the recipe for
     * @throws IllegalArgumentException if any of the lines did not have the length 3
     */
    public VBItemRecipe(VBItemEnum e) {

        this.itemEnum = e;

        FileConfiguration config = Vanillabosses.getInstance().getConfig();

        ArrayList<String> shape = (ArrayList<String>) config.getStringList(e.configSection + ".recipeShape");

        if (shape.size() != 3) {
            throw new IllegalArgumentException("Shape list did not have the correct size. List in question: " + e.name());
        }

        String firstLine = shape.get(0);
        String secondLine = shape.get(1);
        String thirdLine = shape.get(2);

        boolean shouldThrow = firstLine.length() != 3
                || secondLine.length() != 3
                || thirdLine.length() != 3;

        if (shouldThrow) throw new IllegalArgumentException("lines have to have the length 3");

        this.firstLine = firstLine;
        this.secondLine = secondLine;
        this.thirdLine = thirdLine;
    }

    private void registerRecipe() {

        if (this.firstLine == null || this.firstLine.equals("")) return;

        FileConfiguration config = Vanillabosses.getInstance().getConfig();

        if (this.itemEnum == VBItemEnum.HMC) return;

        if (!config.getBoolean(this.itemEnum.configSection + ".enableCraftingRecipe")) {
            new VBLogger(getClass().getName(), Level.INFO, itemEnum.name() + " Recipe disabled or does not exist!").logToFile();
            return;
        }

        ArrayList<String> ingredients = (ArrayList<String>) config.getStringList(itemEnum.configSection + ".recipeIngredients");

        ShapedRecipe recipe;
        try {
            recipe = new ShapedRecipe(itemEnum.pdcKey, itemEnum.instance.makeItem());
        } catch (ItemCreationException ex) {
            new VBLogger(getClass().getName(), Level.SEVERE, "Could not create Item for Recipe registration. Exception: " + ex).logToFile();
            return;
        }

        recipe.shape(this.firstLine, this.secondLine, this.thirdLine);

        for (String string : ingredients) {
            MaterialKey matKey;
            try {
                matKey = new MaterialKey(string);
                recipe.setIngredient(matKey.key, matKey.material);
                continue;
            } catch (IllegalArgumentException ignored) {
            }

            try {
                ItemKey itemKey = new ItemKey(string);
                recipe.setIngredient(itemKey.key, new RecipeChoice.ExactChoice(itemKey.itemStack));
            } catch (IllegalArgumentException ex) {
                new VBLogger(getClass().getName(), Level.SEVERE, "String could not be matched to materials or special items. Exception: " + ex + "\n" +
                        "String: " + string).logToFile();
            }
        }

        recipeList.add(recipe);
        Vanillabosses.getInstance().getServer().addRecipe(recipe);
    }

    /**
     * registers all recipes for items inside the VBItemEnum
     */
    public static void registerAllRecipes() {
        for (VBItemEnum e : VBItemEnum.values()) {
            try {
                new VBItemRecipe(e).registerRecipe();
            } catch (IllegalArgumentException ignored) {
            }
        }
        registerHMCRecipe(2);
        registerHMCRecipe(3);
    }

    public static void removeAllRecipes() {
        for (ShapedRecipe recipe : recipeList) {
            Vanillabosses.getInstance().getServer().removeRecipe(recipe.getKey());
        }
    }

    private static void registerHMCRecipe(int levelTo) {

        try {
            ItemStack from = new HeatedMagmaCream(levelTo - 1).makeItem();
            ItemStack to = new HeatedMagmaCream(levelTo).makeItem();

            ShapedRecipe recipe = new ShapedRecipe(new NamespacedKey(Vanillabosses.getInstance(), "HMCRecipe" + levelTo), to);

            recipe.shape("HHH", "   ", "   ");

            recipe.setIngredient('H', new RecipeChoice.ExactChoice(from));

            recipeList.add(recipe);

            Vanillabosses.getInstance().getServer().addRecipe(recipe);

        } catch (ItemCreationException e) {
            new VBLogger("VBItemRecipe", Level.WARNING, "Problem occurred making Heated magma cream recipe. " + e).logToFile();
        }
    }

    /**
     * A class to represent the key-material pairs required in shaped recipes
     */
    static class MaterialKey {

        public final char key;
        public final Material material;

        public MaterialKey(String string) {

            if (string == null || string.equals("")) {
                throw new IllegalArgumentException("Empty Strings cannot be parsed for MaterialKey.");
            }

            String[] strings = string.split(":");

            // simple checks for whether the key-value pair looks to be alright
            boolean shouldThrow = strings.length != 2
                    || strings[0].length() != 1
                    || strings[1].length() < 2;

            if (shouldThrow) {
                new VBLogger("MaterialKey", Level.SEVERE, "Unable to parse for Key-value pair. Key has to be a single character.\n" +
                        "Value has to be a material or a plugin item. Separate them with a ':'. Bad String: " + string).logToFile();
                throw new IllegalArgumentException("Unable to parse for Key-value pair. Bad String: " + string);
            }

            this.key = strings[0].charAt(0);
            this.material = Material.valueOf(strings[1].toUpperCase());
        }
    }


    static class ItemKey {

        public final char key;
        public final ItemStack itemStack;

        public ItemKey(String string) {
            if (string == null || string.equals("")) {
                throw new IllegalArgumentException("Empty Strings cannot be parsed for MaterialKey.");
            }

            String[] strings = string.split(":");

            // simple checks for whether the key-value pair looks to be alright
            boolean shouldThrow = strings.length != 2
                    || strings[0].length() != 1
                    || strings[1].length() < 2;

            if (shouldThrow) {
                new VBLogger("MaterialKey", Level.SEVERE, "Unable to parse for Key-value pair. Key has to be a single character.\n" +
                        "Value has to be a material or a plugin item. Separate them with a ':'. Bad String: " + string).logToFile();
                throw new IllegalArgumentException("Unable to parse for Key-value pair. Bad String: " + string);
            }

            this.key = strings[0].charAt(0);
            this.itemStack = specialItemMap.get(strings[1]);
        }
    }
}
