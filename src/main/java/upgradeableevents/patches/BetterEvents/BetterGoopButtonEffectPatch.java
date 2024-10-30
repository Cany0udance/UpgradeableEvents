package upgradeableevents.patches.BetterEvents;

import basemod.ReflectionHacks;
import betterThird.relics.SlimedRelic;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.events.AbstractEvent;
import com.megacrit.cardcrawl.vfx.RainingGoldEffect;
import com.megacrit.cardcrawl.vfx.combat.FlashAtkImgEffect;
import upgradeableevents.UpgradeEventManager;
import upgradeableevents.eventupgrades.BetterEvents.BetterGoopUpgrade;
import upgradeableevents.interfaces.AbstractEventUpgrade;
import upgradeableevents.util.ButtonEffectHelper;

@SpirePatch(
        optional = true,
        cls = "betterThird.events.BetterGoopEvent",
        method = "buttonEffect"
)
public class BetterGoopButtonEffectPatch {
    @SpirePrefixPatch
    public static SpireReturn<Void> Prefix(AbstractEvent __instance, int buttonPressed) {
        if (!BetterGoopUpgrade.isBetterThirdLoaded) return SpireReturn.Continue();

        AbstractEventUpgrade currentUpgrade = UpgradeEventManager.getCurrentUpgrade();
        if (currentUpgrade instanceof BetterGoopUpgrade && ((BetterGoopUpgrade)currentUpgrade).isUpgraded()) {
            Enum<?> screen = ReflectionHacks.getPrivate(__instance, BetterGoopUpgrade.eventClass, "screen");

            if ("INTRO".equals(screen.name())) {
                int gold = ReflectionHacks.getPrivate(__instance, BetterGoopUpgrade.eventClass, "gold");
                int damage = ReflectionHacks.getPrivate(__instance, BetterGoopUpgrade.eventClass, "damage");
                boolean hasBag = ReflectionHacks.getPrivate(__instance, BetterGoopUpgrade.eventClass, "bag");

                String GOLD_DIALOG = ReflectionHacks.getPrivateStatic(BetterGoopUpgrade.eventClass, "GOLD_DIALOG");
                String RELIC_DIALOG = ReflectionHacks.getPrivateStatic(BetterGoopUpgrade.eventClass, "RELIC_DIALOG");
                String LEAVE_DIALOG = ReflectionHacks.getPrivateStatic(BetterGoopUpgrade.eventClass, "LEAVE_DIALOG");
                String[] OPTIONS = ReflectionHacks.getPrivateStatic(BetterGoopUpgrade.eventClass, "OPTIONS");

                switch (buttonPressed) {
                    case 0: // Gold only, no damage
                        __instance.imageEventText.updateBodyText(GOLD_DIALOG);
                        AbstractDungeon.effectList.add(new RainingGoldEffect(gold));
                        AbstractDungeon.player.gainGold(gold);
                        AbstractEvent.logMetric("Better World of Goop", "Gold (Upgraded)", null, null, null, null, null, null, null, 0, 0, 0, hasBag ? 1 : 0, gold, 0);
                        break;

                    case 1: // Relic with damage but no gold loss if no bag
                        __instance.imageEventText.updateBodyText(RELIC_DIALOG);
                        if (hasBag) {
                            int goldLoss = ReflectionHacks.getPrivate(__instance, BetterGoopUpgrade.eventClass, "goldLoss");
                            AbstractDungeon.player.loseGold(goldLoss);
                        }
                        AbstractDungeon.player.damage(new DamageInfo(AbstractDungeon.player, damage));
                        AbstractDungeon.effectList.add(new FlashAtkImgEffect(AbstractDungeon.player.hb.cX, AbstractDungeon.player.hb.cY, AbstractGameAction.AttackEffect.FIRE));
                        AbstractDungeon.getCurrRoom().spawnRelicAndObtain(Settings.WIDTH / 2.0f, Settings.HEIGHT / 2.0f, new SlimedRelic());
                        AbstractEvent.logMetric("Better World of Goop", "Slimed (Upgraded)", null, null, null, null, null, null, null, damage, 0, 0, hasBag ? 1 : 0, 0, hasBag ? ReflectionHacks.getPrivate(__instance, BetterGoopUpgrade.eventClass, "goldLoss") : 0);
                        break;

                    case 2: // Leave with no gold loss if no bag
                        __instance.imageEventText.updateBodyText(LEAVE_DIALOG);
                        if (hasBag) {
                            int goldLoss = ReflectionHacks.getPrivate(__instance, BetterGoopUpgrade.eventClass, "goldLoss");
                            AbstractDungeon.player.loseGold(goldLoss);
                        }
                        AbstractEvent.logMetric("Better World of Goop", "Left (Upgraded)", null, null, null, null, null, null, null, 0, 0, 0, hasBag ? 1 : 0, 0, hasBag ? ReflectionHacks.getPrivate(__instance, BetterGoopUpgrade.eventClass, "goldLoss") : 0);
                        break;
                }

                __instance.imageEventText.clearAllDialogs();
                __instance.imageEventText.setDialogOption(OPTIONS[5]);

                // Set screen to RESULT using reflection
                try {
                    for (Class<?> innerClass : BetterGoopUpgrade.eventClass.getDeclaredClasses()) {
                        if (innerClass.getSimpleName().equals("CurScreen")) {
                            Object resultScreen = Enum.valueOf((Class<Enum>) innerClass, "RESULT");
                            ReflectionHacks.setPrivate(__instance, BetterGoopUpgrade.eventClass, "screen", resultScreen);
                            break;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return SpireReturn.Return(null);
            }
        }
        return SpireReturn.Continue();
    }

    @SpirePostfixPatch
    public static void Postfix(AbstractEvent __instance, int buttonPressed) {
        if (BetterGoopUpgrade.isBetterThirdLoaded) {
            ButtonEffectHelper.updateUpgradeAvailability();
        }
    }
}