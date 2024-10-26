package upgradeableevents.eventupgrades.Shrines;

import basemod.ReflectionHacks;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.events.AbstractEvent;
import com.megacrit.cardcrawl.events.shrines.UpgradeShrine;
import com.megacrit.cardcrawl.localization.UIStrings;
import upgradeableevents.interfaces.AbstractEventUpgrade;
import upgradeableevents.interfaces.UpgradeCondition;

import static upgradeableevents.UpgradeableEvents.makeID;

public class UpgradeShrineUpgrade extends AbstractEventUpgrade {
    private static final UIStrings uiStrings = CardCrawlGame.languagePack.getUIString(makeID("UpgradeShrineUpgrade"));
    private static final int CHOOSE_SCREEN_NUM = 0;  // INTRO screen
    private boolean isUpgraded = false;

    public UpgradeShrineUpgrade(UpgradeShrine event) {
        super(event, new UpgradeShrineUpgradeCondition());
    }

    private static class UpgradeShrineUpgradeCondition implements UpgradeCondition {
        @Override
        public boolean canUpgrade(AbstractEvent event) {
            UpgradeShrine shrineEvent = (UpgradeShrine)event;
            Object screenEnum = ReflectionHacks.getPrivate(shrineEvent, UpgradeShrine.class, "screen");
            int screenNum = ((Enum<?>) screenEnum).ordinal();
            return screenNum == CHOOSE_SCREEN_NUM;
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
        UpgradeShrine shrineEvent = (UpgradeShrine)event;

        if (AbstractDungeon.player.masterDeck.hasUpgradableCards()) {
            shrineEvent.imageEventText.updateDialogOption(0, uiStrings.TEXT[0]);
        } else {
            shrineEvent.imageEventText.updateDialogOption(0,
                    UpgradeShrine.OPTIONS[3], true);
        }

        shrineEvent.imageEventText.setDialogOption(UpgradeShrine.OPTIONS[1]);
    }
}