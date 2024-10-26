package upgradeableevents.eventupgrades.Shrines;

import basemod.ReflectionHacks;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.events.AbstractEvent;
import com.megacrit.cardcrawl.events.shrines.PurificationShrine;
import com.megacrit.cardcrawl.localization.UIStrings;
import upgradeableevents.interfaces.AbstractEventUpgrade;
import upgradeableevents.interfaces.UpgradeCondition;

import static upgradeableevents.UpgradeableEvents.makeID;

public class PurificationShrineUpgrade extends AbstractEventUpgrade {
    private static final UIStrings uiStrings = CardCrawlGame.languagePack.getUIString(makeID("PurificationShrineUpgrade"));
    private static final int CHOOSE_SCREEN_NUM = 0;  // INTRO screen
    private static final int CARDS_TO_REMOVE = 2;
    private boolean isUpgraded = false;

    public PurificationShrineUpgrade(PurificationShrine event) {
        super(event, new PurificationShrineUpgradeCondition());
    }

    private static class PurificationShrineUpgradeCondition implements UpgradeCondition {
        @Override
        public boolean canUpgrade(AbstractEvent event) {
            PurificationShrine purifierEvent = (PurificationShrine)event;
            Object screenEnum = ReflectionHacks.getPrivate(purifierEvent, PurificationShrine.class, "screen");
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
        PurificationShrine purifierEvent = (PurificationShrine)event;

        // Update the first option to indicate that two cards can be removed
        purifierEvent.imageEventText.updateDialogOption(0, uiStrings.TEXT[0]);

        purifierEvent.imageEventText.setDialogOption(PurificationShrine.OPTIONS[1]);
    }
}