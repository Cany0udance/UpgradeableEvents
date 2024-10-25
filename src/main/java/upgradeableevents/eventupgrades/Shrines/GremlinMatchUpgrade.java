package upgradeableevents.eventupgrades.Shrines;

import basemod.ReflectionHacks;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.events.AbstractEvent;
import com.megacrit.cardcrawl.events.shrines.GremlinMatchGame;
import com.megacrit.cardcrawl.localization.UIStrings;
import upgradeableevents.UpgradeEventManager;
import upgradeableevents.interfaces.AbstractEventUpgrade;
import upgradeableevents.interfaces.UpgradeCondition;

public class GremlinMatchUpgrade extends AbstractEventUpgrade {

    public GremlinMatchUpgrade(GremlinMatchGame event) {
        super(event, new GremlinMatchUpgradeCondition());
    }

    private static class GremlinMatchUpgradeCondition implements UpgradeCondition {
        @Override
        public boolean canUpgrade(AbstractEvent event) {
            GremlinMatchGame matchEvent = (GremlinMatchGame)event;
            Enum<?> screenNum = ReflectionHacks.getPrivate(matchEvent, GremlinMatchGame.class, "screen");
            return screenNum.name().equals("PLAY");
        }
    }

    @Override
    public void upgrade() {
        if (!canBeUpgraded()) return;

        GremlinMatchGame matchEvent = (GremlinMatchGame)event;
        int currentAttempts = (Integer)ReflectionHacks.getPrivate(matchEvent, GremlinMatchGame.class, "attemptCount");
        ReflectionHacks.setPrivate(matchEvent, GremlinMatchGame.class, "attemptCount", currentAttempts + 2);
        UpgradeEventManager.playUpgradeVfx();
    }

    @Override
    protected void rebuildOptions() {
        // This event doesn't use standard dialog options during gameplay
    }
}