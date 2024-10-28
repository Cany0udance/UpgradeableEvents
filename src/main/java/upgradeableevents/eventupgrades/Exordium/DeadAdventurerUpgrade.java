package upgradeableevents.eventupgrades.Exordium;

import basemod.BaseMod;
import basemod.ReflectionHacks;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.events.AbstractEvent;
import com.megacrit.cardcrawl.events.exordium.DeadAdventurer;
import com.megacrit.cardcrawl.localization.UIStrings;
import upgradeableevents.interfaces.AbstractEventUpgrade;
import upgradeableevents.interfaces.UpgradeCondition;

import static upgradeableevents.UpgradeableEvents.makeID;

public class DeadAdventurerUpgrade extends AbstractEventUpgrade {
    private static final UIStrings uiStrings = CardCrawlGame.languagePack.getUIString(makeID("DeadAdventurerUpgrade"));
    private boolean isUpgraded = false;

    public DeadAdventurerUpgrade(DeadAdventurer event) {
        super(event, new DeadAdventurerUpgradeCondition());
    }

    private static class DeadAdventurerUpgradeCondition implements UpgradeCondition {
        @Override
        public boolean canUpgrade(AbstractEvent event) {
            DeadAdventurer deadAdvEvent = (DeadAdventurer) event;
            // Get the current screen using reflection
            Enum<?> curScreen = ReflectionHacks.getPrivate(deadAdvEvent, DeadAdventurer.class, "screen");
            int selectedOption = deadAdvEvent.roomEventText.getSelectedOption();

            return "INTRO".equals(curScreen.name()) && selectedOption == -1;
        }
    }

    @Override
    public void upgrade() {
        if (!canBeUpgraded()) return;

        DeadAdventurer deadAdvEvent = (DeadAdventurer) event;
        // Actually halve the current encounter chance when upgraded
        int currentChance = (Integer) ReflectionHacks.getPrivate(deadAdvEvent, DeadAdventurer.class, "encounterChance");
        ReflectionHacks.setPrivate(deadAdvEvent, DeadAdventurer.class, "encounterChance", currentChance / 2);

        isUpgraded = true;
        clearAndRebuildOptions();
    }

    public boolean isUpgraded() {
        return isUpgraded;
    }

    @Override
    protected void rebuildOptions() {
        DeadAdventurer deadAdvEvent = (DeadAdventurer) event;
        int encounterChance = (Integer) ReflectionHacks.getPrivate(deadAdvEvent, DeadAdventurer.class, "encounterChance");

        // Update first option to show the current chance (which is already halved)
        deadAdvEvent.roomEventText.updateDialogOption(0,
                DeadAdventurer.OPTIONS[0] + encounterChance + DeadAdventurer.OPTIONS[4]);
    }
}