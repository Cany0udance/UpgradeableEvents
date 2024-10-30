package upgradeableevents.eventupgrades.BetterEvents;

import basemod.ReflectionHacks;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.events.AbstractEvent;
import com.megacrit.cardcrawl.localization.UIStrings;
import upgradeableevents.interfaces.AbstractEventUpgrade;

import static upgradeableevents.UpgradeableEvents.makeID;

@SpirePatch(
        optional = true,
        cls = "betterThird.events.BetterPortalEvent",
        method = SpirePatch.CLASS
)
public class BetterPortalUpgrade extends AbstractEventUpgrade {
    private static final String BETTER_THIRD_CLASS = "betterThird.BetterThird";
    private static final String BETTER_PORTAL_EVENT_CLASS = "betterThird.events.BetterPortalEvent";
    public static boolean isBetterThirdLoaded = false;
    public static Class<?> eventClass = null;

    static {
        try {
            Class.forName(BETTER_THIRD_CLASS);
            eventClass = Class.forName(BETTER_PORTAL_EVENT_CLASS);
            isBetterThirdLoaded = true;
        } catch (Exception e) {
            isBetterThirdLoaded = false;
        }
    }

    private static final UIStrings uiStrings = CardCrawlGame.languagePack.getUIString(makeID("BetterPortalUpgrade"));
    private boolean isUpgraded = false;

    public BetterPortalUpgrade(AbstractEvent event) {
        super(event, (AbstractEvent e) -> {
            if (!isBetterThirdLoaded) return false;
            Enum<?> curScreen = ReflectionHacks.getPrivate(e, eventClass, "screen");
            return "INTRO".equals(curScreen.name());
        });
    }

    public void upgrade() {
        if (!canBeUpgraded()) return;
        isUpgraded = true;
        clearAndRebuildOptions();
    }

    public boolean isUpgraded() {
        return isUpgraded;
    }

    protected void rebuildOptions() {
        if (!isBetterThirdLoaded) return;

        AbstractEvent portalEvent = event;
        String[] OPTIONS = ReflectionHacks.getPrivateStatic(eventClass, "OPTIONS");
        boolean hasPrism = ReflectionHacks.getPrivate(portalEvent, eventClass, "prism");

        // Update first option to include healing and keys
        portalEvent.imageEventText.updateDialogOption(0, uiStrings.TEXT[0]);

        // Second option - allow choice regardless of Prismatic Shard
        if (!hasPrism) {
            portalEvent.imageEventText.updateDialogOption(1, uiStrings.TEXT[1]);
        } else {
            portalEvent.imageEventText.updateDialogOption(1, OPTIONS[4]);
        }

        // Keep third option (leave) the same
        portalEvent.imageEventText.updateDialogOption(2, OPTIONS[2]);
    }
}