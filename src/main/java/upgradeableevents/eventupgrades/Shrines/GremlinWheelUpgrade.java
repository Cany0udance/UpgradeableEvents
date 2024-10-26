package upgradeableevents.eventupgrades.Shrines;

import basemod.ReflectionHacks;
import com.megacrit.cardcrawl.events.AbstractEvent;
import com.megacrit.cardcrawl.events.shrines.GremlinWheelGame;
import upgradeableevents.UpgradeEventManager;
import upgradeableevents.interfaces.AbstractEventUpgrade;
import upgradeableevents.interfaces.UpgradeCondition;
import upgradeableevents.patches.Shrines.GremlinWheelUpgradedField;

public class GremlinWheelUpgrade extends AbstractEventUpgrade {

    public GremlinWheelUpgrade(GremlinWheelGame event) {
        super(event, new GremlinWheelUpgradeCondition());
    }


    private static class GremlinWheelUpgradeCondition implements UpgradeCondition {
        @Override
        public boolean canUpgrade(AbstractEvent event) {
            GremlinWheelGame wheelEvent = (GremlinWheelGame)event;
            Enum<?> screenNum = ReflectionHacks.getPrivate(wheelEvent, GremlinWheelGame.class, "screen");
            boolean startSpin = ReflectionHacks.getPrivate(wheelEvent, GremlinWheelGame.class, "startSpin");
            boolean buttonPressed = ReflectionHacks.getPrivate(wheelEvent, GremlinWheelGame.class, "buttonPressed");
            float bounceTimer = ReflectionHacks.getPrivate(wheelEvent, GremlinWheelGame.class, "bounceTimer");

            // Can only upgrade when wheel is visible but not spun
            return screenNum.name().equals("INTRO") &&
                    startSpin &&
                    !buttonPressed &&
                    bounceTimer == 0.0F;
        }
    }

    @Override
    public void upgrade() {
        if (!canBeUpgraded()) return;

        GremlinWheelGame wheelEvent = (GremlinWheelGame)event;

        GremlinWheelUpgradedField.upgraded.set(wheelEvent, true);
        UpgradeEventManager.playUpgradeVfx();
    }

    @Override
    protected void rebuildOptions() {
        // This event doesn't use standard dialog options during gameplay
    }
}