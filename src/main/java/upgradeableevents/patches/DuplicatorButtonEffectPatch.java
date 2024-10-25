package upgradeableevents.patches;

import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.events.AbstractEvent;
import com.megacrit.cardcrawl.events.shrines.Duplicator;
import com.megacrit.cardcrawl.vfx.cardManip.ShowCardAndObtainEffect;
import javassist.CtBehavior;
import upgradeableevents.UpgradeEventManager;
import upgradeableevents.eventupgrades.Shrines.DuplicatorUpgrade;
import upgradeableevents.interfaces.AbstractEventUpgrade;
import upgradeableevents.util.ButtonEffectHelper;

@SpirePatch(clz = Duplicator.class, method = "buttonEffect")
public class DuplicatorButtonEffectPatch {
    @SpirePostfixPatch
    public static void Postfix(Duplicator __instance, int buttonPressed) {
        DuplicatorUpdatePatch.addedSecondCopy = false;
        ButtonEffectHelper.updateUpgradeAvailability();
    }
}
