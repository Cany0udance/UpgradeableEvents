package upgradeableevents.eventupgrades.Exordium;

import basemod.ReflectionHacks;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.events.AbstractEvent;
import com.megacrit.cardcrawl.events.exordium.GoopPuddle;
import com.megacrit.cardcrawl.localization.UIStrings;
import upgradeableevents.interfaces.AbstractEventUpgrade;
import upgradeableevents.interfaces.UpgradeCondition;

import static upgradeableevents.UpgradeableEvents.makeID;

public class GoopPuddleUpgrade extends AbstractEventUpgrade {
    private static final UIStrings uiStrings = CardCrawlGame.languagePack.getUIString(makeID("GoopPuddleUpgrade"));
    private boolean isUpgraded = false;
    private static final float GOLD_MULTIPLIER = 2.5f;
    private static final int UPGRADED_GOLD_LOSS = 1;
    public GoopPuddleUpgrade(GoopPuddle event) {
        super(event, new GoopPuddleUpgradeCondition());
    }

    private static class GoopPuddleUpgradeCondition implements UpgradeCondition {
        @Override
        public boolean canUpgrade(AbstractEvent event) {
            GoopPuddle goopEvent = (GoopPuddle)event;
            Enum<?> screen = ReflectionHacks.getPrivate(goopEvent, GoopPuddle.class, "screen");
            return screen.name().equals("INTRO");
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
        GoopPuddle goopEvent = (GoopPuddle)event;
        int damage = ReflectionHacks.getPrivate(goopEvent, GoopPuddle.class, "damage");
        int gold = ReflectionHacks.getPrivate(goopEvent, GoopPuddle.class, "gold");
        int goldLoss = ReflectionHacks.getPrivate(goopEvent, GoopPuddle.class, "goldLoss");

        // Calculate upgraded gold amount
        int upgradedGold = (int)(gold * GOLD_MULTIPLIER);

        // Set the options
        goopEvent.imageEventText.updateDialogOption(0,
                GoopPuddle.OPTIONS[0] + upgradedGold + GoopPuddle.OPTIONS[1] + damage + GoopPuddle.OPTIONS[2]);
        goopEvent.imageEventText.updateDialogOption(1,
                GoopPuddle.OPTIONS[3] + UPGRADED_GOLD_LOSS + GoopPuddle.OPTIONS[4]);

    }
}