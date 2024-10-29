package upgradeableevents.patches.TheCity;

import basemod.ReflectionHacks;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.events.AbstractEvent;
import com.megacrit.cardcrawl.events.shrines.Nloth;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.relics.Circlet;
import com.megacrit.cardcrawl.relics.NlothsGift;
import javassist.CtBehavior;
import upgradeableevents.UpgradeEventManager;
import upgradeableevents.eventupgrades.TheCity.NlothUpgrade;
import upgradeableevents.interfaces.AbstractEventUpgrade;
import upgradeableevents.util.ButtonEffectHelper;

import java.util.ArrayList;

@SpirePatch(clz = Nloth.class, method = "buttonEffect")
public class NlothButtonEffectPatch {
    @SpireInsertPatch(
            locator = Locator.class
    )
    public static SpireReturn<Void> Insert(Nloth __instance, int buttonPressed) {
        AbstractEventUpgrade currentUpgrade = UpgradeEventManager.getCurrentUpgrade();
        if (currentUpgrade instanceof NlothUpgrade && ((NlothUpgrade)currentUpgrade).isUpgraded()) {
            int screenNum = ReflectionHacks.getPrivate(__instance, Nloth.class, "screenNum");

            if (screenNum == 0) {
                NlothUpgrade upgrade = (NlothUpgrade)currentUpgrade;

                switch (buttonPressed) {
                    case 0:
                    case 1:
                        AbstractRelic giftRelic;
                        AbstractRelic bonusRelic;

                        // Determine which bonus relic to give based on choice
                        if (buttonPressed == 0) {
                            bonusRelic = ReflectionHacks.getPrivate(upgrade, NlothUpgrade.class, "bonusRelic1");
                        } else {
                            bonusRelic = ReflectionHacks.getPrivate(upgrade, NlothUpgrade.class, "bonusRelic2");
                        }

                        // Handle N'loth's Gift (or Circlet if already have it)
                        if (AbstractDungeon.player.hasRelic("Nloth's Gift")) {
                            giftRelic = new Circlet();
                        } else {
                            giftRelic = new NlothsGift();
                        }

                        __instance.imageEventText.updateBodyText(Nloth.DESCRIPTIONS[1]); // DIALOG_2

                        // Give both relics
                        AbstractDungeon.getCurrRoom().spawnRelicAndObtain(Settings.WIDTH / 3.0F, Settings.HEIGHT / 2.0F, bonusRelic);
                        AbstractDungeon.getCurrRoom().spawnRelicAndObtain(Settings.WIDTH * 2.0F / 3.0F, Settings.HEIGHT / 2.0F, giftRelic);

                        // Log metrics
                        ArrayList<String> relics = new ArrayList<>();
                        relics.add(giftRelic.relicId);
                        relics.add(bonusRelic.relicId);
                        AbstractEvent.logMetric("N'loth", "Accepted Offer (Upgraded)", relics, null, null, null, null, null, null, 0, 0, 0, 0, 0, 0);

                        // Update screen
                        ReflectionHacks.setPrivate(__instance, Nloth.class, "screenNum", 1);
                        __instance.imageEventText.updateDialogOption(0, Nloth.OPTIONS[2]);
                        __instance.imageEventText.clearRemainingOptions();

                        return SpireReturn.Return(null);

                    case 2:
                        AbstractEvent.logMetricIgnored("N'loth");
                        __instance.imageEventText.updateBodyText(Nloth.DESCRIPTIONS[2]); // DIALOG_3
                        ReflectionHacks.setPrivate(__instance, Nloth.class, "screenNum", 1);
                        __instance.imageEventText.updateDialogOption(0, Nloth.OPTIONS[2]);
                        __instance.imageEventText.clearRemainingOptions();
                        return SpireReturn.Return(null);
                }
            }
        }
        return SpireReturn.Continue();
    }

    private static class Locator extends SpireInsertLocator {
        @Override
        public int[] Locate(CtBehavior ctMethodToPatch) throws Exception {
            Matcher finalMatcher = new Matcher.FieldAccessMatcher(Nloth.class, "screenNum");
            return LineFinder.findInOrder(ctMethodToPatch, finalMatcher);
        }
    }

    @SpirePostfixPatch
    public static void Postfix(Nloth __instance, int buttonPressed) {
        ButtonEffectHelper.updateUpgradeAvailability();
    }
}