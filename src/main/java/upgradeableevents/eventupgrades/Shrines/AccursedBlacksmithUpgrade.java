package upgradeableevents.eventupgrades.Shrines;

import basemod.ReflectionHacks;
import com.megacrit.cardcrawl.cards.curses.Pain;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.events.AbstractEvent;
import com.megacrit.cardcrawl.events.shrines.AccursedBlacksmith;
import com.megacrit.cardcrawl.helpers.CardLibrary;
import com.megacrit.cardcrawl.localization.UIStrings;
import com.megacrit.cardcrawl.relics.WarpedTongs;
import upgradeableevents.interfaces.AbstractEventUpgrade;
import upgradeableevents.interfaces.UpgradeCondition;

import static upgradeableevents.UpgradeableEvents.makeID;

public class AccursedBlacksmithUpgrade extends AbstractEventUpgrade {
    private static final UIStrings uiStrings = CardCrawlGame.languagePack.getUIString(makeID("AccursedBlacksmithUpgrade"));

    public AccursedBlacksmithUpgrade(AccursedBlacksmith event) {
        super(event, new BlacksmithUpgradeCondition());
    }
    private boolean isUpgraded = false;
    private static class BlacksmithUpgradeCondition implements UpgradeCondition {
        @Override
        public boolean canUpgrade(AbstractEvent event) {
            AccursedBlacksmith blacksmithEvent = (AccursedBlacksmith)event;
            int screenNum = ReflectionHacks.getPrivate(blacksmithEvent, AccursedBlacksmith.class, "screenNum");
            return screenNum == 0;
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
        AccursedBlacksmith blacksmithEvent = (AccursedBlacksmith)event;

        // Update first option to upgrade 2 random cards
        if (AbstractDungeon.player.masterDeck.hasUpgradableCards()) {
            blacksmithEvent.imageEventText.updateDialogOption(0, uiStrings.TEXT[0]);
        } else {
            blacksmithEvent.imageEventText.updateDialogOption(0, AccursedBlacksmith.OPTIONS[4], true);
        }

        // Update second option to show 50% curse chance
        blacksmithEvent.imageEventText.setDialogOption(uiStrings.TEXT[1], CardLibrary.getCopy(Pain.ID), new WarpedTongs());

        // Leave third option unchanged
        blacksmithEvent.imageEventText.updateDialogOption(2, AccursedBlacksmith.OPTIONS[2]);
    }
}