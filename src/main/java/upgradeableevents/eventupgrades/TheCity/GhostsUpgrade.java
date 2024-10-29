package upgradeableevents.eventupgrades.TheCity;

import basemod.ReflectionHacks;
import com.badlogic.gdx.math.MathUtils;
import com.megacrit.cardcrawl.cards.colorless.Apparition;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.events.AbstractEvent;
import com.megacrit.cardcrawl.events.city.Ghosts;
import com.megacrit.cardcrawl.localization.UIStrings;
import upgradeableevents.interfaces.AbstractEventUpgrade;
import upgradeableevents.interfaces.UpgradeCondition;

import static upgradeableevents.UpgradeableEvents.makeID;

public class GhostsUpgrade extends AbstractEventUpgrade {
    private static final UIStrings uiStrings = CardCrawlGame.languagePack.getUIString(makeID("GhostsUpgrade"));
    private boolean isUpgraded = false;

    public GhostsUpgrade(Ghosts event) {
        super(event, new GhostsUpgradeCondition());
    }

    private static class GhostsUpgradeCondition implements UpgradeCondition {
        @Override
        public boolean canUpgrade(AbstractEvent event) {
            Ghosts ghostsEvent = (Ghosts)event;
            int screenNum = ReflectionHacks.getPrivate(ghostsEvent, Ghosts.class, "screenNum");
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
        Ghosts ghostsEvent = (Ghosts)event;

        // Clear existing options
        ghostsEvent.imageEventText.optionList.clear();

        // Calculate new HP loss (25% instead of 50%)
        int hpLoss = MathUtils.ceil(AbstractDungeon.player.maxHealth * 0.25F);
        if (hpLoss >= AbstractDungeon.player.maxHealth) {
            hpLoss = AbstractDungeon.player.maxHealth - 1;
        }

        // Set the new option with reduced HP cost
        if (AbstractDungeon.ascensionLevel >= 15) {
            ghostsEvent.imageEventText.setDialogOption(Ghosts.OPTIONS[3] + hpLoss + Ghosts.OPTIONS[1], new Apparition());
        } else {
            ghostsEvent.imageEventText.setDialogOption(Ghosts.OPTIONS[0] + hpLoss + Ghosts.OPTIONS[1], new Apparition());
        }

        // Add the decline option
        ghostsEvent.imageEventText.setDialogOption(Ghosts.OPTIONS[2]);

        // Store the new HP loss value
        ReflectionHacks.setPrivate(ghostsEvent, Ghosts.class, "hpLoss", hpLoss);
    }
}