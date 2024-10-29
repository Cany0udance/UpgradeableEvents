package upgradeableevents.eventupgrades.TheCity;

import basemod.ReflectionHacks;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.events.AbstractEvent;
import com.megacrit.cardcrawl.events.city.DrugDealer;
import com.megacrit.cardcrawl.helpers.CardLibrary;
import com.megacrit.cardcrawl.localization.UIStrings;
import com.megacrit.cardcrawl.relics.MutagenicStrength;
import com.megacrit.cardcrawl.relics.Vajra;
import upgradeableevents.interfaces.AbstractEventUpgrade;
import upgradeableevents.interfaces.UpgradeCondition;

import static upgradeableevents.UpgradeableEvents.makeID;

public class DrugDealerUpgrade extends AbstractEventUpgrade {
    private static final UIStrings uiStrings = CardCrawlGame.languagePack.getUIString(makeID("DrugDealerUpgrade"));
    private boolean isUpgraded = false;

    public DrugDealerUpgrade(DrugDealer event) {
        super(event, new DrugDealerUpgradeCondition());
    }

    private static class DrugDealerUpgradeCondition implements UpgradeCondition {
        @Override
        public boolean canUpgrade(AbstractEvent event) {
            DrugDealer drugDealerEvent = (DrugDealer)event;
            int screenNum = ReflectionHacks.getPrivate(drugDealerEvent, DrugDealer.class, "screenNum");
            return screenNum == 0;
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
        DrugDealer drugDealerEvent = (DrugDealer)event;

        // Clear existing options
        drugDealerEvent.imageEventText.optionList.clear();

        // Option 1: Upgraded J.A.X.
        AbstractCard upgradedJax = CardLibrary.getCopy("J.A.X.");
        upgradedJax.upgrade();
        drugDealerEvent.imageEventText.setDialogOption(uiStrings.TEXT[0], upgradedJax);

        // Option 2: Transform 3 cards
        if (AbstractDungeon.player.masterDeck.getPurgeableCards().size() >= 3) {
            drugDealerEvent.imageEventText.setDialogOption(uiStrings.TEXT[1]);
        } else {
            drugDealerEvent.imageEventText.setDialogOption(DrugDealer.OPTIONS[4], true);
        }

        // Option 3: Mutagenic Strength + Vajra (if player doesn't have Vajra)
        if (!AbstractDungeon.player.hasRelic(Vajra.ID)) {
            drugDealerEvent.imageEventText.setDialogOption(uiStrings.TEXT[2], new MutagenicStrength());
        } else {
            drugDealerEvent.imageEventText.setDialogOption(DrugDealer.OPTIONS[2], new MutagenicStrength());
        }
    }
}