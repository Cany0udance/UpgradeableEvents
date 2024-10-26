package upgradeableevents.eventupgrades.Shrines;

import basemod.ReflectionHacks;
import com.badlogic.gdx.math.MathUtils;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.events.AbstractEvent;
import com.megacrit.cardcrawl.events.shrines.WomanInBlue;
import com.megacrit.cardcrawl.localization.UIStrings;
import upgradeableevents.interfaces.AbstractEventUpgrade;
import upgradeableevents.interfaces.UpgradeCondition;

import static com.megacrit.cardcrawl.events.shrines.WomanInBlue.OPTIONS;
import static upgradeableevents.UpgradeableEvents.makeID;

public class WomanInBlueUpgrade extends AbstractEventUpgrade {
    private static final UIStrings uiStrings = CardCrawlGame.languagePack.getUIString(makeID("WomanInBlueUpgrade"));
    private boolean isUpgraded = false;

    public WomanInBlueUpgrade(WomanInBlue event) {
        super(event, new WomanInBlueUpgradeCondition());
    }

    private static class WomanInBlueUpgradeCondition implements UpgradeCondition {
        @Override
        public boolean canUpgrade(AbstractEvent event) {
            if (event == null) {
                return false;
            }

            WomanInBlue womanEvent = (WomanInBlue)event;
            Enum<?> screenNum = ReflectionHacks.getPrivate(womanEvent, WomanInBlue.class, "screen");

            if (screenNum == null) {
                return false;
            }

            return screenNum.name().equals("INTRO");
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
        WomanInBlue womanEvent = (WomanInBlue)event;

        // Update options to show doubled potion amounts
        womanEvent.imageEventText.clearAllDialogs();
        womanEvent.imageEventText.setDialogOption(uiStrings.TEXT[0] + 20 + OPTIONS[3]);
        womanEvent.imageEventText.setDialogOption(uiStrings.TEXT[1] + 30 + OPTIONS[3]);
        womanEvent.imageEventText.setDialogOption(uiStrings.TEXT[2] + 40 + OPTIONS[3]);

        if (AbstractDungeon.ascensionLevel >= 15) {
            womanEvent.imageEventText.setDialogOption(OPTIONS[5] + MathUtils.ceil((float)AbstractDungeon.player.maxHealth * 0.05F) + OPTIONS[6]);
        } else {
            womanEvent.imageEventText.setDialogOption(OPTIONS[4]);
        }
    }
}