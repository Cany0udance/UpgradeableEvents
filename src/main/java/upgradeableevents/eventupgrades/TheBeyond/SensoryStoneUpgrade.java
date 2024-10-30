package upgradeableevents.eventupgrades.TheBeyond;

import basemod.ReflectionHacks;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.events.AbstractEvent;
import com.megacrit.cardcrawl.events.beyond.SensoryStone;
import com.megacrit.cardcrawl.localization.UIStrings;
import upgradeableevents.interfaces.AbstractEventUpgrade;
import upgradeableevents.interfaces.UpgradeCondition;

import static com.megacrit.cardcrawl.events.beyond.SensoryStone.OPTIONS;
import static upgradeableevents.UpgradeableEvents.makeID;

public class SensoryStoneUpgrade extends AbstractEventUpgrade {
    private static final UIStrings uiStrings = CardCrawlGame.languagePack.getUIString(makeID("SensoryStoneUpgrade"));
    private boolean isUpgraded = false;

    public SensoryStoneUpgrade(SensoryStone event) {
        super(event, new SensoryStoneUpgradeCondition());
    }

    private static class SensoryStoneUpgradeCondition implements UpgradeCondition {
        @Override
        public boolean canUpgrade(AbstractEvent event) {
            SensoryStone stoneEvent = (SensoryStone)event;
            Enum<?> curScreen = ReflectionHacks.getPrivate(stoneEvent, SensoryStone.class, "screen");
            return "INTRO_2".equals(curScreen.name());
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
        SensoryStone stoneEvent = (SensoryStone)event;

        // Update option text to indicate cards will be upgraded
        stoneEvent.imageEventText.updateDialogOption(0, uiStrings.TEXT[0]); // "Obtain 1 Colorless card. (All cards will be upgraded)"
        stoneEvent.imageEventText.updateDialogOption(1, uiStrings.TEXT[1] + 5 + OPTIONS[3]);
        stoneEvent.imageEventText.updateDialogOption(2, uiStrings.TEXT[2] + 10 + OPTIONS[3]);
    }
}
