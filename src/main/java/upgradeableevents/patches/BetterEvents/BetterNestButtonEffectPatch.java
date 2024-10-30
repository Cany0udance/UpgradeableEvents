package upgradeableevents.patches.BetterEvents;

import basemod.ReflectionHacks;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.cards.colorless.RitualDagger;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.events.AbstractEvent;
import com.megacrit.cardcrawl.vfx.RainingGoldEffect;
import com.megacrit.cardcrawl.vfx.cardManip.ShowCardAndObtainEffect;
import upgradeableevents.UpgradeEventManager;
import upgradeableevents.eventupgrades.BetterEvents.BetterNestUpgrade;
import upgradeableevents.interfaces.AbstractEventUpgrade;
import upgradeableevents.util.ButtonEffectHelper;

@SpirePatch(
        optional = true,
        cls = "betterThird.events.BetterNestEvent",
        method = "buttonEffect"
)
public class BetterNestButtonEffectPatch {
    @SpirePrefixPatch
    public static SpireReturn<Void> Prefix(AbstractEvent __instance, int buttonPressed) {
        if (!BetterNestUpgrade.isBetterThirdLoaded) return SpireReturn.Continue();

        AbstractEventUpgrade currentUpgrade = UpgradeEventManager.getCurrentUpgrade();
        if (currentUpgrade instanceof BetterNestUpgrade && ((BetterNestUpgrade)currentUpgrade).isUpgraded()) {
            Enum<?> screen = ReflectionHacks.getPrivate(__instance, BetterNestUpgrade.eventClass, "screen");

            if ("RESULT".equals(screen.name())) {
                int goldGain = ReflectionHacks.getPrivate(__instance, BetterNestUpgrade.eventClass, "goldGain");
                String[] DESCRIPTIONS = ReflectionHacks.getPrivateStatic(BetterNestUpgrade.eventClass, "DESCRIPTIONS");
                String[] OPTIONS = ReflectionHacks.getPrivateStatic(BetterNestUpgrade.eventClass, "OPTIONS");

                switch (buttonPressed) {
                    case 0: // Gold option
                        __instance.imageEventText.updateBodyText(DESCRIPTIONS[3]); // EXIT_BODY
                        AbstractDungeon.effectList.add(new RainingGoldEffect(goldGain * 2));
                        AbstractDungeon.player.gainGold(goldGain * 2);
                        AbstractEvent.logMetricGainGold("Better Nest", "Stole (Upgraded)", goldGain * 2);
                        break;
                    case 1: // Ritual Dagger option
                        AbstractCard c = new RitualDagger();
                        c.upgrade();
                        __instance.imageEventText.updateBodyText(DESCRIPTIONS[2]); // ACCEPT_BODY
                        AbstractDungeon.player.damage(new DamageInfo(null, 1));
                        AbstractDungeon.effectList.add(new ShowCardAndObtainEffect(c, Settings.WIDTH * 0.3F, Settings.HEIGHT / 2.0F));
                        AbstractEvent.logMetricObtainCardAndDamage("Better Nest", "Dagger (Upgraded)", c, 1);
                        break;
                    case 2: // Join option (unchanged)
                        return SpireReturn.Continue();
                }

                __instance.imageEventText.clearAllDialogs();
                __instance.imageEventText.setDialogOption(OPTIONS[4]);

                // Set screen to LEAVE using reflection
                try {
                    for (Class<?> innerClass : BetterNestUpgrade.eventClass.getDeclaredClasses()) {
                        if (innerClass.getSimpleName().equals("CurScreen")) {
                            Object leaveScreen = Enum.valueOf((Class<Enum>) innerClass, "LEAVE");
                            ReflectionHacks.setPrivate(__instance, BetterNestUpgrade.eventClass, "screen", leaveScreen);
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
        if (BetterNestUpgrade.isBetterThirdLoaded) {
            ButtonEffectHelper.updateUpgradeAvailability();
        }
    }
}