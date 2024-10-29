package upgradeableevents.patches.TheCity;

import basemod.ReflectionHacks;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.CardGroup;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.events.city.TheLibrary;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.unlock.UnlockTracker;
import javassist.CtBehavior;
import upgradeableevents.UpgradeEventManager;
import upgradeableevents.eventupgrades.TheCity.TheLibraryUpgrade;
import upgradeableevents.interfaces.AbstractEventUpgrade;
import upgradeableevents.util.ButtonEffectHelper;

@SpirePatch(clz = TheLibrary.class, method = "buttonEffect")
public class TheLibraryButtonEffectPatch {

    @SpireInsertPatch(
            locator = Locator.class
    )
    public static SpireReturn<Void> Insert(TheLibrary __instance, int buttonPressed) {
        AbstractEventUpgrade currentUpgrade = UpgradeEventManager.getCurrentUpgrade();
        if (currentUpgrade instanceof TheLibraryUpgrade && ((TheLibraryUpgrade)currentUpgrade).isUpgraded()) {
            int screenNum = ReflectionHacks.getPrivate(__instance, TheLibrary.class, "screenNum");

            if (screenNum == 0) {
                switch (buttonPressed) {
                    case 0: // Read - Show only upgraded cards
                        String bookText = (String)ReflectionHacks.privateMethod(TheLibrary.class, "getBook")
                                .invoke(__instance, new Object[]{});

                        __instance.imageEventText.updateBodyText(bookText);
                        ReflectionHacks.setPrivate(__instance, TheLibrary.class, "screenNum", 1);
                        __instance.imageEventText.updateDialogOption(0, TheLibrary.OPTIONS[3]);
                        __instance.imageEventText.clearRemainingOptions();
                        ReflectionHacks.setPrivate(__instance, TheLibrary.class, "pickCard", true);

                        CardGroup group = new CardGroup(CardGroup.CardGroupType.UNSPECIFIED);

                        for(int i = 0; i < 20; ++i) {
                            AbstractCard card = AbstractDungeon.getCard(AbstractDungeon.rollRarity()).makeCopy();
                            card.upgrade(); // Upgrade the card

                            boolean containsDupe = true;
                            while(containsDupe) {
                                containsDupe = false;
                                for (AbstractCard c : group.group) {
                                    if (c.cardID.equals(card.cardID)) {
                                        containsDupe = true;
                                        card = AbstractDungeon.getCard(AbstractDungeon.rollRarity()).makeCopy();
                                        card.upgrade(); // Upgrade the new card
                                        break;
                                    }
                                }
                            }

                            if (!group.contains(card)) {
                                for (AbstractRelic r : AbstractDungeon.player.relics) {
                                    r.onPreviewObtainCard(card);
                                }
                                group.addToBottom(card);
                            } else {
                                --i;
                            }
                        }

                        for (AbstractCard c : group.group) {
                            UnlockTracker.markCardAsSeen(c.cardID);
                        }

                        AbstractDungeon.gridSelectScreen.open(group, 1, TheLibrary.OPTIONS[4], false);
                        return SpireReturn.Return(null);
                }
            }
        }
        return SpireReturn.Continue();
    }

    private static class Locator extends SpireInsertLocator {
        @Override
        public int[] Locate(CtBehavior ctMethodToPatch) throws Exception {
            Matcher finalMatcher = new Matcher.MethodCallMatcher(TheLibrary.class, "getBook");
            return LineFinder.findInOrder(ctMethodToPatch, finalMatcher);
        }
    }

    @SpirePostfixPatch
    public static void Postfix(TheLibrary __instance, int buttonPressed) {
        ButtonEffectHelper.updateUpgradeAvailability();
    }
}
