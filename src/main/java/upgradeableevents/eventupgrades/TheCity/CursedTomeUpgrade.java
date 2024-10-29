package upgradeableevents.eventupgrades.TheCity;

import basemod.ReflectionHacks;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.events.AbstractEvent;
import com.megacrit.cardcrawl.events.city.CursedTome;
import com.megacrit.cardcrawl.localization.UIStrings;
import upgradeableevents.interfaces.AbstractEventUpgrade;
import upgradeableevents.interfaces.UpgradeCondition;

import static upgradeableevents.UpgradeableEvents.makeID;

public class CursedTomeUpgrade extends AbstractEventUpgrade {
    private static final UIStrings uiStrings = CardCrawlGame.languagePack.getUIString(makeID("CursedTomeUpgrade"));
    private boolean isUpgraded = false;

    public CursedTomeUpgrade(CursedTome event) {
        super(event, new CursedTomeUpgradeCondition());
    }

    private static class CursedTomeUpgradeCondition implements UpgradeCondition {
        @Override
        public boolean canUpgrade(AbstractEvent event) {
            CursedTome cursedTomeEvent = (CursedTome)event;
            Enum<?> screen = ReflectionHacks.getPrivate(cursedTomeEvent, CursedTome.class, "screen");
            return "INTRO".equals(screen.name());
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
        CursedTome cursedTomeEvent = (CursedTome)event;

        // Get the finalDmg value
        int finalDmg = ReflectionHacks.getPrivate(cursedTomeEvent, CursedTome.class, "finalDmg");

        // Set the options that would normally appear on the last page
        cursedTomeEvent.imageEventText.updateDialogOption(0, CursedTome.OPTIONS[5] + finalDmg + CursedTome.OPTIONS[6]);
        cursedTomeEvent.imageEventText.updateDialogOption(1, CursedTome.OPTIONS[7]); // Leave option
    }
}