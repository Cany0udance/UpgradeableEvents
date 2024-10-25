package upgradeableevents.patches;

import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.potions.AbstractPotion;
import com.megacrit.cardcrawl.potions.BlessingOfTheForge;
import javassist.CtBehavior;
import upgradeableevents.UpgradeEventManager;

@SpirePatch(clz = AbstractPotion.class, method = "canUse")
public class AbstractPotionPatch {
    @SpireInsertPatch(locator = Locator.class)
    public static SpireReturn<Boolean> Insert(AbstractPotion __instance) {
        if (__instance.ID.equals(BlessingOfTheForge.POTION_ID) &&
                UpgradeEventManager.canUpgradeCurrentEvent()) {
            return SpireReturn.Return(true);
        }
        return SpireReturn.Continue();
    }

    private static class Locator extends SpireInsertLocator {
        @Override
        public int[] Locate(CtBehavior ctMethodToPatch) throws Exception {
            Matcher finalMatcher = new Matcher.MethodCallMatcher(AbstractDungeon.class, "getCurrRoom");
            return LineFinder.findInOrder(ctMethodToPatch, finalMatcher);
        }
    }
}