package upgradeableevents.eventupgrades.TheCity;

import basemod.ReflectionHacks;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.events.AbstractEvent;
import com.megacrit.cardcrawl.events.city.KnowingSkull;
import com.megacrit.cardcrawl.localization.UIStrings;
import upgradeableevents.interfaces.AbstractEventUpgrade;
import upgradeableevents.interfaces.UpgradeCondition;

import java.util.HashSet;
import java.util.Set;

import static upgradeableevents.UpgradeableEvents.makeID;

public class KnowingSkullUpgrade extends AbstractEventUpgrade {
    private static final UIStrings uiStrings = CardCrawlGame.languagePack.getUIString(makeID("KnowingSkullUpgrade"));
    private boolean isUpgraded = false;
    private Set<Integer> freeOptionsUsed;

    public KnowingSkullUpgrade(KnowingSkull event) {
        super(event, new KnowingSkullUpgradeCondition());
        this.freeOptionsUsed = new HashSet<>();
    }

    private static class KnowingSkullUpgradeCondition implements UpgradeCondition {
        @Override
        public boolean canUpgrade(AbstractEvent event) {
            KnowingSkull skullEvent = (KnowingSkull)event;
            Enum<?> curScreen = ReflectionHacks.getPrivate(skullEvent, KnowingSkull.class, "screen");
            return "ASK".equals(curScreen.name());
        }
    }

    @Override
    public void upgrade() {
        if (!canBeUpgraded()) return;
        isUpgraded = true;
        clearAndRebuildOptions();
    }

    public boolean isUpgraded() {
        return isUpgraded;
    }

    public boolean hasUsedFreeOption(int optionIndex) {
        return freeOptionsUsed.contains(optionIndex);
    }

    public void markOptionUsed(int optionIndex) {
        freeOptionsUsed.add(optionIndex);
    }

    @Override
    protected void rebuildOptions() {
        KnowingSkull skullEvent = (KnowingSkull)event;

        // Get the current costs
        int potionCost = ReflectionHacks.getPrivate(skullEvent, KnowingSkull.class, "potionCost");
        int goldCost = ReflectionHacks.getPrivate(skullEvent, KnowingSkull.class, "goldCost");
        int cardCost = ReflectionHacks.getPrivate(skullEvent, KnowingSkull.class, "cardCost");
        int leaveCost = ReflectionHacks.getPrivate(skullEvent, KnowingSkull.class, "leaveCost");

        // Update each option text to show either 0 or regular cost
        skullEvent.imageEventText.updateDialogOption(0,
                KnowingSkull.OPTIONS[4] + (hasUsedFreeOption(0) ? potionCost : 0) + KnowingSkull.OPTIONS[1]);
        skullEvent.imageEventText.updateDialogOption(1,
                KnowingSkull.OPTIONS[5] + "90" + KnowingSkull.OPTIONS[6] + (hasUsedFreeOption(1) ? goldCost : 0) + KnowingSkull.OPTIONS[1]);
        skullEvent.imageEventText.updateDialogOption(2,
                KnowingSkull.OPTIONS[3] + (hasUsedFreeOption(2) ? cardCost : 0) + KnowingSkull.OPTIONS[1]);
        skullEvent.imageEventText.updateDialogOption(3,
                KnowingSkull.OPTIONS[7] + (hasUsedFreeOption(3) ? leaveCost : 0) + KnowingSkull.OPTIONS[1]);
    }
}