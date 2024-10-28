

package upgradeableevents.patches.Exordium;


import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.events.exordium.Cleric;

import static upgradeableevents.util.ButtonEffectHelper.updateUpgradeAvailability;

@SpirePatch(clz = Cleric.class, method = "buttonEffect")
public class ClericButtonEffectPatch {
    @SpirePostfixPatch
    public static void Postfix(Cleric __instance, int buttonPressed) {
        updateUpgradeAvailability();
    }
}