package upgradeableevents.eventupgrades.Exordium;

import basemod.ReflectionHacks;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.events.AbstractEvent;
import com.megacrit.cardcrawl.events.exordium.GoldenIdolEvent;
import com.megacrit.cardcrawl.localization.UIStrings;
import com.megacrit.cardcrawl.relics.GoldenIdol;
import upgradeableevents.interfaces.AbstractEventUpgrade;
import upgradeableevents.interfaces.UpgradeCondition;

import static upgradeableevents.UpgradeableEvents.makeID;

public class GoldenIdolUpgrade extends AbstractEventUpgrade {
    private static final UIStrings uiStrings = CardCrawlGame.languagePack.getUIString(makeID("GoldenIdolUpgrade"));
    private boolean isUpgraded = false;

    public GoldenIdolUpgrade(GoldenIdolEvent event) {
        super(event, new GoldenIdolUpgradeCondition());
    }

    private static class GoldenIdolUpgradeCondition implements UpgradeCondition {
        @Override
        public boolean canUpgrade(AbstractEvent event) {
            GoldenIdolEvent goldenIdolEvent = (GoldenIdolEvent)event;
            int screenNum = ReflectionHacks.getPrivate(goldenIdolEvent, GoldenIdolEvent.class, "screenNum");
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
        GoldenIdolEvent goldenIdolEvent = (GoldenIdolEvent)event;

        // Clear options directly from the optionList
        goldenIdolEvent.imageEventText.optionList.clear();

        // Add the modified "Take Golden Idol" option
        goldenIdolEvent.imageEventText.setDialogOption(uiStrings.TEXT[0], new GoldenIdol());

        // Add the "Leave" option
        goldenIdolEvent.imageEventText.setDialogOption(GoldenIdolEvent.OPTIONS[1]);
    }
}