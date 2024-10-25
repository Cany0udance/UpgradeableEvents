package upgradeableevents.eventupgrades;

import basemod.ReflectionHacks;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.events.AbstractEvent;
import com.megacrit.cardcrawl.events.exordium.Cleric;
import upgradeableevents.interfaces.AbstractEventUpgrade;
import upgradeableevents.interfaces.UpgradeCondition;

public class ClericEventUpgrade extends AbstractEventUpgrade {
    private static final float UPGRADED_HEAL_MULTIPLIER = 0.5F;
    private static final int UPGRADED_PURIFY_COST = 10;

    public ClericEventUpgrade(Cleric event) {
        super(event, new ClericUpgradeCondition());
    }

    private static class ClericUpgradeCondition implements UpgradeCondition {
        @Override
        public boolean canUpgrade(AbstractEvent event) {
            Cleric clericEvent = (Cleric)event;
            // Get screenNum from AbstractImageEvent class instead of Cleric
            Integer screenNum = ReflectionHacks.getPrivate(clericEvent, AbstractEvent.class, "screenNum");
            return screenNum == 0;
        }
    }
    @Override
    public void upgrade() {
        if (!canBeUpgraded()) return;

        Cleric clericEvent = (Cleric)event;
        int healAmt = (int)((float)AbstractDungeon.player.maxHealth * UPGRADED_HEAL_MULTIPLIER);

        ReflectionHacks.setPrivate(clericEvent, Cleric.class, "purifyCost", UPGRADED_PURIFY_COST);
        ReflectionHacks.setPrivate(clericEvent, Cleric.class, "healAmt", healAmt);

        clearAndRebuildOptions();
    }

    @Override
    protected void rebuildOptions() {
        Cleric clericEvent = (Cleric)event;
        int gold = AbstractDungeon.player.gold;
        int healAmt = ReflectionHacks.getPrivate(clericEvent, Cleric.class, "healAmt");

        // Heal option
        if (gold >= 35) {
            clericEvent.imageEventText.setDialogOption(
                    Cleric.OPTIONS[0] + healAmt + Cleric.OPTIONS[8],
                    false
            );
        } else {
            clericEvent.imageEventText.setDialogOption(
                    Cleric.OPTIONS[1] + 35 + Cleric.OPTIONS[2],
                    true
            );
        }

        // Purify option
        if (gold >= UPGRADED_PURIFY_COST) {
            clericEvent.imageEventText.setDialogOption(
                    Cleric.OPTIONS[3] + UPGRADED_PURIFY_COST + Cleric.OPTIONS[4],
                    false
            );
        } else {
            clericEvent.imageEventText.setDialogOption(
                    Cleric.OPTIONS[5],
                    true
            );
        }

        // Leave option
        clericEvent.imageEventText.setDialogOption(Cleric.OPTIONS[6]);
    }
}