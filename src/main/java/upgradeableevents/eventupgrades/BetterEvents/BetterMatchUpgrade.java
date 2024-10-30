package upgradeableevents.eventupgrades.BetterEvents;

import basemod.ReflectionHacks;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.megacrit.cardcrawl.events.AbstractEvent;
import upgradeableevents.UpgradeEventManager;
import upgradeableevents.interfaces.AbstractEventUpgrade;

@SpirePatch(
        optional = true,
        cls = "betterMatch.events.BetterMatchEvent",
        method = SpirePatch.CLASS
)
public class BetterMatchUpgrade extends AbstractEventUpgrade {
    private static final String BETTER_MATCH_CLASS = "betterMatch.BetterMatch";
    private static final String BETTER_MATCH_EVENT_CLASS = "betterMatch.events.BetterMatchEvent";
    public static boolean isBetterMatchLoaded = false;
    public static Class<?> eventClass = null;

    static {
        try {
            Class.forName(BETTER_MATCH_CLASS);
            eventClass = Class.forName(BETTER_MATCH_EVENT_CLASS);
            isBetterMatchLoaded = true;
        } catch (Exception e) {
            isBetterMatchLoaded = false;
        }
    }

    public BetterMatchUpgrade(AbstractEvent event) {
        super(event, (AbstractEvent e) -> {
            if (!isBetterMatchLoaded) return false;
            Enum<?> curScreen = ReflectionHacks.getPrivate(e, eventClass, "screen");
            return "PLAY".equals(curScreen.name());
        });
    }

    @Override
    public void upgrade() {
        if (!canBeUpgraded()) return;
        if (!isBetterMatchLoaded) return;

        int currentAttempts = ReflectionHacks.getPrivate(event, eventClass, "attemptCount");
        ReflectionHacks.setPrivate(event, eventClass, "attemptCount", currentAttempts + 1);
        UpgradeEventManager.playUpgradeVfx();
    }

    @Override
    protected void rebuildOptions() {
        // This event doesn't use standard dialog options during gameplay
    }
}
