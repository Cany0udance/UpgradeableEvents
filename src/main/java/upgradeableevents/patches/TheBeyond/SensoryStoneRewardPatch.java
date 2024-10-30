package upgradeableevents.patches.TheBeyond;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.events.beyond.SensoryStone;
import com.megacrit.cardcrawl.rewards.RewardItem;
import upgradeableevents.UpgradeEventManager;
import upgradeableevents.eventupgrades.TheBeyond.SensoryStoneUpgrade;
import upgradeableevents.interfaces.AbstractEventUpgrade;

import java.util.ArrayList;

@SpirePatch(clz = SensoryStone.class, method = "reward")
public class SensoryStoneRewardPatch {
    @SpirePostfixPatch
    public static void Postfix(SensoryStone __instance) {
        AbstractEventUpgrade currentUpgrade = UpgradeEventManager.getCurrentUpgrade();
        if (currentUpgrade instanceof SensoryStoneUpgrade && ((SensoryStoneUpgrade)currentUpgrade).isUpgraded()) {
            // Get the current rewards
            ArrayList<RewardItem> rewards = AbstractDungeon.getCurrRoom().rewards;

            // For each reward
            for (RewardItem reward : rewards) {
                if (reward.cards != null) {
                    // Upgrade all cards in the reward
                    for (AbstractCard card : reward.cards) {
                        if (card.canUpgrade()) {
                            card.upgrade();
                        }
                    }
                }
            }
        }
    }
}