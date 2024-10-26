package upgradeableevents.patches;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.potions.BlessingOfTheForge;
import upgradeableevents.UpgradeEventManager;

@SpirePatch(clz = BlessingOfTheForge.class, method = "use")
public class BlessingOfTheForgePatch {
    @SpirePrefixPatch
    public static SpireReturn<Void> Prefix(BlessingOfTheForge __instance, AbstractCreature target) {
        if (UpgradeEventManager.canUpgradeCurrentEvent()) {
            UpgradeEventManager.upgradeCurrentEvent();
            UpgradeEventManager.setEventUpgradeAvailable(false);
            return SpireReturn.Return(null);
        }
        return SpireReturn.Continue();
    }
}