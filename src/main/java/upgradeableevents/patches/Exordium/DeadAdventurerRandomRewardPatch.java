package upgradeableevents.patches.Exordium;

import basemod.ReflectionHacks;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.events.exordium.DeadAdventurer;
import upgradeableevents.UpgradeEventManager;
import upgradeableevents.eventupgrades.Exordium.DeadAdventurerUpgrade;
import upgradeableevents.interfaces.AbstractEventUpgrade;

@SpirePatch(clz = DeadAdventurer.class, method = "randomReward")
public class DeadAdventurerRandomRewardPatch {
    @SpirePostfixPatch
    public static void Postfix(DeadAdventurer __instance) {
        AbstractEventUpgrade currentUpgrade = UpgradeEventManager.getCurrentUpgrade();
        if (currentUpgrade instanceof DeadAdventurerUpgrade && ((DeadAdventurerUpgrade)currentUpgrade).isUpgraded()) {
            int beforeChange = (Integer)ReflectionHacks.getPrivate(__instance, DeadAdventurer.class, "encounterChance");
            // Reduce it by 13 (25 - 12) to effectively make it +12 instead of +25
            int newChance = beforeChange - 13;
            ReflectionHacks.setPrivate(__instance, DeadAdventurer.class, "encounterChance", newChance);

            Enum<?> currentScreen = ReflectionHacks.getPrivate(__instance, DeadAdventurer.class, "screen");

            // Only update the dialog option if we're not in SUCCESS screen
            if (!"SUCCESS".equals(currentScreen.name())) {
                __instance.roomEventText.updateDialogOption(0,
                        DeadAdventurer.OPTIONS[3] + newChance + DeadAdventurer.OPTIONS[4]);
            }
        }
    }
}