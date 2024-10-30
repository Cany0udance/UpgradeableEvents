package upgradeableevents.patches.BetterEvents;

import basemod.ReflectionHacks;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.events.AbstractEvent;
import com.megacrit.cardcrawl.vfx.ObtainKeyEffect;
import upgradeableevents.UpgradeEventManager;
import upgradeableevents.eventupgrades.BetterEvents.BetterPortalUpgrade;
import upgradeableevents.interfaces.AbstractEventUpgrade;
import upgradeableevents.util.ButtonEffectHelper;

@SpirePatch(
        optional = true,
        cls = "betterThird.events.BetterPortalEvent",
        method = "buttonEffect"
)
public class BetterPortalButtonEffectPatch {
    @SpirePrefixPatch
    public static SpireReturn<Void> Prefix(AbstractEvent __instance, int buttonPressed) {
        if (!BetterPortalUpgrade.isBetterThirdLoaded) return SpireReturn.Continue();

        AbstractEventUpgrade currentUpgrade = UpgradeEventManager.getCurrentUpgrade();
        if (currentUpgrade instanceof BetterPortalUpgrade && ((BetterPortalUpgrade)currentUpgrade).isUpgraded()) {
            Enum<?> screen = ReflectionHacks.getPrivate(__instance, BetterPortalUpgrade.eventClass, "screen");
            boolean hasPrism = (boolean)ReflectionHacks.getPrivate(__instance, BetterPortalUpgrade.eventClass, "prism");

            if ("CHOICE".equals(screen.name()) && !hasPrism) {
                // Handle color selection without Prismatic Shard
                String[] colors = {"[#FF0000]", "[#FF7F00]", "[#FFFF00]", "[#00FF00]", "[#0000FF]", "[#8B00FF]"};
                if (buttonPressed >= 0 && buttonPressed < colors.length) {
                    ReflectionHacks.setPrivate(__instance, BetterPortalUpgrade.eventClass, "color", colors[buttonPressed]);
                    ReflectionHacks.privateMethod(BetterPortalUpgrade.eventClass, "portalAction")
                            .invoke(__instance);
                }
                return SpireReturn.Return(null);
            }
            if ("INTRO".equals(screen.name())) {
                String[] DESCRIPTIONS = ReflectionHacks.getPrivateStatic(BetterPortalUpgrade.eventClass, "DESCRIPTIONS");
                String[] OPTIONS = ReflectionHacks.getPrivateStatic(BetterPortalUpgrade.eventClass, "OPTIONS");

                if (buttonPressed == 0) {
                    // Heal to full
                    int healAmount = AbstractDungeon.player.maxHealth - AbstractDungeon.player.currentHealth;
                    if (healAmount > 0) {
                        AbstractDungeon.player.heal(healAmount, true);
                    }

                    // Get all keys
                    AbstractDungeon.topLevelEffects.add(new ObtainKeyEffect(ObtainKeyEffect.KeyColor.RED));
                    AbstractDungeon.topLevelEffects.add(new ObtainKeyEffect(ObtainKeyEffect.KeyColor.GREEN));
                    AbstractDungeon.topLevelEffects.add(new ObtainKeyEffect(ObtainKeyEffect.KeyColor.BLUE));

                    // Original portal effect
                    __instance.imageEventText.updateBodyText(DESCRIPTIONS[1]);

                    // Set screen to ACCEPT
                    try {
                        for (Class<?> innerClass : BetterPortalUpgrade.eventClass.getDeclaredClasses()) {
                            if (innerClass.getSimpleName().equals("CurScreen")) {
                                Object acceptScreen = Enum.valueOf((Class<Enum>) innerClass, "ACCEPT");
                                ReflectionHacks.setPrivate(__instance, BetterPortalUpgrade.eventClass, "screen", acceptScreen);
                                break;
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    __instance.imageEventText.updateDialogOption(0, OPTIONS[2]);
                    __instance.imageEventText.clearRemainingOptions();

                    // Log metrics
                    AbstractEvent.logMetric("Better Portal", "Took Portal (Upgraded)", null, null, null, null, null, null, null, healAmount, 0, 0, 0, 0, 0);

                    CardCrawlGame.screenShake.mildRumble(5.0F);
                    CardCrawlGame.sound.play("ATTACK_MAGIC_SLOW_2");
                    return SpireReturn.Return(null);
                } else if (buttonPressed == 1 && !(boolean)ReflectionHacks.getPrivate(__instance, BetterPortalUpgrade.eventClass, "prism")) {
                    // Allow portal choice without consuming Prismatic Shard
                    ReflectionHacks.setPrivate(__instance, BetterPortalUpgrade.eventClass, "optionsChosen", "Chosen (Upgraded): ");
                    __instance.imageEventText.updateBodyText(DESCRIPTIONS[7]);  // DIALOG_CHOICE

                    // Set screen to CHOICE
                    try {
                        for (Class<?> innerClass : BetterPortalUpgrade.eventClass.getDeclaredClasses()) {
                            if (innerClass.getSimpleName().equals("CurScreen")) {
                                Object choiceScreen = Enum.valueOf((Class<Enum>) innerClass, "CHOICE");
                                ReflectionHacks.setPrivate(__instance, BetterPortalUpgrade.eventClass, "screen", choiceScreen);
                                break;
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    __instance.imageEventText.clearAllDialogs();
                    __instance.imageEventText.setDialogOption(OPTIONS[5] + "#|#FF0000" + OPTIONS[6] + "#|#FF0000" + OPTIONS[7]);
                    __instance.imageEventText.setDialogOption(OPTIONS[5] + "#|#FF7F00" + OPTIONS[6] + "#|#FF7F00" + OPTIONS[7]);
                    __instance.imageEventText.setDialogOption(OPTIONS[5] + "#|#FFFF00" + OPTIONS[6] + "#|#FFFF00" + OPTIONS[7]);
                    __instance.imageEventText.setDialogOption(OPTIONS[5] + "#|#00FF00" + OPTIONS[6] + "#|#00FF00" + OPTIONS[7]);
                    __instance.imageEventText.setDialogOption(OPTIONS[5] + "#|#0000FF" + OPTIONS[6] + "#|#0000FF" + OPTIONS[7]);
                    __instance.imageEventText.setDialogOption(OPTIONS[5] + "#|#8B00FF" + OPTIONS[6] + "#|#8B00FF" + OPTIONS[7]);

                    return SpireReturn.Return(null);
                }
            }
        }
        return SpireReturn.Continue();
    }

    @SpirePostfixPatch
    public static void Postfix(AbstractEvent __instance, int buttonPressed) {
        if (BetterPortalUpgrade.isBetterThirdLoaded) {
            ButtonEffectHelper.updateUpgradeAvailability();
        }
    }
}