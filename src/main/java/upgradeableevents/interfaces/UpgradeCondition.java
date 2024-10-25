package upgradeableevents.interfaces;

import com.megacrit.cardcrawl.events.AbstractEvent;

public interface UpgradeCondition {
    boolean canUpgrade(AbstractEvent event);
}