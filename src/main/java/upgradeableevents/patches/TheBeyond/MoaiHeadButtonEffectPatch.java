package upgradeableevents.patches.TheBeyond;

import basemod.ReflectionHacks;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.events.AbstractEvent;
import com.megacrit.cardcrawl.events.beyond.MoaiHead;
import com.megacrit.cardcrawl.helpers.ScreenShake;
import com.megacrit.cardcrawl.relics.GoldenIdol;
import com.megacrit.cardcrawl.vfx.RainingGoldEffect;
import javassist.CtBehavior;
import upgradeableevents.UpgradeEventManager;
import upgradeableevents.eventupgrades.TheBeyond.MoaiHeadUpgrade;
import upgradeableevents.interfaces.AbstractEventUpgrade;
import upgradeableevents.util.ButtonEffectHelper;

@SpirePatch(clz = MoaiHead.class, method = "buttonEffect")
public class MoaiHeadButtonEffectPatch {

    @SpireInsertPatch(
            locator = Locator.class
    )
    public static SpireReturn<Void> Insert(MoaiHead __instance, int buttonPressed) {
        AbstractEventUpgrade currentUpgrade = UpgradeEventManager.getCurrentUpgrade();
        if (currentUpgrade instanceof MoaiHeadUpgrade && ((MoaiHeadUpgrade)currentUpgrade).isUpgraded()) {
            int screenNum = ReflectionHacks.getPrivate(__instance, MoaiHead.class, "screenNum");

            if (screenNum == 0) {
                switch (buttonPressed) {
                    case 0:
                        // Heal to full without max HP loss
                        __instance.imageEventText.updateBodyText(MoaiHead.DESCRIPTIONS[1]);
                        CardCrawlGame.screenShake.shake(ScreenShake.ShakeIntensity.HIGH, ScreenShake.ShakeDur.MED, true);
                        CardCrawlGame.sound.play("BLUNT_HEAVY");

                        int healAmount = AbstractDungeon.player.maxHealth - AbstractDungeon.player.currentHealth;
                        AbstractDungeon.player.heal(AbstractDungeon.player.maxHealth);

                        AbstractEvent.logMetricHeal("The Moai Head", "Healed", healAmount);
                        ReflectionHacks.setPrivate(__instance, MoaiHead.class, "screenNum", 1);
                        __instance.imageEventText.updateDialogOption(0, MoaiHead.OPTIONS[4]);
                        __instance.imageEventText.clearRemainingOptions();
                        return SpireReturn.Return(null);

                    case 1:
                        if (AbstractDungeon.player.hasRelic(GoldenIdol.ID)) {
                            // Give 444 gold instead of 333
                            AbstractEvent.logMetricGainGoldAndLoseRelic("The Moai Head", "Gave Idol", new GoldenIdol(), 444);
                            __instance.imageEventText.updateBodyText(MoaiHead.DESCRIPTIONS[2]);
                            ReflectionHacks.setPrivate(__instance, MoaiHead.class, "screenNum", 1);
                            AbstractDungeon.player.loseRelic(GoldenIdol.ID);
                            AbstractDungeon.effectList.add(new RainingGoldEffect(444));
                            AbstractDungeon.player.gainGold(444);
                            __instance.imageEventText.updateDialogOption(0, MoaiHead.OPTIONS[4]);
                            __instance.imageEventText.clearRemainingOptions();
                            return SpireReturn.Return(null);
                        }
                }
            }
        }
        return SpireReturn.Continue();
    }

    private static class Locator extends SpireInsertLocator {
        @Override
        public int[] Locate(CtBehavior ctMethodToPatch) throws Exception {
            Matcher finalMatcher = new Matcher.FieldAccessMatcher(MoaiHead.class, "screenNum");
            return LineFinder.findInOrder(ctMethodToPatch, finalMatcher);
        }
    }

    @SpirePostfixPatch
    public static void Postfix(MoaiHead __instance, int buttonPressed) {
        ButtonEffectHelper.updateUpgradeAvailability();
    }
}