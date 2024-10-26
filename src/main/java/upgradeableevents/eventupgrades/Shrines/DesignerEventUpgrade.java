package upgradeableevents.eventupgrades.Shrines;

import basemod.ReflectionHacks;
import com.megacrit.cardcrawl.cards.CardGroup;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.events.AbstractEvent;
import com.megacrit.cardcrawl.events.shrines.Designer;
import com.megacrit.cardcrawl.localization.UIStrings;
import upgradeableevents.interfaces.AbstractEventUpgrade;
import upgradeableevents.interfaces.UpgradeCondition;

import static upgradeableevents.UpgradeableEvents.makeID;

public class DesignerEventUpgrade extends AbstractEventUpgrade {
    private static final UIStrings uiStrings = CardCrawlGame.languagePack.getUIString(makeID("DesignerUpgrade"));
    public DesignerEventUpgrade(Designer event) {
        super(event, new DesignerUpgradeCondition());
    }

    private static class DesignerUpgradeCondition implements UpgradeCondition {
        @Override
        public boolean canUpgrade(AbstractEvent event) {
            Designer designerEvent = (Designer)event;
            // Get the CurrentScreen enum value
            Enum<?> curScreen = ReflectionHacks.getPrivate(designerEvent, Designer.class, "curScreen");
            // Compare the enum's name() instead of direct comparison
            return "MAIN".equals(curScreen.name());
        }
    }

    @Override
    public void upgrade() {
        if (!canBeUpgraded()) return;

        Designer designerEvent = (Designer)event;

        // Set all costs to 0
        ReflectionHacks.setPrivate(designerEvent, Designer.class, "adjustCost", 0);
        ReflectionHacks.setPrivate(designerEvent, Designer.class, "cleanUpCost", 0);
        ReflectionHacks.setPrivate(designerEvent, Designer.class, "fullServiceCost", 0);

        clearAndRebuildOptions();
    }

    @Override
    protected void rebuildOptions() {
        Designer designerEvent = (Designer)event;

        // Get the private fields we need
        boolean adjustmentUpgradesOne = ReflectionHacks.getPrivate(designerEvent, Designer.class, "adjustmentUpgradesOne");
        boolean cleanUpRemovesCards = ReflectionHacks.getPrivate(designerEvent, Designer.class, "cleanUpRemovesCards");
        int hpLoss = ReflectionHacks.getPrivate(designerEvent, Designer.class, "hpLoss");

        // First option (Adjustment)
        if (adjustmentUpgradesOne) {
            designerEvent.imageEventText.updateDialogOption(
                    0,
                    uiStrings.TEXT[0] + Designer.OPTIONS[9],
                    !AbstractDungeon.player.masterDeck.hasUpgradableCards()
            );
        } else {
            designerEvent.imageEventText.updateDialogOption(
                    0,
                    uiStrings.TEXT[0] + Designer.OPTIONS[7] + "2" + Designer.OPTIONS[8],
                    !AbstractDungeon.player.masterDeck.hasUpgradableCards()
            );
        }

        // Second option (Clean Up)
        if (cleanUpRemovesCards) {
            designerEvent.imageEventText.setDialogOption(
                    uiStrings.TEXT[1] + Designer.OPTIONS[10],
                    CardGroup.getGroupWithoutBottledCards(AbstractDungeon.player.masterDeck).size() == 0
            );
        } else {
            designerEvent.imageEventText.setDialogOption(
                    uiStrings.TEXT[1] + Designer.OPTIONS[11] + "2" + Designer.OPTIONS[12],
                    CardGroup.getGroupWithoutBottledCards(AbstractDungeon.player.masterDeck).size() < 2
            );
        }

        // Third option (Full Service)
        designerEvent.imageEventText.setDialogOption(
                uiStrings.TEXT[2] + Designer.OPTIONS[13],
                CardGroup.getGroupWithoutBottledCards(AbstractDungeon.player.masterDeck).size() == 0
        );

        // Fourth option (Leave/Punch)
        designerEvent.imageEventText.setDialogOption(
                Designer.OPTIONS[4] + hpLoss + Designer.OPTIONS[5]
        );
    }
}
