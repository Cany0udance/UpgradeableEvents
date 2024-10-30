package upgradeableevents.eventupgrades.TheBeyond;

import basemod.ReflectionHacks;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.events.AbstractEvent;
import com.megacrit.cardcrawl.events.beyond.SecretPortal;
import com.megacrit.cardcrawl.localization.UIStrings;
import upgradeableevents.interfaces.AbstractEventUpgrade;
import upgradeableevents.interfaces.UpgradeCondition;

import static upgradeableevents.UpgradeableEvents.makeID;

public class SecretPortalUpgrade extends AbstractEventUpgrade {
    private static final UIStrings uiStrings = CardCrawlGame.languagePack.getUIString(makeID("SecretPortalUpgrade"));
    private boolean isUpgraded = false;

    public SecretPortalUpgrade(SecretPortal event) {
        super(event, new SecretPortalUpgradeCondition());
    }

    private static class SecretPortalUpgradeCondition implements UpgradeCondition {
        @Override
        public boolean canUpgrade(AbstractEvent event) {
            SecretPortal portalEvent = (SecretPortal)event;
            Enum<?> curScreen = ReflectionHacks.getPrivate(portalEvent, SecretPortal.class, "screen");
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
        SecretPortal portalEvent = (SecretPortal)event;

        // Update first option to include healing and keys
        portalEvent.imageEventText.updateDialogOption(0,
                uiStrings.TEXT[0]); // Should be something like "Heal to full HP, obtain all Keys, and go directly to the boss."

        // Keep second option the same
        portalEvent.imageEventText.updateDialogOption(1, SecretPortal.OPTIONS[1]);
    }
}