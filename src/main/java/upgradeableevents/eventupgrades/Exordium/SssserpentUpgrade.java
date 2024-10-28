package upgradeableevents.eventupgrades.Exordium;

import basemod.ReflectionHacks;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.events.AbstractEvent;
import com.megacrit.cardcrawl.events.exordium.Sssserpent;
import com.megacrit.cardcrawl.helpers.CardLibrary;
import com.megacrit.cardcrawl.localization.UIStrings;
import com.megacrit.cardcrawl.relics.Courier;
import com.megacrit.cardcrawl.relics.MembershipCard;
import upgradeableevents.interfaces.AbstractEventUpgrade;
import upgradeableevents.interfaces.UpgradeCondition;

import static upgradeableevents.UpgradeableEvents.makeID;

public class SssserpentUpgrade extends AbstractEventUpgrade {
    private static final UIStrings uiStrings = CardCrawlGame.languagePack.getUIString(makeID("SssserpentUpgrade"));
    private boolean isUpgraded = false;

    public SssserpentUpgrade(Sssserpent event) {
        super(event, new SssserpentUpgradeCondition());
    }

    private static class SssserpentUpgradeCondition implements UpgradeCondition {
        @Override
        public boolean canUpgrade(AbstractEvent event) {
            Sssserpent serpentEvent = (Sssserpent)event;
            Enum<?> screen = ReflectionHacks.getPrivate(serpentEvent, Sssserpent.class, "screen");
            return screen.name().equals("INTRO");
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
        Sssserpent serpentEvent = (Sssserpent)event;
        AbstractCard curse = ReflectionHacks.getPrivate(serpentEvent, Sssserpent.class, "curse");
        int goldReward = ReflectionHacks.getPrivate(serpentEvent, Sssserpent.class, "goldReward");

        // Check for relics in order of priority
        boolean hasMembershipCard = AbstractDungeon.player.hasRelic(MembershipCard.ID);
        boolean hasCourier = AbstractDungeon.player.hasRelic(Courier.ID);

        // First option based on relic checks
        if (!hasMembershipCard) {
            serpentEvent.imageEventText.setDialogOption(uiStrings.TEXT[0], new MembershipCard());
        } else if (!hasCourier) {
            serpentEvent.imageEventText.setDialogOption(uiStrings.TEXT[1], new Courier());
        } else {
            serpentEvent.imageEventText.setDialogOption(
                    Sssserpent.OPTIONS[0] + (goldReward * 2) + Sssserpent.OPTIONS[1],
                    CardLibrary.getCopy(curse.cardID));
        }

        // Add the decline option
        serpentEvent.imageEventText.setDialogOption(Sssserpent.OPTIONS[2]);
    }
}