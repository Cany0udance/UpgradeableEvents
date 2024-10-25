package upgradeableevents.patches;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.events.AbstractEvent;
import upgradeableevents.UpgradeEventManager;

@SpirePatch(clz = AbstractEvent.class, method = SpirePatch.CONSTRUCTOR)
public class EventConstructorPatch {
    @SpirePostfixPatch
    public static void Postfix(AbstractEvent __instance) {
        UpgradeEventManager.setEventUpgradeAvailable(true);
    }
}