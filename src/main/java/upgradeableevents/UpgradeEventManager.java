package upgradeableevents;

import basemod.BaseMod;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.events.AbstractEvent;
import com.megacrit.cardcrawl.events.exordium.Cleric;
import com.megacrit.cardcrawl.events.shrines.*;
import upgradeableevents.effects.EventUpgradeShineEffect;
import upgradeableevents.eventupgrades.Shrines.*;
import upgradeableevents.eventupgrades.ClericEventUpgrade;
import upgradeableevents.interfaces.AbstractEventUpgrade;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;


public class UpgradeEventManager {
    private static boolean eventUpgradeAvailable = false;
    private static final Map<Class<? extends AbstractEvent>, Class<? extends AbstractEventUpgrade>> UPGRADE_MAPPINGS = new HashMap<>();
    private static AbstractEventUpgrade currentUpgrade = null;

    static {
        // Register all event upgrades here
        UPGRADE_MAPPINGS.put(Cleric.class, ClericEventUpgrade.class);
        UPGRADE_MAPPINGS.put(NoteForYourself.class, NoteForYourselfUpgrade.class);
        UPGRADE_MAPPINGS.put(Bonfire.class, BonfireUpgrade.class);
        UPGRADE_MAPPINGS.put(FountainOfCurseRemoval.class, FountainOfCurseRemovalUpgrade.class);
        UPGRADE_MAPPINGS.put(Duplicator.class, DuplicatorUpgrade.class);
        UPGRADE_MAPPINGS.put(GoldShrine.class, GoldShrineUpgrade.class);
        UPGRADE_MAPPINGS.put(Lab.class, LabUpgrade.class);
        UPGRADE_MAPPINGS.put(GremlinMatchGame.class, GremlinMatchUpgrade.class);
        UPGRADE_MAPPINGS.put(AccursedBlacksmith.class, AccursedBlacksmithUpgrade.class);
        UPGRADE_MAPPINGS.put(PurificationShrine.class, PurificationShrineUpgrade.class);
        UPGRADE_MAPPINGS.put(Transmogrifier.class, TransmogrifierUpgrade.class);
        UPGRADE_MAPPINGS.put(UpgradeShrine.class, UpgradeShrineUpgrade.class);
        UPGRADE_MAPPINGS.put(WeMeetAgain.class, WeMeetAgainUpgrade.class);
        UPGRADE_MAPPINGS.put(GremlinWheelGame.class, GremlinWheelUpgrade.class);
        UPGRADE_MAPPINGS.put(WomanInBlue.class, WomanInBlueUpgrade.class);
        UPGRADE_MAPPINGS.put(Designer.class, DesignerEventUpgrade.class);
    }

    public static void registerEventUpgrade(Class<? extends AbstractEvent> eventClass,
                                            Class<? extends AbstractEventUpgrade> upgradeClass) {
        UPGRADE_MAPPINGS.put(eventClass, upgradeClass);
    }

    public static void forceCheckUpgradeAvailability() {
        eventUpgradeAvailable = true;
        createNewUpgradeInstance(AbstractDungeon.getCurrRoom().event);
    }

    public static boolean canUpgradeCurrentEvent() {
        if (!eventUpgradeAvailable) return false;

        AbstractEvent currentEvent = AbstractDungeon.getCurrRoom().event;
        if (currentEvent == null) return false;

        if (currentEvent instanceof GremlinWheelGame) {
            createNewUpgradeInstance(currentEvent);
        }
        else if (currentUpgrade == null || currentUpgrade.event != currentEvent) {
            createNewUpgradeInstance(currentEvent);
        }
        return currentUpgrade != null && currentUpgrade.canBeUpgraded();
    }

    private static void createNewUpgradeInstance(AbstractEvent event) {
        Class<? extends AbstractEventUpgrade> upgradeClass = UPGRADE_MAPPINGS.get(event.getClass());
        if (upgradeClass == null) {
            currentUpgrade = null;
            return;
        }

        try {
            Constructor<?> constructor = upgradeClass.getConstructor(event.getClass());
            currentUpgrade = (AbstractEventUpgrade) constructor.newInstance(event);
        } catch (Exception e) {
            BaseMod.logger.error("Failed to create upgrade instance for: " + event.getClass().getName());
            e.printStackTrace();
            currentUpgrade = null;
        }
    }

    public static void upgradeCurrentEvent() {
        if (!canUpgradeCurrentEvent()) return;
        currentUpgrade.upgrade();
        setEventUpgradeAvailable(false);
    }

    public static void setEventUpgradeAvailable(boolean value) {
        eventUpgradeAvailable = value;
    }


    // Potentially useless method?
    /*
    public static void reset() {
        eventUpgradeAvailable = false;
        currentUpgrade = null;
    }
     */

    public static void playUpgradeVfx() {
        AbstractDungeon.effectsQueue.add(new EventUpgradeShineEffect(
                Settings.WIDTH / 2.0F,
                Settings.HEIGHT / 2.0F,
                1,
                500.0F,
                110.0F
        ));
    }

    public static AbstractEventUpgrade getCurrentUpgrade() {
        return currentUpgrade;
    }
}
