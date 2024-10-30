package upgradeableevents.patches.TheBeyond;

import basemod.ReflectionHacks;
import com.badlogic.gdx.graphics.Texture;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.events.AbstractEvent;
import com.megacrit.cardcrawl.events.beyond.MysteriousSphere;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import javassist.CtBehavior;
import upgradeableevents.UpgradeEventManager;
import upgradeableevents.eventupgrades.TheBeyond.MysteriousSphereUpgrade;
import upgradeableevents.interfaces.AbstractEventUpgrade;
import upgradeableevents.util.ButtonEffectHelper;

@SpirePatch(clz = MysteriousSphere.class, method = "buttonEffect")
public class MysteriousSphereButtonEffectPatch {

    @SpireInsertPatch(
            locator = Locator.class
    )
    public static SpireReturn<Void> Insert(MysteriousSphere __instance, int buttonPressed) {
        AbstractEventUpgrade currentUpgrade = UpgradeEventManager.getCurrentUpgrade();
        if (currentUpgrade instanceof MysteriousSphereUpgrade && ((MysteriousSphereUpgrade)currentUpgrade).isUpgraded()) {
            Enum<?> screen = ReflectionHacks.getPrivate(__instance, MysteriousSphere.class, "screen");

            if ("PRE_COMBAT".equals(screen.name())) {
                // Double the gold reward
                int goldReward;
                if (Settings.isDailyRun) {
                    goldReward = AbstractDungeon.miscRng.random(50) * 2;
                } else {
                    goldReward = AbstractDungeon.miscRng.random(45, 55) * 2;
                }

                AbstractDungeon.getCurrRoom().addGoldToRewards(goldReward);
                AbstractDungeon.getCurrRoom().addRelicToRewards(AbstractDungeon.returnRandomScreenlessRelic(AbstractRelic.RelicTier.RARE));

                Texture currentImage = ReflectionHacks.getPrivate(__instance, AbstractEvent.class, "img");
                if (currentImage != null) {
                    currentImage.dispose();
                    ReflectionHacks.setPrivate(__instance, AbstractEvent.class, "img", null);
                }
                ReflectionHacks.setPrivate(__instance, AbstractEvent.class, "img", ImageMaster.loadImage("images/events/sphereOpen.png"));

                __instance.enterCombat();
                AbstractDungeon.lastCombatMetricKey = "2 Orb Walkers";
                return SpireReturn.Return(null);
            }
        }
        return SpireReturn.Continue();
    }

    private static class Locator extends SpireInsertLocator {
        @Override
        public int[] Locate(CtBehavior ctMethodToPatch) throws Exception {
            Matcher finalMatcher = new Matcher.FieldAccessMatcher(MysteriousSphere.class, "screen");
            return LineFinder.findInOrder(ctMethodToPatch, finalMatcher);
        }
    }

    @SpirePostfixPatch
    public static void Postfix(MysteriousSphere __instance, int buttonPressed) {
        ButtonEffectHelper.updateUpgradeAvailability();
    }
}