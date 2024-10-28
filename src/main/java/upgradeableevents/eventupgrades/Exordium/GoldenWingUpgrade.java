package upgradeableevents.eventupgrades.Exordium;

import basemod.ReflectionHacks;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.events.AbstractEvent;
import com.megacrit.cardcrawl.events.exordium.GoldenWing;
import com.megacrit.cardcrawl.localization.UIStrings;
import upgradeableevents.interfaces.AbstractEventUpgrade;
import upgradeableevents.interfaces.UpgradeCondition;

import static com.megacrit.cardcrawl.events.exordium.ShiningLight.OPTIONS;
import static upgradeableevents.UpgradeableEvents.makeID;

public class GoldenWingUpgrade extends AbstractEventUpgrade {
    private static final UIStrings uiStrings = CardCrawlGame.languagePack.getUIString(makeID("GoldenWingUpgrade"));
    private boolean isUpgraded = false;
    private static final int MIN_UPGRADED_GOLD = 50;
    private static final int MAX_UPGRADED_GOLD = 160;

    public GoldenWingUpgrade(GoldenWing event) {
        super(event, new GoldenWingUpgradeCondition());
    }

    private static class GoldenWingUpgradeCondition implements UpgradeCondition {
        @Override
        public boolean canUpgrade(AbstractEvent event) {
            GoldenWing goldEvent = (GoldenWing)event;
            Enum<?> screen = ReflectionHacks.getPrivate(goldEvent, GoldenWing.class, "screen");
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
        GoldenWing goldEvent = (GoldenWing)event;
        boolean canAttack = ReflectionHacks.getPrivate(goldEvent, GoldenWing.class, "canAttack");

        // First option - remove card with no HP loss
        goldEvent.imageEventText.updateDialogOption(0, GoldenWing.OPTIONS[0] + "0" + GoldenWing.OPTIONS[1]);

        // Second option - increased gold range
        if (canAttack) {
            goldEvent.imageEventText.updateDialogOption(1,
                    GoldenWing.OPTIONS[2] + MIN_UPGRADED_GOLD + GoldenWing.OPTIONS[3] + MAX_UPGRADED_GOLD + GoldenWing.OPTIONS[4]);
        } else {
            goldEvent.imageEventText.updateDialogOption(1,
                    GoldenWing.OPTIONS[5] + "10" + GoldenWing.OPTIONS[6], true);
        }
        goldEvent.imageEventText.updateDialogOption(2, GoldenWing.OPTIONS[7]);
    }
}