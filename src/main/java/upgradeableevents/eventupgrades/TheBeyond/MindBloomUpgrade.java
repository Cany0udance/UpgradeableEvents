package upgradeableevents.eventupgrades.TheBeyond;

import basemod.ReflectionHacks;
import com.megacrit.cardcrawl.cards.curses.Decay;
import com.megacrit.cardcrawl.cards.curses.Doubt;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.events.AbstractEvent;
import com.megacrit.cardcrawl.events.beyond.MindBloom;
import com.megacrit.cardcrawl.helpers.CardLibrary;
import com.megacrit.cardcrawl.localization.UIStrings;
import upgradeableevents.interfaces.AbstractEventUpgrade;
import upgradeableevents.interfaces.UpgradeCondition;

import static upgradeableevents.UpgradeableEvents.makeID;

public class MindBloomUpgrade extends AbstractEventUpgrade {
    private static final UIStrings uiStrings = CardCrawlGame.languagePack.getUIString(makeID("MindBloomUpgrade"));
    private boolean isUpgraded = false;

    public MindBloomUpgrade(MindBloom event) {
        super(event, new MindBloomUpgradeCondition());
    }

    private static class MindBloomUpgradeCondition implements UpgradeCondition {
        @Override
        public boolean canUpgrade(AbstractEvent event) {
            MindBloom mindBloomEvent = (MindBloom)event;
            Enum<?> curScreen = ReflectionHacks.getPrivate(mindBloomEvent, MindBloom.class, "screen");
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
        MindBloom mindBloomEvent = (MindBloom)event;

        // Update option text to reflect the upgraded benefits
        mindBloomEvent.imageEventText.updateDialogOption(0,
                uiStrings.TEXT[0]); // "I AM WAR (Boss Relic)"

        mindBloomEvent.imageEventText.updateDialogOption(1,
                uiStrings.TEXT[1]); // "I AM AWAKE (Upgrade all cards, heal to full, obtain Mark of the Bloom)"

        if (AbstractDungeon.floorNum % 50 <= 40) {
            mindBloomEvent.imageEventText.updateDialogOption(2,
                    uiStrings.TEXT[2], // "I AM RICH (Gain 999 Gold, obtain 2 Decay)"
                    CardLibrary.getCopy(Decay.ID));
        } else {
            mindBloomEvent.imageEventText.updateDialogOption(2,
                    uiStrings.TEXT[3], // "I AM HEALTHY (Heal to full, gain 15 Max HP, obtain a Doubt)"
                    CardLibrary.getCopy(Doubt.ID));
        }
    }
}
