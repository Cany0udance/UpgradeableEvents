package upgradeableevents.eventupgrades.TheCity;

import basemod.ReflectionHacks;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.events.AbstractEvent;
import com.megacrit.cardcrawl.events.city.Addict;
import com.megacrit.cardcrawl.helpers.CardLibrary;
import com.megacrit.cardcrawl.localization.UIStrings;
import upgradeableevents.interfaces.AbstractEventUpgrade;
import upgradeableevents.interfaces.UpgradeCondition;

import static upgradeableevents.UpgradeableEvents.makeID;

public class AddictUpgrade extends AbstractEventUpgrade {
    private static final UIStrings uiStrings = CardCrawlGame.languagePack.getUIString(makeID("AddictUpgrade"));
    private boolean isUpgraded = false;

    public AddictUpgrade(Addict event) {
        super(event, new AddictUpgradeCondition());
    }

    private static class AddictUpgradeCondition implements UpgradeCondition {
        @Override
        public boolean canUpgrade(AbstractEvent event) {
            Addict addictEvent = (Addict)event;
            int screenNum = ReflectionHacks.getPrivate(addictEvent, Addict.class, "screenNum");
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
        Addict addictEvent = (Addict)event;

        // Update first option to show 1 gold instead of 85
        if (AbstractDungeon.player.gold >= 1) {
            addictEvent.imageEventText.updateDialogOption(0,
                    Addict.OPTIONS[0] + "1" + Addict.OPTIONS[1],
                    AbstractDungeon.player.gold < 1);
        } else {
            addictEvent.imageEventText.updateDialogOption(0,
                    Addict.OPTIONS[2] + "1" + Addict.OPTIONS[3],
                    AbstractDungeon.player.gold < 1);
        }

        // Update second option to mention shop relic
        addictEvent.imageEventText.updateDialogOption(1,
                uiStrings.TEXT[0],
                CardLibrary.getCopy("Shame"));

        addictEvent.imageEventText.updateDialogOption(2,
                Addict.OPTIONS[5]);

    }
}