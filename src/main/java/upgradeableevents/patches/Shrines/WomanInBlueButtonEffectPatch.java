package upgradeableevents.patches.Shrines;

import basemod.ReflectionHacks;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.events.shrines.WomanInBlue;
import com.megacrit.cardcrawl.helpers.PotionHelper;
import com.megacrit.cardcrawl.rewards.RewardItem;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import upgradeableevents.UpgradeEventManager;
import upgradeableevents.eventupgrades.Shrines.WomanInBlueUpgrade;
import upgradeableevents.interfaces.AbstractEventUpgrade;
import upgradeableevents.util.ButtonEffectHelper;

@SpirePatch(clz = WomanInBlue.class, method = "buttonEffect")
public class WomanInBlueButtonEffectPatch {
    @SpirePrefixPatch
    public static SpireReturn<Void> Prefix(WomanInBlue __instance, int buttonPressed) {

        AbstractEventUpgrade currentUpgrade = UpgradeEventManager.getCurrentUpgrade();
        Enum<?> screenNum = ReflectionHacks.getPrivate(__instance, WomanInBlue.class, "screen");

        if (currentUpgrade instanceof WomanInBlueUpgrade &&
                ((WomanInBlueUpgrade)currentUpgrade).isUpgraded() &&
                screenNum.name().equals("INTRO") &&
                buttonPressed <= 2) {

            // Get the DESCRIPTIONS array using reflection
            String[] DESCRIPTIONS = ReflectionHacks.getPrivateStatic(WomanInBlue.class, "DESCRIPTIONS");

            // Handle the upgraded version
            switch (buttonPressed) {
                case 0:
                    AbstractDungeon.player.loseGold(20);
                    __instance.imageEventText.updateBodyText(DESCRIPTIONS[1]);
                    __instance.logMetric("Bought 2 Potions (Upgraded)");
                    AbstractDungeon.getCurrRoom().rewards.clear();
                    for (int i = 0; i < 2; i++) {
                        AbstractDungeon.getCurrRoom().rewards.add(new RewardItem(PotionHelper.getRandomPotion()));
                    }
                    break;
                case 1:
                    AbstractDungeon.player.loseGold(30);
                    __instance.imageEventText.updateBodyText(DESCRIPTIONS[1]);
                    __instance.logMetric("Bought 4 Potions (Upgraded)");
                    AbstractDungeon.getCurrRoom().rewards.clear();
                    for (int i = 0; i < 4; i++) {
                        AbstractDungeon.getCurrRoom().rewards.add(new RewardItem(PotionHelper.getRandomPotion()));
                    }
                    break;
                case 2:
                    AbstractDungeon.player.loseGold(40);
                    __instance.imageEventText.updateBodyText(DESCRIPTIONS[1]);
                    __instance.logMetric("Bought 6 Potions (Upgraded)");
                    AbstractDungeon.getCurrRoom().rewards.clear();
                    for (int i = 0; i < 6; i++) {
                        AbstractDungeon.getCurrRoom().rewards.add(new RewardItem(PotionHelper.getRandomPotion()));
                    }
                    break;
            }

            if (buttonPressed <= 2) {
                AbstractDungeon.getCurrRoom().phase = AbstractRoom.RoomPhase.COMPLETE;
                AbstractDungeon.combatRewardScreen.open();
                __instance.imageEventText.clearAllDialogs();
                __instance.imageEventText.setDialogOption(((String[])ReflectionHacks.getPrivateStatic(WomanInBlue.class, "OPTIONS"))[4]);

                try {
                    // Try to get the enum class using the full inner class name
                    Class<?> curScreenClass = Class.forName("com.megacrit.cardcrawl.events.shrines.WomanInBlue$CurScreen");

                    Object resultEnum = Enum.valueOf((Class<Enum>)curScreenClass, "RESULT");

                    ReflectionHacks.setPrivate(__instance, WomanInBlue.class, "screen", resultEnum);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return SpireReturn.Return(null);
            }
        }

        return SpireReturn.Continue();
    }

    @SpirePostfixPatch
    public static void Postfix(WomanInBlue __instance, int buttonPressed) {
        ButtonEffectHelper.updateUpgradeAvailability();
    }
}