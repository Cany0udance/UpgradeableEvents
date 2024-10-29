package upgradeableevents.eventupgrades.TheCity;

import basemod.ReflectionHacks;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.events.AbstractEvent;
import com.megacrit.cardcrawl.events.city.MaskedBandits;
import com.megacrit.cardcrawl.localization.UIStrings;
import upgradeableevents.interfaces.AbstractEventUpgrade;
import upgradeableevents.interfaces.UpgradeCondition;

import static upgradeableevents.UpgradeableEvents.makeID;

public class MaskedBanditsUpgrade extends AbstractEventUpgrade {
    private static final UIStrings uiStrings = CardCrawlGame.languagePack.getUIString(makeID("MaskedBanditsUpgrade"));
    private boolean isUpgraded = false;

    public MaskedBanditsUpgrade(MaskedBandits event) {
        super(event, new MaskedBanditsUpgradeCondition());
    }

    private static class MaskedBanditsUpgradeCondition implements UpgradeCondition {
        @Override
        public boolean canUpgrade(AbstractEvent event) {
            MaskedBandits banditsEvent = (MaskedBandits)event;
            Enum<?> curScreen = ReflectionHacks.getPrivate(banditsEvent, MaskedBandits.class, "screen");
            return "INTRO".equals(curScreen.name());
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
        MaskedBandits banditsEvent = (MaskedBandits)event;

        // Keep the fight option text the same, but the reward will be doubled in the patch
        banditsEvent.roomEventText.updateDialogOption(1, MaskedBandits.OPTIONS[1]);

        // Update the "leave" option text if player has enough gold
        if (AbstractDungeon.player.gold >= 3) {
            banditsEvent.roomEventText.updateDialogOption(0, uiStrings.TEXT[0]);
        } else {
            banditsEvent.roomEventText.updateDialogOption(0, MaskedBandits.OPTIONS[0]);
        }
    }
}