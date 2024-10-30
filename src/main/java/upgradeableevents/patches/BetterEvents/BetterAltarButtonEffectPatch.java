package upgradeableevents.patches.BetterEvents;

import basemod.ReflectionHacks;
import betterAltar.potions.AltarPotion;
import betterAltar.relics.BloodRelic;
import betterAltar.util.AbstractEventDialog;
import com.badlogic.gdx.math.MathUtils;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.events.AbstractEvent;
import com.megacrit.cardcrawl.helpers.ScreenShake;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.relics.BloodVial;
import com.megacrit.cardcrawl.relics.BloodyIdol;
import com.megacrit.cardcrawl.relics.Circlet;
import com.megacrit.cardcrawl.vfx.ObtainPotionEffect;
import upgradeableevents.UpgradeEventManager;
import upgradeableevents.eventupgrades.BetterEvents.BetterAltarUpgrade;
import upgradeableevents.interfaces.AbstractEventUpgrade;
import upgradeableevents.util.ButtonEffectHelper;

@SpirePatch(
        optional = true,
        cls = "betterAltar.events.BetterAltarEvent",
        method = "buttonEffect"
)
public class BetterAltarButtonEffectPatch {
    @SpirePrefixPatch
    public static SpireReturn<Void> Prefix(AbstractEvent __instance, int buttonPressed) {
        if (!BetterAltarUpgrade.isBetterAltarLoaded) return SpireReturn.Continue();

        AbstractEventUpgrade currentUpgrade = UpgradeEventManager.getCurrentUpgrade();
        if (currentUpgrade instanceof BetterAltarUpgrade && ((BetterAltarUpgrade)currentUpgrade).isUpgraded()) {
            int screenNum = ReflectionHacks.getPrivate(__instance, AbstractEvent.class, "screenNum");

            if (screenNum == 0) {
                boolean idol = ReflectionHacks.getPrivate(__instance, BetterAltarUpgrade.eventClass, "idol");
                boolean vial = ReflectionHacks.getPrivate(__instance, BetterAltarUpgrade.eventClass, "vial");
                boolean curse = ReflectionHacks.getPrivate(__instance, BetterAltarUpgrade.eventClass, "curse");
                AbstractEventDialog eventText = ReflectionHacks.getPrivate(__instance, BetterAltarUpgrade.eventClass, "EventText");

                switch (buttonPressed) {
                    case 0: // Bloody Idol option
                        if (idol) {
                            if (AbstractDungeon.player.hasRelic("Bloody Idol")) {
                                AbstractDungeon.getCurrRoom().spawnRelicAndObtain(
                                        Settings.WIDTH / 2.0f,
                                        Settings.HEIGHT / 2.0f,
                                        new Circlet()
                                );
                            } else {
                                AbstractRelic bloodyIdol = new BloodyIdol();
                                bloodyIdol.instantObtain();
                            }
                            CardCrawlGame.sound.play("HEAL_1");
                        } else {
                            int reducedHpLoss = MathUtils.round(AbstractDungeon.player.maxHealth * 0.25F);
                            AbstractDungeon.player.damage(new DamageInfo(null, reducedHpLoss));
                            AbstractDungeon.getCurrRoom().spawnRelicAndObtain(
                                    Settings.WIDTH / 2.0f,
                                    Settings.HEIGHT / 2.0f,
                                    new BloodyIdol()
                            );
                        }
                        updateDialogs(__instance, buttonPressed);
                        return SpireReturn.Return(null);

                    case 1: // Blood Relic option
                        if (vial) {
                            if (AbstractDungeon.player.hasRelic("Blood Relic")) {
                                AbstractDungeon.getCurrRoom().spawnRelicAndObtain(
                                        Settings.WIDTH / 2.0f,
                                        Settings.HEIGHT / 2.0f,
                                        new Circlet()
                                );
                            } else {
                                int relicIndex = -1;
                                for (int i = 0; i < AbstractDungeon.player.relics.size(); i++) {
                                    if (AbstractDungeon.player.relics.get(i).relicId.equals("Blood Vial")) {
                                        relicIndex = i;
                                        break;
                                    }
                                }
                                if (relicIndex >= 0) {
                                    AbstractDungeon.player.relics.get(relicIndex).onUnequip();
                                    AbstractRelic relic = new BloodRelic();
                                    relic.instantObtain(AbstractDungeon.player, relicIndex, false);
                                }
                            }
                        } else {
                            int reducedHpLoss = MathUtils.round(AbstractDungeon.player.maxHealth * 0.13F);
                            AbstractDungeon.player.damage(new DamageInfo(null, reducedHpLoss));
                            AbstractDungeon.getCurrRoom().spawnRelicAndObtain(
                                    Settings.WIDTH / 2.0f,
                                    Settings.HEIGHT / 2.0f,
                                    new BloodVial()
                            );
                        }
                        updateDialogs(__instance, buttonPressed);
                        return SpireReturn.Return(null);

                    case 2: // Altar Potion option
                        int reducedHpLoss = MathUtils.round(AbstractDungeon.player.maxHealth * 0.12F);
                        AbstractDungeon.player.damage(new DamageInfo(null, reducedHpLoss));
                        AbstractDungeon.effectList.add(new ObtainPotionEffect(new AltarPotion()));
                        updateDialogs(__instance, buttonPressed);
                        return SpireReturn.Return(null);

                    case 3: // Defy option (replaced with small HP loss)
                        if (curse) {
                            int smallHpLoss = MathUtils.round(AbstractDungeon.player.maxHealth * 0.05F);
                            AbstractDungeon.player.decreaseMaxHealth(smallHpLoss);
                            CardCrawlGame.sound.play("BLUNT_HEAVY");
                            CardCrawlGame.screenShake.shake(ScreenShake.ShakeIntensity.HIGH, ScreenShake.ShakeDur.MED, true);
                            updateDialogs(__instance, buttonPressed);
                            return SpireReturn.Return(null);
                        }
                        break;
                }
            }
        }
        return SpireReturn.Continue();
    }

    private static void updateDialogs(AbstractEvent __instance, int option) {
        String[] DESCRIPTIONS = ReflectionHacks.getPrivateStatic(BetterAltarUpgrade.eventClass, "DESCRIPTIONS");
        String[] OPTIONS = ReflectionHacks.getPrivateStatic(BetterAltarUpgrade.eventClass, "OPTIONS");

        boolean idol = ReflectionHacks.getPrivate(__instance, BetterAltarUpgrade.eventClass, "idol");
        boolean vial = ReflectionHacks.getPrivate(__instance, BetterAltarUpgrade.eventClass, "vial");
        boolean curse = ReflectionHacks.getPrivate(__instance, BetterAltarUpgrade.eventClass, "curse");

        AbstractEventDialog eventText = ReflectionHacks.getPrivate(__instance, BetterAltarUpgrade.eventClass, "EventText");

        switch (option) {
            case 0:
                ReflectionHacks.setPrivate(__instance, BetterAltarUpgrade.eventClass, "curse", false);
                eventText.updateBodyText(idol ? DESCRIPTIONS[5] : DESCRIPTIONS[1]);
                eventText.updateDialogOption(option, OPTIONS[6], true);
                eventText.updateDialogOption(3, OPTIONS[7]);
                break;
            case 1:
                ReflectionHacks.setPrivate(__instance, BetterAltarUpgrade.eventClass, "curse", false);
                eventText.updateBodyText(vial ? DESCRIPTIONS[6] : DESCRIPTIONS[2]);
                eventText.updateDialogOption(option, OPTIONS[6], true);
                eventText.updateDialogOption(3, OPTIONS[7]);
                break;
            case 2:
                ReflectionHacks.setPrivate(__instance, BetterAltarUpgrade.eventClass, "curse", false);
                eventText.updateBodyText(DESCRIPTIONS[3]);
                eventText.updateDialogOption(option, OPTIONS[6], true);
                eventText.updateDialogOption(3, OPTIONS[7]);
                break;
            case 3:
                eventText.updateBodyText(curse ? DESCRIPTIONS[4] : DESCRIPTIONS[7]);
                eventText.updateDialogOption(0, OPTIONS[4]);
                eventText.clearRemainingOptions();
                ReflectionHacks.setPrivate(__instance, AbstractEvent.class, "screenNum", 99);
                break;
        }
    }

    @SpirePostfixPatch
    public static void Postfix(AbstractEvent __instance, int buttonPressed) {
        if (BetterAltarUpgrade.isBetterAltarLoaded) {
            ButtonEffectHelper.updateUpgradeAvailability();
        }
    }
}