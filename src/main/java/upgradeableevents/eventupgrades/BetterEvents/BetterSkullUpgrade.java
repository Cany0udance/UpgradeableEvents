package upgradeableevents.eventupgrades.BetterEvents;

import basemod.ReflectionHacks;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.events.AbstractEvent;
import com.megacrit.cardcrawl.localization.UIStrings;
import upgradeableevents.interfaces.AbstractEventUpgrade;

import java.util.HashSet;
import java.util.Set;

import static upgradeableevents.UpgradeableEvents.makeID;

@SpirePatch(
        optional = true,
        cls = "betterSkull.events.BetterSkullEvent",
        method = SpirePatch.CLASS
)
public class BetterSkullUpgrade extends AbstractEventUpgrade {
    private static final String BETTER_SKULL_CLASS = "betterSkull.BetterSkull";
    private static final String BETTER_SKULL_EVENT_CLASS = "betterSkull.events.BetterSkullEvent";
    public static boolean isBetterSkullLoaded = false;
    public static Class<?> eventClass = null;

    static {
        try {
            Class.forName(BETTER_SKULL_CLASS);
            eventClass = Class.forName(BETTER_SKULL_EVENT_CLASS);
            isBetterSkullLoaded = true;
        } catch (Exception e) {
            isBetterSkullLoaded = false;
        }
    }

    private static final UIStrings uiStrings = CardCrawlGame.languagePack.getUIString(makeID("BetterSkullUpgrade"));
    private boolean isUpgraded = false;
    private Set<Integer> freeOptionsUsed;

    public BetterSkullUpgrade(AbstractEvent event) {
        super(event, (AbstractEvent e) -> {
            if (!isBetterSkullLoaded) return false;
            Enum<?> curScreen = ReflectionHacks.getPrivate(e, eventClass, "screen");
            return "ASK".equals(curScreen.name());
        });
        this.freeOptionsUsed = new HashSet<>();
    }

    public void upgrade() {
        if (!canBeUpgraded()) return;
        isUpgraded = true;
        clearAndRebuildOptions();
    }

    public boolean isUpgraded() {
        return isUpgraded;
    }

    public boolean hasUsedFreeOption(int optionIndex) {
        return freeOptionsUsed.contains(optionIndex);
    }

    public void markOptionUsed(int optionIndex) {
        freeOptionsUsed.add(optionIndex);
    }

    @Override
    protected void rebuildOptions() {
        if (!isBetterSkullLoaded) return;

        String[] OPTIONS = ReflectionHacks.getPrivateStatic(eventClass, "OPTIONS");
        int goldCost = ReflectionHacks.getPrivate(event, eventClass, "goldCost");
        int upgradeCost = ReflectionHacks.getPrivate(event, eventClass, "upgradeCost");
        int relicCost = ReflectionHacks.getPrivate(event, eventClass, "relicCost");
        int leaveCost = ReflectionHacks.getPrivate(event, eventClass, "leaveCost");
        int goldReward = ReflectionHacks.getPrivate(event, eventClass, "goldReward");

        event.imageEventText.clearAllDialogs();

        // Gold option
        event.imageEventText.setDialogOption(
                OPTIONS[5] + goldReward + OPTIONS[6] + (hasUsedFreeOption(0) ? goldCost : 0) + OPTIONS[1]
        );

        // Upgrade option
        event.imageEventText.setDialogOption(
                OPTIONS[3] + (hasUsedFreeOption(1) ? upgradeCost : 0) + OPTIONS[1]
        );

        // Relic option
        event.imageEventText.setDialogOption(
                OPTIONS[4] + (hasUsedFreeOption(2) ? relicCost : 0) + OPTIONS[1]
        );

        // Leave option
        event.imageEventText.setDialogOption(
                OPTIONS[7] + (hasUsedFreeOption(3) ? leaveCost : 0) + OPTIONS[1]
        );
    }
}