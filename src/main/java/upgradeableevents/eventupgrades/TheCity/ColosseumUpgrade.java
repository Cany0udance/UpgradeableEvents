package upgradeableevents.eventupgrades.TheCity;

import basemod.ReflectionHacks;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.events.AbstractEvent;
import com.megacrit.cardcrawl.events.city.Colosseum;
import com.megacrit.cardcrawl.localization.UIStrings;
import upgradeableevents.interfaces.AbstractEventUpgrade;
import upgradeableevents.interfaces.UpgradeCondition;

import static upgradeableevents.UpgradeableEvents.makeID;

public class ColosseumUpgrade extends AbstractEventUpgrade {
    private static final UIStrings uiStrings = CardCrawlGame.languagePack.getUIString(makeID("ColosseumUpgrade"));
    private boolean isUpgraded = false;

    public ColosseumUpgrade(Colosseum event) {
        super(event, new ColosseumUpgradeCondition());
    }

    private static class ColosseumUpgradeCondition implements UpgradeCondition {
        @Override
        public boolean canUpgrade(AbstractEvent event) {
            Colosseum colosseumEvent = (Colosseum)event;
            Enum<?> curScreen = ReflectionHacks.getPrivate(colosseumEvent, Colosseum.class, "screen");
            return "FIGHT".equals(curScreen.name());
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
        Colosseum colosseumEvent = (Colosseum)event;

        // Add fight option with better rewards text
        colosseumEvent.imageEventText.updateDialogOption(0, Colosseum.OPTIONS[3]);
    }
}