package upgradeableevents.patches.TheCity;

import basemod.ReflectionHacks;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.events.city.Beggar;
import javassist.CtBehavior;
import upgradeableevents.UpgradeEventManager;
import upgradeableevents.eventupgrades.TheCity.BeggarUpgrade;
import upgradeableevents.interfaces.AbstractEventUpgrade;
import upgradeableevents.util.ButtonEffectHelper;

@SpirePatch(clz = Beggar.class, method = "buttonEffect")
public class BeggarButtonEffectPatch {

    @SpireInsertPatch(
            locator = Locator.class
    )
    public static SpireReturn<Void> Insert(Beggar __instance, int buttonPressed) {
        AbstractEventUpgrade currentUpgrade = UpgradeEventManager.getCurrentUpgrade();
        if (currentUpgrade instanceof BeggarUpgrade && ((BeggarUpgrade)currentUpgrade).isUpgraded()) {
            Enum<?> screen = ReflectionHacks.getPrivate(__instance, Beggar.class, "screen");

            if ("INTRO".equals(screen.name()) && buttonPressed == 0) {
                // Load cleric image
                __instance.imageEventText.loadImage("images/events/cleric.jpg");
                // Update body text
                __instance.imageEventText.updateBodyText(Beggar.DESCRIPTIONS[2]);
                // Lose 1 gold instead of 75
                AbstractDungeon.player.loseGold(1);
                // Clear and set new dialog option
                __instance.imageEventText.clearAllDialogs();
                __instance.imageEventText.setDialogOption(Beggar.OPTIONS[4]);

                // Set screen to GAVE_MONEY using reflection
                Class<?> screenEnum = Beggar.class.getDeclaredClasses()[0];
                Object gaveMoneyScreen = Enum.valueOf((Class<Enum>) screenEnum, "GAVE_MONEY");
                ReflectionHacks.setPrivate(__instance, Beggar.class, "screen", gaveMoneyScreen);

                return SpireReturn.Return(null);
            }
        }
        return SpireReturn.Continue();
    }

    private static class Locator extends SpireInsertLocator {
        @Override
        public int[] Locate(CtBehavior ctMethodToPatch) throws Exception {
            Matcher finalMatcher = new Matcher.FieldAccessMatcher(Beggar.class, "screen");
            return LineFinder.findInOrder(ctMethodToPatch, finalMatcher);
        }
    }

    @SpirePostfixPatch
    public static void Postfix(Beggar __instance, int buttonPressed) {
        ButtonEffectHelper.updateUpgradeAvailability();
    }
}