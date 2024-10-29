package upgradeableevents.patches.TheCity;

import basemod.BaseMod;
import basemod.ReflectionHacks;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.colorless.JAX;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.events.AbstractEvent;
import com.megacrit.cardcrawl.events.city.DrugDealer;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.relics.Circlet;
import com.megacrit.cardcrawl.relics.MutagenicStrength;
import com.megacrit.cardcrawl.relics.Vajra;
import com.megacrit.cardcrawl.vfx.cardManip.ShowCardAndObtainEffect;
import javassist.CtBehavior;
import upgradeableevents.UpgradeEventManager;
import upgradeableevents.eventupgrades.TheCity.DrugDealerUpgrade;
import upgradeableevents.interfaces.AbstractEventUpgrade;
import upgradeableevents.util.ButtonEffectHelper;

import java.util.ArrayList;

@SpirePatch(clz = DrugDealer.class, method = "buttonEffect")
public class DrugDealerButtonEffectPatch {
    @SpireInsertPatch(
            locator = Locator.class
    )
    public static SpireReturn<Void> Insert(DrugDealer __instance, int buttonPressed) {
        BaseMod.logger.info("DrugDealer patch triggered, buttonPressed: " + buttonPressed);

        AbstractEventUpgrade currentUpgrade = UpgradeEventManager.getCurrentUpgrade();
        if (currentUpgrade instanceof DrugDealerUpgrade && ((DrugDealerUpgrade)currentUpgrade).isUpgraded()) {
            BaseMod.logger.info("DrugDealer is upgraded");
            int screenNum = ReflectionHacks.getPrivate(__instance, DrugDealer.class, "screenNum");

            if (screenNum == 0) {
                BaseMod.logger.info("Screen is 0, handling upgraded option: " + buttonPressed);

                // Get drawX and drawY using reflection
                float drawX = ReflectionHacks.getPrivate(__instance, AbstractEvent.class, "drawX");
                float drawY = ReflectionHacks.getPrivate(__instance, AbstractEvent.class, "drawY");

                switch (buttonPressed) {
                    case 0: // Upgraded J.A.X.
                        AbstractCard jax = new JAX();
                        jax.upgrade();
                        __instance.imageEventText.updateBodyText(DrugDealer.DESCRIPTIONS[1]);
                        AbstractDungeon.effectList.add(new ShowCardAndObtainEffect(jax, Settings.WIDTH / 2.0F, Settings.HEIGHT / 2.0F));
                        AbstractEvent.logMetricObtainCard("Drug Dealer", "Obtain Upgraded J.A.X.", jax);

                        __instance.imageEventText.updateDialogOption(0, DrugDealer.OPTIONS[3]);
                        __instance.imageEventText.clearRemainingOptions();
                        break;

                    case 1: // Transform 3 cards
                        __instance.imageEventText.updateBodyText(DrugDealer.DESCRIPTIONS[2]);
                        transformThreeCards(__instance);
                        __instance.imageEventText.updateDialogOption(0, DrugDealer.OPTIONS[3]);
                        __instance.imageEventText.clearRemainingOptions();
                        break;

                    case 2: // Mutagenic Strength + Vajra
                        __instance.imageEventText.updateBodyText(DrugDealer.DESCRIPTIONS[3]);

                        // Handle Mutagenic Strength
                        AbstractRelic r1 = !AbstractDungeon.player.hasRelic("MutagenicStrength")
                                ? new MutagenicStrength()
                                : new Circlet();
                        AbstractDungeon.getCurrRoom().spawnRelicAndObtain(drawX, drawY, r1);

                        // Handle Vajra
                        if (!AbstractDungeon.player.hasRelic("Vajra")) {
                            AbstractRelic r2 = new Vajra();
                            AbstractDungeon.getCurrRoom().spawnRelicAndObtain(drawX, drawY, r2);
                            ArrayList<String> relics = new ArrayList<>();
                            relics.add(r1.relicId);
                            relics.add(r2.relicId);
                            AbstractEvent.logMetric("Drug Dealer", "Inject Mutagens", relics, null, null, null, null, null, null, 0, 0, 0, 0, 0, 0);
                        } else {
                            AbstractEvent.logMetricObtainRelic("Drug Dealer", "Inject Mutagens", r1);
                        }

                        __instance.imageEventText.updateDialogOption(0, DrugDealer.OPTIONS[3]);
                        __instance.imageEventText.clearRemainingOptions();
                        break;
                }

                ReflectionHacks.setPrivate(__instance, DrugDealer.class, "screenNum", 1);
                return SpireReturn.Return(null);
            }
        }
        return SpireReturn.Continue();
    }

    private static void transformThreeCards(DrugDealer instance) {
        if (!AbstractDungeon.isScreenUp) {
            AbstractDungeon.gridSelectScreen.open(
                    AbstractDungeon.player.masterDeck.getPurgeableCards(),
                    3,  // Changed from 2 to 3
                    DrugDealer.OPTIONS[5],
                    false, false, false, false);
        } else {
            AbstractDungeon.dynamicBanner.hide();
            AbstractDungeon.previousScreen = AbstractDungeon.screen;
            AbstractDungeon.gridSelectScreen.open(
                    AbstractDungeon.player.masterDeck.getPurgeableCards(),
                    3,  // Changed from 2 to 3
                    DrugDealer.OPTIONS[5],
                    false, false, false, false);
        }
    }

    private static class Locator extends SpireInsertLocator {
        @Override
        public int[] Locate(CtBehavior ctMethodToPatch) throws Exception {
            Matcher finalMatcher = new Matcher.FieldAccessMatcher(DrugDealer.class, "screenNum");
            int[] matches = LineFinder.findAllInOrder(ctMethodToPatch, finalMatcher);
            // We want the first field access of 'screenNum'
            return new int[]{matches[0]};
        }
    }

    @SpirePostfixPatch
    public static void Postfix(DrugDealer __instance, int buttonPressed) {
        ButtonEffectHelper.updateUpgradeAvailability();
    }
}