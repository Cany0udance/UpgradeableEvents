package upgradeableevents.util;

import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.events.AbstractEvent;
import upgradeableevents.UpgradeEventManager;

public class ButtonEffectHelper {
    public static void updateUpgradeAvailability() {
        AbstractEvent currentEvent = AbstractDungeon.getCurrRoom().event;
        if (currentEvent != null) {
            UpgradeEventManager.setEventUpgradeAvailable(true);
        }
    }
}