package upgradeableevents.patches.Shrines;

import basemod.ReflectionHacks;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.cards.CardGroup;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.events.shrines.PurificationShrine;
import javassist.CtBehavior;
import upgradeableevents.UpgradeEventManager;
import upgradeableevents.eventupgrades.Shrines.PurificationShrineUpgrade;
import upgradeableevents.interfaces.AbstractEventUpgrade;
import upgradeableevents.util.ButtonEffectHelper;

@SpirePatch(clz = PurificationShrine.class, method = "buttonEffect")
public class PurificationShrineButtonEffectPatch {
    @SpireInsertPatch(
            locator = Locator.class
    )
    public static SpireReturn<Void> Insert(PurificationShrine __instance, int buttonPressed) {
        AbstractEventUpgrade currentUpgrade = UpgradeEventManager.getCurrentUpgrade();
        if (buttonPressed == 0 && currentUpgrade instanceof PurificationShrineUpgrade) {
            PurificationShrineUpgrade upgrade = (PurificationShrineUpgrade) currentUpgrade;
            if (upgrade.isUpgraded()) {
                // Open grid with option to select 2 cards instead of 1
                AbstractDungeon.gridSelectScreen.open(
                        CardGroup.getGroupWithoutBottledCards(AbstractDungeon.player.masterDeck.getPurgeableCards()),
                        2,  // Select 2 cards
                        PurificationShrine.OPTIONS[2],
                        false, false, false, true
                );
                __instance.imageEventText.updateDialogOption(0, PurificationShrine.OPTIONS[1]);
                __instance.imageEventText.clearRemainingOptions();
                try {
                    Class<?> screenEnumClass = Class.forName("com.megacrit.cardcrawl.events.shrines.PurificationShrine$CUR_SCREEN");
                    Enum<?> completeScreen = Enum.valueOf((Class<Enum>) screenEnumClass, "COMPLETE");
                    ReflectionHacks.setPrivate(__instance, PurificationShrine.class, "screen", completeScreen);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                return SpireReturn.Return();
            }
        }
        return SpireReturn.Continue();
    }

    private static class Locator extends SpireInsertLocator {
        @Override
        public int[] Locate(CtBehavior ctMethodToPatch) throws Exception {
            Matcher finalMatcher = new Matcher.FieldAccessMatcher(
                    AbstractDungeon.class, "gridSelectScreen"
            );
            return LineFinder.findInOrder(ctMethodToPatch, finalMatcher);
        }
    }

    @SpirePostfixPatch
    public static void Postfix(PurificationShrine __instance, int buttonPressed) {
        ButtonEffectHelper.updateUpgradeAvailability();
    }
}
