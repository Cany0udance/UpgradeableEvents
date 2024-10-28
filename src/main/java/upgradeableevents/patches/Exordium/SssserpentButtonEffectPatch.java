package upgradeableevents.patches.Exordium;

import basemod.BaseMod;
import basemod.ReflectionHacks;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.events.AbstractEvent;
import com.megacrit.cardcrawl.events.exordium.Sssserpent;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.relics.Courier;
import com.megacrit.cardcrawl.relics.MembershipCard;
import com.megacrit.cardcrawl.vfx.RainingGoldEffect;
import com.megacrit.cardcrawl.vfx.cardManip.ShowCardAndObtainEffect;
import javassist.CtBehavior;
import upgradeableevents.UpgradeEventManager;
import upgradeableevents.eventupgrades.Exordium.SssserpentUpgrade;
import upgradeableevents.interfaces.AbstractEventUpgrade;
import upgradeableevents.util.ButtonEffectHelper;

@SpirePatch(clz = Sssserpent.class, method = "buttonEffect")
public class SssserpentButtonEffectPatch {

    @SpirePatch(
            clz = Sssserpent.class,
            method = "buttonEffect"
    )
    public static class MainPatch {
        @SpireInsertPatch(
                locator = IntroLocator.class
        )
        public static SpireReturn<Void> PatchIntro(Sssserpent __instance, int buttonPressed) {
            AbstractEventUpgrade currentUpgrade = UpgradeEventManager.getCurrentUpgrade();

            if (!(currentUpgrade instanceof SssserpentUpgrade) ||
                    !((SssserpentUpgrade)currentUpgrade).isUpgraded()) {
                return SpireReturn.Continue();
            }

            if (buttonPressed == 0) {
                __instance.imageEventText.updateBodyText(Sssserpent.DESCRIPTIONS[1]);
                __instance.imageEventText.removeDialogOption(1);
                __instance.imageEventText.updateDialogOption(0, Sssserpent.OPTIONS[3]);

                try {
                    Class<?> curScreenEnum = Class.forName("com.megacrit.cardcrawl.events.exordium.Sssserpent$CUR_SCREEN");
                    ReflectionHacks.setPrivate(__instance, Sssserpent.class, "screen",
                            Enum.valueOf((Class<Enum>)curScreenEnum, "AGREE"));
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return SpireReturn.Return(null);
            }

            return SpireReturn.Continue();
        }

        @SpireInsertPatch(
                locator = AgreeLocator.class
        )
        public static SpireReturn<Void> PatchAgree(Sssserpent __instance, int buttonPressed) {
            AbstractEventUpgrade currentUpgrade = UpgradeEventManager.getCurrentUpgrade();

            if (!(currentUpgrade instanceof SssserpentUpgrade) ||
                    !((SssserpentUpgrade)currentUpgrade).isUpgraded()) {
                return SpireReturn.Continue();
            }

            AbstractCard curse = ReflectionHacks.getPrivate(__instance, Sssserpent.class, "curse");
            int goldReward = ReflectionHacks.getPrivate(__instance, Sssserpent.class, "goldReward");

            boolean hasMembershipCard = AbstractDungeon.player.hasRelic(MembershipCard.ID);
            boolean hasCourier = AbstractDungeon.player.hasRelic(Courier.ID);

            // Give curse first
            AbstractDungeon.effectList.add(new ShowCardAndObtainEffect(curse, Settings.WIDTH / 2.0F, Settings.HEIGHT / 2.0F));

            // Then give appropriate reward
            if (!hasMembershipCard) {
                AbstractRelic relic = new MembershipCard();
                AbstractDungeon.getCurrRoom().spawnRelicAndObtain(Settings.WIDTH / 2.0F, Settings.HEIGHT / 2.0F, relic);
             //   AbstractEvent.logMetricObtainRelicAndCard("Liars Game", "AGREE", relic, curse);
            } else if (!hasCourier) {
                AbstractRelic relic = new Courier();
                AbstractDungeon.getCurrRoom().spawnRelicAndObtain(Settings.WIDTH / 2.0F, Settings.HEIGHT / 2.0F, relic);
             //   AbstractEvent.logMetricObtainRelicAndCard("Liars Game", "AGREE", relic, curse);
            } else {
                AbstractDungeon.effectList.add(new RainingGoldEffect(goldReward * 2));
                AbstractDungeon.player.gainGold(goldReward * 2);
                AbstractEvent.logMetricGainGoldAndCard("Liars Game", "AGREE", curse, goldReward * 2);
            }

            __instance.imageEventText.updateBodyText(Sssserpent.DESCRIPTIONS[3]);
            __instance.imageEventText.updateDialogOption(0, Sssserpent.OPTIONS[4]);

            try {
                Class<?> curScreenEnum = Class.forName("com.megacrit.cardcrawl.events.exordium.Sssserpent$CUR_SCREEN");
                ReflectionHacks.setPrivate(__instance, Sssserpent.class, "screen",
                        Enum.valueOf((Class<Enum>)curScreenEnum, "COMPLETE"));
            } catch (Exception e) {
                e.printStackTrace();
            }

            return SpireReturn.Return(null);
        }

        private static class IntroLocator extends SpireInsertLocator {
            @Override
            public int[] Locate(CtBehavior ctMethodToPatch) throws Exception {
                Matcher finalMatcher = new Matcher.MethodCallMatcher(
                        AbstractEvent.class, "logMetricGainGoldAndCard"
                );
                return LineFinder.findInOrder(ctMethodToPatch, finalMatcher);
            }
        }

        private static class AgreeLocator extends SpireInsertLocator {
            @Override
            public int[] Locate(CtBehavior ctMethodToPatch) throws Exception {
                Matcher finalMatcher = new Matcher.FieldAccessMatcher(
                        AbstractDungeon.class, "effectList"
                );
                return LineFinder.findInOrder(ctMethodToPatch, finalMatcher);
            }
        }
    }

    @SpirePostfixPatch
    public static void Postfix(Sssserpent __instance, int buttonPressed) {
        ButtonEffectHelper.updateUpgradeAvailability();
    }
}