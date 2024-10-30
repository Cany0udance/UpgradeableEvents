package upgradeableevents.eventupgrades.BetterEvents;

import basemod.ReflectionHacks;
import betterThird.relics.NestCultRelic;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.colorless.RitualDagger;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.events.AbstractEvent;
import com.megacrit.cardcrawl.localization.UIStrings;
import upgradeableevents.interfaces.AbstractEventUpgrade;

import static upgradeableevents.UpgradeableEvents.makeID;

@SpirePatch(
        optional = true,
        cls = "betterThird.events.BetterNestEvent",
        method = SpirePatch.CLASS
)
public class BetterNestUpgrade extends AbstractEventUpgrade {
    private static final String BETTER_THIRD_CLASS = "betterThird.BetterThird";
    private static final String BETTER_NEST_EVENT_CLASS = "betterThird.events.BetterNestEvent";
    public static boolean isBetterThirdLoaded = false;
    public static Class<?> eventClass = null;

    static {
        try {
            Class.forName(BETTER_THIRD_CLASS);
            eventClass = Class.forName(BETTER_NEST_EVENT_CLASS);
            isBetterThirdLoaded = true;
        } catch (Exception e) {
            isBetterThirdLoaded = false;
        }
    }

    private static final UIStrings uiStrings = CardCrawlGame.languagePack.getUIString(makeID("NestUpgrade"));
    private boolean isUpgraded = false;

    public BetterNestUpgrade(AbstractEvent event) {
        super(event, (AbstractEvent e) -> {
            if (!isBetterThirdLoaded) return false;
            Enum<?> curScreen = ReflectionHacks.getPrivate(e, eventClass, "screen");
            return "RESULT".equals(curScreen.name());
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

        AbstractEvent nestEvent = event;
        nestEvent.imageEventText.optionList.clear();

        int goldGain = ReflectionHacks.getPrivate(nestEvent, eventClass, "goldGain");
        String[] OPTIONS = ReflectionHacks.getPrivateStatic(eventClass, "OPTIONS");

        // Create an upgraded Ritual Dagger for preview
        AbstractCard ritualDagger = new RitualDagger();
        ritualDagger.upgrade();

        // Gold option
        nestEvent.imageEventText.setDialogOption(
                OPTIONS[2] + (goldGain * 2) + OPTIONS[3]
        );

        // Dagger option
        nestEvent.imageEventText.setDialogOption(
                uiStrings.TEXT[0] + 1 + OPTIONS[1],
                ritualDagger
        );

        // Join cult option (unchanged)
        nestEvent.imageEventText.setDialogOption(
                OPTIONS[6],
                new NestCultRelic()
        );
    }
}