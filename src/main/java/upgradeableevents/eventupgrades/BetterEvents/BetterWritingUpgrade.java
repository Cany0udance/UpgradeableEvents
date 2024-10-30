package upgradeableevents.eventupgrades.BetterEvents;

import basemod.ReflectionHacks;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.events.AbstractEvent;
import com.megacrit.cardcrawl.localization.UIStrings;
import upgradeableevents.interfaces.AbstractEventUpgrade;

import static upgradeableevents.UpgradeableEvents.makeID;

@SpirePatch(
        optional = true,
        cls = "betterThird.events.BetterWritingEvent",
        method = SpirePatch.CLASS
)
public class BetterWritingUpgrade extends AbstractEventUpgrade {
    private static final String BETTER_THIRD_CLASS = "betterThird.BetterThird";
    private static final String BETTER_WRITING_EVENT_CLASS = "betterThird.events.BetterWritingEvent";
    public static boolean isBetterThirdLoaded = false;
    public static Class<?> eventClass = null;

    static {
        try {
            Class.forName(BETTER_THIRD_CLASS);
            eventClass = Class.forName(BETTER_WRITING_EVENT_CLASS);
            isBetterThirdLoaded = true;
        } catch (Exception e) {
            isBetterThirdLoaded = false;
        }
    }

    private static final UIStrings uiStrings = CardCrawlGame.languagePack.getUIString(makeID("BackToBasicsUpgrade"));
    private boolean isUpgraded = false;

    public BetterWritingUpgrade(AbstractEvent event) {
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

        String[] OPTIONS = ReflectionHacks.getPrivateStatic(eventClass, "OPTIONS");
        boolean watcher = ReflectionHacks.getPrivate(event, eventClass, "watcher");
        boolean defect = ReflectionHacks.getPrivate(event, eventClass, "defect");
        AbstractCard card = ReflectionHacks.getPrivate(event, eventClass, "card");

        // Update the "Remove a card" option to mention the gold reward
        event.imageEventText.updateDialogOption(0, OPTIONS[0] + uiStrings.TEXT[0]);

        // Keep the character-specific option unchanged
        if (watcher) {
            event.imageEventText.updateDialogOption(1, OPTIONS[5]);
        } else if (defect) {
            event.imageEventText.updateDialogOption(1, OPTIONS[6]);
        } else {
            event.imageEventText.updateDialogOption(1, OPTIONS[4] + card.name + ".", card);
        }

        // Update the "Upgrade Strikes and Defends" option to mention all Basic and Rare cards
        event.imageEventText.updateDialogOption(2, uiStrings.TEXT[1]);
    }
}
