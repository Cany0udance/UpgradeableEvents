package upgradeableevents.patches.Exordium;

import basemod.ReflectionHacks;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.curses.Regret;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.events.AbstractEvent;
import com.megacrit.cardcrawl.events.exordium.BigFish;
import com.megacrit.cardcrawl.helpers.CardLibrary;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.vfx.cardManip.ShowCardAndObtainEffect;
import javassist.CtBehavior;
import upgradeableevents.UpgradeEventManager;
import upgradeableevents.eventupgrades.Exordium.BigFishUpgrade;
import upgradeableevents.interfaces.AbstractEventUpgrade;
import upgradeableevents.util.ButtonEffectHelper;

import java.util.ArrayList;

@SpirePatch(clz = BigFish.class, method = "buttonEffect")
public class BigFishButtonEffectPatch {

    @SpireInsertPatch(
            locator = Locator.class
    )
    public static SpireReturn<Void> Insert(BigFish __instance, int buttonPressed) {
        AbstractEventUpgrade currentUpgrade = UpgradeEventManager.getCurrentUpgrade();
        if (currentUpgrade instanceof BigFishUpgrade && ((BigFishUpgrade)currentUpgrade).isUpgraded()) {
            Enum<?> screen = ReflectionHacks.getPrivate(__instance, BigFish.class, "screen");

            if ("INTRO".equals(screen.name())) {
                switch (buttonPressed) {
                    case 0: // Banana - 1/2 max HP heal instead of 1/3
                        int healAmt = AbstractDungeon.player.maxHealth / 2;
                        AbstractDungeon.player.heal(healAmt, true);
                        __instance.imageEventText.updateBodyText(BigFish.DESCRIPTIONS[1]);
                        AbstractEvent.logMetricHeal("Big Fish", "Banana", healAmt);
                        break;

                    case 1: // Donut - 10 max HP instead of 5
                        AbstractDungeon.player.increaseMaxHp(10, true);
                        __instance.imageEventText.updateBodyText(BigFish.DESCRIPTIONS[2]);
                        AbstractEvent.logMetricMaxHPGain("Big Fish", "Donut", 10);
                        break;

                    case 2: // Box - Two relics instead of one
                        __instance.imageEventText.updateBodyText(BigFish.DESCRIPTIONS[4] + BigFish.DESCRIPTIONS[5]);
                        AbstractCard c = new Regret();
                        AbstractRelic r1 = AbstractDungeon.returnRandomScreenlessRelic(AbstractDungeon.returnRandomRelicTier());
                        AbstractRelic r2 = AbstractDungeon.returnRandomScreenlessRelic(AbstractDungeon.returnRandomRelicTier());

                        // Log metrics for both relics
                        ArrayList<String> relics = new ArrayList<>();
                        relics.add(r1.relicId);
                        relics.add(r2.relicId);
                        AbstractEvent.logMetric("Big Fish", "Box", relics, null, null, null, null, null, null, 0, 0, 0, 0, 0, 0);

                        AbstractDungeon.effectList.add(new ShowCardAndObtainEffect(CardLibrary.getCopy(c.cardID), Settings.WIDTH / 2.0F, Settings.HEIGHT / 2.0F));
                        AbstractDungeon.getCurrRoom().spawnRelicAndObtain(Settings.WIDTH / 3.0F, Settings.HEIGHT / 2.0F, r1);
                        AbstractDungeon.getCurrRoom().spawnRelicAndObtain(Settings.WIDTH * 2.0F / 3.0F, Settings.HEIGHT / 2.0F, r2);
                        break;
                }

                __instance.imageEventText.clearAllDialogs();
                __instance.imageEventText.setDialogOption(BigFish.OPTIONS[5]);

                // Set screen to RESULT
                Class<?> screenEnum = BigFish.class.getDeclaredClasses()[0];
                Object resultScreen = Enum.valueOf((Class<Enum>) screenEnum, "RESULT");
                ReflectionHacks.setPrivate(__instance, BigFish.class, "screen", resultScreen);

                return SpireReturn.Return(null);
            }
        }
        return SpireReturn.Continue();
    }

    private static class Locator extends SpireInsertLocator {
        @Override
        public int[] Locate(CtBehavior ctMethodToPatch) throws Exception {
            Matcher finalMatcher = new Matcher.FieldAccessMatcher(BigFish.class, "screen");
            return LineFinder.findInOrder(ctMethodToPatch, finalMatcher);
        }
    }

    @SpirePostfixPatch
    public static void Postfix(BigFish __instance, int buttonPressed) {
        ButtonEffectHelper.updateUpgradeAvailability();
    }
}