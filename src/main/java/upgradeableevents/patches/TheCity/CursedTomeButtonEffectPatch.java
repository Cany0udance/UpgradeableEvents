package upgradeableevents.patches.TheCity;

import basemod.ReflectionHacks;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.events.AbstractEvent;
import com.megacrit.cardcrawl.events.city.CursedTome;
import javassist.CtBehavior;
import upgradeableevents.UpgradeEventManager;
import upgradeableevents.eventupgrades.TheCity.CursedTomeUpgrade;
import upgradeableevents.interfaces.AbstractEventUpgrade;
import upgradeableevents.util.ButtonEffectHelper;

import java.lang.reflect.Method;

@SpirePatch(clz = CursedTome.class, method = "buttonEffect")
public class CursedTomeButtonEffectPatch {
    @SpireInsertPatch(
            locator = Locator.class
    )
    public static SpireReturn<Void> Insert(CursedTome __instance, int buttonPressed) {

        AbstractEventUpgrade currentUpgrade = UpgradeEventManager.getCurrentUpgrade();
        if (currentUpgrade instanceof CursedTomeUpgrade && ((CursedTomeUpgrade)currentUpgrade).isUpgraded()) {
            Enum<?> screen = ReflectionHacks.getPrivate(__instance, CursedTome.class, "screen");

            if ("INTRO".equals(screen.name())) {

                int finalDmg = ReflectionHacks.getPrivate(__instance, CursedTome.class, "finalDmg");

                if (buttonPressed == 0) {
                    // Take the book directly
                    AbstractDungeon.player.damage(new DamageInfo(null, finalDmg, DamageInfo.DamageType.HP_LOSS));
                    ReflectionHacks.setPrivate(__instance, CursedTome.class, "damageTaken", finalDmg);

                    __instance.imageEventText.updateBodyText(CursedTome.DESCRIPTIONS[5]); // OBTAIN_MSG

                    // Call the original randomBook method using reflection
                    try {
                        Method randomBook = CursedTome.class.getDeclaredMethod("randomBook");
                        randomBook.setAccessible(true);
                        randomBook.invoke(__instance);
                    } catch (Exception e) {
                    }

                    __instance.imageEventText.clearAllDialogs();
                    __instance.imageEventText.setDialogOption(CursedTome.OPTIONS[7]); // OPT_LEAVE

                } else {
                    // Leave without taking damage
                    __instance.imageEventText.updateBodyText(CursedTome.DESCRIPTIONS[6]); // IGNORE_MSG
                    __instance.imageEventText.clearAllDialogs();
                    __instance.imageEventText.setDialogOption(CursedTome.OPTIONS[7]); // OPT_LEAVE
                    AbstractEvent.logMetricIgnored("Cursed Tome");

                    // Set to END screen
                    Class<?> screenEnum = CursedTome.class.getDeclaredClasses()[0];
                    Object endScreen = Enum.valueOf((Class<Enum>) screenEnum, "END");
                    ReflectionHacks.setPrivate(__instance, CursedTome.class, "screen", endScreen);
                }

                return SpireReturn.Return(null);
            }
        }
        return SpireReturn.Continue();
    }

    private static class Locator extends SpireInsertLocator {
        @Override
        public int[] Locate(CtBehavior ctMethodToPatch) throws Exception {
            Matcher finalMatcher = new Matcher.FieldAccessMatcher(CursedTome.class, "screen");
            int[] matches = LineFinder.findAllInOrder(ctMethodToPatch, finalMatcher);
            return new int[]{matches[0]};
        }
    }

    @SpirePostfixPatch
    public static void Postfix(CursedTome __instance, int buttonPressed) {
        ButtonEffectHelper.updateUpgradeAvailability();
    }
}
