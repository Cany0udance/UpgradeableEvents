package upgradeableevents.patches.BetterEvents;

import basemod.ReflectionHacks;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.colorless.Enlightenment;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.events.AbstractEvent;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.relics.Courier;
import com.megacrit.cardcrawl.relics.MembershipCard;
import com.megacrit.cardcrawl.vfx.RainingGoldEffect;
import com.megacrit.cardcrawl.vfx.cardManip.ShowCardAndObtainEffect;
import upgradeableevents.UpgradeEventManager;
import upgradeableevents.eventupgrades.BetterEvents.BetterSerpentUpgrade;
import upgradeableevents.interfaces.AbstractEventUpgrade;
import upgradeableevents.util.ButtonEffectHelper;

import java.util.ArrayList;
import java.util.List;

@SpirePatch(
        optional = true,
        cls = "betterThird.events.BetterSerpentEvent",
        method = "buttonEffect"
)
public class BetterSerpentButtonEffectPatch {
    @SpirePrefixPatch
    public static SpireReturn<Void> Prefix(AbstractEvent __instance, int buttonPressed) {
        if (!BetterSerpentUpgrade.isBetterThirdLoaded) return SpireReturn.Continue();

        AbstractEventUpgrade currentUpgrade = UpgradeEventManager.getCurrentUpgrade();
        if (!(currentUpgrade instanceof BetterSerpentUpgrade) || !((BetterSerpentUpgrade)currentUpgrade).isUpgraded()) {
            return SpireReturn.Continue();
        }

        Enum<?> screen = ReflectionHacks.getPrivate(__instance, BetterSerpentUpgrade.eventClass, "screen");
        String[] DESCRIPTIONS = ReflectionHacks.getPrivateStatic(BetterSerpentUpgrade.eventClass, "DESCRIPTIONS");
        String[] OPTIONS = ReflectionHacks.getPrivateStatic(BetterSerpentUpgrade.eventClass, "OPTIONS");

        if ("INTRO".equals(screen.name())) {
            if (buttonPressed == 0) {
                // Handle gold/relic reward option
                __instance.imageEventText.updateBodyText(DESCRIPTIONS[1]); // AGREE_DIALOG
                __instance.imageEventText.clearAllDialogs();
                __instance.imageEventText.setDialogOption(OPTIONS[3]);

                try {
                    for (Class<?> innerClass : BetterSerpentUpgrade.eventClass.getDeclaredClasses()) {
                        if (innerClass.getSimpleName().equals("CUR_SCREEN")) {
                            Object agreeScreen = Enum.valueOf((Class<Enum>) innerClass, "AGREE");
                            ReflectionHacks.setPrivate(__instance, BetterSerpentUpgrade.eventClass, "screen", agreeScreen);
                            break;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return SpireReturn.Return(null);
            } else if (buttonPressed == 1) {
                // Handle enlightenment option
                AbstractCard enlightenment = new Enlightenment();
                enlightenment.upgrade();
                enlightenment.misc = 1;

                int goldCost = ReflectionHacks.getPrivate(__instance, BetterSerpentUpgrade.eventClass, "goldCost");
                int maxHPGain = ReflectionHacks.getPrivate(__instance, BetterSerpentUpgrade.eventClass, "maxHPGain");

                List<String> cardsList = new ArrayList<>();
                cardsList.add(enlightenment.cardID);
                AbstractEvent.logMetric("Better Serpent", "Renounce (Upgraded)", cardsList, null, null, null, null, null, null,
                        goldCost, 0, 0, 0, maxHPGain, goldCost);

                AbstractDungeon.effectList.add(new ShowCardAndObtainEffect(enlightenment, Settings.WIDTH / 2.0F, Settings.HEIGHT / 2.0F));
                AbstractDungeon.player.loseGold(goldCost);
                AbstractDungeon.player.increaseMaxHp(maxHPGain, true);

                __instance.imageEventText.updateBodyText(DESCRIPTIONS[4]); // RENOUNCE_DIALOG
                __instance.imageEventText.clearAllDialogs();
                __instance.imageEventText.setDialogOption(OPTIONS[3]);

                AbstractDungeon.shrineList.remove("Golden Shrine");

                try {
                    for (Class<?> innerClass : BetterSerpentUpgrade.eventClass.getDeclaredClasses()) {
                        if (innerClass.getSimpleName().equals("CUR_SCREEN")) {
                            Object disagreeScreen = Enum.valueOf((Class<Enum>) innerClass, "DISAGREE");
                            ReflectionHacks.setPrivate(__instance, BetterSerpentUpgrade.eventClass, "screen", disagreeScreen);
                            break;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return SpireReturn.Return(null);
            }
        } else if ("AGREE".equals(screen.name())) {
            AbstractCard curse = ReflectionHacks.getPrivate(__instance, BetterSerpentUpgrade.eventClass, "curse");
            int goldReward = ReflectionHacks.getPrivate(__instance, BetterSerpentUpgrade.eventClass, "goldReward");

            boolean hasMembershipCard = AbstractDungeon.player.hasRelic(MembershipCard.ID);
            boolean hasCourier = AbstractDungeon.player.hasRelic(Courier.ID);

            // Give curse first
            AbstractDungeon.effectList.add(new ShowCardAndObtainEffect(curse, Settings.WIDTH / 2.0F, Settings.HEIGHT / 2.0F));

            // Then give appropriate reward
            if (!hasMembershipCard) {
                AbstractRelic relic = new MembershipCard();
                AbstractDungeon.getCurrRoom().spawnRelicAndObtain(Settings.WIDTH / 2.0F, Settings.HEIGHT / 2.0F, relic);
              //  AbstractEvent.logMetricObtainRelicAndCard(ID, "Agree (Upgraded)", relic, curse);
            } else if (!hasCourier) {
                AbstractRelic relic = new Courier();
                AbstractDungeon.getCurrRoom().spawnRelicAndObtain(Settings.WIDTH / 2.0F, Settings.HEIGHT / 2.0F, relic);
             //   AbstractEvent.logMetricObtainRelicAndCard(ID, "Agree (Upgraded)", relic, curse);
            } else {
                AbstractDungeon.effectList.add(new RainingGoldEffect(goldReward * 2));
                AbstractDungeon.player.gainGold(goldReward * 2);
              //  AbstractEvent.logMetricGainGoldAndCard(ID, "Agree (Upgraded)", curse, goldReward * 2);
            }

            __instance.imageEventText.updateBodyText(DESCRIPTIONS[3]); // GOLD_RAIN_MSG
            __instance.imageEventText.updateDialogOption(0, OPTIONS[4]);

            try {
                for (Class<?> innerClass : BetterSerpentUpgrade.eventClass.getDeclaredClasses()) {
                    if (innerClass.getSimpleName().equals("CUR_SCREEN")) {
                        Object completeScreen = Enum.valueOf((Class<Enum>) innerClass, "COMPLETE");
                        ReflectionHacks.setPrivate(__instance, BetterSerpentUpgrade.eventClass, "screen", completeScreen);
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return SpireReturn.Return(null);
        }

        return SpireReturn.Continue();
    }

    @SpirePostfixPatch
    public static void Postfix(AbstractEvent __instance, int buttonPressed) {
        if (BetterSerpentUpgrade.isBetterThirdLoaded) {
            ButtonEffectHelper.updateUpgradeAvailability();
        }
    }
}