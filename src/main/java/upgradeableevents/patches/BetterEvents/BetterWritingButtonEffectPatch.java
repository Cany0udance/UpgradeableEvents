package upgradeableevents.patches.BetterEvents;

import basemod.ReflectionHacks;
import com.badlogic.gdx.math.MathUtils;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.CardGroup;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.events.AbstractEvent;
import com.megacrit.cardcrawl.vfx.cardManip.ShowCardBrieflyEffect;
import upgradeableevents.UpgradeEventManager;
import upgradeableevents.eventupgrades.BetterEvents.BetterWritingUpgrade;
import upgradeableevents.interfaces.AbstractEventUpgrade;
import upgradeableevents.util.ButtonEffectHelper;

import java.util.ArrayList;

@SpirePatch(
        optional = true,
        cls = "betterThird.events.BetterWritingEvent",
        method = "buttonEffect"
)
public class BetterWritingButtonEffectPatch {
    @SpirePrefixPatch
    public static SpireReturn<Void> Prefix(AbstractEvent __instance, int buttonPressed) {
        if (!BetterWritingUpgrade.isBetterThirdLoaded) return SpireReturn.Continue();

        AbstractEventUpgrade currentUpgrade = UpgradeEventManager.getCurrentUpgrade();
        if (currentUpgrade instanceof BetterWritingUpgrade && ((BetterWritingUpgrade)currentUpgrade).isUpgraded()) {
            Enum<?> screen = ReflectionHacks.getPrivate(__instance, BetterWritingUpgrade.eventClass, "screen");

            if ("INTRO".equals(screen.name())) {
                switch (buttonPressed) {
                    case 0: // Remove a card + gain 50 gold
                        if (CardGroup.getGroupWithoutBottledCards(AbstractDungeon.player.masterDeck.getPurgeableCards()).size() > 0) {
                            __instance.imageEventText.updateBodyText(ReflectionHacks.getPrivateStatic(BetterWritingUpgrade.eventClass, "DIALOG_2"));
                            AbstractDungeon.gridSelectScreen.open(
                                    CardGroup.getGroupWithoutBottledCards(AbstractDungeon.player.masterDeck.getPurgeableCards()),
                                    1,
                                    ((String[])ReflectionHacks.getPrivateStatic(BetterWritingUpgrade.eventClass, "OPTIONS"))[2],
                                    false
                            );
                            // Add 50 gold
                            AbstractDungeon.player.gainGold(50);
                            AbstractEvent.logMetricGainGold("Better Writing", "Elegance", 50);

                            __instance.imageEventText.updateDialogOption(0, ((String[])ReflectionHacks.getPrivateStatic(BetterWritingUpgrade.eventClass, "OPTIONS"))[3]);
                            __instance.imageEventText.clearRemainingOptions();
                        }
                        break;

                    case 2: // Upgrade all Basic and Rare cards
                        __instance.imageEventText.updateBodyText(ReflectionHacks.getPrivateStatic(BetterWritingUpgrade.eventClass, "DIALOG_3"));
                        upgradeBasicAndRareCards(__instance);
                        __instance.imageEventText.updateDialogOption(0, ((String[])ReflectionHacks.getPrivateStatic(BetterWritingUpgrade.eventClass, "OPTIONS"))[3]);
                        __instance.imageEventText.clearRemainingOptions();
                        break;

                    default:
                        return SpireReturn.Continue();
                }

                // Set screen to COMPLETE
                setScreenToComplete(__instance);
                return SpireReturn.Return(null);
            }
        }
        return SpireReturn.Continue();
    }

    private static void upgradeBasicAndRareCards(AbstractEvent __instance) {
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

        AbstractEvent.logMetricUpgradeCards("Better Writing", "Simplicity", cardsUpgraded);
    }

    private static void setScreenToComplete(AbstractEvent __instance) {
        try {
            for (Class<?> innerClass : BetterWritingUpgrade.eventClass.getDeclaredClasses()) {
                if (innerClass.getSimpleName().equals("CUR_SCREEN")) {
                    Object completeScreen = Enum.valueOf((Class<Enum>) innerClass, "COMPLETE");
                    ReflectionHacks.setPrivate(__instance, BetterWritingUpgrade.eventClass, "screen", completeScreen);
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SpirePostfixPatch
    public static void Postfix(AbstractEvent __instance, int buttonPressed) {
        if (BetterWritingUpgrade.isBetterThirdLoaded) {
            ButtonEffectHelper.updateUpgradeAvailability();
        }
    }
}