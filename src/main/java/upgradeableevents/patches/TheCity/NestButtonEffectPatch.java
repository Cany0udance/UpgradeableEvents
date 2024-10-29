package upgradeableevents.patches.TheCity;

import basemod.ReflectionHacks;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.cards.colorless.RitualDagger;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.events.AbstractEvent;
import com.megacrit.cardcrawl.events.city.Nest;
import com.megacrit.cardcrawl.vfx.RainingGoldEffect;
import com.megacrit.cardcrawl.vfx.cardManip.ShowCardAndObtainEffect;
import javassist.CtBehavior;
import upgradeableevents.UpgradeEventManager;
import upgradeableevents.eventupgrades.TheCity.NestUpgrade;
import upgradeableevents.interfaces.AbstractEventUpgrade;
import upgradeableevents.util.ButtonEffectHelper;

@SpirePatch(clz = Nest.class, method = "buttonEffect")
public class NestButtonEffectPatch {
    @SpireInsertPatch(
            locator = Locator.class
    )
    public static SpireReturn<Void> Insert(Nest __instance, int buttonPressed) {
        AbstractEventUpgrade currentUpgrade = UpgradeEventManager.getCurrentUpgrade();
        if (currentUpgrade instanceof NestUpgrade && ((NestUpgrade)currentUpgrade).isUpgraded()) {
            int screenNum = ReflectionHacks.getPrivate(__instance, Nest.class, "screenNum");

            if (screenNum == 1) {
                int goldGain = ReflectionHacks.getPrivate(__instance, Nest.class, "goldGain");

                switch (buttonPressed) {
                    case 0: // Gold option
                        AbstractEvent.logMetricGainGold("Nest", "Stole From Cult (Upgraded)", goldGain * 2);
                        __instance.imageEventText.updateBodyText(Nest.DESCRIPTIONS[3]); // EXIT_BODY
                        AbstractDungeon.effectList.add(new RainingGoldEffect(goldGain * 2));
                        AbstractDungeon.player.gainGold(goldGain * 2);
                        __instance.imageEventText.updateDialogOption(0, Nest.OPTIONS[4]);
                        __instance.imageEventText.clearRemainingOptions();
                        ReflectionHacks.setPrivate(__instance, Nest.class, "screenNum", 2);
                        return SpireReturn.Return(null);

                    case 1: // Ritual Dagger option
                        AbstractCard c = new RitualDagger();
                        c.upgrade();
                        AbstractEvent.logMetricObtainCardAndDamage("Nest", "Joined the Cult (Upgraded)", c, 1);
                        __instance.imageEventText.updateBodyText(Nest.DESCRIPTIONS[2]); // ACCEPT_BODY
                        AbstractDungeon.player.damage(new DamageInfo(null, 1));
                        AbstractDungeon.effectList.add(new ShowCardAndObtainEffect(c, Settings.WIDTH * 0.3F, Settings.HEIGHT / 2.0F));
                        __instance.imageEventText.updateDialogOption(0, Nest.OPTIONS[4]);
                        __instance.imageEventText.clearRemainingOptions();
                        ReflectionHacks.setPrivate(__instance, Nest.class, "screenNum", 2);
                        return SpireReturn.Return(null);
                }
            }
        }
        return SpireReturn.Continue();
    }

    private static class Locator extends SpireInsertLocator {
        @Override
        public int[] Locate(CtBehavior ctMethodToPatch) throws Exception {
            Matcher finalMatcher = new Matcher.FieldAccessMatcher(Nest.class, "screenNum");
            return LineFinder.findInOrder(ctMethodToPatch, finalMatcher);
        }
    }

    @SpirePostfixPatch
    public static void Postfix(Nest __instance, int buttonPressed) {
        ButtonEffectHelper.updateUpgradeAvailability();
    }
}