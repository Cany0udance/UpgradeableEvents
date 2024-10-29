package upgradeableevents.eventupgrades.TheCity;

import basemod.ReflectionHacks;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.colorless.RitualDagger;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.events.AbstractEvent;
import com.megacrit.cardcrawl.events.city.Nest;
import com.megacrit.cardcrawl.localization.UIStrings;
import upgradeableevents.interfaces.AbstractEventUpgrade;
import upgradeableevents.interfaces.UpgradeCondition;

import static upgradeableevents.UpgradeableEvents.makeID;

public class NestUpgrade extends AbstractEventUpgrade {
    private static final UIStrings uiStrings = CardCrawlGame.languagePack.getUIString(makeID("NestUpgrade"));
    private boolean isUpgraded = false;

    public NestUpgrade(Nest event) {
        super(event, new NestUpgradeCondition());
    }

    private static class NestUpgradeCondition implements UpgradeCondition {
        @Override
        public boolean canUpgrade(AbstractEvent event) {
            Nest nestEvent = (Nest)event;
            int screenNum = ReflectionHacks.getPrivate(nestEvent, Nest.class, "screenNum");
            return screenNum == 1;  // Can only upgrade when choices are shown
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
        Nest nestEvent = (Nest)event;
        int goldGain = ReflectionHacks.getPrivate(nestEvent, Nest.class, "goldGain");

        // Create an upgraded Ritual Dagger for preview
        AbstractCard ritualDagger = new RitualDagger();
        ritualDagger.upgrade();

        // Update options with doubled gold and reduced HP cost
        nestEvent.imageEventText.updateDialogOption(0,
                Nest.OPTIONS[2] + (goldGain * 2) + Nest.OPTIONS[3]);
        nestEvent.imageEventText.updateDialogOption(1,
                uiStrings.TEXT[0] + 1 + Nest.OPTIONS[1],
                ritualDagger);
    }
}