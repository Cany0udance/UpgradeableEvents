package upgradeableevents.patches.Exordium;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.events.exordium.DeadAdventurer;

import static upgradeableevents.util.ButtonEffectHelper.updateUpgradeAvailability;

@SpirePatch(clz = DeadAdventurer.class, method = "buttonEffect")
public class DeadAdventurerButtonEffectPatch {
    @SpirePostfixPatch
    public static void Postfix(DeadAdventurer __instance, int buttonPressed) {
        updateUpgradeAvailability();
    }
}