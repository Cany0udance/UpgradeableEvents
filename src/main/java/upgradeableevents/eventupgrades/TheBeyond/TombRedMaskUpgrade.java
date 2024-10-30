package upgradeableevents.eventupgrades.TheBeyond;

import basemod.ReflectionHacks;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.events.AbstractEvent;
import com.megacrit.cardcrawl.events.beyond.TombRedMask;
import com.megacrit.cardcrawl.localization.UIStrings;
import com.megacrit.cardcrawl.relics.RedMask;
import upgradeableevents.interfaces.AbstractEventUpgrade;
import upgradeableevents.interfaces.UpgradeCondition;

import static com.megacrit.cardcrawl.events.beyond.TombRedMask.OPTIONS;
import static upgradeableevents.UpgradeableEvents.makeID;

public class TombRedMaskUpgrade extends AbstractEventUpgrade {
    private static final UIStrings uiStrings = CardCrawlGame.languagePack.getUIString(makeID("TombRedMaskUpgrade"));
    private boolean isUpgraded = false;

    public TombRedMaskUpgrade(TombRedMask event) {
        super(event, new TombRedMaskUpgradeCondition());
    }

    private static class TombRedMaskUpgradeCondition implements UpgradeCondition {
        @Override
        public boolean canUpgrade(AbstractEvent event) {
            TombRedMask maskEvent = (TombRedMask)event;
            Enum<?> curScreen = ReflectionHacks.getPrivate(maskEvent, TombRedMask.class, "screen");
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
        TombRedMask maskEvent = (TombRedMask)event;
        maskEvent.imageEventText.optionList.clear();

        if (AbstractDungeon.player.hasRelic("Red Mask")) {
            // Update first option to show 333 gold instead of 222
            maskEvent.imageEventText.setDialogOption(uiStrings.TEXT[0]); // "Take 333 Gold (Red Mask)"
        } else {
            // First option remains locked
            maskEvent.imageEventText.setDialogOption(OPTIONS[1], true);
            // Update second option to show one-third of player's gold
            int goldCost = AbstractDungeon.player.gold / 3;
            maskEvent.imageEventText.setDialogOption(uiStrings.TEXT[1] + goldCost + uiStrings.TEXT[2], new RedMask()); // "Pay [1/3 Gold] to obtain Red Mask"
        }

        // Keep the leave option the same - just add it normally
        maskEvent.imageEventText.setDialogOption(OPTIONS[4]);
    }
}