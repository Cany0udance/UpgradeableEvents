package upgradeableevents.eventupgrades.Exordium;

import basemod.ReflectionHacks;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.events.AbstractEvent;
import com.megacrit.cardcrawl.events.exordium.ShiningLight;
import com.megacrit.cardcrawl.localization.UIStrings;
import upgradeableevents.interfaces.AbstractEventUpgrade;
import upgradeableevents.interfaces.UpgradeCondition;

import static com.megacrit.cardcrawl.events.exordium.ShiningLight.OPTIONS;
import static upgradeableevents.UpgradeableEvents.makeID;

public class ShiningLightUpgrade extends AbstractEventUpgrade {
    private static final UIStrings uiStrings = CardCrawlGame.languagePack.getUIString(makeID("ShiningLightUpgrade"));
    private boolean isUpgraded = false;

    public ShiningLightUpgrade(ShiningLight event) {
        super(event, new ShiningLightUpgradeCondition());
    }

    private static class ShiningLightUpgradeCondition implements UpgradeCondition {
        @Override
        public boolean canUpgrade(AbstractEvent event) {
            ShiningLight shiningLightEvent = (ShiningLight)event;
            Enum<?> screen = ReflectionHacks.getPrivate(shiningLightEvent, ShiningLight.class, "screen");
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
        ShiningLight shiningLightEvent = (ShiningLight)event;
        int damage = ReflectionHacks.getPrivate(shiningLightEvent, ShiningLight.class, "damage");

        if (AbstractDungeon.player.masterDeck.hasUpgradableCards()) {
            shiningLightEvent.imageEventText.updateDialogOption(0, uiStrings.TEXT[0] + damage + OPTIONS[1]);
        } else {
            shiningLightEvent.imageEventText.updateDialogOption(0, OPTIONS[3], true);
        }

        shiningLightEvent.imageEventText.updateDialogOption(1, OPTIONS[2]);
    }
}