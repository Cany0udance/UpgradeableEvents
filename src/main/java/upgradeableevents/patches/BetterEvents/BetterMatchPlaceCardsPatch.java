package upgradeableevents.patches.BetterEvents;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.events.AbstractEvent;
import upgradeableevents.eventupgrades.BetterEvents.BetterMatchUpgrade;
import upgradeableevents.util.ButtonEffectHelper;

@SpirePatch(
        optional = true,
        cls = "betterMatch.events.BetterMatchEvent",
        method = "placeCards"
)
public class BetterMatchPlaceCardsPatch {
    @SpirePostfixPatch
    public static void Postfix(AbstractEvent __instance) {
        if (BetterMatchUpgrade.isBetterMatchLoaded) {
            ButtonEffectHelper.updateUpgradeAvailability();
        }
    }
}