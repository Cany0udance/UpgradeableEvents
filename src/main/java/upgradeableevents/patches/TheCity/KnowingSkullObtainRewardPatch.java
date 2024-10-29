package upgradeableevents.patches.TheCity;

import basemod.ReflectionHacks;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.events.city.KnowingSkull;
import upgradeableevents.UpgradeEventManager;
import upgradeableevents.eventupgrades.TheCity.KnowingSkullUpgrade;
import upgradeableevents.interfaces.AbstractEventUpgrade;

@SpirePatch(clz = KnowingSkull.class, method = "obtainReward")
public class KnowingSkullObtainRewardPatch {
    @SpirePostfixPatch
    public static void Postfix(KnowingSkull __instance, int slot) {
        AbstractEventUpgrade currentUpgrade = UpgradeEventManager.getCurrentUpgrade();
        if (currentUpgrade instanceof KnowingSkullUpgrade && ((KnowingSkullUpgrade)currentUpgrade).isUpgraded()) {
            KnowingSkull skullEvent = __instance;
            KnowingSkullUpgrade upgrade = (KnowingSkullUpgrade)currentUpgrade;

            // Get the current costs
            int potionCost = ReflectionHacks.getPrivate(skullEvent, KnowingSkull.class, "potionCost");
            int goldCost = ReflectionHacks.getPrivate(skullEvent, KnowingSkull.class, "goldCost");
            int cardCost = ReflectionHacks.getPrivate(skullEvent, KnowingSkull.class, "cardCost");
            int leaveCost = ReflectionHacks.getPrivate(skullEvent, KnowingSkull.class, "leaveCost");

            // Update each option text to show either 0 or regular cost based on whether it's been used
            skullEvent.imageEventText.updateDialogOption(0,
                    KnowingSkull.OPTIONS[4] + (upgrade.hasUsedFreeOption(0) ? potionCost : 0) + KnowingSkull.OPTIONS[1]);
            skullEvent.imageEventText.updateDialogOption(1,
                    KnowingSkull.OPTIONS[5] + "90" + KnowingSkull.OPTIONS[6] + (upgrade.hasUsedFreeOption(1) ? goldCost : 0) + KnowingSkull.OPTIONS[1]);
            skullEvent.imageEventText.updateDialogOption(2,
                    KnowingSkull.OPTIONS[3] + (upgrade.hasUsedFreeOption(2) ? cardCost : 0) + KnowingSkull.OPTIONS[1]);
            skullEvent.imageEventText.updateDialogOption(3,
                    KnowingSkull.OPTIONS[7] + (upgrade.hasUsedFreeOption(3) ? leaveCost : 0) + KnowingSkull.OPTIONS[1]);
        }
    }
}