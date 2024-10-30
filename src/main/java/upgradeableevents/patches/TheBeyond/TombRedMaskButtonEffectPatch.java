package upgradeableevents.patches.TheBeyond;

import basemod.ReflectionHacks;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.events.AbstractEvent;
import com.megacrit.cardcrawl.events.beyond.TombRedMask;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.relics.RedMask;
import com.megacrit.cardcrawl.vfx.RainingGoldEffect;
import javassist.CtBehavior;
import upgradeableevents.UpgradeEventManager;
import upgradeableevents.eventupgrades.TheBeyond.TombRedMaskUpgrade;
import upgradeableevents.interfaces.AbstractEventUpgrade;
import upgradeableevents.util.ButtonEffectHelper;

@SpirePatch(clz = TombRedMask.class, method = "buttonEffect")
public class TombRedMaskButtonEffectPatch {
    @SpireInsertPatch(
            locator = Locator.class
    )
    public static SpireReturn<Void> Insert(TombRedMask __instance, int buttonPressed) {
        AbstractEventUpgrade currentUpgrade = UpgradeEventManager.getCurrentUpgrade();
        if (currentUpgrade instanceof TombRedMaskUpgrade && ((TombRedMaskUpgrade)currentUpgrade).isUpgraded()) {
            Enum<?> screen = ReflectionHacks.getPrivate(__instance, TombRedMask.class, "screen");

            if ("INTRO".equals(screen.name())) {
                if (buttonPressed == 0 && AbstractDungeon.player.hasRelic("Red Mask")) {
                    // Increased gold reward (333 instead of 222)
                    AbstractDungeon.effectList.add(new RainingGoldEffect(333));
                    AbstractDungeon.player.gainGold(333);
                    __instance.imageEventText.updateBodyText(TombRedMask.DESCRIPTIONS[1]);
                    AbstractEvent.logMetricGainGold("Tomb of Lord Red Mask", "Wore Mask (Upgraded)", 333);

                    __instance.imageEventText.clearAllDialogs();
                    __instance.imageEventText.setDialogOption(TombRedMask.OPTIONS[4]);

                    // Set screen to RESULT
                    Class<?> screenEnum = TombRedMask.class.getDeclaredClasses()[0];
                    Object resultScreen = Enum.valueOf((Class<Enum>) screenEnum, "RESULT");
                    ReflectionHacks.setPrivate(__instance, TombRedMask.class, "screen", resultScreen);

                    return SpireReturn.Return(null);
                } else if (buttonPressed == 1 && !AbstractDungeon.player.hasRelic("Red Mask")) {
                    // Only pay 1/3 of gold for mask
                    int goldCost = AbstractDungeon.player.gold / 3;
                    AbstractRelic r = new RedMask();
                    AbstractEvent.logMetricObtainRelicAtCost("Tomb of Lord Red Mask", "Paid (Upgraded)", r, goldCost);
                    AbstractDungeon.player.loseGold(goldCost);
                    AbstractDungeon.getCurrRoom().spawnRelicAndObtain(
                            (float)(Settings.WIDTH / 2),
                            (float)(Settings.HEIGHT / 2),
                            r
                    );
                    __instance.imageEventText.updateBodyText(TombRedMask.DESCRIPTIONS[2]);

                    __instance.imageEventText.clearAllDialogs();
                    __instance.imageEventText.setDialogOption(TombRedMask.OPTIONS[4]);

                    // Set screen to RESULT
                    Class<?> screenEnum = TombRedMask.class.getDeclaredClasses()[0];
                    Object resultScreen = Enum.valueOf((Class<Enum>) screenEnum, "RESULT");
                    ReflectionHacks.setPrivate(__instance, TombRedMask.class, "screen", resultScreen);

                    return SpireReturn.Return(null);
                }
            }
        }
        return SpireReturn.Continue();
    }

    private static class Locator extends SpireInsertLocator {
        @Override
        public int[] Locate(CtBehavior ctMethodToPatch) throws Exception {
            Matcher finalMatcher = new Matcher.FieldAccessMatcher(TombRedMask.class, "screen");
            return LineFinder.findInOrder(ctMethodToPatch, finalMatcher);
        }
    }

    @SpirePostfixPatch
    public static void Postfix(TombRedMask __instance, int buttonPressed) {
        ButtonEffectHelper.updateUpgradeAvailability();
    }
}