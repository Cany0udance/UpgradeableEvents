package upgradeableevents.eventupgrades.TheBeyond;

import basemod.ReflectionHacks;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.events.AbstractEvent;
import com.megacrit.cardcrawl.events.beyond.MysteriousSphere;
import com.megacrit.cardcrawl.localization.UIStrings;
import upgradeableevents.interfaces.AbstractEventUpgrade;
import upgradeableevents.interfaces.UpgradeCondition;

import static upgradeableevents.UpgradeableEvents.makeID;

public class MysteriousSphereUpgrade extends AbstractEventUpgrade {
    private static final UIStrings uiStrings = CardCrawlGame.languagePack.getUIString(makeID("MysteriousSphereUpgrade"));
    private boolean isUpgraded = false;

    public MysteriousSphereUpgrade(MysteriousSphere event) {
        super(event, new MysteriousSphereUpgradeCondition());
    }

    private static class MysteriousSphereUpgradeCondition implements UpgradeCondition {
        @Override
        public boolean canUpgrade(AbstractEvent event) {
            MysteriousSphere sphereEvent = (MysteriousSphere)event;
            Enum<?> curScreen = ReflectionHacks.getPrivate(sphereEvent, MysteriousSphere.class, "screen");
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
        MysteriousSphere sphereEvent = (MysteriousSphere)event;

        // Update option text to indicate doubled gold
        sphereEvent.roomEventText.updateDialogOption(0, MysteriousSphere.OPTIONS[0]);
        sphereEvent.roomEventText.updateDialogOption(1, MysteriousSphere.OPTIONS[1]);
    }
}