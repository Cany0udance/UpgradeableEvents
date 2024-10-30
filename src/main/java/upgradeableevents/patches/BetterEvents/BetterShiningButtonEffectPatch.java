package upgradeableevents.patches.BetterEvents;

import basemod.ReflectionHacks;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.events.AbstractEvent;
import com.megacrit.cardcrawl.vfx.UpgradeShineEffect;
import com.megacrit.cardcrawl.vfx.cardManip.ShowCardAndObtainEffect;
import com.megacrit.cardcrawl.vfx.cardManip.ShowCardBrieflyEffect;
import com.megacrit.cardcrawl.vfx.combat.FlashAtkImgEffect;
import upgradeableevents.UpgradeEventManager;
import upgradeableevents.eventupgrades.BetterEvents.BetterShiningUpgrade;
import upgradeableevents.interfaces.AbstractEventUpgrade;
import upgradeableevents.util.ButtonEffectHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

@SpirePatch(
        optional = true,
        cls = "betterThird.events.BetterShiningEvent",
        method = "buttonEffect"
)
public class BetterShiningButtonEffectPatch {
    @SpirePrefixPatch
    public static SpireReturn<Void> Prefix(AbstractEvent __instance, int buttonPressed) {
        if (!BetterShiningUpgrade.isBetterThirdLoaded) return SpireReturn.Continue();

        AbstractEventUpgrade currentUpgrade = UpgradeEventManager.getCurrentUpgrade();
        if (currentUpgrade instanceof BetterShiningUpgrade && ((BetterShiningUpgrade)currentUpgrade).isUpgraded()) {
            Enum<?> screen = ReflectionHacks.getPrivate(__instance, BetterShiningUpgrade.eventClass, "screen");

            if ("INTRO".equals(screen.name())) {
                String[] OPTIONS = ReflectionHacks.getPrivateStatic(BetterShiningUpgrade.eventClass, "OPTIONS");
                int damage = ReflectionHacks.getPrivate(__instance, BetterShiningUpgrade.eventClass, "damage");
                int startHP = ReflectionHacks.getPrivate(__instance, BetterShiningUpgrade.eventClass, "startHP");
                int maxHP = ReflectionHacks.getPrivate(__instance, BetterShiningUpgrade.eventClass, "maxHP");
                boolean dreamcatcher = ReflectionHacks.getPrivate(__instance, BetterShiningUpgrade.eventClass, "dreamcatcher");
                AbstractCard card = ReflectionHacks.getPrivate(__instance, BetterShiningUpgrade.eventClass, "card");
                AbstractCard burn = ReflectionHacks.getPrivate(__instance, BetterShiningUpgrade.eventClass, "burn");

                switch (buttonPressed) {
                    case 0: // Upgrade 3 cards option
                        upgradeThreeCards(__instance, damage, startHP, maxHP, dreamcatcher);
                        break;
                    case 1: // Modified Apotheosis option
                        String choice = "Embrace";
                        int roll = AbstractDungeon.miscRng.random(0, 99);
                        if (roll < 35) { // 35% chance
                            burn.upgrade();
                            AbstractDungeon.effectList.add(new ShowCardAndObtainEffect(burn, Settings.WIDTH * 0.5F, Settings.HEIGHT / 2.0F, false));
                            __instance.imageEventText.updateBodyText(ReflectionHacks.getPrivateStatic(BetterShiningUpgrade.eventClass, "BURN_DIALOG"));
                            choice = choice + " Burn";
                        } else {
                            AbstractDungeon.effectList.add(new ShowCardAndObtainEffect(card, Settings.WIDTH * 0.5F, Settings.HEIGHT / 2.0F, false));
                            __instance.imageEventText.updateBodyText(ReflectionHacks.getPrivateStatic(BetterShiningUpgrade.eventClass, "EMBRACE_DIALOG"));
                        }
                        __instance.imageEventText.clearAllDialogs();
                        __instance.imageEventText.setDialogOption(OPTIONS[2]);
                        setScreenToComplete(__instance);
                        logMetricHelper(__instance, choice, 0, startHP, maxHP, dreamcatcher);
                        break;
                    default:
                        return SpireReturn.Continue();
                }
                return SpireReturn.Return(null);
            }
        }
        return SpireReturn.Continue();
    }

    private static void upgradeThreeCards(AbstractEvent __instance, int damage, int startHP, int maxHP, boolean dreamcatcher) {
        AbstractDungeon.topLevelEffects.add(new UpgradeShineEffect(Settings.WIDTH / 2.0F, Settings.HEIGHT / 2.0F));
        ArrayList<AbstractCard> upgradableCards = new ArrayList<>();
        for (AbstractCard c : AbstractDungeon.player.masterDeck.group) {
            if (c.canUpgrade()) {
                upgradableCards.add(c);
            }
        }

        if (!upgradableCards.isEmpty()) {
            Collections.shuffle(upgradableCards, new Random(AbstractDungeon.miscRng.randomLong()));
            List<String> cardMetrics = new ArrayList<>();
            int numToUpgrade = Math.min(3, upgradableCards.size());

            for (int i = 0; i < numToUpgrade; i++) {
                upgradableCards.get(i).upgrade();
                cardMetrics.add(upgradableCards.get(i).cardID);
                AbstractDungeon.player.bottledCardUpgradeCheck(upgradableCards.get(i));

                float xPos = Settings.WIDTH / 2.0F;
                if (numToUpgrade == 2) {
                    xPos += (i == 0 ? -190.0F : 190.0F) * Settings.scale;
                } else if (numToUpgrade == 3) {
                    xPos += (i == 0 ? -380.0F : (i == 2 ? 380.0F : 0.0F)) * Settings.scale;
                }

                AbstractDungeon.effectList.add(new ShowCardBrieflyEffect(
                        upgradableCards.get(i).makeStatEquivalentCopy(),
                        xPos,
                        Settings.HEIGHT / 2.0F
                ));
            }

            AbstractDungeon.player.damage(new DamageInfo(AbstractDungeon.player, damage));
            AbstractDungeon.effectList.add(new FlashAtkImgEffect(
                    AbstractDungeon.player.hb.cX,
                    AbstractDungeon.player.hb.cY,
                    AbstractGameAction.AttackEffect.FIRE
            ));

            __instance.imageEventText.updateBodyText(ReflectionHacks.getPrivateStatic(BetterShiningUpgrade.eventClass, "AGREE_DIALOG"));
            __instance.imageEventText.clearAllDialogs();
            __instance.imageEventText.setDialogOption(((String[])ReflectionHacks.getPrivateStatic(BetterShiningUpgrade.eventClass, "OPTIONS"))[2]);

            setScreenToComplete(__instance);
            logMetricHelper(__instance, "Upgrade", damage, startHP, maxHP, dreamcatcher);
        }
    }

    private static void setScreenToComplete(AbstractEvent __instance) {
        try {
            for (Class<?> innerClass : BetterShiningUpgrade.eventClass.getDeclaredClasses()) {
                if (innerClass.getSimpleName().equals("CUR_SCREEN")) {
                    Object completeScreen = Enum.valueOf((Class<Enum>) innerClass, "COMPLETE");
                    ReflectionHacks.setPrivate(__instance, BetterShiningUpgrade.eventClass, "screen", completeScreen);
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void logMetricHelper(AbstractEvent __instance, String choice, int damage, int startHP, int maxHP, boolean dreamcatcher) {
        AbstractEvent.logMetric(
                ReflectionHacks.getPrivateStatic(BetterShiningUpgrade.eventClass, "ID"),
                choice,
                null, null, null, null, null, null, null,
                damage, startHP, maxHP, 0, 0,
                dreamcatcher ? 1 : 0
        );
    }

    @SpirePostfixPatch
    public static void Postfix(AbstractEvent __instance, int buttonPressed) {
        if (BetterShiningUpgrade.isBetterThirdLoaded) {
            ButtonEffectHelper.updateUpgradeAvailability();
        }
    }
}