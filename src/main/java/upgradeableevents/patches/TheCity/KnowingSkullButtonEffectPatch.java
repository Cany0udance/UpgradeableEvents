package upgradeableevents.patches.TheCity;

import basemod.BaseMod;
import basemod.ReflectionHacks;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.events.city.KnowingSkull;
import com.megacrit.cardcrawl.helpers.PotionHelper;
import com.megacrit.cardcrawl.potions.AbstractPotion;
import com.megacrit.cardcrawl.vfx.RainingGoldEffect;
import com.megacrit.cardcrawl.vfx.cardManip.ShowCardAndObtainEffect;
import javassist.CtBehavior;
import upgradeableevents.UpgradeEventManager;
import upgradeableevents.eventupgrades.TheCity.KnowingSkullUpgrade;
import upgradeableevents.interfaces.AbstractEventUpgrade;
import upgradeableevents.util.ButtonEffectHelper;

import java.util.List;

@SpirePatch(clz = KnowingSkull.class, method = "buttonEffect")
public class KnowingSkullButtonEffectPatch {
    @SpireInsertPatch(
            locator = Locator.class
    )
    public static SpireReturn<Void> Insert(KnowingSkull __instance, int buttonPressed) {
        AbstractEventUpgrade currentUpgrade = UpgradeEventManager.getCurrentUpgrade();
        if (currentUpgrade instanceof KnowingSkullUpgrade && ((KnowingSkullUpgrade)currentUpgrade).isUpgraded()) {

            Enum<?> screen = ReflectionHacks.getPrivate(__instance, KnowingSkull.class, "screen");
            if ("ASK".equals(screen.name())) {
                KnowingSkullUpgrade upgrade = (KnowingSkullUpgrade)currentUpgrade;
                boolean wasUnused = !upgrade.hasUsedFreeOption(buttonPressed);

                if (wasUnused) {
                    upgrade.markOptionUsed(buttonPressed);

                    if (buttonPressed == 3) {
                        // Handle leave option
                        ReflectionHacks.privateMethod(AbstractEventUpgrade.class, "clearAndRebuildOptions")
                                .invoke(upgrade);
                        __instance.imageEventText.updateBodyText(KnowingSkull.DESCRIPTIONS[7]);
                        __instance.imageEventText.clearAllDialogs();
                        __instance.imageEventText.setDialogOption(KnowingSkull.OPTIONS[8]);

                        // Set screen to COMPLETE using the correct enum class (CurScreen)
                        Class<?>[] innerClasses = KnowingSkull.class.getDeclaredClasses();
                        Class<?> screenEnum = null;
                        for (Class<?> innerClass : innerClasses) {
                            if (innerClass.getSimpleName().equals("CurScreen")) {
                                screenEnum = innerClass;
                                break;
                            }
                        }
                        Object completeScreen = Enum.valueOf((Class<Enum>) screenEnum, "COMPLETE");
                        ReflectionHacks.setPrivate(__instance, KnowingSkull.class, "screen", completeScreen);

                        // Add to options chosen
                        String optionsChosen = ReflectionHacks.getPrivate(__instance, KnowingSkull.class, "optionsChosen");
                        ReflectionHacks.setPrivate(__instance, KnowingSkull.class, "optionsChosen", optionsChosen + "LEAVE ");
                    } else {
                        // Call obtainReward without taking damage
                        obtainRewardWithoutDamage(__instance, buttonPressed, upgrade);
                    }

                    return SpireReturn.Return(null);
                } else {
                }
            }
        }
        return SpireReturn.Continue();
    }

    private static void obtainRewardWithoutDamage(KnowingSkull __instance, int slot, KnowingSkullUpgrade upgrade) {
        switch (slot) {
            case 0: // Potion
                String optionsChosen = ReflectionHacks.getPrivate(__instance, KnowingSkull.class, "optionsChosen");
                ReflectionHacks.setPrivate(__instance, KnowingSkull.class, "optionsChosen", optionsChosen + "POTION ");
                __instance.imageEventText.updateBodyText(KnowingSkull.DESCRIPTIONS[4] + KnowingSkull.DESCRIPTIONS[2]);

                if (AbstractDungeon.player.hasRelic("Sozu")) {
                    AbstractDungeon.player.getRelic("Sozu").flash();
                } else {
                    AbstractPotion p = PotionHelper.getRandomPotion();
                    List<String> potions = ReflectionHacks.getPrivate(__instance, KnowingSkull.class, "potions");
                    potions.add(p.ID);
                    AbstractDungeon.player.obtainPotion(p);
                }
                break;

            case 1: // Gold
                optionsChosen = ReflectionHacks.getPrivate(__instance, KnowingSkull.class, "optionsChosen");
                ReflectionHacks.setPrivate(__instance, KnowingSkull.class, "optionsChosen", optionsChosen + "GOLD ");
                __instance.imageEventText.updateBodyText(KnowingSkull.DESCRIPTIONS[6] + KnowingSkull.DESCRIPTIONS[2]);
                AbstractDungeon.effectList.add(new RainingGoldEffect(90));
                AbstractDungeon.player.gainGold(90);
                int goldEarned = ReflectionHacks.getPrivate(__instance, KnowingSkull.class, "goldEarned");
                ReflectionHacks.setPrivate(__instance, KnowingSkull.class, "goldEarned", goldEarned + 90);
                break;

            case 2: // Card
                optionsChosen = ReflectionHacks.getPrivate(__instance, KnowingSkull.class, "optionsChosen");
                ReflectionHacks.setPrivate(__instance, KnowingSkull.class, "optionsChosen", optionsChosen + "CARD ");
                __instance.imageEventText.updateBodyText(KnowingSkull.DESCRIPTIONS[5] + KnowingSkull.DESCRIPTIONS[2]);
                AbstractCard c = AbstractDungeon.returnColorlessCard(AbstractCard.CardRarity.UNCOMMON).makeCopy();
                List<String> cards = ReflectionHacks.getPrivate(__instance, KnowingSkull.class, "cards");
                cards.add(c.cardID);
                AbstractDungeon.effectList.add(new ShowCardAndObtainEffect(c, Settings.WIDTH / 2.0F, Settings.HEIGHT / 2.0F));
                break;
        }

        ReflectionHacks.privateMethod(AbstractEventUpgrade.class, "clearAndRebuildOptions")
                .invoke(upgrade);
    }

    private static class Locator extends SpireInsertLocator {
        @Override
        public int[] Locate(CtBehavior ctMethodToPatch) throws Exception {
            Matcher finalMatcher = new Matcher.FieldAccessMatcher(KnowingSkull.class, "screen");
            return LineFinder.findInOrder(ctMethodToPatch, finalMatcher);
        }
    }

    @SpirePostfixPatch
    public static void Postfix(KnowingSkull __instance, int buttonPressed) {
        ButtonEffectHelper.updateUpgradeAvailability();
    }
}
