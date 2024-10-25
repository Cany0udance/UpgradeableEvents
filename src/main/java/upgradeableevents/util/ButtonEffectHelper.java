package upgradeableevents.util;

import upgradeableevents.UpgradeEventManager;

public class ButtonEffectHelper {
    public static void updateUpgradeAvailability() {
        if (UpgradeEventManager.canUpgradeCurrentEvent()) {
            UpgradeEventManager.setEventUpgradeAvailable(true);
        } else {
            UpgradeEventManager.setEventUpgradeAvailable(false);
        }
    }
}