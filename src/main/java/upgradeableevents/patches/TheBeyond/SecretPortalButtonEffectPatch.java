package upgradeableevents.patches.TheBeyond;

import basemod.ReflectionHacks;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.events.AbstractEvent;
import com.megacrit.cardcrawl.events.beyond.SecretPortal;
import com.megacrit.cardcrawl.vfx.ObtainKeyEffect;
import javassist.CtBehavior;
import upgradeableevents.UpgradeEventManager;
import upgradeableevents.eventupgrades.TheBeyond.SecretPortalUpgrade;
import upgradeableevents.interfaces.AbstractEventUpgrade;
import upgradeableevents.util.ButtonEffectHelper;

import java.util.ArrayList;

@SpirePatch(clz = SecretPortal.class, method = "buttonEffect")
public class SecretPortalButtonEffectPatch {
    @SpireInsertPatch(
            locator = Locator.class
    )
    public static SpireReturn<Void> Insert(SecretPortal __instance, int buttonPressed) {
        AbstractEventUpgrade currentUpgrade = UpgradeEventManager.getCurrentUpgrade();
        if (currentUpgrade instanceof SecretPortalUpgrade && ((SecretPortalUpgrade)currentUpgrade).isUpgraded()) {
            Enum<?> screen = ReflectionHacks.getPrivate(__instance, SecretPortal.class, "screen");

            if ("INTRO".equals(screen.name()) && buttonPressed == 0) {
                // Heal to full
                int healAmount = AbstractDungeon.player.maxHealth - AbstractDungeon.player.currentHealth;
                if (healAmount > 0) {
                    AbstractDungeon.player.heal(healAmount, true);
                }

                // Get all keys
                AbstractDungeon.topLevelEffects.add(new ObtainKeyEffect(ObtainKeyEffect.KeyColor.RED));
                AbstractDungeon.topLevelEffects.add(new ObtainKeyEffect(ObtainKeyEffect.KeyColor.GREEN));
                AbstractDungeon.topLevelEffects.add(new ObtainKeyEffect(ObtainKeyEffect.KeyColor.BLUE));

                // Original portal effect
                __instance.imageEventText.updateBodyText(SecretPortal.DESCRIPTIONS[1]);

                // Set screen to ACCEPT
                Class<?> screenEnum = SecretPortal.class.getDeclaredClasses()[0];
                Object acceptScreen = Enum.valueOf((Class<Enum>) screenEnum, "ACCEPT");
                ReflectionHacks.setPrivate(__instance, SecretPortal.class, "screen", acceptScreen);

                __instance.imageEventText.updateDialogOption(0, SecretPortal.OPTIONS[1]);
                __instance.imageEventText.clearRemainingOptions();

                // Log metrics
                ArrayList<String> customStrings = new ArrayList<>();
                customStrings.add("Took Portal with Upgrade");
                AbstractEvent.logMetric("Secret Portal", "Took Portal with Upgrade", customStrings, null, null, null, null,
                        null, null, healAmount, 0, 0, 0, 0, 0);

                CardCrawlGame.screenShake.mildRumble(5.0F);
                CardCrawlGame.sound.play("ATTACK_MAGIC_SLOW_2");

                return SpireReturn.Return(null);
            }
        }
        return SpireReturn.Continue();
    }

    private static class Locator extends SpireInsertLocator {
        @Override
        public int[] Locate(CtBehavior ctMethodToPatch) throws Exception {
            Matcher finalMatcher = new Matcher.FieldAccessMatcher(SecretPortal.class, "screen");
            return LineFinder.findInOrder(ctMethodToPatch, finalMatcher);
        }
    }

    @SpirePostfixPatch
    public static void Postfix(SecretPortal __instance, int buttonPressed) {
        ButtonEffectHelper.updateUpgradeAvailability();
    }
}