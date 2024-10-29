package upgradeableevents.eventupgrades.TheCity;

import basemod.ReflectionHacks;
import com.megacrit.cardcrawl.cards.curses.Writhe;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.events.AbstractEvent;
import com.megacrit.cardcrawl.events.city.TheMausoleum;
import com.megacrit.cardcrawl.helpers.CardLibrary;
import com.megacrit.cardcrawl.localization.UIStrings;
import upgradeableevents.interfaces.AbstractEventUpgrade;
import upgradeableevents.interfaces.UpgradeCondition;

import static upgradeableevents.UpgradeableEvents.makeID;

public class TheMausoleumUpgrade extends AbstractEventUpgrade {
    private static final UIStrings uiStrings = CardCrawlGame.languagePack.getUIString(makeID("TheMausoleumUpgrade"));
    private boolean isUpgraded = false;

    public TheMausoleumUpgrade(TheMausoleum event) {
        super(event, new TheMausoleumUpgradeCondition());
    }

    private static class TheMausoleumUpgradeCondition implements UpgradeCondition {
        @Override
        public boolean canUpgrade(AbstractEvent event) {
            TheMausoleum mausoleumEvent = (TheMausoleum)event;
            Enum<?> curScreen = ReflectionHacks.getPrivate(mausoleumEvent, TheMausoleum.class, "screen");
            return "INTRO".equals(curScreen.name());
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
        TheMausoleum mausoleumEvent = (TheMausoleum)event;
        int percent = ReflectionHacks.getPrivate(mausoleumEvent, TheMausoleum.class, "percent");

        // Update the option text to indicate it's a Rare relic
        mausoleumEvent.imageEventText.updateDialogOption(0,
                uiStrings.TEXT[0] + percent + TheMausoleum.OPTIONS[1],
                CardLibrary.getCopy(Writhe.ID));

        // Keep the "Leave" option unchanged
        mausoleumEvent.imageEventText.updateDialogOption(1, TheMausoleum.OPTIONS[2]);
    }
}
