package upgradeableevents.patches.TheCity;

import basemod.BaseMod;
import basemod.ReflectionHacks;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.events.city.Colosseum;
import com.megacrit.cardcrawl.helpers.MonsterHelper;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import javassist.CtBehavior;
import upgradeableevents.UpgradeEventManager;
import upgradeableevents.eventupgrades.TheCity.ColosseumUpgrade;
import upgradeableevents.interfaces.AbstractEventUpgrade;
import upgradeableevents.util.ButtonEffectHelper;

@SpirePatch(clz = Colosseum.class, method = "buttonEffect")
public class ColosseumButtonEffectPatch {
    @SpireInsertPatch(
            locator = Locator.class
    )
    public static SpireReturn<Void> Insert(Colosseum __instance, int buttonPressed) {
        BaseMod.logger.info("Colosseum patch triggered, buttonPressed: " + buttonPressed);

        AbstractEventUpgrade currentUpgrade = UpgradeEventManager.getCurrentUpgrade();
        if (currentUpgrade instanceof ColosseumUpgrade && ((ColosseumUpgrade)currentUpgrade).isUpgraded()) {
            BaseMod.logger.info("Colosseum is upgraded");
            Enum<?> screen = ReflectionHacks.getPrivate(__instance, Colosseum.class, "screen");

            if ("FIGHT".equals(screen.name())) {
                BaseMod.logger.info("Screen is INTRO, handling upgraded option: " + buttonPressed);
                if (buttonPressed == 0) {
                    // Set up Nobs fight directly
                    Class<?> screenEnum = Colosseum.class.getDeclaredClasses()[0];
                    Object leaveScreen = Enum.valueOf((Class<Enum>) screenEnum, "LEAVE");
                    ReflectionHacks.setPrivate(__instance, Colosseum.class, "screen", leaveScreen);

                    // Update the event text
                    __instance.imageEventText.updateBodyText(Colosseum.DESCRIPTIONS[4]);

                    __instance.logMetric("Skipped to Nobs");
                    AbstractDungeon.getCurrRoom().monsters = MonsterHelper.getEncounter("Colosseum Nobs");
                    AbstractDungeon.getCurrRoom().rewards.clear();
                    AbstractDungeon.getCurrRoom().addRelicToRewards(AbstractRelic.RelicTier.RARE);
                    AbstractDungeon.getCurrRoom().addRelicToRewards(AbstractRelic.RelicTier.UNCOMMON);
                    AbstractDungeon.getCurrRoom().addGoldToRewards(100);
                    AbstractDungeon.getCurrRoom().eliteTrigger = true;
                    __instance.enterCombatFromImage();
                    AbstractDungeon.lastCombatMetricKey = "Colosseum Nobs";

                    return SpireReturn.Return(null);
                }
            }
        }
        return SpireReturn.Continue();
    }

    private static class Locator extends SpireInsertLocator {
        @Override
        public int[] Locate(CtBehavior ctMethodToPatch) throws Exception {
            Matcher finalMatcher = new Matcher.FieldAccessMatcher(Colosseum.class, "screen");
            int[] matches = LineFinder.findAllInOrder(ctMethodToPatch, finalMatcher);
            return new int[]{matches[0]};
        }
    }

    @SpirePostfixPatch
    public static void Postfix(Colosseum __instance, int buttonPressed) {
        ButtonEffectHelper.updateUpgradeAvailability();
    }
}