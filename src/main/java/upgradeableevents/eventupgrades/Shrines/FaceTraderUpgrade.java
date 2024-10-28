package upgradeableevents.eventupgrades.Shrines;

import basemod.ReflectionHacks;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.events.AbstractEvent;
import com.megacrit.cardcrawl.events.shrines.FaceTrader;
import com.megacrit.cardcrawl.localization.UIStrings;
import upgradeableevents.interfaces.AbstractEventUpgrade;
import upgradeableevents.interfaces.UpgradeCondition;

import static upgradeableevents.UpgradeableEvents.makeID;

public class FaceTraderUpgrade extends AbstractEventUpgrade {
    private static final UIStrings uiStrings = CardCrawlGame.languagePack.getUIString(makeID("FaceTraderUpgrade"));
    private boolean isUpgraded = false;

    public FaceTraderUpgrade(FaceTrader event) {
        super(event, new FaceTraderUpgradeCondition());
    }

    private static class FaceTraderUpgradeCondition implements UpgradeCondition {
        @Override
        public boolean canUpgrade(AbstractEvent event) {
            FaceTrader faceTraderEvent = (FaceTrader)event;
            Enum<?> curScreen = ReflectionHacks.getPrivate(faceTraderEvent, FaceTrader.class, "screen");
            return "MAIN".equals(curScreen.name());
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

    @Override
    protected void rebuildOptions() {
        FaceTrader faceTraderEvent = (FaceTrader)event;

        // Get the current values using reflection
        int damage = ReflectionHacks.getPrivate(faceTraderEvent, FaceTrader.class, "damage");
        int goldReward = ReflectionHacks.getPrivate(faceTraderEvent, FaceTrader.class, "goldReward");

        // Update first option to show doubled gold
        String upgradeText = FaceTrader.OPTIONS[0] + damage + FaceTrader.OPTIONS[5] + (goldReward * 2) + FaceTrader.OPTIONS[1];
        faceTraderEvent.imageEventText.updateDialogOption(0, upgradeText);

        // Update second option to show improved odds
        String tradeText = uiStrings.TEXT[0]; // Add text indicating better odds
        faceTraderEvent.imageEventText.setDialogOption(tradeText);

        // Leave option remains unchanged
        faceTraderEvent.imageEventText.setDialogOption(FaceTrader.OPTIONS[3]);
    }
}