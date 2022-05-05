package me.shreb.vanillabosses.items.utility;

import me.shreb.vanillabosses.items.*;
import org.bukkit.NamespacedKey;

public enum VBItemEnum {

    BASEBALLBAT("Items.BaseballBat", BaseballBat.instance),
    BLAZER("Items.Blazer", Blazer.instance),
    BOUNCYSLIME("Items.BouncySlime", BouncySlime.instance),
    BUTCHERSAXE("Items.ButchersAxe", ButchersAxe.instance),
    HMC("Items.HeatedMagmaCream", HeatedMagmaCream.instance),
    INVISIBILITYCLOAK("Items.cloakOfInvisibility", InvisibilityCloak.instance),
    SKELETOR("Items.Skeletor", Skeletor.instance),
    SLIMEBOOTS("Items.SlimeBoots", SlimeBoots.instance),
    SLINGSHOT("Items.Slingshot", Slingshot.instance),
    WITHEREGG("Items.WitherEgg", WitherEgg.instance),
    BOSSEGGS("Items.BossEggs", BossEggs.instance);

    public String configSection;
    public NamespacedKey pdcKey;
    public VBItem instance;

    VBItemEnum(String configSection, VBItem instance) {
        this.configSection = configSection;
        this.instance = instance;
        this.pdcKey = instance.pdcKey;
    }
}