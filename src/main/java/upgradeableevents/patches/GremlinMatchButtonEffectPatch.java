package upgradeableevents.patches;

import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.events.shrines.GremlinMatchGame;
import upgradeableevents.util.ButtonEffectHelper;

@SpirePatch(clz = GremlinMatchGame.class, method = "placeCards")
public class GremlinMatchButtonEffectPatch {
    @SpirePostfixPatch
    public static void Postfix(GremlinMatchGame __instance) {
        ButtonEffectHelper.updateUpgradeAvailability();
    }
}