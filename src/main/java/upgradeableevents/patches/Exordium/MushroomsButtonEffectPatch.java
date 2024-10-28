package upgradeableevents.patches.Exordium;

import basemod.ReflectionHacks;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.curses.Clumsy;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.events.AbstractEvent;
import com.megacrit.cardcrawl.events.exordium.Mushrooms;
import com.megacrit.cardcrawl.relics.Circlet;
import com.megacrit.cardcrawl.relics.OddMushroom;
import javassist.CtBehavior;
import upgradeableevents.UpgradeEventManager;
import upgradeableevents.eventupgrades.Exordium.MushroomsUpgrade;
import upgradeableevents.interfaces.AbstractEventUpgrade;
import upgradeableevents.util.ButtonEffectHelper;


@SpirePatch(clz = Mushrooms.class, method = "buttonEffect")
public class MushroomsButtonEffectPatch {
    @SpireInsertPatch(
            locator = FightLocator.class
    )
    public static void InsertFight(Mushrooms __instance, int buttonPressed) {
        AbstractEventUpgrade currentUpgrade = UpgradeEventManager.getCurrentUpgrade();

        if (currentUpgrade instanceof MushroomsUpgrade &&
                ((MushroomsUpgrade)currentUpgrade).isUpgraded() &&
                buttonPressed == 0) {

            // Modify gold rewards
            if (Settings.isDailyRun) {
                int goldAmt = (int)(AbstractDungeon.miscRng.random(25) * MushroomsUpgrade.GOLD_MULTIPLIER);
                AbstractDungeon.getCurrRoom().addGoldToRewards(goldAmt);
            } else {
                int goldAmt = (int)(AbstractDungeon.miscRng.random(20, 30) * MushroomsUpgrade.GOLD_MULTIPLIER);
                AbstractDungeon.getCurrRoom().addGoldToRewards(goldAmt);
            }
        }
    }

    private static class FightLocator extends SpireInsertLocator {
        @Override
        public int[] Locate(CtBehavior ctMethodToPatch) throws Exception {
            Matcher finalMatcher = new Matcher.MethodCallMatcher(
                    AbstractDungeon.class, "getCurrRoom"
            );
            return LineFinder.findInOrder(ctMethodToPatch, finalMatcher);
        }
    }

    @SpireInsertPatch(
            locator = HealLocator.class,
            localvars = {"curse"}
    )
    public static void InsertHeal(Mushrooms __instance, int buttonPressed, @ByRef AbstractCard[] curse) {
        AbstractEventUpgrade currentUpgrade = UpgradeEventManager.getCurrentUpgrade();

        if (currentUpgrade instanceof MushroomsUpgrade &&
                ((MushroomsUpgrade)currentUpgrade).isUpgraded() &&
                buttonPressed == 1) {
            curse[0] = new Clumsy();
        }
    }

    private static class HealLocator extends SpireInsertLocator {
        @Override
        public int[] Locate(CtBehavior ctMethodToPatch) throws Exception {
            Matcher finalMatcher = new Matcher.MethodCallMatcher(
                    AbstractEvent.class, "logMetricObtainCardAndHeal"
            );
            return LineFinder.findInOrder(ctMethodToPatch, finalMatcher);
        }
    }

    @SpirePostfixPatch
    public static void Postfix(Mushrooms __instance, int buttonPressed) {
        ButtonEffectHelper.updateUpgradeAvailability();
    }
}