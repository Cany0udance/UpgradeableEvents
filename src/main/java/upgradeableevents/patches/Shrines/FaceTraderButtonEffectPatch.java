package upgradeableevents.patches.Shrines;

import basemod.BaseMod;
import basemod.ReflectionHacks;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.events.shrines.FaceTrader;
import com.megacrit.cardcrawl.helpers.RelicLibrary;
import com.megacrit.cardcrawl.relics.*;
import com.megacrit.cardcrawl.vfx.RainingGoldEffect;
import javassist.CtBehavior;
import upgradeableevents.UpgradeEventManager;
import upgradeableevents.eventupgrades.Shrines.FaceTraderUpgrade;
import upgradeableevents.interfaces.AbstractEventUpgrade;
import upgradeableevents.util.ButtonEffectHelper;

import java.util.ArrayList;

@SpirePatch(clz = FaceTrader.class, method = "buttonEffect")
public class FaceTraderButtonEffectPatch {

    @SpireInsertPatch(
            locator = Locator.class
    )
    public static SpireReturn<Void> Insert(FaceTrader __instance, int buttonPressed) {
        AbstractEventUpgrade currentUpgrade = UpgradeEventManager.getCurrentUpgrade();
        if (currentUpgrade instanceof FaceTraderUpgrade && ((FaceTraderUpgrade)currentUpgrade).isUpgraded()) {
            // Get the CurScreen enum value using reflection
            Enum<?> screen = ReflectionHacks.getPrivate(__instance, FaceTrader.class, "screen");

            // Check if we're on the MAIN screen
            if ("MAIN".equals(screen.name())) {
                switch (buttonPressed) {
                    case 0: // Touch option - double gold
                        int damage = (Integer)ReflectionHacks.getPrivate(__instance, FaceTrader.class, "damage");
                        int goldReward = (Integer)ReflectionHacks.getPrivate(__instance, FaceTrader.class, "goldReward") * 2;

                        __instance.imageEventText.updateBodyText(FaceTrader.DESCRIPTIONS[2]);
                        AbstractDungeon.effectList.add(new RainingGoldEffect(goldReward));
                        AbstractDungeon.player.gainGold(goldReward);
                        AbstractDungeon.player.damage(new DamageInfo(null, damage));
                        CardCrawlGame.sound.play("ATTACK_POISON");

                        __instance.logMetricGainGoldAndDamage("FaceTrader", "Touch", goldReward, damage);
                        break;

                    case 1: // Trade option - improved odds
                        AbstractRelic relic = getUpgradedRandomFace();
                        AbstractDungeon.getCurrRoom().spawnRelicAndObtain(Settings.WIDTH / 2.0F, Settings.HEIGHT / 2.0F, relic);
                        __instance.imageEventText.updateBodyText(FaceTrader.DESCRIPTIONS[3]);
                        __instance.logMetricObtainRelic("FaceTrader", "Trade", relic);
                        break;
                }

                __instance.imageEventText.clearAllDialogs();
                __instance.imageEventText.setDialogOption(FaceTrader.OPTIONS[3]);

                // Get the RESULT enum value using reflection and set it
                Class<?> screenEnum = FaceTrader.class.getDeclaredClasses()[0];
                Object resultScreen = Enum.valueOf((Class<Enum>) screenEnum, "RESULT");
                ReflectionHacks.setPrivate(__instance, FaceTrader.class, "screen", resultScreen);

                return SpireReturn.Return(null);
            }
        }
        return SpireReturn.Continue();
    }

    private static AbstractRelic getUpgradedRandomFace() {
        ArrayList<String> goodFaces = new ArrayList<>();
        ArrayList<String> neutralFaces = new ArrayList<>();
        ArrayList<String> badFaces = new ArrayList<>();

        // Categorize faces
        if (!AbstractDungeon.player.hasRelic(SsserpentHead.ID)) goodFaces.add(SsserpentHead.ID);
        if (!AbstractDungeon.player.hasRelic(FaceOfCleric.ID)) goodFaces.add(FaceOfCleric.ID);
        if (!AbstractDungeon.player.hasRelic(CultistMask.ID)) neutralFaces.add(CultistMask.ID);
        if (!AbstractDungeon.player.hasRelic(GremlinMask.ID)) badFaces.add(GremlinMask.ID);
        if (!AbstractDungeon.player.hasRelic(NlothsMask.ID)) badFaces.add(NlothsMask.ID);

        // Calculate probabilities (20/60/20)
        float roll = AbstractDungeon.miscRng.random(0, 99);
        String chosenId;

        BaseMod.logger.info(roll);

        if (roll < 20) { // 20% bad face
            chosenId = !badFaces.isEmpty() ? badFaces.get(AbstractDungeon.miscRng.random(badFaces.size() - 1)) : "Circlet";
        } else if (roll < 80) { // 60% good face
            chosenId = !goodFaces.isEmpty() ? goodFaces.get(AbstractDungeon.miscRng.random(goodFaces.size() - 1)) : "Circlet";
        } else { // 20% neutral face
            chosenId = !neutralFaces.isEmpty() ? neutralFaces.get(AbstractDungeon.miscRng.random(neutralFaces.size() - 1)) : "Circlet";
        }

        return RelicLibrary.getRelic(chosenId).makeCopy();
    }

    private static class Locator extends SpireInsertLocator {
        @Override
        public int[] Locate(CtBehavior ctMethodToPatch) throws Exception {
            Matcher finalMatcher = new Matcher.FieldAccessMatcher(FaceTrader.class, "screen");
            return LineFinder.findInOrder(ctMethodToPatch, finalMatcher);
        }
    }

    @SpirePostfixPatch
    public static void Postfix(FaceTrader __instance, int buttonPressed) {
        ButtonEffectHelper.updateUpgradeAvailability();
    }
}