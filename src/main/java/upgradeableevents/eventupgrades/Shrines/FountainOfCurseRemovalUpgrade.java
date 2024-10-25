package upgradeableevents.eventupgrades.Shrines;

import basemod.ReflectionHacks;
import com.megacrit.cardcrawl.events.AbstractEvent;
import com.megacrit.cardcrawl.events.shrines.FountainOfCurseRemoval;
import upgradeableevents.interfaces.AbstractEventUpgrade;
import upgradeableevents.interfaces.UpgradeCondition;

public class FountainOfCurseRemovalUpgrade extends AbstractEventUpgrade {
    public static final int HP_PER_CURSE = 6;

    public FountainOfCurseRemovalUpgrade(FountainOfCurseRemoval event) {
        super(event, new FountainUpgradeCondition());
    }

    private static class FountainUpgradeCondition implements UpgradeCondition {
        @Override
        public boolean canUpgrade(AbstractEvent event) {
            FountainOfCurseRemoval fountainEvent = (FountainOfCurseRemoval)event;
            Integer screenNum = ReflectionHacks.getPrivate(fountainEvent, FountainOfCurseRemoval.class, "screenNum");
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
        FountainOfCurseRemoval fountainEvent = (FountainOfCurseRemoval)event;

        // Update first option to show max HP gain
        String upgradeText = FountainOfCurseRemoval.OPTIONS[0] + " Gain #b" + HP_PER_CURSE + " #gMax #gHP for each.";
        fountainEvent.imageEventText.setDialogOption(upgradeText);

        // Leave option
        fountainEvent.imageEventText.setDialogOption(FountainOfCurseRemoval.OPTIONS[1]);
    }
}
