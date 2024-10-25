package upgradeableevents.eventupgrades.Shrines;

import basemod.ReflectionHacks;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.events.AbstractEvent;
import com.megacrit.cardcrawl.events.shrines.Lab;
import com.megacrit.cardcrawl.localization.UIStrings;
import upgradeableevents.interfaces.AbstractEventUpgrade;
import upgradeableevents.interfaces.UpgradeCondition;

import static upgradeableevents.UpgradeableEvents.makeID;

public class LabUpgrade extends AbstractEventUpgrade {
    private static final UIStrings uiStrings = CardCrawlGame.languagePack.getUIString(makeID("LabUpgrade"));
    private boolean isUpgraded = false;

    public LabUpgrade(Lab event) {
        super(event, new LabUpgradeCondition());
    }

    private static class LabUpgradeCondition implements UpgradeCondition {
        @Override
        public boolean canUpgrade(AbstractEvent event) {
            Lab labEvent = (Lab)event;
            Enum<?> screenNum = ReflectionHacks.getPrivate(labEvent, Lab.class, "screen");
            return screenNum.name().equals("INTRO");
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
        Lab labEvent = (Lab)event;

        // Update option to show potion slot gain
        String upgradeText = Lab.OPTIONS[0] + uiStrings.TEXT[0];
        labEvent.imageEventText.setDialogOption(upgradeText);
    }
}