package upgradeableevents.patches.Shrines;

import com.evacipated.cardcrawl.modthespire.lib.SpireInsertPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.events.shrines.Duplicator;
import com.megacrit.cardcrawl.vfx.cardManip.ShowCardAndObtainEffect;
import upgradeableevents.UpgradeEventManager;
import upgradeableevents.eventupgrades.Shrines.AccursedBlacksmithUpgrade;
import upgradeableevents.eventupgrades.Shrines.DuplicatorUpgrade;
import upgradeableevents.interfaces.AbstractEventUpgrade;

@SpirePatch(clz = Duplicator.class, method = "update")
public class DuplicatorUpdatePatch {
    public static boolean addedSecondCopy = false;

    @SpireInsertPatch(
            rlocs = {7} // After the first ShowCardAndObtainEffect is added
    )
    public static void Insert(Duplicator __instance) {
        if (!addedSecondCopy && !AbstractDungeon.gridSelectScreen.selectedCards.isEmpty()) {
            AbstractEventUpgrade currentUpgrade = UpgradeEventManager.getCurrentUpgrade();
            if (currentUpgrade instanceof DuplicatorUpgrade && ((DuplicatorUpgrade)currentUpgrade).isUpgraded()) {
                // Create a second copy of the selected card
                AbstractCard original = AbstractDungeon.gridSelectScreen.selectedCards.get(0);
                AbstractCard secondCopy = original.makeStatEquivalentCopy();
                secondCopy.inBottleFlame = false;
                secondCopy.inBottleLightning = false;
                secondCopy.inBottleTornado = false;

                // Add the second copy with a slight offset
                AbstractDungeon.effectList.add(new ShowCardAndObtainEffect(
                        secondCopy,
                        Settings.WIDTH / 2.0F + 30.0F,
                        Settings.HEIGHT / 2.0F + 30.0F
                ));

                addedSecondCopy = true;
            }
        }
    }
}
