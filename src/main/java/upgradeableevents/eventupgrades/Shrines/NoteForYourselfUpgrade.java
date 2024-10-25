package upgradeableevents.eventupgrades.Shrines;

import basemod.BaseMod;
import basemod.ReflectionHacks;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.events.AbstractEvent;
import com.megacrit.cardcrawl.events.shrines.NoteForYourself;
import com.megacrit.cardcrawl.localization.UIStrings;
import upgradeableevents.interfaces.AbstractEventUpgrade;
import upgradeableevents.interfaces.UpgradeCondition;

import static upgradeableevents.UpgradeableEvents.makeID;

public class NoteForYourselfUpgrade extends AbstractEventUpgrade {
    private static final UIStrings uiStrings = CardCrawlGame.languagePack.getUIString(makeID("NoteForYourselfUpgrade"));
    private static final int BONUS_GOLD = 100;
    private static final int CHOOSE_SCREEN_NUM = 1;
    private boolean isUpgraded = false;

    public NoteForYourselfUpgrade(NoteForYourself event) {
        super(event, new NoteForYourselfUpgradeCondition());
    }

    private static class NoteForYourselfUpgradeCondition implements UpgradeCondition {
        @Override
        public boolean canUpgrade(AbstractEvent event) {
            NoteForYourself noteEvent = (NoteForYourself)event;
            Object screenEnum = ReflectionHacks.getPrivate(noteEvent, NoteForYourself.class, "screen");
            int screenNum = ((Enum<?>) screenEnum).ordinal();
            return screenNum == CHOOSE_SCREEN_NUM;
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
        NoteForYourself noteEvent = (NoteForYourself)event;
        AbstractCard obtainCard = ReflectionHacks.getPrivate(noteEvent, NoteForYourself.class, "obtainCard");

        noteEvent.imageEventText.updateDialogOption(0,
                NoteForYourself.OPTIONS[1] + obtainCard.name +
                        uiStrings.TEXT[0] + BONUS_GOLD + uiStrings.TEXT[1] +
                        NoteForYourself.OPTIONS[2], obtainCard);

        noteEvent.imageEventText.setDialogOption(NoteForYourself.OPTIONS[3]);
    }
}
