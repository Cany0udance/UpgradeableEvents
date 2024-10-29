package upgradeableevents.eventupgrades.TheCity;

import basemod.ReflectionHacks;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.events.AbstractEvent;
import com.megacrit.cardcrawl.events.city.BackToBasics;
import com.megacrit.cardcrawl.localization.UIStrings;
import upgradeableevents.interfaces.AbstractEventUpgrade;
import upgradeableevents.interfaces.UpgradeCondition;

import static upgradeableevents.UpgradeableEvents.makeID;

public class BackToBasicsUpgrade extends AbstractEventUpgrade {
    private static final UIStrings uiStrings = CardCrawlGame.languagePack.getUIString(makeID("BackToBasicsUpgrade"));
    private boolean isUpgraded = false;

    public BackToBasicsUpgrade(BackToBasics event) {
        super(event, new BackToBasicsUpgradeCondition());
    }

    private static class BackToBasicsUpgradeCondition implements UpgradeCondition {
        @Override
        public boolean canUpgrade(AbstractEvent event) {
            BackToBasics backToBasicsEvent = (BackToBasics)event;
            Enum<?> curScreen = ReflectionHacks.getPrivate(backToBasicsEvent, BackToBasics.class, "screen");
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
        BackToBasics backToBasicsEvent = (BackToBasics)event;

        // Update the "Remove a card" option to mention the gold reward
        backToBasicsEvent.imageEventText.updateDialogOption(0,
                BackToBasics.OPTIONS[0] + uiStrings.TEXT[0]);

        // Update the "Upgrade all Strikes and Defends" option to mention all Basic and Rare cards
        backToBasicsEvent.imageEventText.updateDialogOption(1,
                uiStrings.TEXT[1]);
    }
}