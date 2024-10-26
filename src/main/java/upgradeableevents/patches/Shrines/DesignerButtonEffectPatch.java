package upgradeableevents.patches.Shrines;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.events.shrines.Designer;

import static upgradeableevents.util.ButtonEffectHelper.updateUpgradeAvailability;

@SpirePatch(clz = Designer.class, method = "buttonEffect")
public class DesignerButtonEffectPatch {
    @SpirePostfixPatch
    public static void Postfix(Designer __instance, int buttonPressed) {
        updateUpgradeAvailability();
    }
}