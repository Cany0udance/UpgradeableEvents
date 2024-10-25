package upgradeableevents.eventupgrades.Shrines;

import basemod.BaseMod;
import basemod.ReflectionHacks;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.events.AbstractEvent;
import com.megacrit.cardcrawl.events.shrines.Bonfire;
import com.megacrit.cardcrawl.localization.UIStrings;
import upgradeableevents.interfaces.AbstractEventUpgrade;
import upgradeableevents.interfaces.UpgradeCondition;

import static upgradeableevents.UpgradeableEvents.makeID;

public class BonfireUpgrade extends AbstractEventUpgrade {
    private static final UIStrings uiStrings = CardCrawlGame.languagePack.getUIString(makeID("BonfireUpgrade"));
    private static final int CHOOSE_SCREEN_NUM = 1;
    private boolean isUpgraded = false;
    private AbstractCard firstCard = null;

    public BonfireUpgrade(Bonfire event) {
        super(event, new BonfireUpgradeCondition());
    }

    private static class BonfireUpgradeCondition implements UpgradeCondition {
        @Override
        public boolean canUpgrade(AbstractEvent event) {
            Bonfire bonfireEvent = (Bonfire)event;
            Object screenEnum = ReflectionHacks.getPrivate(bonfireEvent, Bonfire.class, "screen");
            int screenNum = ((Enum<?>) screenEnum).ordinal();
            boolean cardSelect = ReflectionHacks.getPrivate(bonfireEvent, Bonfire.class, "cardSelect");

            // Only allow upgrade if we're on the choose screen AND not in card select mode
            return screenNum == CHOOSE_SCREEN_NUM && !cardSelect;
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
        Bonfire bonfireEvent = (Bonfire)event;
        bonfireEvent.imageEventText.updateDialogOption(0, Bonfire.OPTIONS[2] + uiStrings.TEXT[0]);
    }
}