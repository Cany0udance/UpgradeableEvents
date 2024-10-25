package upgradeableevents.interfaces;

import com.megacrit.cardcrawl.events.AbstractEvent;
import upgradeableevents.UpgradeEventManager;

public abstract class AbstractEventUpgrade {
    public final AbstractEvent event;
    protected final UpgradeCondition upgradeCondition;

    public AbstractEventUpgrade(AbstractEvent event, UpgradeCondition upgradeCondition) {
        this.event = event;
        this.upgradeCondition = upgradeCondition;
    }

    public boolean canBeUpgraded() {
        return upgradeCondition.canUpgrade(event);
    }

    public abstract void upgrade();
    protected abstract void rebuildOptions();

    protected void clearAndRebuildOptions() {
        event.imageEventText.clearAllDialogs();
        rebuildOptions();
        UpgradeEventManager.playUpgradeVfx();
    }
}
