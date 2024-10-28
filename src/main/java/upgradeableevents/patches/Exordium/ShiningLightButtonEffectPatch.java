package upgradeableevents.patches.Exordium;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.events.exordium.ShiningLight;

import static upgradeableevents.util.ButtonEffectHelper.updateUpgradeAvailability;

@SpirePatch(clz = ShiningLight.class, method = "buttonEffect")
public class ShiningLightButtonEffectPatch {
    @SpirePostfixPatch
    public static void Postfix(ShiningLight __instance, int buttonPressed) {
        updateUpgradeAvailability();
    }
}