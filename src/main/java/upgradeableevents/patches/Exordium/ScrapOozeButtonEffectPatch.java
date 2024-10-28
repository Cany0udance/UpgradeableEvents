package upgradeableevents.patches.Exordium;

import basemod.BaseMod;
import basemod.ReflectionHacks;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.events.AbstractEvent;
import com.megacrit.cardcrawl.events.exordium.ScrapOoze;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import javassist.CtBehavior;
import org.apache.logging.log4j.Logger;
import upgradeableevents.UpgradeEventManager;
import upgradeableevents.eventupgrades.Exordium.ScrapOozeUpgrade;
import upgradeableevents.interfaces.AbstractEventUpgrade;
import upgradeableevents.util.ButtonEffectHelper;

@SpirePatch(clz = ScrapOoze.class, method = "buttonEffect")
public class ScrapOozeButtonEffectPatch {
    @SpireInsertPatch(
            rloc = 1
    )
    public static SpireReturn<Void> Insert(ScrapOoze __instance, int buttonPressed) {
        AbstractEventUpgrade currentUpgrade = UpgradeEventManager.getCurrentUpgrade();

        if (!(currentUpgrade instanceof ScrapOozeUpgrade) ||
                !((ScrapOozeUpgrade)currentUpgrade).isUpgraded()) {
            return SpireReturn.Continue();
        }

        ScrapOozeUpgrade upgrade = (ScrapOozeUpgrade)currentUpgrade;

        // Get private fields
        int screenNum = ReflectionHacks.getPrivate(__instance, ScrapOoze.class, "screenNum");
        int relicObtainChance = ReflectionHacks.getPrivate(__instance, ScrapOoze.class, "relicObtainChance");
        int dmg = ReflectionHacks.getPrivate(__instance, ScrapOoze.class, "dmg");
        int totalDamageDealt = ReflectionHacks.getPrivate(__instance, ScrapOoze.class, "totalDamageDealt");

        if (screenNum == 0) {
            switch (buttonPressed) {
                case 0:
                    AbstractDungeon.player.damage(new DamageInfo(null, dmg));
                    CardCrawlGame.sound.play("ATTACK_POISON");
                    totalDamageDealt += dmg;
                    ReflectionHacks.setPrivate(__instance, ScrapOoze.class, "totalDamageDealt", totalDamageDealt);

                    int random = AbstractDungeon.miscRng.random(0, 99);

                    if (random >= 99 - relicObtainChance) {
                        upgrade.incrementRelicsObtained();

                        AbstractRelic r = AbstractDungeon.returnRandomScreenlessRelic(AbstractDungeon.returnRandomRelicTier());
                        AbstractDungeon.getCurrRoom().spawnRelicAndObtain(Settings.WIDTH / 2.0F, Settings.HEIGHT / 2.0F, r);
                        AbstractEvent.logMetricObtainRelicAndDamage("Scrap Ooze", "Success", r, totalDamageDealt);

                        // Check if this was the second relic
                        if (upgrade.getRelicsObtained() >= 2) {
                            // End the event as normal
                            ReflectionHacks.setPrivate(__instance, ScrapOoze.class, "screenNum", 1);
                            __instance.imageEventText.updateBodyText(ScrapOoze.DESCRIPTIONS[2]);
                            __instance.imageEventText.updateDialogOption(0, ScrapOoze.OPTIONS[3]);
                            __instance.imageEventText.removeDialogOption(1);
                        } else {
                            // Continue with increased damage and chance as if we failed
                            __instance.imageEventText.updateBodyText(ScrapOoze.DESCRIPTIONS[2]);
                            ReflectionHacks.setPrivate(__instance, ScrapOoze.class, "relicObtainChance", relicObtainChance + 10);
                            ReflectionHacks.setPrivate(__instance, ScrapOoze.class, "dmg", dmg + 1);

                            __instance.imageEventText.updateDialogOption(0, ScrapOoze.OPTIONS[4] + (dmg + 1) +
                                    ScrapOoze.OPTIONS[1] + (relicObtainChance + 10) + ScrapOoze.OPTIONS[2]);
                            __instance.imageEventText.updateDialogOption(1, ScrapOoze.OPTIONS[3]);
                        }

                        return SpireReturn.Return(null);
                    } else {
                        __instance.imageEventText.updateBodyText(ScrapOoze.DESCRIPTIONS[1]);
                        ReflectionHacks.setPrivate(__instance, ScrapOoze.class, "relicObtainChance", relicObtainChance + 10);
                        ReflectionHacks.setPrivate(__instance, ScrapOoze.class, "dmg", dmg + 1);

                        __instance.imageEventText.updateDialogOption(0, ScrapOoze.OPTIONS[4] + (dmg + 1) +
                                ScrapOoze.OPTIONS[1] + (relicObtainChance + 10) + ScrapOoze.OPTIONS[2]);
                        return SpireReturn.Return(null);
                    }
            }
        }

        return SpireReturn.Continue();
    }

    @SpirePostfixPatch
    public static void Postfix(ScrapOoze __instance, int buttonPressed) {
        ButtonEffectHelper.updateUpgradeAvailability();
    }
}