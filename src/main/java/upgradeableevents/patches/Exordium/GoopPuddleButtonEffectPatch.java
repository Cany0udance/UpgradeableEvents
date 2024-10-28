package upgradeableevents.patches.Exordium;

import basemod.BaseMod;
import basemod.ReflectionHacks;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.events.AbstractEvent;
import com.megacrit.cardcrawl.events.exordium.Cleric;
import com.megacrit.cardcrawl.events.exordium.GoopPuddle;
import com.megacrit.cardcrawl.vfx.RainingGoldEffect;
import com.megacrit.cardcrawl.vfx.combat.FlashAtkImgEffect;
import javassist.CtBehavior;
import upgradeableevents.UpgradeEventManager;
import upgradeableevents.eventupgrades.Exordium.GoopPuddleUpgrade;
import upgradeableevents.interfaces.AbstractEventUpgrade;
import upgradeableevents.util.ButtonEffectHelper;

import static upgradeableevents.util.ButtonEffectHelper.updateUpgradeAvailability;

@SpirePatch(clz = GoopPuddle.class, method = "buttonEffect")
public class GoopPuddleButtonEffectPatch {
    private static final int UPGRADED_GOLD_LOSS = 1;

    @SpirePrefixPatch
    public static SpireReturn<Void> Prefix(GoopPuddle __instance, int buttonPressed) {

        AbstractEventUpgrade currentUpgrade = UpgradeEventManager.getCurrentUpgrade();

        if (!(currentUpgrade instanceof GoopPuddleUpgrade)) {
            return SpireReturn.Continue();
        }

        if (!((GoopPuddleUpgrade)currentUpgrade).isUpgraded()) {
            return SpireReturn.Continue();
        }

        Enum<?> screen = ReflectionHacks.getPrivate(__instance, GoopPuddle.class, "screen");

        if (screen.name().equals("INTRO")) {
            if (buttonPressed == 0) {
                // Handle the gold for HP option
                int damage = ReflectionHacks.getPrivate(__instance, GoopPuddle.class, "damage");
                int gold = ReflectionHacks.getPrivate(__instance, GoopPuddle.class, "gold");
                int upgradedGold = (int)(gold * 2.5f);

                __instance.imageEventText.updateBodyText(GoopPuddle.DESCRIPTIONS[1]);
                __instance.imageEventText.clearAllDialogs();
                AbstractDungeon.player.damage(new DamageInfo(AbstractDungeon.player, damage));
                AbstractDungeon.effectList.add(new FlashAtkImgEffect(AbstractDungeon.player.hb.cX, AbstractDungeon.player.hb.cY, AbstractGameAction.AttackEffect.FIRE));
                AbstractDungeon.effectList.add(new RainingGoldEffect(upgradedGold));
                AbstractDungeon.player.gainGold(upgradedGold);

                __instance.imageEventText.setDialogOption(GoopPuddle.OPTIONS[5]);

                try {
                    Class<?> curScreenEnum = Class.forName("com.megacrit.cardcrawl.events.exordium.GoopPuddle$CurScreen");
                    ReflectionHacks.setPrivate(__instance, GoopPuddle.class, "screen",
                            Enum.valueOf((Class<Enum>)curScreenEnum, "RESULT"));
                } catch (Exception e) {
                    e.printStackTrace();
                }

                AbstractEvent.logMetricGainGoldAndDamage("World of Goop", "Gather Gold", upgradedGold, damage);
                return SpireReturn.Return(null);
            } else if (buttonPressed == 1) {
                // Handle the lose gold option
                __instance.imageEventText.updateBodyText(GoopPuddle.DESCRIPTIONS[2]); // LEAVE_DIALOG is DESCRIPTIONS[2]
                AbstractDungeon.player.loseGold(UPGRADED_GOLD_LOSS);
                __instance.imageEventText.clearAllDialogs();
                __instance.imageEventText.setDialogOption(GoopPuddle.OPTIONS[5]);

                try {
                    Class<?> curScreenEnum = Class.forName("com.megacrit.cardcrawl.events.exordium.GoopPuddle$CurScreen");
                    ReflectionHacks.setPrivate(__instance, GoopPuddle.class, "screen",
                            Enum.valueOf((Class<Enum>)curScreenEnum, "RESULT"));
                } catch (Exception e) {
                    e.printStackTrace();
                }

                AbstractEvent.logMetricLoseGold("World of Goop", "Left Gold", UPGRADED_GOLD_LOSS);
                return SpireReturn.Return(null);
            }
        }

        return SpireReturn.Continue();
    }

    @SpirePostfixPatch
    public static void Postfix(GoopPuddle __instance, int buttonPressed) {
        ButtonEffectHelper.updateUpgradeAvailability();
    }
}