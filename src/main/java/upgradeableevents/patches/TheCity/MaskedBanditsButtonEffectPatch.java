package upgradeableevents.patches.TheCity;

import basemod.ReflectionHacks;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.events.city.MaskedBandits;
import com.megacrit.cardcrawl.relics.Circlet;
import com.megacrit.cardcrawl.relics.RedMask;
import com.megacrit.cardcrawl.vfx.GainPennyEffect;
import javassist.CtBehavior;
import upgradeableevents.UpgradeEventManager;
import upgradeableevents.eventupgrades.TheCity.MaskedBanditsUpgrade;
import upgradeableevents.interfaces.AbstractEventUpgrade;
import upgradeableevents.util.ButtonEffectHelper;

@SpirePatch(clz = MaskedBandits.class, method = "buttonEffect")
public class MaskedBanditsButtonEffectPatch {

    @SpireInsertPatch(
            locator = Locator.class
    )
    public static SpireReturn<Void> Insert(MaskedBandits __instance, int buttonPressed) {
        AbstractEventUpgrade currentUpgrade = UpgradeEventManager.getCurrentUpgrade();
        if (currentUpgrade instanceof MaskedBanditsUpgrade && ((MaskedBanditsUpgrade)currentUpgrade).isUpgraded()) {
            Enum<?> screen = ReflectionHacks.getPrivate(__instance, MaskedBandits.class, "screen");

            if ("INTRO".equals(screen.name())) {
                switch (buttonPressed) {
                    case 1: // Fight - Double gold rewards
                      //  logMetric("Masked Bandits", "Fought Bandits (Upgraded)");
                        // Double the gold rewards
                        if (Settings.isDailyRun) {
                            AbstractDungeon.getCurrRoom().addGoldToRewards(AbstractDungeon.miscRng.random(60));
                        } else {
                            AbstractDungeon.getCurrRoom().addGoldToRewards(AbstractDungeon.miscRng.random(50, 70));
                        }

                        // Add the Red Mask relic (or Circlet if player already has Red Mask)
                        if (AbstractDungeon.player.hasRelic("Red Mask")) {
                            AbstractDungeon.getCurrRoom().addRelicToRewards(new Circlet());
                        } else {
                            AbstractDungeon.getCurrRoom().addRelicToRewards(new RedMask());
                        }

                        __instance.enterCombat();
                        AbstractDungeon.lastCombatMetricKey = "Masked Bandits";
                        return SpireReturn.Return(null);

                    case 0: // Leave - Only pay 3 gold if you have it
                        if (AbstractDungeon.player.gold >= 3) {
                           // logMetricLoseGold("Masked Bandits", "Paid Small Fee (Upgraded)", 3);
                            AbstractDungeon.player.loseGold(3);

                            // Show gold stealing animation for just 3 gold
                            CardCrawlGame.sound.play("GOLD_JINGLE");
                            for(int i = 0; i < 3; ++i) {
                                AbstractCreature source = AbstractDungeon.getCurrRoom().monsters.getRandomMonster();
                                AbstractDungeon.effectList.add(new GainPennyEffect(source,
                                        AbstractDungeon.player.hb.cX,
                                        AbstractDungeon.player.hb.cY,
                                        source.hb.cX,
                                        source.hb.cY,
                                        false));
                            }

                            __instance.roomEventText.updateBodyText(MaskedBandits.DESCRIPTIONS[0]);
                            __instance.roomEventText.updateDialogOption(0, MaskedBandits.OPTIONS[2]);
                            __instance.roomEventText.clearRemainingOptions();

                            // Set screen to PAID_1
                            Class<?> screenEnum = MaskedBandits.class.getDeclaredClasses()[0];
                            Object paidScreen = Enum.valueOf((Class<Enum>) screenEnum, "PAID_1");
                            ReflectionHacks.setPrivate(__instance, MaskedBandits.class, "screen", paidScreen);

                            return SpireReturn.Return(null);
                        }
                        break;
                }
            }
        }
        return SpireReturn.Continue();
    }

    private static class Locator extends SpireInsertLocator {
        @Override
        public int[] Locate(CtBehavior ctMethodToPatch) throws Exception {
            Matcher finalMatcher = new Matcher.FieldAccessMatcher(MaskedBandits.class, "screen");
            return LineFinder.findInOrder(ctMethodToPatch, finalMatcher);
        }
    }

    @SpirePostfixPatch
    public static void Postfix(MaskedBandits __instance, int buttonPressed) {
        ButtonEffectHelper.updateUpgradeAvailability();
    }
}