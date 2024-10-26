package upgradeableevents.patches.Shrines;

import basemod.ReflectionHacks;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.events.shrines.Bonfire;
import com.megacrit.cardcrawl.helpers.RelicLibrary;
import com.megacrit.cardcrawl.relics.Circlet;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import com.megacrit.cardcrawl.vfx.cardManip.PurgeCardEffect;
import javassist.CtBehavior;
import upgradeableevents.UpgradeEventManager;
import upgradeableevents.eventupgrades.Shrines.BonfireUpgrade;
import upgradeableevents.interfaces.AbstractEventUpgrade;

@SpirePatch(clz = Bonfire.class, method = "update")
public class BonfireUpdatePatch {
    private static boolean processingCards = false;

    @SpireInsertPatch(
            locator = Locator.class
    )
    public static SpireReturn<Void> Insert(Bonfire __instance) {
        AbstractEventUpgrade currentUpgrade = UpgradeEventManager.getCurrentUpgrade();
        if (currentUpgrade instanceof BonfireUpgrade &&
                ((BonfireUpgrade)currentUpgrade).isUpgraded() &&
                !AbstractDungeon.gridSelectScreen.selectedCards.isEmpty()) {

            boolean cardSelect = ReflectionHacks.getPrivate(__instance, Bonfire.class, "cardSelect");

            if (!cardSelect || processingCards) return SpireReturn.Continue();

            processingCards = true;

            try {
                // Get both cards
                AbstractCard firstCard = AbstractDungeon.gridSelectScreen.selectedCards.get(0);
                AbstractCard secondCard = AbstractDungeon.gridSelectScreen.selectedCards.size() > 1 ?
                        AbstractDungeon.gridSelectScreen.selectedCards.get(1) : null;

                if (secondCard == null) {
                    return SpireReturn.Return(null);
                }

                // Process both cards using the original logic, but prevent screen from changing
                Object originalScreen = ReflectionHacks.getPrivate(__instance, Bonfire.class, "screen");
                Object[] enumConstants = originalScreen.getClass().getEnumConstants();

                // Process first card
                processCardWithoutScreenChange(firstCard);

                // Process second card
                processCardWithoutScreenChange(secondCard);

                // Clear selected cards
                AbstractDungeon.gridSelectScreen.selectedCards.clear();

                // Find the highest rarity card and set the dialog text
                AbstractCard highestRarityCard = getHigherRarityCard(firstCard, secondCard);
                setRewardDialog(__instance, highestRarityCard.rarity);

                // Now set the final state
                __instance.imageEventText.updateDialogOption(0, Bonfire.OPTIONS[1]);
                ReflectionHacks.setPrivate(__instance, Bonfire.class, "screen", enumConstants[2]); // COMPLETE
                ReflectionHacks.setPrivate(__instance, Bonfire.class, "cardSelect", false);
                AbstractDungeon.getCurrRoom().phase = AbstractRoom.RoomPhase.COMPLETE;

                return SpireReturn.Return(null);
            } finally {
                processingCards = false;
            }
        }
        return SpireReturn.Continue();
    }

    private static void setRewardDialog(Bonfire instance, AbstractCard.CardRarity rarity) {
        String dialog3 = ReflectionHacks.getPrivateStatic(Bonfire.class, "DIALOG_3");
        String[] descriptions = ReflectionHacks.getPrivateStatic(Bonfire.class, "DESCRIPTIONS");

        String dialog = dialog3;
        switch (rarity) {
            case CURSE:
                dialog = dialog + descriptions[3];
                break;
            case BASIC:
                dialog = dialog + descriptions[4];
                break;
            case COMMON:
            case SPECIAL:
                dialog = dialog + descriptions[5];
                break;
            case UNCOMMON:
                dialog = dialog + descriptions[6];
                break;
            case RARE:
                dialog = dialog + descriptions[7];
                break;
        }

        instance.imageEventText.updateBodyText(dialog);
    }

    private static AbstractCard getHigherRarityCard(AbstractCard card1, AbstractCard card2) {
        int rarity1 = getRarityValue(card1.rarity);
        int rarity2 = getRarityValue(card2.rarity);
        return rarity1 >= rarity2 ? card1 : card2;
    }

    private static int getRarityValue(AbstractCard.CardRarity rarity) {
        switch (rarity) {
            case RARE: return 5;
            case UNCOMMON: return 4;
            case COMMON: return 3;
            case BASIC: return 2;
            case CURSE: return 1;
            case SPECIAL: return 3; // Treat as common
            default: return 0;
        }
    }

    private static void processCardWithoutScreenChange(AbstractCard card) {

        // Add purge effect
        AbstractDungeon.topLevelEffects.add(new PurgeCardEffect(card, Settings.WIDTH / 2.0F, Settings.HEIGHT / 2.0F));
        AbstractDungeon.player.masterDeck.removeCard(card);

        // Process rewards based on rarity
        switch (card.rarity) {
            case CURSE:
                if (!AbstractDungeon.player.hasRelic("Spirit Poop")) {
                    AbstractDungeon.getCurrRoom().spawnRelicAndObtain(
                            Settings.WIDTH / 2.0F,
                            Settings.HEIGHT / 2.0F,
                            RelicLibrary.getRelic("Spirit Poop").makeCopy()
                    );
                } else {
                    AbstractDungeon.getCurrRoom().spawnRelicAndObtain(
                            Settings.WIDTH / 2.0F,
                            Settings.HEIGHT / 2.0F,
                            new Circlet()
                    );
                }
                break;

            case BASIC:
                break;

            case COMMON:
            case SPECIAL:
                AbstractDungeon.player.heal(5);
                break;

            case UNCOMMON:
                AbstractDungeon.player.heal(AbstractDungeon.player.maxHealth);
                break;

            case RARE:
                AbstractDungeon.player.increaseMaxHp(10, false);
                AbstractDungeon.player.heal(AbstractDungeon.player.maxHealth);
                break;
        }
    }

    private static class Locator extends SpireInsertLocator {
        @Override
        public int[] Locate(CtBehavior ctMethodToPatch) throws Exception {
            Matcher finalMatcher = new Matcher.FieldAccessMatcher(
                    AbstractDungeon.class, "gridSelectScreen"
            );
            return LineFinder.findInOrder(ctMethodToPatch, finalMatcher);
        }
    }
}