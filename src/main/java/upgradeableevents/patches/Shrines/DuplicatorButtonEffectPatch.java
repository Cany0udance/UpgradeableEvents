package upgradeableevents.patches.Shrines;

import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.events.shrines.Duplicator;
import upgradeableevents.util.ButtonEffectHelper;

@SpirePatch(clz = Duplicator.class, method = "buttonEffect")
public class DuplicatorButtonEffectPatch {
    @SpirePostfixPatch
    public static void Postfix(Duplicator __instance, int buttonPressed) {
        DuplicatorUpdatePatch.addedSecondCopy = false;
        ButtonEffectHelper.updateUpgradeAvailability();
    }
}
