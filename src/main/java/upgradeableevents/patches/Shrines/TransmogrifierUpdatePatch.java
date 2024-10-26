package upgradeableevents.patches.Shrines;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.events.shrines.Transmogrifier;
import com.megacrit.cardcrawl.vfx.cardManip.ShowCardAndObtainEffect;
import upgradeableevents.UpgradeEventManager;
import upgradeableevents.eventupgrades.Shrines.TransmogrifierUpgrade;
import upgradeableevents.interfaces.AbstractEventUpgrade;

import java.util.ArrayList;

@SpirePatch(clz = Transmogrifier.class, method = "update")
public class TransmogrifierUpdatePatch {
    @SpirePrefixPatch
    public static SpireReturn<Void> Prefix(Transmogrifier __instance) {
        AbstractEventUpgrade currentUpgrade = UpgradeEventManager.getCurrentUpgrade();
        if (currentUpgrade instanceof TransmogrifierUpgrade) {
            TransmogrifierUpgrade upgrade = (TransmogrifierUpgrade) currentUpgrade;
            if (upgrade.isUpgraded() && !AbstractDungeon.isScreenUp &&
                    !AbstractDungeon.gridSelectScreen.selectedCards.isEmpty()) {

                AbstractCard c = AbstractDungeon.gridSelectScreen.selectedCards.get(0);
                AbstractDungeon.player.masterDeck.removeCard(c);

                // Get a random rare card instead of using normal transform logic
                ArrayList<AbstractCard> rareCards = AbstractDungeon.rareCardPool.group;
                AbstractCard transCard = rareCards.get(AbstractDungeon.miscRng.random(rareCards.size() - 1)).makeCopy();

                // Log the transformation
                __instance.logMetricTransformCard("Transmorgrifier", "Transformed (Rare)", c, transCard);

                // Show the card and add it to the deck
                AbstractDungeon.effectsQueue.add(new ShowCardAndObtainEffect(
                        transCard, Settings.WIDTH / 2.0F, Settings.HEIGHT / 2.0F));

                AbstractDungeon.gridSelectScreen.selectedCards.clear();
                return SpireReturn.Return();
            }
        }
        return SpireReturn.Continue();
    }
}