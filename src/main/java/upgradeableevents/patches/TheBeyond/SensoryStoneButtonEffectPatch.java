package upgradeableevents.patches.TheBeyond;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.events.beyond.SensoryStone;
import upgradeableevents.util.ButtonEffectHelper;

@SpirePatch(clz = SensoryStone.class, method = "buttonEffect")
public class SensoryStoneButtonEffectPatch {
    @SpirePostfixPatch
    public static void Postfix(SensoryStone __instance, int buttonPressed) {
        ButtonEffectHelper.updateUpgradeAvailability();
    }
}