package upgradeableevents;

import basemod.BaseMod;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.events.AbstractEvent;
import com.megacrit.cardcrawl.events.exordium.Cleric;
import com.megacrit.cardcrawl.events.shrines.Bonfire;
import com.megacrit.cardcrawl.events.shrines.Duplicator;
import com.megacrit.cardcrawl.events.shrines.FountainOfCurseRemoval;
import com.megacrit.cardcrawl.events.shrines.NoteForYourself;
import upgradeableevents.effects.EventUpgradeShineEffect;
import upgradeableevents.eventupgrades.Shrines.BonfireUpgrade;
import upgradeableevents.eventupgrades.ClericEventUpgrade;
import upgradeableevents.eventupgrades.Shrines.DuplicatorUpgrade;
import upgradeableevents.eventupgrades.Shrines.FountainOfCurseRemovalUpgrade;
import upgradeableevents.eventupgrades.Shrines.NoteForYourselfUpgrade;
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
    }

    public static void registerEventUpgrade(Class<? extends AbstractEvent> eventClass,
                                            Class<? extends AbstractEventUpgrade> upgradeClass) {
        UPGRADE_MAPPINGS.put(eventClass, upgradeClass);
    }

    public static boolean canUpgradeCurrentEvent() {
        if (!eventUpgradeAvailable) return false;

        AbstractEvent currentEvent = AbstractDungeon.getCurrRoom().event;
        if (currentEvent == null) return false;

        // Check if we need to create a new upgrade instance
        if (currentUpgrade == null || currentUpgrade.event != currentEvent) {
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

    public static void reset() {
        eventUpgradeAvailable = false;
        currentUpgrade = null;
    }

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
