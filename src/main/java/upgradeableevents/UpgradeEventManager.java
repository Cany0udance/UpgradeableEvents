package upgradeableevents;

import basemod.BaseMod;
import basemod.ReflectionHacks;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.events.AbstractEvent;
import com.megacrit.cardcrawl.events.beyond.*;
import com.megacrit.cardcrawl.events.city.*;
import com.megacrit.cardcrawl.events.exordium.*;
import com.megacrit.cardcrawl.events.shrines.*;
import upgradeableevents.effects.EventUpgradeShineEffect;
import upgradeableevents.eventupgrades.BetterEvents.*;
import upgradeableevents.eventupgrades.Exordium.*;
import upgradeableevents.eventupgrades.Shrines.*;
import upgradeableevents.eventupgrades.TheBeyond.*;
import upgradeableevents.eventupgrades.TheCity.*;
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
        UPGRADE_MAPPINGS.put(FaceTrader.class, FaceTraderUpgrade.class);

        UPGRADE_MAPPINGS.put(BigFish.class, BigFishUpgrade.class);
        UPGRADE_MAPPINGS.put(Cleric.class, ClericEventUpgrade.class);
        UPGRADE_MAPPINGS.put(DeadAdventurer.class, DeadAdventurerUpgrade.class);
        UPGRADE_MAPPINGS.put(GoldenIdolEvent.class, GoldenIdolUpgrade.class);
        UPGRADE_MAPPINGS.put(Mushrooms.class, MushroomsUpgrade.class);
        UPGRADE_MAPPINGS.put(LivingWall.class, LivingWallUpgrade.class);
        UPGRADE_MAPPINGS.put(ScrapOoze.class, ScrapOozeUpgrade.class);
        UPGRADE_MAPPINGS.put(ShiningLight.class, ShiningLightUpgrade.class);
        UPGRADE_MAPPINGS.put(Sssserpent.class, SssserpentUpgrade.class);
        UPGRADE_MAPPINGS.put(GoopPuddle.class, GoopPuddleUpgrade.class);
        UPGRADE_MAPPINGS.put(GoldenWing.class, GoldenWingUpgrade.class);

        UPGRADE_MAPPINGS.put(BackToBasics.class, BackToBasicsUpgrade.class);
        UPGRADE_MAPPINGS.put(DrugDealer.class, DrugDealerUpgrade.class);
        UPGRADE_MAPPINGS.put(Colosseum.class, ColosseumUpgrade.class);
        UPGRADE_MAPPINGS.put(Ghosts.class, GhostsUpgrade.class);
        UPGRADE_MAPPINGS.put(CursedTome.class, CursedTomeUpgrade.class);
        UPGRADE_MAPPINGS.put(ForgottenAltar.class, ForgottenAltarUpgrade.class);
        UPGRADE_MAPPINGS.put(TheJoust.class, TheJoustUpgrade.class);
        UPGRADE_MAPPINGS.put(KnowingSkull.class, KnowingSkullUpgrade.class);
        UPGRADE_MAPPINGS.put(TheLibrary.class, TheLibraryUpgrade.class);
        UPGRADE_MAPPINGS.put(MaskedBandits.class, MaskedBanditsUpgrade.class);
        UPGRADE_MAPPINGS.put(TheMausoleum.class, TheMausoleumUpgrade.class);
        UPGRADE_MAPPINGS.put(Nest.class, NestUpgrade.class);
        UPGRADE_MAPPINGS.put(Nloth.class, NlothUpgrade.class);
        UPGRADE_MAPPINGS.put(Beggar.class, BeggarUpgrade.class);
        UPGRADE_MAPPINGS.put(Addict.class, AddictUpgrade.class);
        UPGRADE_MAPPINGS.put(Vampires.class, VampiresUpgrade.class);

        UPGRADE_MAPPINGS.put(Falling.class, FallingUpgrade.class);
        UPGRADE_MAPPINGS.put(MindBloom.class, MindBloomUpgrade.class);
        UPGRADE_MAPPINGS.put(MoaiHead.class, MoaiHeadUpgrade.class);
        UPGRADE_MAPPINGS.put(MysteriousSphere.class, MysteriousSphereUpgrade.class);
        UPGRADE_MAPPINGS.put(SecretPortal.class, SecretPortalUpgrade.class);
        UPGRADE_MAPPINGS.put(SensoryStone.class, SensoryStoneUpgrade.class);
        UPGRADE_MAPPINGS.put(TombRedMask.class, TombRedMaskUpgrade.class);
        UPGRADE_MAPPINGS.put(WindingHalls.class, WindingHallsUpgrade.class);

        // Register optional mod event upgrades
        registerBetterThirdUpgrades();
    }

    private static void registerBetterThirdUpgrades() {
        try {
            Class<?> betterGoopEventClass = Class.forName("betterThird.events.BetterGoopEvent");
            UPGRADE_MAPPINGS.put(
                    (Class<? extends AbstractEvent>) betterGoopEventClass,
                    BetterGoopUpgrade.class
            );

            Class<?> betterNestEventClass = Class.forName("betterThird.events.BetterNestEvent");
            UPGRADE_MAPPINGS.put(
                    (Class<? extends AbstractEvent>) betterNestEventClass,
                    BetterNestUpgrade.class
            );

            Class<?> betterPortalEventClass = Class.forName("betterThird.events.BetterPortalEvent");
            UPGRADE_MAPPINGS.put(
                    (Class<? extends AbstractEvent>) betterPortalEventClass,
                    BetterPortalUpgrade.class
            );

            Class<?> betterScrapEventClass = Class.forName("betterThird.events.BetterScrapEvent");
            UPGRADE_MAPPINGS.put(
                    (Class<? extends AbstractEvent>) betterScrapEventClass,
                    BetterScrapUpgrade.class
            );

            Class<?> betterSerpentEventClass = Class.forName("betterThird.events.BetterSerpentEvent");
            UPGRADE_MAPPINGS.put(
                    (Class<? extends AbstractEvent>) betterSerpentEventClass,
                    BetterSerpentUpgrade.class
            );

            Class<?> betterShiningEventClass = Class.forName("betterThird.events.BetterShiningEvent");
            UPGRADE_MAPPINGS.put(
                    (Class<? extends AbstractEvent>) betterShiningEventClass,
                    BetterShiningUpgrade.class
            );

            Class<?> betterWritingEventClass = Class.forName("betterThird.events.BetterWritingEvent");
            UPGRADE_MAPPINGS.put(
                    (Class<? extends AbstractEvent>) betterWritingEventClass,
                    BetterWritingUpgrade.class
            );

            Class<?> betterAltarEventClass = Class.forName("betterAltar.events.BetterAltarEvent");
            UPGRADE_MAPPINGS.put(
                    (Class<? extends AbstractEvent>) betterAltarEventClass,
                    BetterAltarUpgrade.class
            );

            Class<?> betterSkullEventClass = Class.forName("betterSkull.events.BetterSkullEvent");
            UPGRADE_MAPPINGS.put(
                    (Class<? extends AbstractEvent>) betterSkullEventClass,
                    BetterSkullUpgrade.class
            );

            Class<?> betterMatchEventClass = Class.forName("betterMatch.events.BetterMatchEvent");
            UPGRADE_MAPPINGS.put(
                    (Class<? extends AbstractEvent>) betterMatchEventClass,
                    BetterMatchUpgrade.class
            );
        } catch (ClassNotFoundException e) {
            // Skip registration if mod not found
        }
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
            // First try to get the specific constructor
            Constructor<?> constructor = null;
            try {
                constructor = upgradeClass.getConstructor(event.getClass());
            } catch (NoSuchMethodException e) {
                // If that fails, try to get the AbstractEvent constructor
                try {
                    constructor = upgradeClass.getConstructor(AbstractEvent.class);
                } catch (NoSuchMethodException e2) {
                    // If that fails too, try no-args constructor as last resort
                    constructor = upgradeClass.getConstructor();
                }
            }

            if (constructor != null) {
                if (constructor.getParameterCount() > 0) {
                    currentUpgrade = (AbstractEventUpgrade) constructor.newInstance(event);
                } else {
                    // No-args constructor
                    currentUpgrade = (AbstractEventUpgrade) constructor.newInstance();
                    // Need to set the event after construction
                    ReflectionHacks.setPrivate(currentUpgrade, AbstractEventUpgrade.class, "event", event);
                }
            }
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
