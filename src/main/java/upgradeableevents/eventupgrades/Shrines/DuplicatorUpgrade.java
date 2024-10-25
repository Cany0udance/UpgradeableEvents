package upgradeableevents.eventupgrades.Shrines;

import basemod.ReflectionHacks;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.events.AbstractEvent;
import com.megacrit.cardcrawl.events.shrines.Duplicator;
import com.megacrit.cardcrawl.localization.UIStrings;
import upgradeableevents.interfaces.AbstractEventUpgrade;
import upgradeableevents.interfaces.UpgradeCondition;

import static upgradeableevents.UpgradeableEvents.makeID;

public class DuplicatorUpgrade extends AbstractEventUpgrade {
    private static final UIStrings uiStrings = CardCrawlGame.languagePack.getUIString(makeID("DuplicatorUpgrade"));
    private boolean isUpgraded = false;
    public DuplicatorUpgrade(Duplicator event) {
        super(event, new DuplicatorUpgradeCondition());
    }

    private static class DuplicatorUpgradeCondition implements UpgradeCondition {
        @Override
        public boolean canUpgrade(AbstractEvent event) {
            Duplicator duplicatorEvent = (Duplicator)event;
            Integer screenNum = ReflectionHacks.getPrivate(duplicatorEvent, Duplicator.class, "screenNum");
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
        Duplicator duplicatorEvent = (Duplicator)event;

        // Update first option to show double duplication
        String upgradeText = uiStrings.TEXT[0];
        duplicatorEvent.imageEventText.setDialogOption(upgradeText);

        // Leave option remains the same
        duplicatorEvent.imageEventText.setDialogOption(Duplicator.OPTIONS[1]);
    }
}
