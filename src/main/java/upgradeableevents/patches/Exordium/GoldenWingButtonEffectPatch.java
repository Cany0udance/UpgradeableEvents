package upgradeableevents.patches.Exordium;

import basemod.ReflectionHacks;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.events.AbstractEvent;
import com.megacrit.cardcrawl.events.exordium.GoldenWing;
import com.megacrit.cardcrawl.vfx.RainingGoldEffect;
import upgradeableevents.UpgradeEventManager;
import upgradeableevents.eventupgrades.Exordium.GoldenWingUpgrade;
import upgradeableevents.interfaces.AbstractEventUpgrade;
import upgradeableevents.util.ButtonEffectHelper;

@SpirePatch(clz = GoldenWing.class, method = "buttonEffect")
public class GoldenWingButtonEffectPatch {
    @SpirePrefixPatch
    public static SpireReturn<Void> Prefix(GoldenWing __instance, int buttonPressed) {

        AbstractEventUpgrade currentUpgrade = UpgradeEventManager.getCurrentUpgrade();

        if (!(currentUpgrade instanceof GoldenWingUpgrade)) {
            return SpireReturn.Continue();
        }

        if (!((GoldenWingUpgrade)currentUpgrade).isUpgraded()) {
            return SpireReturn.Continue();
        }

        Enum<?> screen = ReflectionHacks.getPrivate(__instance, GoldenWing.class, "screen");

        if (screen.name().equals("INTRO")) {
            if (buttonPressed == 0) {
                // Handle the card removal option (no HP loss)
                __instance.imageEventText.updateBodyText(GoldenWing.DESCRIPTIONS[1]);
                __instance.imageEventText.updateDialogOption(0, GoldenWing.OPTIONS[8]);
                __instance.imageEventText.removeDialogOption(1);
                __instance.imageEventText.removeDialogOption(1);

                try {
                    Class<?> curScreenEnum = Class.forName("com.megacrit.cardcrawl.events.exordium.GoldenWing$CUR_SCREEN");
                    ReflectionHacks.setPrivate(__instance, GoldenWing.class, "screen",
                            Enum.valueOf((Class<Enum>)curScreenEnum, "PURGE"));
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return SpireReturn.Return(null);
            } else if (buttonPressed == 1) {
                boolean canAttack = ReflectionHacks.getPrivate(__instance, GoldenWing.class, "canAttack");
                if (canAttack) {
                    // Handle the gold option with increased range
                    int goldAmount = AbstractDungeon.miscRng.random(50, 160);
                    AbstractDungeon.effectList.add(new RainingGoldEffect(goldAmount));
                    AbstractDungeon.player.gainGold(goldAmount);

                    __instance.imageEventText.updateBodyText(GoldenWing.DESCRIPTIONS[2]);
                    __instance.imageEventText.updateDialogOption(0, GoldenWing.OPTIONS[7]);
                    __instance.imageEventText.removeDialogOption(1);
                    __instance.imageEventText.removeDialogOption(1);

                    try {
                        Class<?> curScreenEnum = Class.forName("com.megacrit.cardcrawl.events.exordium.GoldenWing$CUR_SCREEN");
                        ReflectionHacks.setPrivate(__instance, GoldenWing.class, "screen",
                                Enum.valueOf((Class<Enum>)curScreenEnum, "MAP"));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    AbstractEvent.logMetricGainGold("Golden Wing", "Gained Gold", goldAmount);
                    return SpireReturn.Return(null);
                }
            }
        }

        return SpireReturn.Continue();
    }

    @SpirePostfixPatch
    public static void Postfix(GoldenWing __instance, int buttonPressed) {
        ButtonEffectHelper.updateUpgradeAvailability();
    }
}