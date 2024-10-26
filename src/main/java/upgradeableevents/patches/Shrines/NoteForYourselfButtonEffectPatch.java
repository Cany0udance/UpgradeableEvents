package upgradeableevents.patches.Shrines;

import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.events.shrines.NoteForYourself;
import javassist.CtBehavior;
import upgradeableevents.UpgradeEventManager;
import upgradeableevents.eventupgrades.Shrines.NoteForYourselfUpgrade;
import upgradeableevents.interfaces.AbstractEventUpgrade;
import upgradeableevents.util.ButtonEffectHelper;

@SpirePatch(clz = NoteForYourself.class, method = "buttonEffect")
public class NoteForYourselfButtonEffectPatch {
    @SpireInsertPatch(
            locator = Locator.class
    )
    public static void Insert(NoteForYourself __instance, int buttonPressed) {
        AbstractEventUpgrade currentUpgrade = UpgradeEventManager.getCurrentUpgrade();
        if (buttonPressed == 0 && currentUpgrade instanceof NoteForYourselfUpgrade) {
            NoteForYourselfUpgrade upgrade = (NoteForYourselfUpgrade) currentUpgrade;
            if (upgrade.isUpgraded()) {
                AbstractDungeon.player.gainGold(100);
            }
        }
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
    public static void Postfix(NoteForYourself __instance, int buttonPressed) {
        ButtonEffectHelper.updateUpgradeAvailability();
    }
}