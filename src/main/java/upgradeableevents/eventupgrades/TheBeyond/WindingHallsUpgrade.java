package upgradeableevents.eventupgrades.TheBeyond;

import basemod.ReflectionHacks;
import com.badlogic.gdx.math.MathUtils;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.events.AbstractEvent;
import com.megacrit.cardcrawl.events.beyond.WindingHalls;
import com.megacrit.cardcrawl.helpers.CardLibrary;
import com.megacrit.cardcrawl.localization.UIStrings;
import upgradeableevents.interfaces.AbstractEventUpgrade;
import upgradeableevents.interfaces.UpgradeCondition;

import static upgradeableevents.UpgradeableEvents.makeID;

public class WindingHallsUpgrade extends AbstractEventUpgrade {
    private static final UIStrings uiStrings = CardCrawlGame.languagePack.getUIString(makeID("WindingHallsUpgrade"));
    private boolean isUpgraded = false;

    public WindingHallsUpgrade(WindingHalls event) {
        super(event, new WindingHallsUpgradeCondition());
    }

    private static class WindingHallsUpgradeCondition implements UpgradeCondition {
        @Override
        public boolean canUpgrade(AbstractEvent event) {
            WindingHalls hallsEvent = (WindingHalls)event;
            int screenNum = ReflectionHacks.getPrivate(hallsEvent, WindingHalls.class, "screenNum");
            return screenNum == 1;
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
        WindingHalls hallsEvent = (WindingHalls)event;
        hallsEvent.imageEventText.optionList.clear();

        // Get heal amount (for display only - we'll heal to full)
        int healAmount = AbstractDungeon.player.maxHealth - AbstractDungeon.player.currentHealth;

        // Get max HP change amount
        int maxHPAmt = MathUtils.round((float)AbstractDungeon.player.maxHealth * 0.05F);

        // First option - Madness cards with no HP loss
        hallsEvent.imageEventText.setDialogOption(
                uiStrings.TEXT[0],
                CardLibrary.getCopy("Madness")
        );

        // Second option - Full heal with Writhe
        hallsEvent.imageEventText.setDialogOption(
                WindingHalls.OPTIONS[3] + healAmount + WindingHalls.OPTIONS[5],
                CardLibrary.getCopy("Writhe")
        );

        // Third option - Increase max HP
        hallsEvent.imageEventText.setDialogOption(
                uiStrings.TEXT[1] + maxHPAmt + uiStrings.TEXT[2]
        );
    }
}