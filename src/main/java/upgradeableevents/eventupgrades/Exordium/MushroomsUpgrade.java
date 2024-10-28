package upgradeableevents.eventupgrades.Exordium;

import basemod.ReflectionHacks;
import com.megacrit.cardcrawl.cards.curses.Clumsy;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.events.AbstractEvent;
import com.megacrit.cardcrawl.events.exordium.Mushrooms;
import com.megacrit.cardcrawl.helpers.CardLibrary;
import com.megacrit.cardcrawl.localization.UIStrings;
import upgradeableevents.interfaces.AbstractEventUpgrade;
import upgradeableevents.interfaces.UpgradeCondition;

import static com.megacrit.cardcrawl.events.exordium.Mushrooms.OPTIONS;
import static upgradeableevents.UpgradeableEvents.makeID;

public class MushroomsUpgrade extends AbstractEventUpgrade {
    private static final UIStrings uiStrings = CardCrawlGame.languagePack.getUIString(makeID("MushroomsUpgrade"));
    public static final float GOLD_MULTIPLIER = 1.5f;
    private boolean isUpgraded = false;

    public MushroomsUpgrade(Mushrooms event) {
        super(event, new MushroomsUpgradeCondition());
    }

    private static class MushroomsUpgradeCondition implements UpgradeCondition {
        @Override
        public boolean canUpgrade(AbstractEvent event) {
            Mushrooms mushroomsEvent = (Mushrooms)event;
            int screenNum = ReflectionHacks.getPrivate(mushroomsEvent, Mushrooms.class, "screenNum");
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
        Mushrooms mushroomsEvent = (Mushrooms)event;

        // Clear options directly from the optionList
        mushroomsEvent.roomEventText.optionList.clear();

        // Re-add fight option
        String fightText = OPTIONS[0];
        mushroomsEvent.roomEventText.addDialogOption(fightText);

        // Re-add heal option with Clumsy preview
        int healAmt = (int)(AbstractDungeon.player.maxHealth * 0.25F);
        String healText = Mushrooms.OPTIONS[1] + healAmt + uiStrings.TEXT[0];
        mushroomsEvent.roomEventText.addDialogOption(healText, CardLibrary.getCopy(Clumsy.ID));
    }
}