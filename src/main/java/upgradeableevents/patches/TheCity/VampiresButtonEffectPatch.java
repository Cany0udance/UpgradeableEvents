package upgradeableevents.patches.TheCity;

import basemod.ReflectionHacks;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.colorless.Bite;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.events.AbstractEvent;
import com.megacrit.cardcrawl.events.city.Vampires;
import com.megacrit.cardcrawl.relics.BloodVial;
import com.megacrit.cardcrawl.vfx.cardManip.ShowCardAndObtainEffect;
import javassist.CtBehavior;
import upgradeableevents.UpgradeEventManager;
import upgradeableevents.eventupgrades.TheCity.VampiresUpgrade;
import upgradeableevents.interfaces.AbstractEventUpgrade;
import upgradeableevents.util.ButtonEffectHelper;

import java.util.ArrayList;
import java.util.List;

@SpirePatch(clz = Vampires.class, method = "buttonEffect")
public class VampiresButtonEffectPatch {

    @SpireInsertPatch(
            locator = Locator.class
    )
    public static SpireReturn<Void> Insert(Vampires __instance, int buttonPressed) {
        AbstractEventUpgrade currentUpgrade = UpgradeEventManager.getCurrentUpgrade();
        if (currentUpgrade instanceof VampiresUpgrade && ((VampiresUpgrade)currentUpgrade).isUpgraded()) {
            int screenNum = ReflectionHacks.getPrivate(__instance, Vampires.class, "screenNum");
            boolean hasVial = ReflectionHacks.getPrivate(__instance, Vampires.class, "hasVial");
            int maxHpLoss = ReflectionHacks.getPrivate(__instance, Vampires.class, "maxHpLoss");
            @SuppressWarnings("unchecked")
            List<String> bites = ReflectionHacks.getPrivate(__instance, Vampires.class, "bites");

            if (screenNum == 0) {
                switch (buttonPressed) {
                    case 0:
                        CardCrawlGame.sound.play("EVENT_VAMP_BITE");
                        __instance.imageEventText.updateBodyText(Vampires.DESCRIPTIONS[2]);
                        AbstractDungeon.player.decreaseMaxHealth(maxHpLoss);
                        replaceAllStrikes(__instance, bites);
                        AbstractEvent.logMetricObtainCardsLoseMapHP("Vampires", "Became a vampire (All Strikes)", bites, maxHpLoss);
                        ReflectionHacks.setPrivate(__instance, Vampires.class, "screenNum", 1);
                        __instance.imageEventText.updateDialogOption(0, Vampires.OPTIONS[5]);
                        __instance.imageEventText.clearRemainingOptions();
                        return SpireReturn.Return(null);

                    case 1:
                        if (hasVial) {
                            CardCrawlGame.sound.play("EVENT_VAMP_BITE");
                            __instance.imageEventText.updateBodyText(Vampires.DESCRIPTIONS[4]);
                            AbstractDungeon.player.loseRelic("Blood Vial");
                            replaceAllStrikes(__instance, bites);
                            AbstractEvent.logMetricObtainCardsLoseRelic("Vampires", "Became a vampire (All Strikes, Vial)", bites, new BloodVial());
                            ReflectionHacks.setPrivate(__instance, Vampires.class, "screenNum", 1);
                            __instance.imageEventText.updateDialogOption(0, Vampires.OPTIONS[5]);
                            __instance.imageEventText.clearRemainingOptions();
                            return SpireReturn.Return(null);
                        }
                }
            }
        }
        return SpireReturn.Continue();
    }

    private static void replaceAllStrikes(Vampires vampiresEvent, List<String> bites) {
        ArrayList<AbstractCard> masterDeck = AbstractDungeon.player.masterDeck.group;
        int strikeCount = 0;

        // Count Strikes first
        for(AbstractCard card : masterDeck) {
            if (card.tags.contains(AbstractCard.CardTags.STARTER_STRIKE)) {
                strikeCount++;
            }
        }

        // Remove Strikes
        for(int i = masterDeck.size() - 1; i >= 0; --i) {
            AbstractCard card = masterDeck.get(i);
            if (card.tags.contains(AbstractCard.CardTags.STARTER_STRIKE)) {
                AbstractDungeon.player.masterDeck.removeCard(card);
            }
        }

        // Add Bites equal to number of Strikes removed
        for(int i = 0; i < strikeCount; ++i) {
            AbstractCard c = new Bite();
            AbstractDungeon.effectList.add(new ShowCardAndObtainEffect(c, (float)Settings.WIDTH / 2.0F, (float)Settings.HEIGHT / 2.0F));
            bites.add(c.cardID);
        }
    }

    private static class Locator extends SpireInsertLocator {
        @Override
        public int[] Locate(CtBehavior ctMethodToPatch) throws Exception {
            Matcher finalMatcher = new Matcher.FieldAccessMatcher(Vampires.class, "screenNum");
            return LineFinder.findInOrder(ctMethodToPatch, finalMatcher);
        }
    }

    @SpirePostfixPatch
    public static void Postfix(Vampires __instance, int buttonPressed) {
        ButtonEffectHelper.updateUpgradeAvailability();
    }
}