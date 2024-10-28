package upgradeableevents.eventupgrades.Exordium;

import basemod.ReflectionHacks;
import com.megacrit.cardcrawl.cards.curses.Regret;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.events.AbstractEvent;
import com.megacrit.cardcrawl.events.exordium.BigFish;
import com.megacrit.cardcrawl.helpers.CardLibrary;
import com.megacrit.cardcrawl.localization.UIStrings;
import upgradeableevents.interfaces.AbstractEventUpgrade;
import upgradeableevents.interfaces.UpgradeCondition;

import static upgradeableevents.UpgradeableEvents.makeID;

public class BigFishUpgrade extends AbstractEventUpgrade {
    private static final UIStrings uiStrings = CardCrawlGame.languagePack.getUIString(makeID("BigFishUpgrade"));
    private boolean isUpgraded = false;

    public BigFishUpgrade(BigFish event) {
        super(event, new BigFishUpgradeCondition());
    }

    private static class BigFishUpgradeCondition implements UpgradeCondition {
        @Override
        public boolean canUpgrade(AbstractEvent event) {
            BigFish bigFishEvent = (BigFish)event;
            Enum<?> curScreen = ReflectionHacks.getPrivate(bigFishEvent, BigFish.class, "screen");
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
        BigFish bigFishEvent = (BigFish)event;

        // Heal for half max HP instead of one third
        int healAmt = AbstractDungeon.player.maxHealth / 2;
        bigFishEvent.imageEventText.updateDialogOption(0,
                BigFish.OPTIONS[0] + healAmt + BigFish.OPTIONS[1]);

        // 10 Max HP instead of 5
        bigFishEvent.imageEventText.updateDialogOption(1,
                BigFish.OPTIONS[2] + "10" + BigFish.OPTIONS[3]);

        // Two relics instead of one
        bigFishEvent.imageEventText.updateDialogOption(2, uiStrings.TEXT[0], CardLibrary.getCopy(Regret.ID));
    }
}