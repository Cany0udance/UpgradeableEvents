package upgradeableevents.patches.Exordium;


import basemod.BaseMod;
import basemod.ReflectionHacks;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.events.AbstractEvent;
import com.megacrit.cardcrawl.events.exordium.LivingWall;
import com.megacrit.cardcrawl.vfx.UpgradeShineEffect;
import com.megacrit.cardcrawl.vfx.cardManip.PurgeCardEffect;
import com.megacrit.cardcrawl.vfx.cardManip.ShowCardAndObtainEffect;
import com.megacrit.cardcrawl.vfx.cardManip.ShowCardBrieflyEffect;
import javassist.CtBehavior;
import upgradeableevents.UpgradeEventManager;
import upgradeableevents.eventupgrades.Exordium.LivingWallUpgrade;
import upgradeableevents.interfaces.AbstractEventUpgrade;
import upgradeableevents.util.ButtonEffectHelper;

import java.util.ArrayList;

@SpirePatch(clz = LivingWall.class, method = "buttonEffect")
public class LivingWallButtonEffectPatch {
    @SpireInsertPatch(
            locator = Locator.class
    )
    public static SpireReturn<Void> Insert(LivingWall __instance, int buttonPressed) {
        AbstractEventUpgrade currentUpgrade = UpgradeEventManager.getCurrentUpgrade();

        // Get screen enum safely
        Enum<?> screen = ReflectionHacks.getPrivate(__instance, LivingWall.class, "screen");
        if (!(currentUpgrade instanceof LivingWallUpgrade) ||
                !((LivingWallUpgrade)currentUpgrade).isUpgraded() ||
                screen == null ||
                !"INTRO".equals(screen.name())) {
            return SpireReturn.Continue();
        }

        LivingWallUpgrade upgrade = (LivingWallUpgrade)currentUpgrade;

        switch (buttonPressed) {
            case 0: // Remove
                if (!upgrade.getCardsToRemove().isEmpty()) {
                    ArrayList<String> cardNames = new ArrayList<>();
                    for (AbstractCard card : upgrade.getCardsToRemove()) {
                        cardNames.add(card.cardID);
                        CardCrawlGame.sound.play("CARD_EXHAUST");
                        AbstractDungeon.topLevelEffects.add(new PurgeCardEffect(card, Settings.WIDTH / 2.0f, Settings.HEIGHT / 2.0f));
                        AbstractDungeon.player.masterDeck.removeCard(card);
                    }
                    AbstractEvent.logMetricRemoveCards("Living Wall", "Removed Multiple", cardNames);
                }
                break;

            case 1: // Transform
                if (!upgrade.getCardsToTransform().isEmpty()) {
                    for (AbstractCard card : upgrade.getCardsToTransform()) {
                        AbstractDungeon.player.masterDeck.removeCard(card);
                        AbstractDungeon.transformCard(card, false, AbstractDungeon.miscRng);
                        AbstractCard transformedCard = AbstractDungeon.getTransformedCard();
                        AbstractDungeon.effectList.add(new ShowCardAndObtainEffect(transformedCard, card.current_x, card.current_y));
                        AbstractEvent.logMetricTransformCard("Living Wall", "Transform", card, transformedCard);
                    }
                }
                break;

            case 2: // Upgrade
                if (!upgrade.getCardsToUpgrade().isEmpty()) {
                    float startX = Settings.WIDTH / 2.0f - (upgrade.getCardsToUpgrade().size() * 50.0f);
                    for (int i = 0; i < upgrade.getCardsToUpgrade().size(); i++) {
                        AbstractCard card = upgrade.getCardsToUpgrade().get(i);
                        float cardX = startX + (i * 100.0f);

                        // Add upgrade effect
                        AbstractDungeon.effectsQueue.add(new UpgradeShineEffect(cardX, Settings.HEIGHT / 2.0f));

                        // Upgrade the card
                        card.upgrade();
                        AbstractDungeon.player.bottledCardUpgradeCheck(card);

                        // Show the upgraded card
                        AbstractCard upgradedCard = card.makeStatEquivalentCopy();
                        AbstractDungeon.effectsQueue.add(new ShowCardBrieflyEffect(upgradedCard, cardX, Settings.HEIGHT / 2.0f));

                        AbstractEvent.logMetricCardUpgrade("Living Wall", "Upgrade", card);
                    }
                }
                break;
        }

        try {
            // Get the enum values properly
            Class<?> curScreenEnum = Class.forName("com.megacrit.cardcrawl.events.exordium.LivingWall$CurScreen");
            Class<?> choiceEnum = Class.forName("com.megacrit.cardcrawl.events.exordium.LivingWall$Choice");

            // Update screen state and dialog
            ReflectionHacks.setPrivate(__instance, LivingWall.class, "screen", Enum.valueOf((Class<Enum>)curScreenEnum, "RESULT"));
            ReflectionHacks.setPrivate(__instance, LivingWall.class, "choice", Enum.valueOf((Class<Enum>)choiceEnum, "GROW"));
            ReflectionHacks.setPrivate(__instance, LivingWall.class, "pickCard", false);
            __instance.imageEventText.updateBodyText(LivingWall.DESCRIPTIONS[1]);
            __instance.imageEventText.clearAllDialogs();
            __instance.imageEventText.setDialogOption(LivingWall.OPTIONS[6]);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return SpireReturn.Return(null);
    }

    private static class Locator extends SpireInsertLocator {
        @Override
        public int[] Locate(CtBehavior ctMethodToPatch) throws Exception {
            Matcher finalMatcher = new Matcher.FieldAccessMatcher(
                    LivingWall.class, "screen"
            );
            return LineFinder.findInOrder(ctMethodToPatch, finalMatcher);
        }
    }

    @SpirePostfixPatch
    public static void Postfix(LivingWall __instance, int buttonPressed) {
        ButtonEffectHelper.updateUpgradeAvailability();
    }
}