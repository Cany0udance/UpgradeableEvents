package upgradeableevents.eventupgrades.BetterEvents;

import basemod.ReflectionHacks;
import betterThird.relics.SlimedRelic;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.events.AbstractEvent;
import com.megacrit.cardcrawl.localization.UIStrings;
import upgradeableevents.interfaces.AbstractEventUpgrade;

import static upgradeableevents.UpgradeableEvents.makeID;

@SpirePatch(
        optional = true,
        cls = "betterThird.events.BetterGoopEvent",
        method = SpirePatch.CLASS
)
public class BetterGoopUpgrade extends AbstractEventUpgrade {
    private static final String BETTER_THIRD_CLASS = "betterThird.BetterThird";
    private static final String BETTER_GOOP_EVENT_CLASS = "betterThird.events.BetterGoopEvent";
    public static boolean isBetterThirdLoaded = false;
    public static Class<?> eventClass = null;

    static {
        try {
            Class.forName(BETTER_THIRD_CLASS);
            eventClass = Class.forName(BETTER_GOOP_EVENT_CLASS);
            isBetterThirdLoaded = true;
        } catch (Exception e) {
            isBetterThirdLoaded = false;
        }
    }

    private static final UIStrings uiStrings = CardCrawlGame.languagePack.getUIString(makeID("BetterGoopUpgrade"));
    private boolean isUpgraded = false;

    public BetterGoopUpgrade(AbstractEvent event) {
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

        AbstractEvent goopEvent = event;
        goopEvent.imageEventText.optionList.clear();

        int gold = ReflectionHacks.getPrivate(goopEvent, eventClass, "gold");
        boolean hasBag = ReflectionHacks.getPrivate(goopEvent, eventClass, "bag");
        int goldLoss = ReflectionHacks.getPrivate(goopEvent, eventClass, "goldLoss");
        String[] OPTIONS = ReflectionHacks.getPrivateStatic(eventClass, "OPTIONS");

        // First option - Gold without damage
        goopEvent.imageEventText.setDialogOption(
                uiStrings.TEXT[0] + gold + uiStrings.TEXT[1]
        );

        // Second option - Keep gold loss if has bag, otherwise no gold loss
        if (hasBag) {
            goopEvent.imageEventText.setDialogOption(
                    OPTIONS[6] + goldLoss + OPTIONS[4] + OPTIONS[7] + "11" + OPTIONS[8],
                    new SlimedRelic()
            );
        } else {
            goopEvent.imageEventText.setDialogOption(
                    uiStrings.TEXT[2],
                    new SlimedRelic()
            );
        }

        // Third option - Keep gold loss if has bag, otherwise no gold loss
        if (hasBag) {
            goopEvent.imageEventText.setDialogOption(
                    OPTIONS[3] + goldLoss + OPTIONS[4]
            );
        } else {
            goopEvent.imageEventText.setDialogOption(
                    uiStrings.TEXT[3]
            );
        }
    }
}
