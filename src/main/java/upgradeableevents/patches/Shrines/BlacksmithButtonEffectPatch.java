package upgradeableevents.patches.Shrines;

import basemod.ReflectionHacks;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.events.shrines.AccursedBlacksmith;
import com.megacrit.cardcrawl.relics.WarpedTongs;
import com.megacrit.cardcrawl.vfx.UpgradeShineEffect;
import com.megacrit.cardcrawl.vfx.cardManip.ShowCardBrieflyEffect;
import javassist.CtBehavior;
import upgradeableevents.UpgradeEventManager;
import upgradeableevents.eventupgrades.Shrines.AccursedBlacksmithUpgrade;
import upgradeableevents.interfaces.AbstractEventUpgrade;
import upgradeableevents.util.ButtonEffectHelper;

import java.util.*;

@SpirePatch(clz = AccursedBlacksmith.class, method = "buttonEffect")
public class BlacksmithButtonEffectPatch {
    @SpireInsertPatch(
            locator = Locator.class
    )
    public static SpireReturn<Void> Insert(AccursedBlacksmith __instance, int buttonPressed) {
        AbstractEventUpgrade currentUpgrade = UpgradeEventManager.getCurrentUpgrade();
        int screenNum = ReflectionHacks.getPrivate(__instance, AccursedBlacksmith.class, "screenNum");

        if (currentUpgrade instanceof AccursedBlacksmithUpgrade && ((AccursedBlacksmithUpgrade)currentUpgrade).isUpgraded() && screenNum == 0) {
            if (buttonPressed == 0) {
                AbstractDungeon.topLevelEffects.add(new UpgradeShineEffect((float)Settings.WIDTH / 2.0F, (float)Settings.HEIGHT / 2.0F));
                ArrayList<AbstractCard> upgradableCards = new ArrayList();
                Iterator var2 = AbstractDungeon.player.masterDeck.group.iterator();
                while(var2.hasNext()) {
                    AbstractCard c = (AbstractCard)var2.next();
                    if (c.canUpgrade()) {
                        upgradableCards.add(c);
                    }
                }
                List<String> cardMetrics = new ArrayList();
                Collections.shuffle(upgradableCards, new Random(AbstractDungeon.miscRng.randomLong()));
                if (!upgradableCards.isEmpty()) {
                    if (upgradableCards.size() == 1) {
                        upgradableCards.get(0).upgrade();
                        cardMetrics.add(upgradableCards.get(0).cardID);
                        AbstractDungeon.player.bottledCardUpgradeCheck(upgradableCards.get(0));
                        AbstractDungeon.effectList.add(new ShowCardBrieflyEffect(upgradableCards.get(0).makeStatEquivalentCopy()));
                    } else {
                        upgradableCards.get(0).upgrade();
                        upgradableCards.get(1).upgrade();
                        cardMetrics.add(upgradableCards.get(0).cardID);
                        cardMetrics.add(upgradableCards.get(1).cardID);
                        AbstractDungeon.player.bottledCardUpgradeCheck(upgradableCards.get(0));
                        AbstractDungeon.player.bottledCardUpgradeCheck(upgradableCards.get(1));
                        AbstractDungeon.effectList.add(new ShowCardBrieflyEffect(upgradableCards.get(0).makeStatEquivalentCopy(),
                                (float)Settings.WIDTH / 2.0F - 190.0F * Settings.scale, (float)Settings.HEIGHT / 2.0F));
                        AbstractDungeon.effectList.add(new ShowCardBrieflyEffect(upgradableCards.get(1).makeStatEquivalentCopy(),
                                (float)Settings.WIDTH / 2.0F + 190.0F * Settings.scale, (float)Settings.HEIGHT / 2.0F));
                    }
                }

                ReflectionHacks.setPrivate(__instance, AccursedBlacksmith.class, "screenNum", 2);
                __instance.imageEventText.updateBodyText(AccursedBlacksmith.DESCRIPTIONS[1]);
                __instance.imageEventText.updateDialogOption(0, AccursedBlacksmith.OPTIONS[2]);
                __instance.imageEventText.clearRemainingOptions();

                return SpireReturn.Return();
            } else if (buttonPressed == 1) {
                // 50% chance to avoid curse
                if (AbstractDungeon.miscRng.randomBoolean()) {
                    ReflectionHacks.setPrivate(__instance, AccursedBlacksmith.class, "screenNum", 2);
                    __instance.imageEventText.updateBodyText(AccursedBlacksmith.DESCRIPTIONS[2]);
                    AbstractDungeon.getCurrRoom().spawnRelicAndObtain((float)(Settings.WIDTH / 2), (float)(Settings.HEIGHT / 2), new WarpedTongs());
                    __instance.imageEventText.updateDialogOption(0, AccursedBlacksmith.OPTIONS[2]);
                    __instance.imageEventText.clearRemainingOptions();
                    return SpireReturn.Return();
                }
            }
        }
        return SpireReturn.Continue();
    }

    private static class Locator extends SpireInsertLocator {
        @Override
        public int[] Locate(CtBehavior ctMethodToPatch) throws Exception {
            Matcher finalMatcher = new Matcher.FieldAccessMatcher(AccursedBlacksmith.class, "screenNum");
            return LineFinder.findInOrder(ctMethodToPatch, finalMatcher);
        }
    }

    @SpirePostfixPatch
    public static void Postfix(AccursedBlacksmith __instance, int buttonPressed) {
        ButtonEffectHelper.updateUpgradeAvailability();
    }
}