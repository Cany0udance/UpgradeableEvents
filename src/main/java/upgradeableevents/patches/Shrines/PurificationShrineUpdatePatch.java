package upgradeableevents.patches.Shrines;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.events.shrines.PurificationShrine;
import com.megacrit.cardcrawl.vfx.cardManip.PurgeCardEffect;
import upgradeableevents.UpgradeEventManager;
import upgradeableevents.eventupgrades.Shrines.PurificationShrineUpgrade;
import upgradeableevents.interfaces.AbstractEventUpgrade;

@SpirePatch(clz = PurificationShrine.class, method = "update")
public class PurificationShrineUpdatePatch {
    @SpirePrefixPatch
    public static SpireReturn<Void> Prefix(PurificationShrine __instance) {
        AbstractEventUpgrade currentUpgrade = UpgradeEventManager.getCurrentUpgrade();
        if (currentUpgrade instanceof PurificationShrineUpgrade) {
            PurificationShrineUpgrade upgrade = (PurificationShrineUpgrade) currentUpgrade;
            if (upgrade.isUpgraded() && !AbstractDungeon.isScreenUp &&
                    !AbstractDungeon.gridSelectScreen.selectedCards.isEmpty()) {

                // Handle removal of both selected cards
                for (AbstractCard card : AbstractDungeon.gridSelectScreen.selectedCards) {
                    CardCrawlGame.sound.play("CARD_EXHAUST");
                    __instance.logMetricCardRemoval("Purifier", "Purged", card);
                    AbstractDungeon.topLevelEffects.add(new PurgeCardEffect(card,
                            Settings.WIDTH / 2f, Settings.HEIGHT / 2f));
                    AbstractDungeon.player.masterDeck.removeCard(card);
                }
                AbstractDungeon.gridSelectScreen.selectedCards.clear();
                return SpireReturn.Return();
            }
        }
        return SpireReturn.Continue();
    }
}