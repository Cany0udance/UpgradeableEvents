package upgradeableevents.eventupgrades.Shrines;

import basemod.ReflectionHacks;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.events.AbstractEvent;
import com.megacrit.cardcrawl.events.shrines.WeMeetAgain;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.localization.UIStrings;
import com.megacrit.cardcrawl.potions.AbstractPotion;
import upgradeableevents.interfaces.AbstractEventUpgrade;
import upgradeableevents.interfaces.UpgradeCondition;

import static upgradeableevents.UpgradeableEvents.makeID;

public class WeMeetAgainUpgrade extends AbstractEventUpgrade {
    private static final UIStrings uiStrings = CardCrawlGame.languagePack.getUIString(makeID("WeMeetAgainUpgrade"));
    private static final int CHOOSE_SCREEN_NUM = 0;  // INTRO screen
    private boolean isUpgraded = false;

    public WeMeetAgainUpgrade(WeMeetAgain event) {
        super(event, new WeMeetAgainUpgradeCondition());
    }

    private static class WeMeetAgainUpgradeCondition implements UpgradeCondition {
        @Override
        public boolean canUpgrade(AbstractEvent event) {
            WeMeetAgain wmaEvent = (WeMeetAgain)event;
            Object screenEnum = ReflectionHacks.getPrivate(wmaEvent, WeMeetAgain.class, "screen");
            int screenNum = ((Enum<?>) screenEnum).ordinal();
            return screenNum == CHOOSE_SCREEN_NUM;
        }
    }

    @Override
    public void upgrade() {
        if (!canBeUpgraded()) return;
        isUpgraded = true;
        clearAndRebuildOptions();
    }

    public boolean isUpgraded() {
        return isUpgraded;
    }

    @Override
    protected void rebuildOptions() {
        WeMeetAgain wmaEvent = (WeMeetAgain)event;

        // Get the private fields we need
        AbstractPotion potionOption = ReflectionHacks.getPrivate(wmaEvent, WeMeetAgain.class, "potionOption");
        int goldAmount = ReflectionHacks.getPrivate(wmaEvent, WeMeetAgain.class, "goldAmount");
        AbstractCard cardOption = ReflectionHacks.getPrivate(wmaEvent, WeMeetAgain.class, "cardOption");

        // Update potion option
        if (potionOption != null) {
            wmaEvent.imageEventText.updateDialogOption(0,
                    uiStrings.TEXT[0] + FontHelper.colorString(potionOption.name, "g") + WeMeetAgain.OPTIONS[6]);
        } else {
            wmaEvent.imageEventText.updateDialogOption(0, WeMeetAgain.OPTIONS[1], true);
        }

        // Update gold option
        if (goldAmount != 0) {
            wmaEvent.imageEventText.updateDialogOption(1,
                    uiStrings.TEXT[1] + goldAmount + uiStrings.TEXT[3] + WeMeetAgain.OPTIONS[6]);
        } else {
            wmaEvent.imageEventText.updateDialogOption(1, WeMeetAgain.OPTIONS[3], true);
        }

        // Update card option
        if (cardOption != null) {
            wmaEvent.imageEventText.updateDialogOption(2,
                    uiStrings.TEXT[2] + cardOption.name + WeMeetAgain.OPTIONS[6], cardOption.makeStatEquivalentCopy());
        } else {
            wmaEvent.imageEventText.updateDialogOption(2, WeMeetAgain.OPTIONS[5], true);
        }

        wmaEvent.imageEventText.setDialogOption(WeMeetAgain.OPTIONS[7]);
    }
}