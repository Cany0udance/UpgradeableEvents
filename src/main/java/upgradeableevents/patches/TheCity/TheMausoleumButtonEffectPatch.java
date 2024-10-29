package upgradeableevents.patches.TheCity;

import basemod.ReflectionHacks;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.cards.curses.Writhe;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.events.AbstractEvent;
import com.megacrit.cardcrawl.events.city.TheMausoleum;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.vfx.cardManip.ShowCardAndObtainEffect;
import javassist.CtBehavior;
import upgradeableevents.UpgradeEventManager;
import upgradeableevents.eventupgrades.TheCity.TheMausoleumUpgrade;
import upgradeableevents.interfaces.AbstractEventUpgrade;
import upgradeableevents.util.ButtonEffectHelper;

@SpirePatch(clz = TheMausoleum.class, method = "buttonEffect")
public class TheMausoleumButtonEffectPatch {
    @SpireInsertPatch(
            locator = Locator.class
    )
    public static SpireReturn<Void> Insert(TheMausoleum __instance, int buttonPressed) {
        AbstractEventUpgrade currentUpgrade = UpgradeEventManager.getCurrentUpgrade();
        if (currentUpgrade instanceof TheMausoleumUpgrade && ((TheMausoleumUpgrade)currentUpgrade).isUpgraded()) {
            Enum<?> screen = ReflectionHacks.getPrivate(__instance, TheMausoleum.class, "screen");

            if ("INTRO".equals(screen.name()) && buttonPressed == 0) {
                // Check for curse (keeping original ascension logic)
                boolean result = AbstractDungeon.miscRng.randomBoolean();
                if (AbstractDungeon.ascensionLevel >= 15) {
                    result = true;
                }

                if (result) {
                    __instance.imageEventText.updateBodyText(TheMausoleum.DESCRIPTIONS[1]); // CURSED_RESULT
                    AbstractDungeon.effectList.add(new ShowCardAndObtainEffect(new Writhe(), Settings.WIDTH / 2.0F, Settings.HEIGHT / 2.0F));
                } else {
                    __instance.imageEventText.updateBodyText(TheMausoleum.DESCRIPTIONS[2]); // NORMAL_RESULT
                }

                CardCrawlGame.sound.play("BLUNT_HEAVY");
                CardCrawlGame.screenShake.rumble(2.0F);

                // Always get a Rare relic when upgraded
                AbstractRelic r = AbstractDungeon.returnRandomScreenlessRelic(AbstractRelic.RelicTier.RARE);
                AbstractDungeon.getCurrRoom().spawnRelicAndObtain(Settings.WIDTH / 2.0F, Settings.HEIGHT / 2.0F, r);

                if (result) {
                    AbstractEvent.logMetricObtainCardAndRelic("The Mausoleum", "Opened", new Writhe(), r);
                } else {
                    AbstractEvent.logMetricObtainRelic("The Mausoleum", "Opened", r);
                }

                __instance.imageEventText.clearAllDialogs();
                __instance.imageEventText.setDialogOption(TheMausoleum.OPTIONS[2]);

                // Set screen to RESULT using reflection
                Class<?> screenEnum = TheMausoleum.class.getDeclaredClasses()[0];
                Object resultScreen = Enum.valueOf((Class<Enum>) screenEnum, "RESULT");
                ReflectionHacks.setPrivate(__instance, TheMausoleum.class, "screen", resultScreen);

                return SpireReturn.Return(null);
            }
        }
        return SpireReturn.Continue();
    }

    private static class Locator extends SpireInsertLocator {
        @Override
        public int[] Locate(CtBehavior ctMethodToPatch) throws Exception {
            Matcher finalMatcher = new Matcher.FieldAccessMatcher(TheMausoleum.class, "screen");
            return LineFinder.findInOrder(ctMethodToPatch, finalMatcher);
        }
    }

    @SpirePostfixPatch
    public static void Postfix(TheMausoleum __instance, int buttonPressed) {
        ButtonEffectHelper.updateUpgradeAvailability();
    }
}