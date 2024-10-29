package upgradeableevents.patches.TheCity;

import basemod.ReflectionHacks;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.events.AbstractEvent;
import com.megacrit.cardcrawl.events.city.TheJoust;
import javassist.CtBehavior;
import upgradeableevents.UpgradeEventManager;
import upgradeableevents.eventupgrades.TheCity.TheJoustUpgrade;
import upgradeableevents.interfaces.AbstractEventUpgrade;
import upgradeableevents.util.ButtonEffectHelper;

@SpirePatch(clz = TheJoust.class, method = "buttonEffect")
public class TheJoustButtonEffectPatch {
    @SpireInsertPatch(
            locator = Locator.class
    )
    public static SpireReturn<Void> Insert(TheJoust __instance, int buttonPressed) {
        AbstractEventUpgrade currentUpgrade = UpgradeEventManager.getCurrentUpgrade();
        if (currentUpgrade instanceof TheJoustUpgrade && ((TheJoustUpgrade)currentUpgrade).isUpgraded()) {
            Enum<?> screen = ReflectionHacks.getPrivate(__instance, TheJoust.class, "screen");
            boolean betFor = ReflectionHacks.getPrivate(__instance, TheJoust.class, "betFor");
            if ("JOUST".equals(screen.name())) {
                boolean win;
                if (betFor) {
                    // 10% chance to win big when betting on owner
                    win = AbstractDungeon.miscRng.randomBoolean(0.1F);
                } else {
                    // 90% chance to win when betting against owner
                    win = AbstractDungeon.miscRng.randomBoolean(0.9F);
                }

                ReflectionHacks.setPrivate(__instance, TheJoust.class, "ownerWins", betFor ? win : !win);

                String tmp;
                if (win) {
                    if (betFor) {
                        // Jackpot win
                        tmp = TheJoust.DESCRIPTIONS[5] + TheJoust.DESCRIPTIONS[7];
                        AbstractDungeon.player.gainGold(1500);
                        CardCrawlGame.sound.play("GOLD_GAIN");
                        AbstractEvent.logMetricGainAndLoseGold("The Joust", "Bet on Owner (Jackpot)", 1500, 50);
                    } else {
                        // Regular win
                        tmp = TheJoust.DESCRIPTIONS[6] + TheJoust.DESCRIPTIONS[7];
                        AbstractDungeon.player.gainGold(200);
                        CardCrawlGame.sound.play("GOLD_GAIN");
                        AbstractEvent.logMetricGainAndLoseGold("The Joust", "Bet on Murderer", 200, 50);
                    }
                } else {
                    tmp = (betFor ? TheJoust.DESCRIPTIONS[6] : TheJoust.DESCRIPTIONS[5]) + TheJoust.DESCRIPTIONS[8];
                    AbstractEvent.logMetricLoseGold("The Joust", "Bet Loss", 50);
                }

                __instance.imageEventText.updateBodyText(tmp);
                __instance.imageEventText.updateDialogOption(0, TheJoust.OPTIONS[7]);

                Class<?> screenEnum = TheJoust.class.getDeclaredClasses()[0];
                Object completeScreen = Enum.valueOf((Class<Enum>) screenEnum, "COMPLETE");
                ReflectionHacks.setPrivate(__instance, TheJoust.class, "screen", completeScreen);
                return SpireReturn.Return(null);
            }
        }
        return SpireReturn.Continue();
    }

    private static class Locator extends SpireInsertLocator {
        @Override
        public int[] Locate(CtBehavior ctMethodToPatch) throws Exception {
            Matcher finalMatcher = new Matcher.FieldAccessMatcher(TheJoust.class, "screen");
            return LineFinder.findInOrder(ctMethodToPatch, finalMatcher);
        }
    }

    @SpirePostfixPatch
    public static void Postfix(TheJoust __instance, int buttonPressed) {
        ButtonEffectHelper.updateUpgradeAvailability();
    }
}