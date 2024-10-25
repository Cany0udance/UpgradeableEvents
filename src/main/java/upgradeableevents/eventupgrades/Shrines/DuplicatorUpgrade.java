package upgradeableevents.eventupgrades.Shrines;

import basemod.ReflectionHacks;
import com.megacrit.cardcrawl.events.AbstractEvent;
import com.megacrit.cardcrawl.events.shrines.Duplicator;
import upgradeableevents.interfaces.AbstractEventUpgrade;
import upgradeableevents.interfaces.UpgradeCondition;

public class DuplicatorUpgrade extends AbstractEventUpgrade {
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

        clearAndRebuildOptions();
    }

    @Override
    protected void rebuildOptions() {
        Duplicator duplicatorEvent = (Duplicator)event;

        // Update first option to show double duplication
        String upgradeText = "[Pray] #gDuplicate #ga #gcard #gin #gyour #gdeck #gtwice.";
        duplicatorEvent.imageEventText.setDialogOption(upgradeText);

        // Leave option remains the same
        duplicatorEvent.imageEventText.setDialogOption(Duplicator.OPTIONS[1]);
    }
}
