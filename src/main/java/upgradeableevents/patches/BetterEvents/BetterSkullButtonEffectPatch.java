package upgradeableevents.patches.BetterEvents;

import basemod.ReflectionHacks;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.events.AbstractEvent;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.vfx.RainingGoldEffect;
import upgradeableevents.UpgradeEventManager;
import upgradeableevents.eventupgrades.BetterEvents.BetterSkullUpgrade;
import upgradeableevents.interfaces.AbstractEventUpgrade;
import upgradeableevents.util.ButtonEffectHelper;

import java.util.List;

@SpirePatch(
        optional = true,
        cls = "betterSkull.events.BetterSkullEvent",
        method = "buttonEffect"
)
public class BetterSkullButtonEffectPatch {
    @SpirePrefixPatch
    public static SpireReturn<Void> Prefix(AbstractEvent __instance, int buttonPressed) {
        if (!BetterSkullUpgrade.isBetterSkullLoaded) return SpireReturn.Continue();

        AbstractEventUpgrade currentUpgrade = UpgradeEventManager.getCurrentUpgrade();
        if (currentUpgrade instanceof BetterSkullUpgrade && ((BetterSkullUpgrade)currentUpgrade).isUpgraded()) {
            Enum<?> screen = ReflectionHacks.getPrivate(__instance, BetterSkullUpgrade.eventClass, "screen");

            if ("ASK".equals(screen.name())) {
                BetterSkullUpgrade upgrade = (BetterSkullUpgrade)currentUpgrade;
                boolean wasUnused = !upgrade.hasUsedFreeOption(buttonPressed);

                if (wasUnused) {
                    upgrade.markOptionUsed(buttonPressed);

                    switch (buttonPressed) {
                        case 0: // Gold
                            int goldReward = ReflectionHacks.getPrivate(__instance, BetterSkullUpgrade.eventClass, "goldReward");
                            String optionsChosen = ReflectionHacks.getPrivate(__instance, BetterSkullUpgrade.eventClass, "optionsChosen");
                            ReflectionHacks.setPrivate(__instance, BetterSkullUpgrade.eventClass, "optionsChosen", optionsChosen + "GO ");
                            String goldMsg = (String)ReflectionHacks.getPrivateStatic(BetterSkullUpgrade.eventClass, "GOLD_MSG");
                            String askAgainMsg = (String)ReflectionHacks.getPrivateStatic(BetterSkullUpgrade.eventClass, "ASK_AGAIN_MSG");
                            __instance.imageEventText.updateBodyText(goldMsg + askAgainMsg);
                            AbstractDungeon.effectList.add(new RainingGoldEffect(goldReward));
                            AbstractDungeon.player.gainGold(goldReward);
                            int goldEarned = ReflectionHacks.getPrivate(__instance, BetterSkullUpgrade.eventClass, "goldEarned");
                            ReflectionHacks.setPrivate(__instance, BetterSkullUpgrade.eventClass, "goldEarned", goldEarned + goldReward);
                            break;

                        case 1: // Upgrade
                            optionsChosen = ReflectionHacks.getPrivate(__instance, BetterSkullUpgrade.eventClass, "optionsChosen");
                            ReflectionHacks.setPrivate(__instance, BetterSkullUpgrade.eventClass, "optionsChosen", optionsChosen + "UP ");
                            String upgradeMsg = (String)ReflectionHacks.getPrivateStatic(BetterSkullUpgrade.eventClass, "UPGRADE_MSG");
                            askAgainMsg = (String)ReflectionHacks.getPrivateStatic(BetterSkullUpgrade.eventClass, "ASK_AGAIN_MSG");
                            __instance.imageEventText.updateBodyText(upgradeMsg + askAgainMsg);
                            ReflectionHacks.privateMethod(BetterSkullUpgrade.eventClass, "upgrade")
                                    .invoke(__instance);
                            break;

                        case 2: // Relic
                            optionsChosen = ReflectionHacks.getPrivate(__instance, BetterSkullUpgrade.eventClass, "optionsChosen");
                            ReflectionHacks.setPrivate(__instance, BetterSkullUpgrade.eventClass, "optionsChosen", optionsChosen + "RE ");
                            String relicMsg = (String)ReflectionHacks.getPrivateStatic(BetterSkullUpgrade.eventClass, "RELIC_MSG");
                            askAgainMsg = (String)ReflectionHacks.getPrivateStatic(BetterSkullUpgrade.eventClass, "ASK_AGAIN_MSG");
                            __instance.imageEventText.updateBodyText(relicMsg + askAgainMsg);
                            AbstractRelic relic = AbstractDungeon.returnRandomScreenlessRelic(AbstractRelic.RelicTier.COMMON);
                            AbstractDungeon.getCurrRoom().spawnRelicAndObtain(Settings.WIDTH / 2.0F, Settings.HEIGHT / 2.0F, relic);
                            List<String> relicsObtained = ReflectionHacks.getPrivate(__instance, BetterSkullUpgrade.eventClass, "relicsObtained");
                            relicsObtained.add(relic.relicId);
                            break;

                        case 3: // Leave
                            setLeaveState(__instance);
                            return SpireReturn.Return(null);
                    }

                    // Use reflection to call clearAndRebuildOptions
                    ReflectionHacks.privateMethod(AbstractEventUpgrade.class, "clearAndRebuildOptions")
                            .invoke(upgrade);
                    return SpireReturn.Return(null);
                }
            }
        }
        return SpireReturn.Continue();
    }

    private static void setLeaveState(AbstractEvent __instance) {
        String LEAVE_MSG = (String)ReflectionHacks.getPrivateStatic(BetterSkullUpgrade.eventClass, "LEAVE_MSG");
        String[] OPTIONS = (String[])ReflectionHacks.getPrivateStatic(BetterSkullUpgrade.eventClass, "OPTIONS");

        String optionsChosen = ReflectionHacks.getPrivate(__instance, BetterSkullUpgrade.eventClass, "optionsChosen");
        ReflectionHacks.setPrivate(__instance, BetterSkullUpgrade.eventClass, "optionsChosen", optionsChosen + "LEAVE ");

        __instance.imageEventText.updateBodyText(LEAVE_MSG);
        __instance.imageEventText.clearAllDialogs();
        __instance.imageEventText.setDialogOption(OPTIONS[8]);

        try {
            for (Class<?> innerClass : BetterSkullUpgrade.eventClass.getDeclaredClasses()) {
                if (innerClass.getSimpleName().equals("CurScreen")) {
                    Object completeScreen = Enum.valueOf((Class<Enum>) innerClass, "COMPLETE");
                    ReflectionHacks.setPrivate(__instance, BetterSkullUpgrade.eventClass, "screen", completeScreen);
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SpirePostfixPatch
    public static void Postfix(AbstractEvent __instance, int buttonPressed) {
        if (BetterSkullUpgrade.isBetterSkullLoaded) {
            ButtonEffectHelper.updateUpgradeAvailability();
        }
    }
}