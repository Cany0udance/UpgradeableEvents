package upgradeableevents.patches.Shrines;

import basemod.ReflectionHacks;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.events.shrines.WeMeetAgain;
import com.megacrit.cardcrawl.potions.AbstractPotion;
import com.megacrit.cardcrawl.vfx.cardManip.ShowCardAndObtainEffect;
import upgradeableevents.UpgradeEventManager;
import upgradeableevents.eventupgrades.Shrines.WeMeetAgainUpgrade;
import upgradeableevents.interfaces.AbstractEventUpgrade;
import upgradeableevents.util.ButtonEffectHelper;

import java.lang.reflect.Method;

@SpirePatch(clz = WeMeetAgain.class, method = "buttonEffect")
public class WeMeetAgainButtonEffectPatch {
    @SpirePrefixPatch
    public static SpireReturn<Void> Prefix(WeMeetAgain __instance, int buttonPressed) {

        // Check if we're on the COMPLETE screen (Leave button)
        Object screenEnum = ReflectionHacks.getPrivate(__instance, WeMeetAgain.class, "screen");
        int screenOrdinal = ((Enum<?>) screenEnum).ordinal();
        if (screenOrdinal == 1) { // COMPLETE
            return SpireReturn.Continue();
        }

        AbstractEventUpgrade currentUpgrade = UpgradeEventManager.getCurrentUpgrade();
        if (currentUpgrade instanceof WeMeetAgainUpgrade) {
            WeMeetAgainUpgrade upgrade = (WeMeetAgainUpgrade) currentUpgrade;
            if (upgrade.isUpgraded() && buttonPressed < 3) {  // Not the Leave option

                try {
                    Class<?> screenEnumClass = Class.forName("com.megacrit.cardcrawl.events.shrines.WeMeetAgain$CUR_SCREEN");
                    Enum<?> completeScreen = Enum.valueOf((Class<Enum>) screenEnumClass, "COMPLETE");
                    ReflectionHacks.setPrivate(__instance, WeMeetAgain.class, "screen", completeScreen);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }

                AbstractPotion potionOption = ReflectionHacks.getPrivate(__instance, WeMeetAgain.class, "potionOption");
                int goldAmount = ReflectionHacks.getPrivate(__instance, WeMeetAgain.class, "goldAmount");
                AbstractCard cardOption = ReflectionHacks.getPrivate(__instance, WeMeetAgain.class, "cardOption");
                String[] DESCRIPTIONS = CardCrawlGame.languagePack.getEventString("WeMeetAgain").DESCRIPTIONS;

                // Call relicReward() using reflection
                try {
                    Method relicRewardMethod = WeMeetAgain.class.getDeclaredMethod("relicReward");
                    relicRewardMethod.setAccessible(true);
                    relicRewardMethod.invoke(__instance);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                switch(buttonPressed) {
                    case 0:  // Potion option
                        if (potionOption != null) {
                            AbstractDungeon.player.obtainPotion(potionOption.makeCopy());
                            __instance.imageEventText.updateBodyText(DESCRIPTIONS[1] + DESCRIPTIONS[5]);
                        }
                        break;
                    case 1:  // Gold option
                        if (goldAmount != 0) {
                            AbstractDungeon.player.gainGold(goldAmount);
                            __instance.imageEventText.updateBodyText(DESCRIPTIONS[2] + DESCRIPTIONS[5]);
                        }
                        break;
                    case 2:  // Card option
                        if (cardOption != null) {
                            AbstractCard cardCopy = cardOption.makeStatEquivalentCopy();
                            AbstractDungeon.effectsQueue.add(new ShowCardAndObtainEffect(cardCopy, Settings.WIDTH / 2.0f, Settings.HEIGHT / 2.0f));
                            __instance.imageEventText.updateBodyText(DESCRIPTIONS[3] + DESCRIPTIONS[5]);
                        }
                        break;
                }

                __instance.imageEventText.updateDialogOption(0, WeMeetAgain.OPTIONS[8]);
                __instance.imageEventText.clearRemainingOptions();

                return SpireReturn.Return();
            }
        }
        return SpireReturn.Continue();
    }

    @SpirePostfixPatch
    public static void Postfix(WeMeetAgain __instance, int buttonPressed) {
        ButtonEffectHelper.updateUpgradeAvailability();
    }
}