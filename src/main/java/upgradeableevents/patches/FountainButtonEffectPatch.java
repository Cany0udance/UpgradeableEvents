package upgradeableevents.patches;

import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.events.shrines.FountainOfCurseRemoval;
import com.megacrit.cardcrawl.vfx.AbstractGameEffect;
import com.megacrit.cardcrawl.vfx.cardManip.PurgeCardEffect;
import javassist.CtBehavior;
import upgradeableevents.UpgradeEventManager;
import upgradeableevents.eventupgrades.Shrines.DuplicatorUpgrade;
import upgradeableevents.eventupgrades.Shrines.FountainOfCurseRemovalUpgrade;
import upgradeableevents.interfaces.AbstractEventUpgrade;
import upgradeableevents.util.ButtonEffectHelper;

import java.util.ArrayList;

@SpirePatch(clz = FountainOfCurseRemoval.class, method = "buttonEffect")
public class FountainButtonEffectPatch {
    private static final ArrayList<AbstractGameEffect> countedEffects = new ArrayList<>();

    @SpireInsertPatch(
            locator = Locator.class
    )
    public static void Insert(FountainOfCurseRemoval __instance, int buttonPressed) {
        AbstractEventUpgrade currentUpgrade = UpgradeEventManager.getCurrentUpgrade();
        if (currentUpgrade instanceof FountainOfCurseRemovalUpgrade && ((FountainOfCurseRemovalUpgrade)currentUpgrade).isUpgraded() && buttonPressed == 0) {
            // Count new PurgeCardEffects that we haven't counted before
            int newCursesRemoved = 0;

            for (AbstractGameEffect effect : AbstractDungeon.effectList) {
                if (effect instanceof PurgeCardEffect && !countedEffects.contains(effect)) {
                    newCursesRemoved++;
                    countedEffects.add(effect);
                }
            }

            // Grant max HP for new curses removed
            if (newCursesRemoved > 0) {
                int hpGain = newCursesRemoved * FountainOfCurseRemovalUpgrade.HP_PER_CURSE;
                AbstractDungeon.player.increaseMaxHp(hpGain, true);
            }
        }
    }

    private static class Locator extends SpireInsertLocator {
        @Override
        public int[] Locate(CtBehavior ctMethodToPatch) throws Exception {
            Matcher finalMatcher = new Matcher.MethodCallMatcher(
                    FountainOfCurseRemoval.class, "logMetricRemoveCards"
            );
            return LineFinder.findInOrder(ctMethodToPatch, finalMatcher);
        }
    }

    @SpirePostfixPatch
    public static void Postfix(FountainOfCurseRemoval __instance, int buttonPressed) {
        ButtonEffectHelper.updateUpgradeAvailability();
    }
}