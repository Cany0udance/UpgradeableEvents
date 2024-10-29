package upgradeableevents.patches.TheCity;

import basemod.BaseMod;
import basemod.ReflectionHacks;
import com.badlogic.gdx.math.MathUtils;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.events.AbstractEvent;
import com.megacrit.cardcrawl.events.city.ForgottenAltar;
import com.megacrit.cardcrawl.helpers.RelicLibrary;
import com.megacrit.cardcrawl.helpers.ScreenShake;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.relics.BloodyIdol;
import com.megacrit.cardcrawl.relics.Circlet;
import javassist.CtBehavior;
import upgradeableevents.UpgradeEventManager;
import upgradeableevents.eventupgrades.TheCity.ForgottenAltarUpgrade;
import upgradeableevents.interfaces.AbstractEventUpgrade;
import upgradeableevents.util.ButtonEffectHelper;

import static basemod.BaseMod.logger;

@SpirePatch(clz = ForgottenAltar.class, method = "buttonEffect")
public class ForgottenAltarButtonEffectPatch {
    @SpireInsertPatch(
            locator = Locator.class
    )
    public static SpireReturn<Void> Insert(ForgottenAltar __instance, int buttonPressed) {

        AbstractEventUpgrade currentUpgrade = UpgradeEventManager.getCurrentUpgrade();
        if (currentUpgrade instanceof ForgottenAltarUpgrade && ((ForgottenAltarUpgrade)currentUpgrade).isUpgraded()) {
            int screenNum = (int)ReflectionHacks.getPrivate(__instance, AbstractEvent.class, "screenNum");

            if (screenNum == 0) {
                switch (buttonPressed) {
                    case 0:
                        if (AbstractDungeon.player.hasRelic("Golden Idol")) {
                            if (AbstractDungeon.player.hasRelic("Bloody Idol")) {
                                AbstractDungeon.getCurrRoom().spawnRelicAndObtain(
                                        Settings.WIDTH / 2.0f,
                                        Settings.HEIGHT / 2.0f,
                                        RelicLibrary.getRelic("Circlet").makeCopy()
                                );
                                AbstractEvent.logMetricObtainRelic("Forgotten Altar", "Kept Idol", new Circlet());
                            } else {
                                AbstractRelic bloodyIdol = RelicLibrary.getRelic("Bloody Idol").makeCopy();
                                bloodyIdol.instantObtain();
                                AbstractEvent.logMetricObtainRelic("Forgotten Altar", "Kept Idol", bloodyIdol);
                            }
                            CardCrawlGame.sound.play("HEAL_1");
                            __instance.showProceedScreen(ForgottenAltar.DESCRIPTIONS[1]);
                            return SpireReturn.Return(null);
                        }
                        break;

                    case 1:
                        int hpLoss = ReflectionHacks.getPrivate(__instance, ForgottenAltar.class, "hpLoss");
                        AbstractDungeon.player.increaseMaxHp(10, false);
                        AbstractDungeon.player.damage(new DamageInfo(null, hpLoss));
                        CardCrawlGame.sound.play("HEAL_3");
                        __instance.showProceedScreen(ForgottenAltar.DESCRIPTIONS[2]);
                        AbstractEvent.logMetricDamageAndMaxHPGain("Forgotten Altar", "Shed Blood", hpLoss, 10);
                        return SpireReturn.Return(null);

                    case 2:
                        int smallHpLoss = MathUtils.round(AbstractDungeon.player.maxHealth * 0.05F);
                        AbstractDungeon.player.decreaseMaxHealth(smallHpLoss);
                        CardCrawlGame.sound.play("BLUNT_HEAVY");
                        CardCrawlGame.screenShake.shake(ScreenShake.ShakeIntensity.HIGH, ScreenShake.ShakeDur.MED, true);
                        __instance.showProceedScreen(ForgottenAltar.DESCRIPTIONS[3]);
                        AbstractEvent.logMetricMaxHPLoss("Forgotten Altar", "Smashed Altar", smallHpLoss);
                        return SpireReturn.Return(null);
                }
            }
        }
        return SpireReturn.Continue();
    }

    private static class Locator extends SpireInsertLocator {
        @Override
        public int[] Locate(CtBehavior ctMethodToPatch) throws Exception {
            Matcher finalMatcher = new Matcher.FieldAccessMatcher(ForgottenAltar.class, "screenNum");
            int[] matches = LineFinder.findAllInOrder(ctMethodToPatch, finalMatcher);
            // Get the first instance where screenNum is accessed
            return new int[]{matches[0]};
        }
    }

    @SpirePostfixPatch
    public static void Postfix(ForgottenAltar __instance, int buttonPressed) {
        ButtonEffectHelper.updateUpgradeAvailability();
    }
}