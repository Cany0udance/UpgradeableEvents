package upgradeableevents.eventupgrades.TheCity;

import basemod.ReflectionHacks;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.events.AbstractEvent;
import com.megacrit.cardcrawl.events.city.TheJoust;
import com.megacrit.cardcrawl.localization.UIStrings;
import upgradeableevents.interfaces.AbstractEventUpgrade;
import upgradeableevents.interfaces.UpgradeCondition;

import static upgradeableevents.UpgradeableEvents.makeID;

public class TheJoustUpgrade extends AbstractEventUpgrade {
    private static final UIStrings uiStrings = CardCrawlGame.languagePack.getUIString(makeID("TheJoustUpgrade"));
    private boolean isUpgraded = false;

    public TheJoustUpgrade(TheJoust event) {
        super(event, new TheJoustUpgradeCondition());
    }

    private static class TheJoustUpgradeCondition implements UpgradeCondition {
        @Override
        public boolean canUpgrade(AbstractEvent event) {
            TheJoust joustEvent = (TheJoust)event;
            Enum<?> curScreen = ReflectionHacks.getPrivate(joustEvent, TheJoust.class, "screen");
            return "EXPLANATION".equals(curScreen.name());
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
        TheJoust joustEvent = (TheJoust)event;

        // Update betting options with new gold values
        joustEvent.imageEventText.updateDialogOption(0,
                TheJoust.OPTIONS[1] + "50" + uiStrings.TEXT[0] + "200" + TheJoust.OPTIONS[3]);
        joustEvent.imageEventText.updateDialogOption(1,
                TheJoust.OPTIONS[4] + "50" + uiStrings.TEXT[1] + "1500" + TheJoust.OPTIONS[3]);
    }
}
