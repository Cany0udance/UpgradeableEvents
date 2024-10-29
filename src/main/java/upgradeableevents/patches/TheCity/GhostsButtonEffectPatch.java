package upgradeableevents.patches.TheCity;

import basemod.BaseMod;
import basemod.ReflectionHacks;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.events.city.Ghosts;
import javassist.CtBehavior;
import upgradeableevents.UpgradeEventManager;
import upgradeableevents.eventupgrades.TheCity.GhostsUpgrade;
import upgradeableevents.interfaces.AbstractEventUpgrade;
import upgradeableevents.util.ButtonEffectHelper;

import java.lang.reflect.Method;

@SpirePatch(clz = Ghosts.class, method = "buttonEffect")
public class GhostsButtonEffectPatch {
    @SpireInsertPatch(
            locator = Locator.class
    )
    public static SpireReturn<Void> Insert(Ghosts __instance, int buttonPressed) {

        AbstractEventUpgrade currentUpgrade = UpgradeEventManager.getCurrentUpgrade();
        if (currentUpgrade instanceof GhostsUpgrade && ((GhostsUpgrade)currentUpgrade).isUpgraded()) {
            int screenNum = ReflectionHacks.getPrivate(__instance, Ghosts.class, "screenNum");

            if (screenNum == 0 && buttonPressed == 0) {

                int hpLoss = ReflectionHacks.getPrivate(__instance, Ghosts.class, "hpLoss");
                __instance.imageEventText.updateBodyText(Ghosts.DESCRIPTIONS[2]);
                AbstractDungeon.player.decreaseMaxHealth(hpLoss);

                // Call the original becomeGhost method using reflection
                try {
                    Method becomeGhost = Ghosts.class.getDeclaredMethod("becomeGhost");
                    becomeGhost.setAccessible(true);
                    becomeGhost.invoke(__instance);
                } catch (Exception e) {
                }

                ReflectionHacks.setPrivate(__instance, Ghosts.class, "screenNum", 1);
                __instance.imageEventText.updateDialogOption(0, Ghosts.OPTIONS[5]);
                __instance.imageEventText.clearRemainingOptions();

                return SpireReturn.Return(null);
            }
        }
        return SpireReturn.Continue();
    }

    private static class Locator extends SpireInsertLocator {
        @Override
        public int[] Locate(CtBehavior ctMethodToPatch) throws Exception {
            Matcher finalMatcher = new Matcher.FieldAccessMatcher(Ghosts.class, "screenNum");
            int[] matches = LineFinder.findAllInOrder(ctMethodToPatch, finalMatcher);
            return new int[]{matches[0]};
        }
    }

    @SpirePostfixPatch
    public static void Postfix(Ghosts __instance, int buttonPressed) {
        ButtonEffectHelper.updateUpgradeAvailability();
    }
}