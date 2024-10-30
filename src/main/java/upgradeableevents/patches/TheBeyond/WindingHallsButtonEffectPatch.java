package upgradeableevents.patches.TheBeyond;

import basemod.ReflectionHacks;
import com.badlogic.gdx.math.MathUtils;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.colorless.Madness;
import com.megacrit.cardcrawl.cards.curses.Writhe;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.events.AbstractEvent;
import com.megacrit.cardcrawl.events.beyond.WindingHalls;
import com.megacrit.cardcrawl.helpers.ScreenShake;
import com.megacrit.cardcrawl.vfx.cardManip.ShowCardAndObtainEffect;
import upgradeableevents.UpgradeEventManager;
import upgradeableevents.eventupgrades.TheBeyond.WindingHallsUpgrade;
import upgradeableevents.interfaces.AbstractEventUpgrade;
import upgradeableevents.util.ButtonEffectHelper;

import java.util.ArrayList;
import java.util.List;

@SpirePatch(clz = WindingHalls.class, method = "buttonEffect")
public class WindingHallsButtonEffectPatch {
    @SpirePrefixPatch
    public static SpireReturn<Void> Prefix(WindingHalls __instance, int buttonPressed) {
        AbstractEventUpgrade currentUpgrade = UpgradeEventManager.getCurrentUpgrade();
        if (currentUpgrade instanceof WindingHallsUpgrade && ((WindingHallsUpgrade)currentUpgrade).isUpgraded()) {
            int screenNum = ReflectionHacks.getPrivate(__instance, WindingHalls.class, "screenNum");

            if (screenNum == 1) {
                switch (buttonPressed) {
                    case 0: // Madness without HP loss
                        List<String> cards = new ArrayList();
                        cards.add("Madness");
                        cards.add("Madness");
                        AbstractEvent.logMetric("Winding Halls", "Embrace Madness (Upgraded)", cards, null, null, null, null, null, null, 0, 0, 0, 0, 0, 0);

                        __instance.imageEventText.updateBodyText(WindingHalls.DESCRIPTIONS[2]);
                        AbstractDungeon.effectList.add(new ShowCardAndObtainEffect(
                                new Madness(),
                                Settings.WIDTH / 2.0F - 350.0F * Settings.xScale,
                                Settings.HEIGHT / 2.0F
                        ));
                        AbstractDungeon.effectList.add(new ShowCardAndObtainEffect(
                                new Madness(),
                                Settings.WIDTH / 2.0F + 350.0F * Settings.xScale,
                                Settings.HEIGHT / 2.0F
                        ));
                        break;

                    case 1: // Full heal
                        int healAmount = AbstractDungeon.player.maxHealth - AbstractDungeon.player.currentHealth;
                        __instance.imageEventText.updateBodyText(WindingHalls.DESCRIPTIONS[3]);
                        AbstractDungeon.player.heal(healAmount);
                        AbstractCard c = new Writhe();
                        AbstractDungeon.effectList.add(new ShowCardAndObtainEffect(
                                c,
                                Settings.WIDTH / 2.0F + 10.0F * Settings.xScale,
                                Settings.HEIGHT / 2.0F
                        ));
                        AbstractEvent.logMetricObtainCardAndHeal("Winding Halls", "Writhe (Upgraded)", c, healAmount);
                        break;

                    case 2: // Increase max HP
                        int maxHPAmt = MathUtils.round((float)AbstractDungeon.player.maxHealth * 0.05F);
                        __instance.imageEventText.updateBodyText(WindingHalls.DESCRIPTIONS[4]);
                        AbstractDungeon.player.increaseMaxHp(maxHPAmt, true);
                        AbstractEvent.logMetricMaxHPGain("Winding Halls", "Max HP (Upgraded)", maxHPAmt);
                        CardCrawlGame.screenShake.shake(ScreenShake.ShakeIntensity.LOW, ScreenShake.ShakeDur.SHORT, true);
                        break;
                }

                // Common cleanup for all options
                ReflectionHacks.setPrivate(__instance, WindingHalls.class, "screenNum", 2);
                __instance.imageEventText.updateDialogOption(0, WindingHalls.OPTIONS[4]);
                __instance.imageEventText.clearRemainingOptions();

                return SpireReturn.Return(null);
            }
        }
        return SpireReturn.Continue();
    }

    @SpirePostfixPatch
    public static void Postfix(WindingHalls __instance, int buttonPressed) {
        ButtonEffectHelper.updateUpgradeAvailability();
    }
}