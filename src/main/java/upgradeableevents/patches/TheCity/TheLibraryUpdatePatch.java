package upgradeableevents.patches.TheCity;

import basemod.ReflectionHacks;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.events.AbstractEvent;
import com.megacrit.cardcrawl.events.city.TheLibrary;
import com.megacrit.cardcrawl.vfx.cardManip.ShowCardAndObtainEffect;
import upgradeableevents.UpgradeEventManager;
import upgradeableevents.eventupgrades.TheCity.TheLibraryUpgrade;
import upgradeableevents.interfaces.AbstractEventUpgrade;

@SpirePatch(clz = TheLibrary.class, method = "update")
public class TheLibraryUpdatePatch {
    @SpirePrefixPatch
    public static void Prefix(TheLibrary __instance) {
        AbstractEventUpgrade currentUpgrade = UpgradeEventManager.getCurrentUpgrade();
        if (currentUpgrade instanceof TheLibraryUpgrade && ((TheLibraryUpgrade)currentUpgrade).isUpgraded()) {
            boolean pickCard = ReflectionHacks.getPrivate(__instance, TheLibrary.class, "pickCard");

            if (pickCard && !AbstractDungeon.isScreenUp && !AbstractDungeon.gridSelectScreen.selectedCards.isEmpty()) {
                AbstractCard c = AbstractDungeon.gridSelectScreen.selectedCards.get(0).makeCopy();
                c.upgrade(); // Make sure to upgrade the copy that gets added to the deck
                AbstractEvent.logMetricObtainCard("The Library", "Read", c);
                AbstractDungeon.effectList.add(new ShowCardAndObtainEffect(c, Settings.WIDTH / 2.0F, Settings.HEIGHT / 2.0F));
                AbstractDungeon.gridSelectScreen.selectedCards.clear();
                ReflectionHacks.setPrivate(__instance, TheLibrary.class, "pickCard", false);
            }
        }
    }
}