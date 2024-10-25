package upgradeableevents.patches;

import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.curses.Clumsy;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.events.shrines.GoldShrine;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import javassist.CtBehavior;
import upgradeableevents.UpgradeEventManager;
import upgradeableevents.eventupgrades.Shrines.GoldShrineUpgrade;
import upgradeableevents.interfaces.AbstractEventUpgrade;
import upgradeableevents.util.ButtonEffectHelper;

@SpirePatch(clz = GoldShrine.class, method = "buttonEffect")
public class GoldShrineButtonEffectPatch {
    @SpireInsertPatch(
            locator = PrayLocator.class
    )
    public static void InsertPray(GoldShrine __instance, int buttonPressed) {

        AbstractEventUpgrade currentUpgrade = UpgradeEventManager.getCurrentUpgrade();

        if (currentUpgrade instanceof GoldShrineUpgrade && ((GoldShrineUpgrade)currentUpgrade).isUpgraded() && buttonPressed == 0) {
            float roll = AbstractDungeon.eventRng.random();

            if (roll < GoldShrineUpgrade.RELIC_CHANCE) {
                AbstractRelic relic = AbstractDungeon.returnRandomRelic(AbstractRelic.RelicTier.RARE);

                AbstractDungeon.getCurrRoom().spawnRelicAndObtain(
                        Settings.WIDTH / 2.0f,
                        Settings.HEIGHT / 2.0f,
                        relic
                );
            }
        }
    }

    private static class PrayLocator extends SpireInsertLocator {
        @Override
        public int[] Locate(CtBehavior ctMethodToPatch) throws Exception {
            Matcher finalMatcher = new Matcher.MethodCallMatcher(
                    GoldShrine.class, "logMetricGainGold"
            );
            return LineFinder.findInOrder(ctMethodToPatch, finalMatcher);
        }
    }

    @SpireInsertPatch(
            locator = DesecrateLocator.class,
            localvars = {"curse"}
    )
    public static void InsertDesecrate(GoldShrine __instance, int buttonPressed, @ByRef AbstractCard[] curse) {

        AbstractEventUpgrade currentUpgrade = UpgradeEventManager.getCurrentUpgrade();

        if (currentUpgrade instanceof GoldShrineUpgrade && ((GoldShrineUpgrade)currentUpgrade).isUpgraded() && buttonPressed == 1) {
            curse[0] = new Clumsy();
        }
    }

    private static class DesecrateLocator extends SpireInsertLocator {
        @Override
        public int[] Locate(CtBehavior ctMethodToPatch) throws Exception {
            Matcher finalMatcher = new Matcher.MethodCallMatcher(
                    GoldShrine.class, "logMetricGainGoldAndCard"
            );
            return LineFinder.findInOrder(ctMethodToPatch, finalMatcher);
        }
    }

    @SpirePostfixPatch
    public static void Postfix(GoldShrine __instance, int buttonPressed) {
        ButtonEffectHelper.updateUpgradeAvailability();
    }
}