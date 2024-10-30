package upgradeableevents.eventupgrades.TheBeyond;

import basemod.ReflectionHacks;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.events.AbstractEvent;
import com.megacrit.cardcrawl.events.beyond.MoaiHead;
import com.megacrit.cardcrawl.localization.UIStrings;
import com.megacrit.cardcrawl.relics.GoldenIdol;
import upgradeableevents.interfaces.AbstractEventUpgrade;
import upgradeableevents.interfaces.UpgradeCondition;

import static upgradeableevents.UpgradeableEvents.makeID;

public class MoaiHeadUpgrade extends AbstractEventUpgrade {
    private static final UIStrings uiStrings = CardCrawlGame.languagePack.getUIString(makeID("MoaiHeadUpgrade"));
    private boolean isUpgraded = false;

    public MoaiHeadUpgrade(MoaiHead event) {
        super(event, new MoaiHeadUpgradeCondition());
    }

    private static class MoaiHeadUpgradeCondition implements UpgradeCondition {
        @Override
        public boolean canUpgrade(AbstractEvent event) {
            MoaiHead moaiEvent = (MoaiHead)event;
            int screenNum = ReflectionHacks.getPrivate(moaiEvent, MoaiHead.class, "screenNum");
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
        MoaiHead moaiEvent = (MoaiHead)event;
        int hpAmt = ReflectionHacks.getPrivate(moaiEvent, MoaiHead.class, "hpAmt");

        // Update first option to indicate no max HP loss
        moaiEvent.imageEventText.updateDialogOption(0, uiStrings.TEXT[0]);

        // Update second option based on Golden Idol possession
        if (AbstractDungeon.player.hasRelic(GoldenIdol.ID)) {
            moaiEvent.imageEventText.updateDialogOption(1,
                    MoaiHead.OPTIONS[2].replace("333", "444"),
                    !AbstractDungeon.player.hasRelic(GoldenIdol.ID));
        } else {
            moaiEvent.imageEventText.updateDialogOption(1,
                    MoaiHead.OPTIONS[3],
                    !AbstractDungeon.player.hasRelic(GoldenIdol.ID));
        }

        // Always add the leave option
        moaiEvent.imageEventText.updateDialogOption(2, MoaiHead.OPTIONS[4]);
    }
}
