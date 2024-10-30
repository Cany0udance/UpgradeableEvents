package upgradeableevents.eventupgrades.BetterEvents;

import basemod.ReflectionHacks;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.colorless.Enlightenment;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.events.AbstractEvent;
import com.megacrit.cardcrawl.helpers.CardLibrary;
import com.megacrit.cardcrawl.localization.UIStrings;
import com.megacrit.cardcrawl.relics.Courier;
import com.megacrit.cardcrawl.relics.MembershipCard;
import upgradeableevents.interfaces.AbstractEventUpgrade;

import static upgradeableevents.UpgradeableEvents.makeID;

@SpirePatch(
        optional = true,
        cls = "betterThird.events.BetterSerpentEvent",
        method = SpirePatch.CLASS
)
public class BetterSerpentUpgrade extends AbstractEventUpgrade {
    private static final String BETTER_THIRD_CLASS = "betterThird.BetterThird";
    private static final String BETTER_SERPENT_EVENT_CLASS = "betterThird.events.BetterSerpentEvent";
    public static boolean isBetterThirdLoaded = false;
    public static Class<?> eventClass = null;

    static {
        try {
            Class.forName(BETTER_THIRD_CLASS);
            eventClass = Class.forName(BETTER_SERPENT_EVENT_CLASS);
            isBetterThirdLoaded = true;
        } catch (Exception e) {
            isBetterThirdLoaded = false;
        }
    }

    private static final UIStrings uiStrings = CardCrawlGame.languagePack.getUIString(makeID("SssserpentUpgrade"));
    private boolean isUpgraded = false;

    public BetterSerpentUpgrade(AbstractEvent event) {
        super(event, (AbstractEvent e) -> {
            if (!isBetterThirdLoaded) return false;
            Enum<?> curScreen = ReflectionHacks.getPrivate(e, eventClass, "screen");
            return "INTRO".equals(curScreen.name());
        });
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
        if (!isBetterThirdLoaded) return;

        AbstractEvent serpentEvent = event;
        AbstractCard curse = ReflectionHacks.getPrivate(serpentEvent, eventClass, "curse");
        int goldReward = ReflectionHacks.getPrivate(serpentEvent, eventClass, "goldReward");
        AbstractCard enlightenment = new Enlightenment();
        enlightenment.upgrade();
        enlightenment.misc = 1;

        String[] OPTIONS = ReflectionHacks.getPrivateStatic(eventClass, "OPTIONS");

        // Check for relics in order of priority
        boolean hasMembershipCard = AbstractDungeon.player.hasRelic(MembershipCard.ID);
        boolean hasCourier = AbstractDungeon.player.hasRelic(Courier.ID);

        serpentEvent.imageEventText.clearAllDialogs();

        // First option based on relic checks
        if (!hasMembershipCard) {
            serpentEvent.imageEventText.setDialogOption(uiStrings.TEXT[0], new MembershipCard());
        } else if (!hasCourier) {
            serpentEvent.imageEventText.setDialogOption(uiStrings.TEXT[1], new Courier());
        } else {
            serpentEvent.imageEventText.setDialogOption(
                    OPTIONS[0] + (goldReward * 2) + OPTIONS[1],
                    CardLibrary.getCopy(curse.cardID));
        }

        // Second option - upgraded Enlightenment
        serpentEvent.imageEventText.setDialogOption(
                OPTIONS[5] + enlightenment + OPTIONS[6],
                enlightenment);

        // Third option (ignore)
        serpentEvent.imageEventText.setDialogOption(OPTIONS[2]);
    }
}