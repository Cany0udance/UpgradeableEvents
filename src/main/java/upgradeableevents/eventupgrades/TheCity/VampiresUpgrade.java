package upgradeableevents.eventupgrades.TheCity;

import basemod.ReflectionHacks;
import com.megacrit.cardcrawl.cards.colorless.Bite;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.events.AbstractEvent;
import com.megacrit.cardcrawl.events.city.Vampires;
import com.megacrit.cardcrawl.localization.UIStrings;
import com.megacrit.cardcrawl.relics.BloodVial;
import upgradeableevents.interfaces.AbstractEventUpgrade;
import upgradeableevents.interfaces.UpgradeCondition;

import static upgradeableevents.UpgradeableEvents.makeID;

public class VampiresUpgrade extends AbstractEventUpgrade {
    private static final UIStrings uiStrings = CardCrawlGame.languagePack.getUIString(makeID("VampiresUpgrade"));
    private boolean isUpgraded = false;

    public VampiresUpgrade(Vampires event) {
        super(event, new VampiresUpgradeCondition());
    }

    private static class VampiresUpgradeCondition implements UpgradeCondition {
        @Override
        public boolean canUpgrade(AbstractEvent event) {
            Vampires vampiresEvent = (Vampires)event;
            int screenNum = ReflectionHacks.getPrivate(vampiresEvent, Vampires.class, "screenNum");
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
        Vampires vampiresEvent = (Vampires) event;
        boolean hasVial = ReflectionHacks.getPrivate(vampiresEvent, Vampires.class, "hasVial");
        int maxHpLoss = ReflectionHacks.getPrivate(vampiresEvent, Vampires.class, "maxHpLoss");

        // Update option texts to reflect that ALL Strikes will be replaced
        vampiresEvent.imageEventText.updateDialogOption(0,
                uiStrings.TEXT[0] + maxHpLoss + Vampires.OPTIONS[1],
                new Bite());

        if (hasVial) {
            String vialName = (new BloodVial()).name;
            vampiresEvent.imageEventText.updateDialogOption(1,
                    Vampires.OPTIONS[3] + vialName + uiStrings.TEXT[1],
                    new Bite());

            vampiresEvent.imageEventText.updateDialogOption(2,
                    Vampires.OPTIONS[2]);
        } else {
            vampiresEvent.imageEventText.updateDialogOption(1,
                    Vampires.OPTIONS[2]);
        }
    }
}