package upgradeableevents.patches.Exordium;

import basemod.ReflectionHacks;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.events.AbstractEvent;
import com.megacrit.cardcrawl.events.exordium.ShiningLight;
import com.megacrit.cardcrawl.vfx.UpgradeShineEffect;
import com.megacrit.cardcrawl.vfx.cardManip.ShowCardBrieflyEffect;
import upgradeableevents.UpgradeEventManager;
import upgradeableevents.eventupgrades.Exordium.ShiningLightUpgrade;
import upgradeableevents.interfaces.AbstractEventUpgrade;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

@SpirePatch(clz = ShiningLight.class, method = "upgradeCards")
public class ShiningLightUpgradeCardsPatch {
    @SpirePrefixPatch
    public static SpireReturn<Void> Prefix(ShiningLight __instance) {
        AbstractEventUpgrade currentUpgrade = UpgradeEventManager.getCurrentUpgrade();
        if (!(currentUpgrade instanceof ShiningLightUpgrade) ||
                !((ShiningLightUpgrade)currentUpgrade).isUpgraded()) {
            return SpireReturn.Continue();
        }

        // Modified upgradeCards logic for 3 cards
        AbstractDungeon.topLevelEffects.add(new UpgradeShineEffect(Settings.WIDTH / 2.0F, Settings.HEIGHT / 2.0F));

        ArrayList<AbstractCard> upgradableCards = new ArrayList<>();
        for (AbstractCard c : AbstractDungeon.player.masterDeck.group) {
            if (c.canUpgrade()) {
                upgradableCards.add(c);
            }
        }

        List<String> cardMetrics = new ArrayList<>();
        Collections.shuffle(upgradableCards, new Random(AbstractDungeon.miscRng.randomLong()));

        if (!upgradableCards.isEmpty()) {
            if (upgradableCards.size() == 1) {
                // One card case
                upgradableCards.get(0).upgrade();
                cardMetrics.add(upgradableCards.get(0).cardID);
                AbstractDungeon.player.bottledCardUpgradeCheck(upgradableCards.get(0));
                AbstractDungeon.effectList.add(new ShowCardBrieflyEffect(upgradableCards.get(0).makeStatEquivalentCopy()));
            } else if (upgradableCards.size() == 2) {
                // Two cards case
                for (int i = 0; i < 2; i++) {
                    upgradableCards.get(i).upgrade();
                    cardMetrics.add(upgradableCards.get(i).cardID);
                    AbstractDungeon.player.bottledCardUpgradeCheck(upgradableCards.get(i));
                }
                AbstractDungeon.effectList.add(new ShowCardBrieflyEffect(upgradableCards.get(0).makeStatEquivalentCopy(),
                        Settings.WIDTH / 2.0F - 190.0F * Settings.scale, Settings.HEIGHT / 2.0F));
                AbstractDungeon.effectList.add(new ShowCardBrieflyEffect(upgradableCards.get(1).makeStatEquivalentCopy(),
                        Settings.WIDTH / 2.0F + 190.0F * Settings.scale, Settings.HEIGHT / 2.0F));
            } else {
                // Three or more cards case
                for (int i = 0; i < 3; i++) {
                    upgradableCards.get(i).upgrade();
                    cardMetrics.add(upgradableCards.get(i).cardID);
                    AbstractDungeon.player.bottledCardUpgradeCheck(upgradableCards.get(i));
                }
                AbstractDungeon.effectList.add(new ShowCardBrieflyEffect(upgradableCards.get(0).makeStatEquivalentCopy(),
                        Settings.WIDTH / 2.0F - 380.0F * Settings.scale, Settings.HEIGHT / 2.0F));
                AbstractDungeon.effectList.add(new ShowCardBrieflyEffect(upgradableCards.get(1).makeStatEquivalentCopy(),
                        Settings.WIDTH / 2.0F, Settings.HEIGHT / 2.0F));
                AbstractDungeon.effectList.add(new ShowCardBrieflyEffect(upgradableCards.get(2).makeStatEquivalentCopy(),
                        Settings.WIDTH / 2.0F + 380.0F * Settings.scale, Settings.HEIGHT / 2.0F));
            }
        }

        int damage = ReflectionHacks.getPrivate(__instance, ShiningLight.class, "damage");
        AbstractEvent.logMetric("Shining Light", "Entered Light", null, null, null, cardMetrics,
                null, null, null, damage, 0, 0, 0, 0, 0);

        return SpireReturn.Return(null);
    }
}