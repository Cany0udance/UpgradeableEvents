package upgradeableevents.eventupgrades.TheCity;

import basemod.ReflectionHacks;
import com.badlogic.gdx.math.MathUtils;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.events.AbstractEvent;
import com.megacrit.cardcrawl.events.city.ForgottenAltar;
import com.megacrit.cardcrawl.localization.UIStrings;
import com.megacrit.cardcrawl.relics.BloodyIdol;
import upgradeableevents.interfaces.AbstractEventUpgrade;
import upgradeableevents.interfaces.UpgradeCondition;

import static upgradeableevents.UpgradeableEvents.makeID;

public class ForgottenAltarUpgrade extends AbstractEventUpgrade {
    private static final UIStrings uiStrings = CardCrawlGame.languagePack.getUIString(makeID("ForgottenAltarUpgrade"));
    private boolean isUpgraded = false;

    public ForgottenAltarUpgrade(ForgottenAltar event) {
        super(event, new ForgottenAltarUpgradeCondition());
    }

    private static class ForgottenAltarUpgradeCondition implements UpgradeCondition {
        @Override
        public boolean canUpgrade(AbstractEvent event) {
            ForgottenAltar altarEvent = (ForgottenAltar)event;
            int screenNum = (int)ReflectionHacks.getPrivate(altarEvent, AbstractEvent.class, "screenNum");
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
        ForgottenAltar altarEvent = (ForgottenAltar)event;

        // Clear existing options
        altarEvent.imageEventText.optionList.clear();

        // Option 1 - Get Bloody Idol without sacrificing Golden Idol
        if (AbstractDungeon.player.hasRelic("Golden Idol")) {
            altarEvent.imageEventText.setDialogOption(uiStrings.TEXT[0], new BloodyIdol());
        } else {
            altarEvent.imageEventText.setDialogOption(ForgottenAltar.OPTIONS[1], true, new BloodyIdol());
        }

        // Option 2 - Increased Max HP gain
        int hpLoss = ReflectionHacks.getPrivate(altarEvent, ForgottenAltar.class, "hpLoss");
        altarEvent.imageEventText.setDialogOption(ForgottenAltar.OPTIONS[2] + "10" + ForgottenAltar.OPTIONS[3] + hpLoss + ForgottenAltar.OPTIONS[4]);

        // Option 3 - Lose 5% Max HP instead of Decay
        int smallHpLoss = MathUtils.round(AbstractDungeon.player.maxHealth * 0.05F);
        altarEvent.imageEventText.setDialogOption(uiStrings.TEXT[1] + smallHpLoss + uiStrings.TEXT[2]);
    }
}