package upgradeableevents.patches.Shrines;

import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.events.shrines.Lab;
import com.megacrit.cardcrawl.potions.PotionSlot;
import javassist.CtBehavior;
import upgradeableevents.UpgradeEventManager;
import upgradeableevents.eventupgrades.Shrines.LabUpgrade;
import upgradeableevents.interfaces.AbstractEventUpgrade;
import upgradeableevents.util.ButtonEffectHelper;

@SpirePatch(clz = Lab.class, method = "buttonEffect")
public class LabButtonEffectPatch {
    @SpireInsertPatch(
            locator = Locator.class
    )
    public static void Insert(Lab __instance, int buttonPressed) {
        AbstractEventUpgrade currentUpgrade = UpgradeEventManager.getCurrentUpgrade();
        if (currentUpgrade instanceof LabUpgrade && ((LabUpgrade)currentUpgrade).isUpgraded() && buttonPressed == 0) {
            // Increase potion slot capacity
            AbstractDungeon.player.potionSlots += 1;
            AbstractDungeon.player.potions.add(new PotionSlot(AbstractDungeon.player.potionSlots - 1));
        }
    }

    private static class Locator extends SpireInsertLocator {
        @Override
        public int[] Locate(CtBehavior ctMethodToPatch) throws Exception {
            Matcher finalMatcher = new Matcher.MethodCallMatcher(
                    Lab.class, "logMetric"
            );
            return LineFinder.findInOrder(ctMethodToPatch, finalMatcher);
        }
    }

    @SpirePostfixPatch
    public static void Postfix(Lab __instance, int buttonPressed) {
        ButtonEffectHelper.updateUpgradeAvailability();
    }
}
