package me.shreb.vanillabosses.items.utility;

import me.shreb.vanillabosses.items.*;
import me.shreb.vanillabosses.logging.VBLogger;

import java.util.logging.Level;

public class ItemListeners {

    public static void registerItemListeners(){

        BaseballBat.instance.registerListener();
        Blazer.instance.registerListener();
        BossEggs.instance.registerListener();
        BouncySlime.instance.registerListener();
        ButchersAxe.instance.registerListener();
        HeatedMagmaCream.instance.registerListener();
        InvisibilityCloak.instance.registerListener();
        Skeletor.instance.registerListener();
        SlimeBoots.instance.registerListener();
        Slingshot.instance.registerListener();
        WitherEgg.instance.registerListener();

        new VBLogger("ItemListeners", Level.INFO, "Registered Item Listeners.").logToFile();
    }

}
