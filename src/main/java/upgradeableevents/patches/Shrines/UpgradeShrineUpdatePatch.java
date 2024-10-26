package upgradeableevents.patches.Shrines;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.events.shrines.UpgradeShrine;
import com.megacrit.cardcrawl.vfx.UpgradeShineEffect;
import com.megacrit.cardcrawl.vfx.cardManip.ShowCardBrieflyEffect;
import upgradeableevents.UpgradeEventManager;
import upgradeableevents.eventupgrades.Shrines.UpgradeShrineUpgrade;
import upgradeableevents.interfaces.AbstractEventUpgrade;

import java.util.ArrayList;


@SpirePatch(clz = UpgradeShrine.class, method = "update")
public class UpgradeShrineUpdatePatch {
    @SpirePrefixPatch
    public static SpireReturn<Void> Prefix(UpgradeShrine __instance) {
        AbstractEventUpgrade currentUpgrade = UpgradeEventManager.getCurrentUpgrade();
        if (currentUpgrade instanceof UpgradeShrineUpgrade) {
            UpgradeShrineUpgrade upgrade = (UpgradeShrineUpgrade) currentUpgrade;
            if (upgrade.isUpgraded() && !AbstractDungeon.isScreenUp &&
                    !AbstractDungeon.gridSelectScreen.selectedCards.isEmpty()) {

                // Handle the player's chosen upgrade first
                AbstractCard chosenCard = AbstractDungeon.gridSelectScreen.selectedCards.get(0);
                chosenCard.upgrade();
                __instance.logMetricCardUpgrade("Upgrade Shrine", "Upgraded (Chosen)", chosenCard);
                AbstractDungeon.player.bottledCardUpgradeCheck(chosenCard);

                // Show the upgrade effect for the chosen card
                AbstractDungeon.effectsQueue.add(new ShowCardBrieflyEffect(chosenCard.makeStatEquivalentCopy()));
                AbstractDungeon.topLevelEffects.add(new UpgradeShineEffect(Settings.WIDTH / 2.0F, Settings.HEIGHT / 2.0F));

                // Now handle the random upgrade
                ArrayList<AbstractCard> upgradableCards = AbstractDungeon.player.masterDeck.getUpgradableCards().group;
                if (!upgradableCards.isEmpty()) {
                    AbstractCard randomCard = upgradableCards.get(AbstractDungeon.miscRng.random(upgradableCards.size() - 1));
                    randomCard.upgrade();
                    __instance.logMetricCardUpgrade("Upgrade Shrine", "Upgraded (Random)", randomCard);
                    AbstractDungeon.player.bottledCardUpgradeCheck(randomCard);

                    AbstractDungeon.effectsQueue.add(new ShowCardBrieflyEffect(randomCard.makeStatEquivalentCopy(),
                            Settings.WIDTH / 3.0F,
                            Settings.HEIGHT / 2.0F));

// And adjust the shine effect to match
                    AbstractDungeon.effectsQueue.add(new UpgradeShineEffect(Settings.WIDTH / 3.0F, Settings.HEIGHT / 2.0F));
                }

                AbstractDungeon.gridSelectScreen.selectedCards.clear();
                return SpireReturn.Return();
            }
        }
        return SpireReturn.Continue();
    }
}