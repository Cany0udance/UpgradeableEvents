package upgradeableevents.eventupgrades.BetterEvents;

import basemod.ReflectionHacks;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.events.AbstractEvent;
import com.megacrit.cardcrawl.localization.UIStrings;
import upgradeableevents.interfaces.AbstractEventUpgrade;

import static upgradeableevents.UpgradeableEvents.makeID;

@SpirePatch(
        optional = true,
        cls = "betterThird.events.BetterShiningEvent",
        method = SpirePatch.CLASS
)
public class BetterShiningUpgrade extends AbstractEventUpgrade {
    private static final String BETTER_THIRD_CLASS = "betterThird.BetterThird";
    private static final String BETTER_SHINING_EVENT_CLASS = "betterThird.events.BetterShiningEvent";
    public static boolean isBetterThirdLoaded = false;
    public static Class<?> eventClass = null;

    static {
        try {
            Class.forName(BETTER_THIRD_CLASS);
            eventClass = Class.forName(BETTER_SHINING_EVENT_CLASS);
            isBetterThirdLoaded = true;
        } catch (Exception e) {
            isBetterThirdLoaded = false;
        }
    }

    private static final UIStrings uiStrings = CardCrawlGame.languagePack.getUIString(makeID("BetterShiningUpgrade"));
    private boolean isUpgraded = false;

    public BetterShiningUpgrade(AbstractEvent event) {
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

        AbstractEvent shiningEvent = event;
        int damage = ReflectionHacks.getPrivate(shiningEvent, eventClass, "damage");
        boolean dreamcatcher = ReflectionHacks.getPrivate(shiningEvent, eventClass, "dreamcatcher");
        AbstractCard card = ReflectionHacks.getPrivate(shiningEvent, eventClass, "card");
        String[] OPTIONS = ReflectionHacks.getPrivateStatic(eventClass, "OPTIONS");

        // Clear existing options
        shiningEvent.imageEventText.clearRemainingOptions();

        // First option - Upgrade 3 cards
        if (AbstractDungeon.player.masterDeck.hasUpgradableCards()) {
            shiningEvent.imageEventText.setDialogOption(uiStrings.TEXT[0] + damage + OPTIONS[1]);
        } else {
            shiningEvent.imageEventText.setDialogOption(OPTIONS[3], true);
        }

        // Second option - Apotheosis with reduced Burn chance
        card.upgrade(); // Always upgraded Apotheosis
        shiningEvent.imageEventText.setDialogOption(
                uiStrings.TEXT[1] + card + OPTIONS[5] + "35" + OPTIONS[6],
                card
        );

        // Third option - Dream Catcher
        if (dreamcatcher) {
            shiningEvent.imageEventText.setDialogOption(OPTIONS[7] + card + OPTIONS[8], card);
        }

        // Fourth option - Leave
        shiningEvent.imageEventText.setDialogOption(OPTIONS[2]);
    }
}
