package upgradeableevents.eventupgrades.Shrines;

import basemod.ReflectionHacks;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.events.AbstractEvent;
import com.megacrit.cardcrawl.events.shrines.Transmogrifier;
import com.megacrit.cardcrawl.localization.UIStrings;
import upgradeableevents.interfaces.AbstractEventUpgrade;
import upgradeableevents.interfaces.UpgradeCondition;

import static upgradeableevents.UpgradeableEvents.makeID;

public class TransmogrifierUpgrade extends AbstractEventUpgrade {
    private static final UIStrings uiStrings = CardCrawlGame.languagePack.getUIString(makeID("TransmogrifierUpgrade"));
    private static final int CHOOSE_SCREEN_NUM = 0;  // INTRO screen
    private boolean isUpgraded = false;

    public TransmogrifierUpgrade(Transmogrifier event) {
        super(event, new TransmogrifierUpgradeCondition());
    }

    private static class TransmogrifierUpgradeCondition implements UpgradeCondition {
        @Override
        public boolean canUpgrade(AbstractEvent event) {
            Transmogrifier transEvent = (Transmogrifier)event;
            Object screenEnum = ReflectionHacks.getPrivate(transEvent, Transmogrifier.class, "screen");
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
        Transmogrifier transEvent = (Transmogrifier)event;

        // Update the first option to indicate that the transformed card will be rare
        transEvent.imageEventText.updateDialogOption(0, uiStrings.TEXT[0]);

        transEvent.imageEventText.setDialogOption(Transmogrifier.OPTIONS[1]);
    }
}
