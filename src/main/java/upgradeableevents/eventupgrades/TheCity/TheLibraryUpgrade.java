package upgradeableevents.eventupgrades.TheCity;

import basemod.ReflectionHacks;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.events.AbstractEvent;
import com.megacrit.cardcrawl.events.city.TheLibrary;
import com.megacrit.cardcrawl.localization.UIStrings;
import upgradeableevents.interfaces.AbstractEventUpgrade;
import upgradeableevents.interfaces.UpgradeCondition;

import static upgradeableevents.UpgradeableEvents.makeID;

public class TheLibraryUpgrade extends AbstractEventUpgrade {
    private static final UIStrings uiStrings = CardCrawlGame.languagePack.getUIString(makeID("TheLibraryUpgrade"));
    private boolean isUpgraded = false;

    public TheLibraryUpgrade(TheLibrary event) {
        super(event, new TheLibraryUpgradeCondition());
    }

    private static class TheLibraryUpgradeCondition implements UpgradeCondition {
        @Override
        public boolean canUpgrade(AbstractEvent event) {
            TheLibrary libraryEvent = (TheLibrary)event;
            // Get screenNum from AbstractImageEvent class instead of Cleric
            Integer screenNum = ReflectionHacks.getPrivate(libraryEvent, TheLibrary.class, "screenNum");
            return screenNum == 0;
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
        TheLibrary libraryEvent = (TheLibrary)event;

        // Update first option to indicate cards will be upgraded
        libraryEvent.imageEventText.updateDialogOption(0, uiStrings.TEXT[0]);

        // Update second option to show heal amount + max HP gain
        int healAmt = ReflectionHacks.getPrivate(libraryEvent, TheLibrary.class, "healAmt");
        libraryEvent.imageEventText.updateDialogOption(1,
                TheLibrary.OPTIONS[1] + healAmt + TheLibrary.OPTIONS[2] + uiStrings.TEXT[1]);
    }
}