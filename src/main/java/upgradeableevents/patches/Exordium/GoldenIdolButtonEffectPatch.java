package upgradeableevents.patches.Exordium;

import basemod.ReflectionHacks;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.events.AbstractEvent;
import com.megacrit.cardcrawl.events.exordium.GoldenIdolEvent;
import com.megacrit.cardcrawl.helpers.RelicLibrary;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import javassist.CtBehavior;
import upgradeableevents.UpgradeEventManager;
import upgradeableevents.eventupgrades.Exordium.GoldenIdolUpgrade;
import upgradeableevents.interfaces.AbstractEventUpgrade;
import upgradeableevents.util.ButtonEffectHelper;

@SpirePatch(clz = GoldenIdolEvent.class, method = "buttonEffect")
public class GoldenIdolButtonEffectPatch {
    @SpireInsertPatch(
            locator = TakeIdolLocator.class
    )
    public static SpireReturn<Void> InsertTakeIdol(GoldenIdolEvent __instance, int buttonPressed) {
        AbstractEventUpgrade currentUpgrade = UpgradeEventManager.getCurrentUpgrade();

        if (currentUpgrade instanceof GoldenIdolUpgrade &&
                ((GoldenIdolUpgrade)currentUpgrade).isUpgraded() &&
                buttonPressed == 0 &&
                ReflectionHacks.<Integer>getPrivate(__instance, GoldenIdolEvent.class, "screenNum") == 0) {

            // Get the relic to give
            AbstractRelic relicToGive;
            if (AbstractDungeon.player.hasRelic("Golden Idol")) {
                relicToGive = RelicLibrary.getRelic("Circlet").makeCopy();
            } else {
                relicToGive = RelicLibrary.getRelic("Golden Idol").makeCopy();
            }

            // Give the relic
            AbstractDungeon.getCurrRoom().spawnRelicAndObtain(
                    (float)(Settings.WIDTH / 2),
                    (float)(Settings.HEIGHT / 2),
                    relicToGive
            );

            // Update the event text
            __instance.imageEventText.updateBodyText(
                    GoldenIdolEvent.DESCRIPTIONS[1] + " " + GoldenIdolEvent.DESCRIPTIONS[3]
            );

            // Set up the leave option
            __instance.imageEventText.optionList.clear();
            __instance.imageEventText.setDialogOption(GoldenIdolEvent.OPTIONS[1]);

            // Set screenNum to 2 to end the event
            ReflectionHacks.setPrivate(__instance, GoldenIdolEvent.class, "screenNum", 2);

            // Play appropriate sound effects
            CardCrawlGame.screenShake.mildRumble(5.0F);
            CardCrawlGame.sound.play("BLUNT_HEAVY");

            // Log the metric
            AbstractEvent.logMetricObtainRelic("Golden Idol", "Upgraded Path", relicToGive);

            return SpireReturn.Return(null);
        }
        return SpireReturn.Continue();
    }

    private static class TakeIdolLocator extends SpireInsertLocator {
        @Override
        public int[] Locate(CtBehavior ctMethodToPatch) throws Exception {
            Matcher finalMatcher = new Matcher.FieldAccessMatcher(
                    GoldenIdolEvent.class, "screenNum"
            );
            return LineFinder.findInOrder(ctMethodToPatch, finalMatcher);
        }
    }

    @SpirePostfixPatch
    public static void Postfix(GoldenIdolEvent __instance, int buttonPressed) {
        ButtonEffectHelper.updateUpgradeAvailability();
    }
}