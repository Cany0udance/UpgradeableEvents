package upgradeableevents.eventupgrades.Shrines;

import basemod.ReflectionHacks;
import com.megacrit.cardcrawl.cards.curses.Clumsy;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.events.AbstractEvent;
import com.megacrit.cardcrawl.events.shrines.GoldShrine;
import com.megacrit.cardcrawl.helpers.CardLibrary;
import com.megacrit.cardcrawl.localization.UIStrings;
import upgradeableevents.interfaces.AbstractEventUpgrade;
import upgradeableevents.interfaces.UpgradeCondition;

import static upgradeableevents.UpgradeableEvents.makeID;

public class GoldShrineUpgrade extends AbstractEventUpgrade {
    private static final UIStrings uiStrings = CardCrawlGame.languagePack.getUIString(makeID("GoldShrineUpgrade"));
    public static final float RELIC_CHANCE = 0.25f;
    private boolean isUpgraded = false;

    public GoldShrineUpgrade(GoldShrine event) {
        super(event, new GoldShrineUpgradeCondition());
    }

    private static class GoldShrineUpgradeCondition implements UpgradeCondition {
        @Override
        public boolean canUpgrade(AbstractEvent event) {
            GoldShrine goldShrineEvent = (GoldShrine)event;
            Enum screen = ReflectionHacks.getPrivate(goldShrineEvent, GoldShrine.class, "screen");
            return screen.name().equals("INTRO");
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
        GoldShrine goldShrineEvent = (GoldShrine)event;
        int goldAmt = ReflectionHacks.getPrivate(goldShrineEvent, GoldShrine.class, "goldAmt");

        // Update first option to show relic chance
        String upgradeText = GoldShrine.OPTIONS[0] + goldAmt + GoldShrine.OPTIONS[1] + uiStrings.TEXT[0];
        goldShrineEvent.imageEventText.setDialogOption(upgradeText);

        // Update second option to show Clumsy instead of Regret
        String desecrateText = uiStrings.TEXT[1];
        goldShrineEvent.imageEventText.setDialogOption(desecrateText, CardLibrary.getCopy(Clumsy.ID));

        // Leave option remains the same
        goldShrineEvent.imageEventText.setDialogOption(GoldShrine.OPTIONS[3]);
    }
}