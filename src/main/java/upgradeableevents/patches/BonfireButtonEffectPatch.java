package upgradeableevents.patches;

import basemod.ReflectionHacks;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.cards.CardGroup;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.events.shrines.Bonfire;
import javassist.CtBehavior;
import upgradeableevents.UpgradeEventManager;
import upgradeableevents.eventupgrades.Shrines.BonfireUpgrade;
import upgradeableevents.interfaces.AbstractEventUpgrade;
import upgradeableevents.util.ButtonEffectHelper;

@SpirePatch(clz = Bonfire.class, method = "buttonEffect")
public class BonfireButtonEffectPatch {
    @SpireInsertPatch(
            locator = Locator.class
    )
    public static SpireReturn<Void> Insert(Bonfire __instance, int buttonPressed) {
        AbstractEventUpgrade currentUpgrade = UpgradeEventManager.getCurrentUpgrade();

        // If we're on the COMPLETE screen (Leave button), just let the original method handle it
        Object screenEnum = ReflectionHacks.getPrivate(__instance, Bonfire.class, "screen");
        int screenOrdinal = ((Enum<?>) screenEnum).ordinal();
        if (screenOrdinal == 2) { // COMPLETE
            return SpireReturn.Continue();
        }

        if (currentUpgrade instanceof BonfireUpgrade && ((BonfireUpgrade)currentUpgrade).isUpgraded()) {
            if (CardGroup.getGroupWithoutBottledCards(AbstractDungeon.player.masterDeck.getPurgeableCards()).size() > 0) {
                AbstractDungeon.gridSelectScreen.open(
                        CardGroup.getGroupWithoutBottledCards(AbstractDungeon.player.masterDeck.getPurgeableCards()),
                        2,
                        Bonfire.OPTIONS[3],
                        false, false, false, true
                );
                ReflectionHacks.setPrivate(__instance, Bonfire.class, "cardSelect", true);
                return SpireReturn.Return(null);
            }
        }
        return SpireReturn.Continue();
    }

    private static class Locator extends SpireInsertLocator {
        @Override
        public int[] Locate(CtBehavior ctMethodToPatch) throws Exception {
            Matcher finalMatcher = new Matcher.MethodCallMatcher(
                    CardGroup.class, "getGroupWithoutBottledCards"
            );
            return LineFinder.findInOrder(ctMethodToPatch, finalMatcher);
        }
    }

    @SpirePostfixPatch
    public static void Postfix(Bonfire __instance, int buttonPressed) {
        ButtonEffectHelper.updateUpgradeAvailability();
    }
}
