package upgradeableevents.patches.Shrines;

import basemod.ReflectionHacks;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.events.shrines.UpgradeShrine;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import javassist.CtBehavior;
import upgradeableevents.UpgradeEventManager;
import upgradeableevents.eventupgrades.Shrines.UpgradeShrineUpgrade;
import upgradeableevents.interfaces.AbstractEventUpgrade;
import upgradeableevents.util.ButtonEffectHelper;

import static com.megacrit.cardcrawl.events.shrines.UpgradeShrine.OPTIONS;

@SpirePatch(clz = UpgradeShrine.class, method = "buttonEffect")
public class UpgradeShrineButtonEffectPatch {
    @SpireInsertPatch(
            locator = Locator.class
    )
    public static SpireReturn<Void> Insert(UpgradeShrine __instance, int buttonPressed) {
        AbstractEventUpgrade currentUpgrade = UpgradeEventManager.getCurrentUpgrade();
        if (buttonPressed == 0 && currentUpgrade instanceof UpgradeShrineUpgrade) {
            UpgradeShrineUpgrade upgrade = (UpgradeShrineUpgrade) currentUpgrade;
            if (upgrade.isUpgraded()) {
                try {
                    Class<?> screenEnumClass = Class.forName("com.megacrit.cardcrawl.events.shrines.UpgradeShrine$CUR_SCREEN");
                    Enum<?> completeScreen = Enum.valueOf((Class<Enum>) screenEnumClass, "COMPLETE");
                    ReflectionHacks.setPrivate(__instance, UpgradeShrine.class, "screen", completeScreen);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }

                String dialog2 = (String)ReflectionHacks.getPrivateStatic(UpgradeShrine.class, "DIALOG_2");
                __instance.imageEventText.updateBodyText(dialog2);
                AbstractDungeon.gridSelectScreen.open(
                        AbstractDungeon.player.masterDeck.getUpgradableCards(),
                        1,
                        OPTIONS[2],
                        true, false, false, false
                );
                __instance.imageEventText.updateDialogOption(0, OPTIONS[1]);
                __instance.imageEventText.clearRemainingOptions();
                AbstractDungeon.getCurrRoom().phase = AbstractRoom.RoomPhase.COMPLETE;
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
    public static void Postfix(UpgradeShrine __instance, int buttonPressed) {
        ButtonEffectHelper.updateUpgradeAvailability();
    }
}