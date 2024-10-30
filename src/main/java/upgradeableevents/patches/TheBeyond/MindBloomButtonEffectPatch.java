package upgradeableevents.patches.TheBeyond;

import basemod.ReflectionHacks;
import com.badlogic.gdx.math.MathUtils;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.curses.Decay;
import com.megacrit.cardcrawl.cards.curses.Doubt;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.events.AbstractEvent;
import com.megacrit.cardcrawl.events.beyond.MindBloom;
import com.megacrit.cardcrawl.helpers.MonsterHelper;
import com.megacrit.cardcrawl.helpers.RelicLibrary;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.vfx.RainingGoldEffect;
import com.megacrit.cardcrawl.vfx.UpgradeShineEffect;
import com.megacrit.cardcrawl.vfx.cardManip.ShowCardAndObtainEffect;
import com.megacrit.cardcrawl.vfx.cardManip.ShowCardBrieflyEffect;
import javassist.CtBehavior;
import upgradeableevents.UpgradeEventManager;
import upgradeableevents.eventupgrades.TheBeyond.MindBloomUpgrade;
import upgradeableevents.interfaces.AbstractEventUpgrade;
import upgradeableevents.util.ButtonEffectHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

@SpirePatch(clz = MindBloom.class, method = "buttonEffect")
public class MindBloomButtonEffectPatch {

    @SpireInsertPatch(
            locator = Locator.class
    )
    public static SpireReturn<Void> Insert(MindBloom __instance, int buttonPressed) {
        AbstractEventUpgrade currentUpgrade = UpgradeEventManager.getCurrentUpgrade();
        if (currentUpgrade instanceof MindBloomUpgrade && ((MindBloomUpgrade)currentUpgrade).isUpgraded()) {
            Enum<?> screen = ReflectionHacks.getPrivate(__instance, MindBloom.class, "screen");

            // Initialize screen enum class early
            Class<?> screenEnum = MindBloom.class.getDeclaredClasses()[0];
            Object leaveScreen = Enum.valueOf((Class<Enum>) screenEnum, "LEAVE");

            if ("INTRO".equals(screen.name())) {
                switch (buttonPressed) {
                    case 0: // I Am War
                        __instance.imageEventText.updateBodyText(MindBloom.DESCRIPTIONS[1]);
                        Object fightScreen = Enum.valueOf((Class<Enum>) screenEnum, "FIGHT");
                        ReflectionHacks.setPrivate(__instance, MindBloom.class, "screen", fightScreen);

                        AbstractEvent.logMetric("MindBloom", "Fight (Boss Relic)");
                        CardCrawlGame.music.playTempBgmInstantly("MINDBLOOM", true);

                        ArrayList<String> bossList = new ArrayList<>();
                        bossList.add("The Guardian");
                        bossList.add("Hexaghost");
                        bossList.add("Slime Boss");
                        Collections.shuffle(bossList, new Random(AbstractDungeon.miscRng.randomLong()));
                        AbstractDungeon.getCurrRoom().monsters = MonsterHelper.getEncounter(bossList.get(0));
                        AbstractDungeon.getCurrRoom().rewards.clear();

                        if (AbstractDungeon.ascensionLevel >= 13) {
                            AbstractDungeon.getCurrRoom().addGoldToRewards(25);
                        } else {
                            AbstractDungeon.getCurrRoom().addGoldToRewards(50);
                        }

                        AbstractDungeon.getCurrRoom().addRelicToRewards(AbstractRelic.RelicTier.BOSS);

                        __instance.enterCombatFromImage();
                        AbstractDungeon.lastCombatMetricKey = "Mind Bloom Boss Battle";
                        return SpireReturn.Return(null);

                    case 1: // I Am Awake
                        __instance.imageEventText.updateBodyText(MindBloom.DESCRIPTIONS[2]);
                        ReflectionHacks.setPrivate(__instance, MindBloom.class, "screen", leaveScreen);

                        int healAmount = AbstractDungeon.player.maxHealth - AbstractDungeon.player.currentHealth;
                        AbstractDungeon.player.heal(healAmount);

                        int effectCount = 0;
                        List<String> upgradedCards = new ArrayList<>();
                        List<String> obtainedRelic = new ArrayList<>();

                        for (AbstractCard c : AbstractDungeon.player.masterDeck.group) {
                            if (c.canUpgrade()) {
                                effectCount++;
                                if (effectCount <= 20) {
                                    float x = MathUtils.random(0.1F, 0.9F) * Settings.WIDTH;
                                    float y = MathUtils.random(0.2F, 0.8F) * Settings.HEIGHT;
                                    AbstractDungeon.effectList.add(new ShowCardBrieflyEffect(c.makeStatEquivalentCopy(), x, y));
                                    AbstractDungeon.topLevelEffects.add(new UpgradeShineEffect(x, y));
                                }
                                upgradedCards.add(c.cardID);
                                c.upgrade();
                                AbstractDungeon.player.bottledCardUpgradeCheck(c);
                            }
                        }

                        AbstractDungeon.getCurrRoom().spawnRelicAndObtain(Settings.WIDTH / 2.0F, Settings.HEIGHT / 2.0F,
                                RelicLibrary.getRelic("Mark of the Bloom").makeCopy());
                        obtainedRelic.add("Mark of the Bloom");

                        AbstractEvent.logMetric("MindBloom", "Upgrade and Heal", null, null, null,
                                upgradedCards, obtainedRelic, null, null, 0, 0, 0, healAmount, 0, 0);

                        __instance.imageEventText.clearAllDialogs();
                        __instance.imageEventText.setDialogOption(MindBloom.OPTIONS[4]);
                        return SpireReturn.Return(null);

                    case 2: // I Am Rich OR I Am Healthy
                        if (AbstractDungeon.floorNum % 50 <= 40) {
                            // I Am Rich
                            __instance.imageEventText.updateBodyText(MindBloom.DESCRIPTIONS[1]);
                            ReflectionHacks.setPrivate(__instance, MindBloom.class, "screen", leaveScreen);

                            List<String> cardsAdded = new ArrayList<>();
                            cardsAdded.add("Decay");
                            cardsAdded.add("Decay");

                            AbstractEvent.logMetric("MindBloom", "Gold and Decay", cardsAdded, null, null,
                                    null, null, null, null, 0, 0, 0, 0, 999, 0);

                            AbstractDungeon.effectList.add(new RainingGoldEffect(999));
                            AbstractDungeon.player.gainGold(999);
                            AbstractDungeon.effectList.add(new ShowCardAndObtainEffect(new Decay(), Settings.WIDTH * 0.6F, Settings.HEIGHT / 2.0F));
                            AbstractDungeon.effectList.add(new ShowCardAndObtainEffect(new Decay(), Settings.WIDTH * 0.3F, Settings.HEIGHT / 2.0F));

                        } else {
                            // I Am Healthy
                            __instance.imageEventText.updateBodyText(MindBloom.DESCRIPTIONS[1]);
                            ReflectionHacks.setPrivate(__instance, MindBloom.class, "screen", leaveScreen);

                            AbstractCard curse = new Doubt();
                            int healing = AbstractDungeon.player.maxHealth - AbstractDungeon.player.currentHealth;
                            AbstractDungeon.player.increaseMaxHp(15, true);
                            AbstractDungeon.player.heal(AbstractDungeon.player.maxHealth);

                            List<String> cardsObtained = new ArrayList<>();
                            cardsObtained.add(curse.cardID);

                            AbstractEvent.logMetric("MindBloom", "Heal and Max HP", cardsObtained, null, null,
                                    null, null, null, null, 0, 0, 0, healing, 0, 15);

                            AbstractDungeon.effectList.add(new ShowCardAndObtainEffect(curse, Settings.WIDTH / 2.0F, Settings.HEIGHT / 2.0F));
                        }

                        __instance.imageEventText.clearAllDialogs();
                        __instance.imageEventText.setDialogOption(MindBloom.OPTIONS[4]);
                        return SpireReturn.Return(null);
                }
            }
        }
        return SpireReturn.Continue();
    }

    private static class Locator extends SpireInsertLocator {
        @Override
        public int[] Locate(CtBehavior ctMethodToPatch) throws Exception {
            Matcher finalMatcher = new Matcher.FieldAccessMatcher(MindBloom.class, "screen");
            return LineFinder.findInOrder(ctMethodToPatch, finalMatcher);
        }
    }

    @SpirePostfixPatch
    public static void Postfix(MindBloom __instance, int buttonPressed) {
        ButtonEffectHelper.updateUpgradeAvailability();
    }
}