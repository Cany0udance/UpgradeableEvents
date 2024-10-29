package upgradeableevents.eventupgrades.TheCity;

import basemod.ReflectionHacks;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.events.AbstractEvent;
import com.megacrit.cardcrawl.events.city.Beggar;
import com.megacrit.cardcrawl.localization.UIStrings;
import upgradeableevents.interfaces.AbstractEventUpgrade;
import upgradeableevents.interfaces.UpgradeCondition;

import static upgradeableevents.UpgradeableEvents.makeID;

public class BeggarUpgrade extends AbstractEventUpgrade {
    private static final UIStrings uiStrings = CardCrawlGame.languagePack.getUIString(makeID("BeggarUpgrade"));
    private boolean isUpgraded = false;

    public BeggarUpgrade(Beggar event) {
        super(event, new BeggarUpgradeCondition());
    }

    private static class BeggarUpgradeCondition implements UpgradeCondition {
        @Override
        public boolean canUpgrade(AbstractEvent event) {
            Beggar beggarEvent = (Beggar)event;
            Enum<?> curScreen = ReflectionHacks.getPrivate(beggarEvent, Beggar.class, "screen");
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
        Beggar beggarEvent = (Beggar)event;

        // Update the option text to show 1 gold instead of 75
        if (AbstractDungeon.player.gold >= 1) {
            beggarEvent.imageEventText.updateDialogOption(0,
                    Beggar.OPTIONS[0] + "1" + Beggar.OPTIONS[1],
                    AbstractDungeon.player.gold < 1);
        } else {
            beggarEvent.imageEventText.updateDialogOption(0,
                    Beggar.OPTIONS[2] + "1" + Beggar.OPTIONS[3],
                    AbstractDungeon.player.gold < 1);
        }

        beggarEvent.imageEventText.updateDialogOption(1,
                Beggar.OPTIONS[5]);
    }
}