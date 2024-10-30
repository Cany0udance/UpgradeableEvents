package upgradeableevents.eventupgrades.BetterEvents;

import basemod.ReflectionHacks;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.events.AbstractEvent;
import com.megacrit.cardcrawl.localization.UIStrings;
import upgradeableevents.UpgradeEventManager;
import upgradeableevents.interfaces.AbstractEventUpgrade;

import static upgradeableevents.UpgradeableEvents.makeID;

@SpirePatch(
        optional = true,
        cls = "betterThird.events.BetterScrapEvent",
        method = SpirePatch.CLASS
)
public class BetterScrapUpgrade extends AbstractEventUpgrade {
    private static final String BETTER_THIRD_CLASS = "betterThird.BetterThird";
    private static final String BETTER_SCRAP_EVENT_CLASS = "betterThird.events.BetterScrapEvent";
    public static boolean isBetterThirdLoaded = false;
    public static Class<?> eventClass = null;

    static {
        try {
            Class.forName(BETTER_THIRD_CLASS);
            eventClass = Class.forName(BETTER_SCRAP_EVENT_CLASS);
            isBetterThirdLoaded = true;
        } catch (Exception e) {
            isBetterThirdLoaded = false;
        }
    }

    private static final UIStrings uiStrings = CardCrawlGame.languagePack.getUIString(makeID("ScrapUpgrade"));
    private boolean isUpgraded = false;
    private int relicsObtained = 0;
    private int cardsObtained = 0;

    public BetterScrapUpgrade(AbstractEvent event) {
        super(event, (AbstractEvent e) -> {
            if (!isBetterThirdLoaded) return false;
            Enum<?> curScreen = ReflectionHacks.getPrivate(e, eventClass, "screen");
            return "INTRO".equals(curScreen.name());
        });
    }

    public void upgrade() {
        if (!canBeUpgraded()) return;
        isUpgraded = true;
        UpgradeEventManager.playUpgradeVfx();
    }

    public boolean isUpgraded() {
        return isUpgraded;
    }

    public int getRelicsObtained() {
        return relicsObtained;
    }

    public void incrementRelicsObtained() {
        relicsObtained++;
    }

    public int getCardsObtained() {
        return cardsObtained;
    }

    public void incrementCardsObtained() {
        cardsObtained++;
    }

    public int getMaxObtainable() {
        boolean hasDefense = (boolean) ReflectionHacks.getPrivate(event, eventClass, "defense");
        return hasDefense ? 3 : 2;
    }

    @Override
    protected void rebuildOptions() {
    }
}