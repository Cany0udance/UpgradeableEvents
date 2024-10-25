package upgradeableevents.eventupgrades.Shrines;

import basemod.ReflectionHacks;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.events.AbstractEvent;
import com.megacrit.cardcrawl.events.shrines.FountainOfCurseRemoval;
import com.megacrit.cardcrawl.localization.UIStrings;
import upgradeableevents.interfaces.AbstractEventUpgrade;
import upgradeableevents.interfaces.UpgradeCondition;

import static upgradeableevents.UpgradeableEvents.makeID;

public class FountainOfCurseRemovalUpgrade extends AbstractEventUpgrade {
    private static final UIStrings uiStrings = CardCrawlGame.languagePack.getUIString(makeID("FountainOfCurseRemovalUpgrade"));
    public static final int HP_PER_CURSE = 6;
    private boolean isUpgraded = false;

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
        isUpgraded = true;
        clearAndRebuildOptions();
    }

    public boolean isUpgraded() {
        return isUpgraded;
    }

    @Override
    protected void rebuildOptions() {
        FountainOfCurseRemoval fountainEvent = (FountainOfCurseRemoval)event;

        // Update first option to show max HP gain
        String upgradeText = FountainOfCurseRemoval.OPTIONS[0] + uiStrings.TEXT[0] + HP_PER_CURSE + uiStrings.TEXT[1];
        fountainEvent.imageEventText.setDialogOption(upgradeText);

        // Leave option
        fountainEvent.imageEventText.setDialogOption(FountainOfCurseRemoval.OPTIONS[1]);
    }
}
