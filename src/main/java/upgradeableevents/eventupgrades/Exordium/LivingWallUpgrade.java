package upgradeableevents.eventupgrades.Exordium;

import basemod.ReflectionHacks;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.CardGroup;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.events.AbstractEvent;
import com.megacrit.cardcrawl.events.exordium.LivingWall;
import com.megacrit.cardcrawl.localization.UIStrings;
import upgradeableevents.interfaces.AbstractEventUpgrade;
import upgradeableevents.interfaces.UpgradeCondition;

import java.util.ArrayList;

import static upgradeableevents.UpgradeableEvents.makeID;

public class LivingWallUpgrade extends AbstractEventUpgrade {
    private static final UIStrings uiStrings = CardCrawlGame.languagePack.getUIString(makeID("LivingWallUpgrade"));
    private boolean isUpgraded = false;
    private ArrayList<AbstractCard> cardsToRemove;
    private ArrayList<AbstractCard> cardsToTransform;
    private ArrayList<AbstractCard> cardsToUpgrade;

    public LivingWallUpgrade(LivingWall event) {
        super(event, new LivingWallUpgradeCondition());
        this.cardsToRemove = new ArrayList<>();
        this.cardsToTransform = new ArrayList<>();
        this.cardsToUpgrade = new ArrayList<>();
    }

    private static class LivingWallUpgradeCondition implements UpgradeCondition {
        @Override
        public boolean canUpgrade(AbstractEvent event) {
            LivingWall livingWallEvent = (LivingWall)event;
            Enum<?> screen = ReflectionHacks.getPrivate(livingWallEvent, LivingWall.class, "screen");
            return "INTRO".equals(screen.name());
        }
    }

    @Override
    public void upgrade() {
        if (!canBeUpgraded()) return;
        selectRandomCards();
        isUpgraded = true;
        clearAndRebuildOptions();
    }

    private void selectRandomCards() {
        CardGroup purgeable = CardGroup.getGroupWithoutBottledCards(AbstractDungeon.player.masterDeck.getPurgeableCards());
        CardGroup upgradeable = AbstractDungeon.player.masterDeck.getUpgradableCards();

        // Clear previous selections
        cardsToRemove.clear();
        cardsToTransform.clear();
        cardsToUpgrade.clear();

        // Select 3 random cards for removal
        if (purgeable.size() > 0) {
            ArrayList<AbstractCard> removePool = new ArrayList<>(purgeable.group);
            for (int i = 0; i < Math.min(3, removePool.size()); i++) {
                AbstractCard card = removePool.get(AbstractDungeon.miscRng.random(removePool.size() - 1));
                cardsToRemove.add(card);
                removePool.remove(card);
            }
        }

        // Select 3 random cards for transformation
        if (purgeable.size() > 0) {
            ArrayList<AbstractCard> transformPool = new ArrayList<>(purgeable.group);
            for (int i = 0; i < Math.min(3, transformPool.size()); i++) {
                AbstractCard card = transformPool.get(AbstractDungeon.miscRng.random(transformPool.size() - 1));
                cardsToTransform.add(card);
                transformPool.remove(card);
            }
        }

        // Select 3 random cards for upgrade
        if (upgradeable.size() > 0) {
            ArrayList<AbstractCard> upgradePool = new ArrayList<>(upgradeable.group);
            for (int i = 0; i < Math.min(3, upgradePool.size()); i++) {
                AbstractCard card = upgradePool.get(AbstractDungeon.miscRng.random(upgradePool.size() - 1));
                cardsToUpgrade.add(card);
                upgradePool.remove(card);
            }
        }
    }

    public boolean isUpgraded() {
        return isUpgraded;
    }

    @Override
    protected void rebuildOptions() {
        LivingWall livingWallEvent = (LivingWall)event;

        // Clear options directly from the optionList
        livingWallEvent.imageEventText.optionList.clear();

        // Add removal option
        if (!cardsToRemove.isEmpty()) {
            livingWallEvent.imageEventText.setDialogOption(uiStrings.TEXT[0]);
        } else {
            livingWallEvent.imageEventText.setDialogOption(LivingWall.OPTIONS[7], true);
        }

        // Add transform option
        if (!cardsToTransform.isEmpty()) {
            livingWallEvent.imageEventText.setDialogOption(uiStrings.TEXT[1]);
        } else {
            livingWallEvent.imageEventText.setDialogOption(LivingWall.OPTIONS[7], true);
        }

        // Add upgrade option
        if (!cardsToUpgrade.isEmpty()) {
            livingWallEvent.imageEventText.setDialogOption(uiStrings.TEXT[2]);
        } else {
            livingWallEvent.imageEventText.setDialogOption(LivingWall.OPTIONS[7], true);
        }
    }

    public ArrayList<AbstractCard> getCardsToRemove() {
        return cardsToRemove;
    }

    public ArrayList<AbstractCard> getCardsToTransform() {
        return cardsToTransform;
    }

    public ArrayList<AbstractCard> getCardsToUpgrade() {
        return cardsToUpgrade;
    }
}
