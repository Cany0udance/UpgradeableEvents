package upgradeableevents.patches.Shrines;

import basemod.ReflectionHacks;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.cards.CardGroup;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.events.shrines.Transmogrifier;
import javassist.CtBehavior;
import upgradeableevents.UpgradeEventManager;
import upgradeableevents.eventupgrades.Shrines.TransmogrifierUpgrade;
import upgradeableevents.interfaces.AbstractEventUpgrade;
import upgradeableevents.util.ButtonEffectHelper;

import static com.megacrit.cardcrawl.events.shrines.PurificationShrine.OPTIONS;

@SpirePatch(clz = Transmogrifier.class, method = "buttonEffect")
public class TransmogrifierButtonEffectPatch {
    @SpireInsertPatch(
            locator = Locator.class
    )
    public static SpireReturn<Void> Insert(Transmogrifier __instance, int buttonPressed) {
        AbstractEventUpgrade currentUpgrade = UpgradeEventManager.getCurrentUpgrade();
        if (buttonPressed == 0 && currentUpgrade instanceof TransmogrifierUpgrade) {
            TransmogrifierUpgrade upgrade = (TransmogrifierUpgrade) currentUpgrade;
            if (upgrade.isUpgraded()) {
                try {
                    Class<?> screenEnumClass = Class.forName("com.megacrit.cardcrawl.events.shrines.Transmogrifier$CUR_SCREEN");
                    Enum<?> completeScreen = Enum.valueOf((Class<Enum>) screenEnumClass, "COMPLETE");
                    ReflectionHacks.setPrivate(__instance, Transmogrifier.class, "screen", completeScreen);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }

                String dialog2 = (String)ReflectionHacks.getPrivateStatic(Transmogrifier.class, "DIALOG_2");
                __instance.imageEventText.updateBodyText(dialog2);
                AbstractDungeon.gridSelectScreen.open(
                        CardGroup.getGroupWithoutBottledCards(AbstractDungeon.player.masterDeck.getPurgeableCards()),
                        1,
                        OPTIONS[2],
                        false, true, false, false
                );
                __instance.imageEventText.updateDialogOption(0, OPTIONS[1]);
                __instance.imageEventText.clearRemainingOptions();
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
    public static void Postfix(Transmogrifier __instance, int buttonPressed) {
        ButtonEffectHelper.updateUpgradeAvailability();
    }
}