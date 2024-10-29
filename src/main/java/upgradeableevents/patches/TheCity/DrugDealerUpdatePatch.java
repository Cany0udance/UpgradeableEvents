package upgradeableevents.patches.TheCity;

import basemod.ReflectionHacks;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.events.AbstractEvent;
import com.megacrit.cardcrawl.events.city.DrugDealer;
import com.megacrit.cardcrawl.vfx.cardManip.ShowCardAndObtainEffect;
import javassist.CtBehavior;
import upgradeableevents.UpgradeEventManager;
import upgradeableevents.eventupgrades.TheCity.DrugDealerUpgrade;
import upgradeableevents.interfaces.AbstractEventUpgrade;

import java.util.ArrayList;

@SpirePatch(clz = DrugDealer.class, method = "update")
public class DrugDealerUpdatePatch {
    @SpireInsertPatch(
            locator = Locator.class
    )
    public static SpireReturn<Void> Insert(DrugDealer __instance) {
        AbstractEventUpgrade currentUpgrade = UpgradeEventManager.getCurrentUpgrade();
        if (currentUpgrade instanceof DrugDealerUpgrade && ((DrugDealerUpgrade)currentUpgrade).isUpgraded()) {
            boolean cardsSelected = ReflectionHacks.getPrivate(__instance, DrugDealer.class, "cardsSelected");
            if (!cardsSelected && AbstractDungeon.gridSelectScreen.selectedCards.size() == 3) {
                ReflectionHacks.setPrivate(__instance, DrugDealer.class, "cardsSelected", true);
                float displayCount = 0.0F;
                ArrayList<String> transformedCards = new ArrayList<>();
                ArrayList<String> obtainedCards = new ArrayList<>();

                for (AbstractCard card : AbstractDungeon.gridSelectScreen.selectedCards) {
                    card.untip();
                    card.unhover();
                    transformedCards.add(card.cardID);
                    AbstractDungeon.player.masterDeck.removeCard(card);
                    AbstractDungeon.transformCard(card, false, AbstractDungeon.miscRng);
                    AbstractCard transformedCard = AbstractDungeon.getTransformedCard();
                    obtainedCards.add(transformedCard.cardID);

                    if (AbstractDungeon.screen != AbstractDungeon.CurrentScreen.TRANSFORM && transformedCard != null) {
                        AbstractDungeon.topLevelEffectsQueue.add(new ShowCardAndObtainEffect(
                                transformedCard.makeCopy(),
                                Settings.WIDTH / 4.0F + displayCount,
                                Settings.HEIGHT / 2.0F,
                                false));
                        displayCount += Settings.WIDTH / 6.0F;
                    }
                }

                AbstractDungeon.gridSelectScreen.selectedCards.clear();
                AbstractEvent.logMetricTransformCards("Drug Dealer", "Became Test Subject", transformedCards, obtainedCards);
                AbstractDungeon.getCurrRoom().rewardPopOutTimer = 0.25F;
                return SpireReturn.Return(null);
            }
        }
        return SpireReturn.Continue();
    }

    private static class Locator extends SpireInsertLocator {
        @Override
        public int[] Locate(CtBehavior ctMethodToPatch) throws Exception {
            Matcher finalMatcher = new Matcher.FieldAccessMatcher(DrugDealer.class, "cardsSelected");
            return LineFinder.findInOrder(ctMethodToPatch, finalMatcher);
        }
    }
}