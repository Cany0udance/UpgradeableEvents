package upgradeableevents.patches.BetterEvents;

import basemod.ReflectionHacks;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.events.AbstractEvent;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.vfx.cardManip.ShowCardAndObtainEffect;
import upgradeableevents.UpgradeEventManager;
import upgradeableevents.eventupgrades.BetterEvents.BetterScrapUpgrade;
import upgradeableevents.interfaces.AbstractEventUpgrade;
import upgradeableevents.util.ButtonEffectHelper;

@SpirePatch(
        optional = true,
        cls = "betterThird.events.BetterScrapEvent",
        method = "buttonEffect"
)
public class BetterScrapButtonEffectPatch {
    @SpirePrefixPatch
    public static SpireReturn<Void> Prefix(AbstractEvent __instance, int buttonPressed) {
        if (!BetterScrapUpgrade.isBetterThirdLoaded) return SpireReturn.Continue();

        AbstractEventUpgrade currentUpgrade = UpgradeEventManager.getCurrentUpgrade();
        if (!(currentUpgrade instanceof BetterScrapUpgrade) || !((BetterScrapUpgrade)currentUpgrade).isUpgraded()) {
            return SpireReturn.Continue();
        }

        BetterScrapUpgrade upgrade = (BetterScrapUpgrade)currentUpgrade;
        Enum<?> screen = ReflectionHacks.getPrivate(__instance, BetterScrapUpgrade.eventClass, "screen");

        if ("INTRO".equals(screen.name())) {
            String[] DESCRIPTIONS = ReflectionHacks.getPrivateStatic(BetterScrapUpgrade.eventClass, "DESCRIPTIONS");
            String[] OPTIONS = ReflectionHacks.getPrivateStatic(BetterScrapUpgrade.eventClass, "OPTIONS");
            boolean defense = ReflectionHacks.getPrivate(__instance, BetterScrapUpgrade.eventClass, "defense");

            switch (buttonPressed) {
                case 0: // Relic attempt
                    int dmg = ReflectionHacks.getPrivate(__instance, BetterScrapUpgrade.eventClass, "dmg");
                    int relicObtainChance = ReflectionHacks.getPrivate(__instance, BetterScrapUpgrade.eventClass, "relicObtainChance");
                    int totalDamageDealt = ReflectionHacks.getPrivate(__instance, BetterScrapUpgrade.eventClass, "totalDamageDealt");
                    boolean relic = ReflectionHacks.getPrivate(__instance, BetterScrapUpgrade.eventClass, "relic");
                    boolean relic2 = ReflectionHacks.getPrivate(__instance, BetterScrapUpgrade.eventClass, "relic2");

                    // Apply damage
                    AbstractDungeon.player.damage(new DamageInfo(null, dmg));
                    CardCrawlGame.sound.play("ATTACK_POISON");
                    totalDamageDealt += dmg;
                    ReflectionHacks.setPrivate(__instance, BetterScrapUpgrade.eventClass, "totalDamageDealt", totalDamageDealt);

                    int random = AbstractDungeon.miscRng.random(0, 99);
                    if (random >= 99 - relicObtainChance) {
                        // Success
                        upgrade.incrementRelicsObtained();
                        AbstractRelic r = AbstractDungeon.returnRandomScreenlessRelic(returnRandomRelicTier(__instance));
                        AbstractDungeon.getCurrRoom().spawnRelicAndObtain(Settings.WIDTH / 2.0F, Settings.HEIGHT / 2.0F, r);

                        if (upgrade.getRelicsObtained() >= upgrade.getMaxObtainable() || (!defense && upgrade.getRelicsObtained() >= 2)) {
                            // Max relics obtained
                            __instance.imageEventText.updateBodyText(DESCRIPTIONS[2] + DESCRIPTIONS[6]); // SUCCESS_MSG + FINAL_MSG
                            __instance.imageEventText.clearAllDialogs();
                            __instance.imageEventText.setDialogOption(OPTIONS[3]);
                            try {
                                for (Class<?> innerClass : BetterScrapUpgrade.eventClass.getDeclaredClasses()) {
                                    if (innerClass.getSimpleName().equals("CurScreen")) {
                                        Object leaveScreen = Enum.valueOf((Class<Enum>) innerClass, "LEAVE");
                                        ReflectionHacks.setPrivate(__instance, BetterScrapUpgrade.eventClass, "screen", leaveScreen);
                                        break;
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            // Can continue searching
                            __instance.imageEventText.updateBodyText(DESCRIPTIONS[2] + DESCRIPTIONS[5]); // SUCCESS_MSG + SUCCESS_TEASE
                            ReflectionHacks.setPrivate(__instance, BetterScrapUpgrade.eventClass, "relicObtainChance", 25);
                            ReflectionHacks.setPrivate(__instance, BetterScrapUpgrade.eventClass, "dmg", dmg);
                            __instance.imageEventText.updateDialogOption(0, OPTIONS[4] + dmg + OPTIONS[1] + "25" + OPTIONS[2]);
                        }
                        return SpireReturn.Return(null);
                    } else {
                        // Failure
                        __instance.imageEventText.updateBodyText(DESCRIPTIONS[1]); // FAIL_MSG
                        ReflectionHacks.setPrivate(__instance, BetterScrapUpgrade.eventClass, "relicObtainChance", relicObtainChance + 10);
                        ReflectionHacks.setPrivate(__instance, BetterScrapUpgrade.eventClass, "dmg", dmg + 1);
                        __instance.imageEventText.updateDialogOption(0, OPTIONS[4] + (dmg + 1) + OPTIONS[1] + (relicObtainChance + 10) + OPTIONS[2]);
                        return SpireReturn.Return(null);
                    }

                case 1: // Card attempt
                    int cardDmg = ReflectionHacks.getPrivate(__instance, BetterScrapUpgrade.eventClass, "cardDmg");
                    int cardObtainChance = ReflectionHacks.getPrivate(__instance, BetterScrapUpgrade.eventClass, "cardObtainChance");
                    totalDamageDealt = ReflectionHacks.getPrivate(__instance, BetterScrapUpgrade.eventClass, "totalDamageDealt");
                    AbstractCard card = ReflectionHacks.getPrivate(__instance, BetterScrapUpgrade.eventClass, "card");
                    boolean cardEarned = ReflectionHacks.getPrivate(__instance, BetterScrapUpgrade.eventClass, "cardEarned");
                    boolean cardEarned2 = ReflectionHacks.getPrivate(__instance, BetterScrapUpgrade.eventClass, "cardEarned2");

                    // Apply damage
                    AbstractDungeon.player.damage(new DamageInfo(null, cardDmg));
                    CardCrawlGame.sound.play("ATTACK_POISON");
                    totalDamageDealt += cardDmg;
                    ReflectionHacks.setPrivate(__instance, BetterScrapUpgrade.eventClass, "totalDamageDealt", totalDamageDealt);

                    random = AbstractDungeon.miscRng.random(0, 99);
                    if (random >= 99 - cardObtainChance) {
                        // Success
                        upgrade.incrementCardsObtained();
                        if (card.color == AbstractCard.CardColor.BLUE && AbstractDungeon.player.masterMaxOrbs == 0) {
                            AbstractDungeon.player.masterMaxOrbs = 1;
                        }
                        AbstractDungeon.effectList.add(new ShowCardAndObtainEffect(card, Settings.WIDTH * 0.3F, Settings.HEIGHT / 2.0F));

                        if (upgrade.getCardsObtained() >= upgrade.getMaxObtainable() || (!defense && upgrade.getCardsObtained() >= 2)) {
                            // Max cards obtained
                            __instance.imageEventText.updateBodyText(DESCRIPTIONS[4] + DESCRIPTIONS[6]); // CARD_SUCCESS_MSG + FINAL_MSG
                            __instance.imageEventText.clearAllDialogs();
                            __instance.imageEventText.setDialogOption(OPTIONS[3]);
                            try {
                                for (Class<?> innerClass : BetterScrapUpgrade.eventClass.getDeclaredClasses()) {
                                    if (innerClass.getSimpleName().equals("CurScreen")) {
                                        Object leaveScreen = Enum.valueOf((Class<Enum>) innerClass, "LEAVE");
                                        ReflectionHacks.setPrivate(__instance, BetterScrapUpgrade.eventClass, "screen", leaveScreen);
                                        break;
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            // Can continue searching
                            __instance.imageEventText.updateBodyText(DESCRIPTIONS[4] + DESCRIPTIONS[5]); // CARD_SUCCESS_MSG + SUCCESS_TEASE
                            ReflectionHacks.setPrivate(__instance, BetterScrapUpgrade.eventClass, "cardObtainChance", 35);
                            ReflectionHacks.setPrivate(__instance, BetterScrapUpgrade.eventClass, "cardDmg", cardDmg);
                            ReflectionHacks.privateMethod(BetterScrapUpgrade.eventClass, "generateCard").invoke(__instance);
                            card = ReflectionHacks.getPrivate(__instance, BetterScrapUpgrade.eventClass, "card");
                            __instance.imageEventText.updateDialogOption(1, OPTIONS[4] + cardDmg + OPTIONS[1] + "35" + OPTIONS[5] + card.name + OPTIONS[6], card);
                        }
                        return SpireReturn.Return(null);
                    } else {
                        // Failure
                        __instance.imageEventText.updateBodyText(DESCRIPTIONS[1]); // FAIL_MSG
                        ReflectionHacks.setPrivate(__instance, BetterScrapUpgrade.eventClass, "cardObtainChance", cardObtainChance + 10);
                        ReflectionHacks.setPrivate(__instance, BetterScrapUpgrade.eventClass, "cardDmg", cardDmg + 1);
                        __instance.imageEventText.updateDialogOption(1, OPTIONS[4] + (cardDmg + 1) + OPTIONS[1] + (cardObtainChance + 10) + OPTIONS[5] + card.name + OPTIONS[6], card);
                        return SpireReturn.Return(null);
                    }
            }
        }

        return SpireReturn.Continue();
    }

    private static AbstractRelic.RelicTier returnRandomRelicTier(AbstractEvent __instance) {
        int roll = AbstractDungeon.relicRng.random(0, 99);
        boolean relic = ReflectionHacks.getPrivate(__instance, BetterScrapUpgrade.eventClass, "relic");

        if (relic) {
            if (roll < 20) return AbstractRelic.RelicTier.COMMON;
            else return roll < 70 ? AbstractRelic.RelicTier.UNCOMMON : AbstractRelic.RelicTier.RARE;
        } else {
            if (roll < 75) return AbstractRelic.RelicTier.COMMON;
            else return roll < 90 ? AbstractRelic.RelicTier.UNCOMMON : AbstractRelic.RelicTier.RARE;
        }
    }

    @SpirePostfixPatch
    public static void Postfix(AbstractEvent __instance, int buttonPressed) {
        if (BetterScrapUpgrade.isBetterThirdLoaded) {
            ButtonEffectHelper.updateUpgradeAvailability();
        }
    }
}