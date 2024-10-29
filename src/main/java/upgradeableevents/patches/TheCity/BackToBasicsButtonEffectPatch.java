package upgradeableevents.patches.TheCity;

import basemod.ReflectionHacks;
import com.badlogic.gdx.math.MathUtils;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.CardGroup;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.events.AbstractEvent;
import com.megacrit.cardcrawl.events.city.BackToBasics;
import com.megacrit.cardcrawl.vfx.cardManip.ShowCardBrieflyEffect;
import javassist.CtBehavior;
import upgradeableevents.UpgradeEventManager;
import upgradeableevents.eventupgrades.TheCity.BackToBasicsUpgrade;
import upgradeableevents.interfaces.AbstractEventUpgrade;
import upgradeableevents.util.ButtonEffectHelper;

import java.util.ArrayList;

@SpirePatch(clz = BackToBasics.class, method = "buttonEffect")
public class BackToBasicsButtonEffectPatch {
    @SpireInsertPatch(
            locator = Locator.class
    )
    public static SpireReturn<Void> Insert(BackToBasics __instance, int buttonPressed) {

        AbstractEventUpgrade currentUpgrade = UpgradeEventManager.getCurrentUpgrade();
        if (currentUpgrade instanceof BackToBasicsUpgrade && ((BackToBasicsUpgrade)currentUpgrade).isUpgraded()) {
            Enum<?> screen = ReflectionHacks.getPrivate(__instance, BackToBasics.class, "screen");

            if ("INTRO".equals(screen.name())) {
                switch (buttonPressed) {
                    case 0: // Remove a card + gain 50 gold
                        if (CardGroup.getGroupWithoutBottledCards(AbstractDungeon.player.masterDeck.getPurgeableCards()).size() > 0) {
                            __instance.imageEventText.updateBodyText(BackToBasics.DESCRIPTIONS[1]);
                            AbstractDungeon.gridSelectScreen.open(
                                    CardGroup.getGroupWithoutBottledCards(AbstractDungeon.player.masterDeck.getPurgeableCards()),
                                    1,
                                    BackToBasics.OPTIONS[2],
                                    false
                            );
                            // Add 50 gold
                            AbstractDungeon.player.gainGold(50);
                            AbstractEvent.logMetricGainGold("Back to Basics", "Elegance", 50);

                            __instance.imageEventText.updateDialogOption(0, BackToBasics.OPTIONS[3]);
                            __instance.imageEventText.clearRemainingOptions();
                        }
                        break;

                    case 1: // Upgrade all Basic and Rare cards
                        __instance.imageEventText.updateBodyText(BackToBasics.DESCRIPTIONS[2]);
                        upgradeBasicAndRareCards();
                        __instance.imageEventText.updateDialogOption(0, BackToBasics.OPTIONS[3]);
                        __instance.imageEventText.clearRemainingOptions();
                        break;
                }

                // Set screen to COMPLETE using reflection
                Class<?> screenEnum = BackToBasics.class.getDeclaredClasses()[0];
                Object completeScreen = Enum.valueOf((Class<Enum>) screenEnum, "COMPLETE");
                ReflectionHacks.setPrivate(__instance, BackToBasics.class, "screen", completeScreen);

                return SpireReturn.Return(null);
            }
        }
        return SpireReturn.Continue();
    }

    private static void upgradeBasicAndRareCards() {
        ArrayList<String> cardsUpgraded = new ArrayList<>();

        for (AbstractCard c : AbstractDungeon.player.masterDeck.group) {
            if ((c.rarity == AbstractCard.CardRarity.BASIC || c.rarity == AbstractCard.CardRarity.RARE) && c.canUpgrade()) {
                c.upgrade();
                cardsUpgraded.add(c.cardID);
                AbstractDungeon.player.bottledCardUpgradeCheck(c);
                AbstractDungeon.effectList.add(new ShowCardBrieflyEffect(
                        c.makeStatEquivalentCopy(),
                        MathUtils.random(0.1F, 0.9F) * Settings.WIDTH,
                        MathUtils.random(0.2F, 0.8F) * Settings.HEIGHT
                ));
            }
        }

        AbstractEvent.logMetricUpgradeCards("Back to Basics", "Simplicity", cardsUpgraded);
    }

    private static class Locator extends SpireInsertLocator {
        @Override
        public int[] Locate(CtBehavior ctMethodToPatch) throws Exception {
            Matcher finalMatcher = new Matcher.FieldAccessMatcher(BackToBasics.class, "screen");
            int[] matches = LineFinder.findAllInOrder(ctMethodToPatch, finalMatcher);
            // We want the first field access of 'screen', which is the switch statement
            return new int[]{matches[0]};
        }
    }

    @SpirePostfixPatch
    public static void Postfix(BackToBasics __instance, int buttonPressed) {
        ButtonEffectHelper.updateUpgradeAvailability();
    }
}