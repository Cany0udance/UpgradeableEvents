package upgradeableevents.eventupgrades.Exordium;

import basemod.ReflectionHacks;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.events.AbstractEvent;
import com.megacrit.cardcrawl.events.exordium.ScrapOoze;
import com.megacrit.cardcrawl.localization.UIStrings;
import upgradeableevents.UpgradeEventManager;
import upgradeableevents.interfaces.AbstractEventUpgrade;
import upgradeableevents.interfaces.UpgradeCondition;

import static upgradeableevents.UpgradeableEvents.makeID;

public class ScrapOozeUpgrade extends AbstractEventUpgrade {
    public static final UIStrings uiStrings = CardCrawlGame.languagePack.getUIString(makeID("ScrapOozeUpgrade"));
    private boolean isUpgraded = false;
    private int relicsObtained = 0;

    public ScrapOozeUpgrade(ScrapOoze event) {
        super(event, new ScrapOozeUpgradeCondition());
    }

    private static class ScrapOozeUpgradeCondition implements UpgradeCondition {
        @Override
        public boolean canUpgrade(AbstractEvent event) {
            ScrapOoze scrapOozeEvent = (ScrapOoze)event;
            int screenNum = ReflectionHacks.getPrivate(scrapOozeEvent, ScrapOoze.class, "screenNum");
            return screenNum == 0;
        }
    }

    @Override
    public void upgrade() {
        if (!canBeUpgraded()) return;
        isUpgraded = true;
        UpgradeEventManager.playUpgradeVfx();
    }

    public boolean isUpgraded() {
        return isUpgraded;
    }

    public int getRelicsObtained() {
        return relicsObtained;
    }

    public void incrementRelicsObtained() {
        relicsObtained++;
    }

    @Override
    protected void rebuildOptions() {
    }

}
