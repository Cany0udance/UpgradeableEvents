package upgradeableevents.patches.TheBeyond;

import basemod.ReflectionHacks;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.events.AbstractEvent;
import com.megacrit.cardcrawl.events.beyond.Falling;
import com.megacrit.cardcrawl.vfx.cardManip.ShowCardAndObtainEffect;
import javassist.CtBehavior;
import upgradeableevents.UpgradeEventManager;
import upgradeableevents.eventupgrades.TheBeyond.FallingUpgrade;
import upgradeableevents.interfaces.AbstractEventUpgrade;
import upgradeableevents.util.ButtonEffectHelper;

@SpirePatch(clz = Falling.class, method = "buttonEffect")
public class FallingButtonEffectPatch {

    @SpireInsertPatch(
            locator = Locator.class
    )
    public static SpireReturn<Void> Insert(Falling __instance, int buttonPressed) {
        AbstractEventUpgrade currentUpgrade = UpgradeEventManager.getCurrentUpgrade();
        if (currentUpgrade instanceof FallingUpgrade && ((FallingUpgrade)currentUpgrade).isUpgraded()) {
            Enum<?> screen = ReflectionHacks.getPrivate(__instance, Falling.class, "screen");

            if ("CHOICE".equals(screen.name())) {
                boolean hasSkill = ReflectionHacks.getPrivate(__instance, Falling.class, "skill");
                boolean hasPower = ReflectionHacks.getPrivate(__instance, Falling.class, "power");
                boolean hasAttack = ReflectionHacks.getPrivate(__instance, Falling.class, "attack");
                AbstractCard skillCard = ReflectionHacks.getPrivate(__instance, Falling.class, "skillCard");
                AbstractCard powerCard = ReflectionHacks.getPrivate(__instance, Falling.class, "powerCard");
                AbstractCard attackCard = ReflectionHacks.getPrivate(__instance, Falling.class, "attackCard");

                // Set up for result screen
                Class<?> screenEnum = Falling.class.getDeclaredClasses()[0];
                Object resultScreen = Enum.valueOf((Class<Enum>) screenEnum, "RESULT");
                ReflectionHacks.setPrivate(__instance, Falling.class, "screen", resultScreen);

                __instance.imageEventText.clearAllDialogs();
                __instance.imageEventText.setDialogOption(Falling.OPTIONS[7]);

                switch (buttonPressed) {
                    case 0:
                        if (!hasSkill && !hasPower && !hasAttack) {
                            __instance.imageEventText.updateBodyText(Falling.DESCRIPTIONS[5]);
                            AbstractEvent.logMetricIgnored("Falling");
                        } else {
                            __instance.imageEventText.updateBodyText(Falling.DESCRIPTIONS[2]);
                            AbstractCard cardCopy = skillCard.makeStatEquivalentCopy();
                            AbstractDungeon.effectList.add(new ShowCardAndObtainEffect(cardCopy, Settings.WIDTH / 2.0F, Settings.HEIGHT / 2.0F));
                            AbstractEvent.logMetricObtainCard("Falling", "Obtained Skill", cardCopy);
                        }
                        return SpireReturn.Return(null);
                    case 1:
                        __instance.imageEventText.updateBodyText(Falling.DESCRIPTIONS[3]);
                        AbstractCard cardCopy = powerCard.makeStatEquivalentCopy();
                        AbstractDungeon.effectList.add(new ShowCardAndObtainEffect(cardCopy, Settings.WIDTH / 2.0F, Settings.HEIGHT / 2.0F));
                        AbstractEvent.logMetricObtainCard("Falling", "Obtained Power", cardCopy);
                        return SpireReturn.Return(null);
                    case 2:
                        __instance.imageEventText.updateBodyText(Falling.DESCRIPTIONS[4]);
                        AbstractCard cardCopy2 = attackCard.makeStatEquivalentCopy();
                        AbstractDungeon.effectList.add(new ShowCardAndObtainEffect(cardCopy2, Settings.WIDTH / 2.0F, Settings.HEIGHT / 2.0F));
                        AbstractEvent.logMetricObtainCard("Falling", "Obtained Attack", cardCopy2);
                        return SpireReturn.Return(null);
                }
            }
        }
        return SpireReturn.Continue();
    }

    private static class Locator extends SpireInsertLocator {
        @Override
        public int[] Locate(CtBehavior ctMethodToPatch) throws Exception {
            Matcher finalMatcher = new Matcher.FieldAccessMatcher(Falling.class, "screen");
            return LineFinder.findInOrder(ctMethodToPatch, finalMatcher);
        }
    }

    @SpirePostfixPatch
    public static void Postfix(Falling __instance, int buttonPressed) {
        ButtonEffectHelper.updateUpgradeAvailability();
    }
}