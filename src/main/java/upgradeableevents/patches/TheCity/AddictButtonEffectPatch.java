package upgradeableevents.patches.TheCity;

import basemod.ReflectionHacks;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.curses.Shame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.events.AbstractEvent;
import com.megacrit.cardcrawl.events.city.Addict;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.vfx.cardManip.ShowCardAndObtainEffect;
import javassist.CtBehavior;
import upgradeableevents.UpgradeEventManager;
import upgradeableevents.eventupgrades.TheCity.AddictUpgrade;
import upgradeableevents.interfaces.AbstractEventUpgrade;
import upgradeableevents.util.ButtonEffectHelper;

@SpirePatch(clz = Addict.class, method = "buttonEffect")
public class AddictButtonEffectPatch {

    @SpireInsertPatch(
            locator = Locator.class
    )
    public static SpireReturn<Void> Insert(Addict __instance, int buttonPressed) {
        AbstractEventUpgrade currentUpgrade = UpgradeEventManager.getCurrentUpgrade();
        if (currentUpgrade instanceof AddictUpgrade && ((AddictUpgrade)currentUpgrade).isUpgraded()) {
            int screenNum = ReflectionHacks.getPrivate(__instance, Addict.class, "screenNum");
            float drawX = ReflectionHacks.getPrivate(__instance, AbstractEvent.class, "drawX");
            float drawY = ReflectionHacks.getPrivate(__instance, AbstractEvent.class, "drawY");

            if (screenNum == 0) {
                switch (buttonPressed) {
                    case 0:
                        // Pay gold option
                        __instance.imageEventText.updateBodyText(Addict.DESCRIPTIONS[1]);
                        if (AbstractDungeon.player.gold >= 1) {
                            AbstractRelic relic = AbstractDungeon.returnRandomScreenlessRelic(AbstractDungeon.returnRandomRelicTier());
                            AbstractEvent.logMetricObtainRelicAtCost("Addict", "Obtained Relic", relic, 1);
                            AbstractDungeon.player.loseGold(1);
                            AbstractDungeon.getCurrRoom().spawnRelicAndObtain(drawX, drawY, relic);
                            __instance.imageEventText.updateDialogOption(0, Addict.OPTIONS[5]);
                            __instance.imageEventText.clearRemainingOptions();
                        }
                        ReflectionHacks.setPrivate(__instance, Addict.class, "screenNum", 1);
                        return SpireReturn.Return(null);

                    case 1:
                        // Shame option - now gives shop relic
                        __instance.imageEventText.updateBodyText(Addict.DESCRIPTIONS[2]);
                        AbstractCard card = new Shame();
                        // Get a shop relic instead of a random relic
                        AbstractRelic relic = AbstractDungeon.returnRandomScreenlessRelic(AbstractRelic.RelicTier.SHOP);
                        AbstractEvent.logMetricObtainCardAndRelic("Addict", "Stole Shop Relic", card, relic);
                        AbstractDungeon.effectList.add(new ShowCardAndObtainEffect(card, (float)(Settings.WIDTH / 2), (float)(Settings.HEIGHT / 2)));
                        AbstractDungeon.getCurrRoom().spawnRelicAndObtain(drawX, drawY, relic);
                        __instance.imageEventText.updateDialogOption(0, Addict.OPTIONS[5]);
                        __instance.imageEventText.clearRemainingOptions();
                        ReflectionHacks.setPrivate(__instance, Addict.class, "screenNum", 1);
                        return SpireReturn.Return(null);
                }
            }
        }
        return SpireReturn.Continue();
    }

    private static class Locator extends SpireInsertLocator {
        @Override
        public int[] Locate(CtBehavior ctMethodToPatch) throws Exception {
            Matcher finalMatcher = new Matcher.FieldAccessMatcher(Addict.class, "screenNum");
            return LineFinder.findInOrder(ctMethodToPatch, finalMatcher);
        }
    }

    @SpirePostfixPatch
    public static void Postfix(Addict __instance, int buttonPressed) {
        ButtonEffectHelper.updateUpgradeAvailability();
    }
}