package upgradeableevents.eventupgrades.TheBeyond;

import basemod.ReflectionHacks;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.events.AbstractEvent;
import com.megacrit.cardcrawl.events.beyond.Falling;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.localization.UIStrings;
import upgradeableevents.interfaces.AbstractEventUpgrade;
import upgradeableevents.interfaces.UpgradeCondition;

import static upgradeableevents.UpgradeableEvents.makeID;

public class FallingUpgrade extends AbstractEventUpgrade {
    private static final UIStrings uiStrings = CardCrawlGame.languagePack.getUIString(makeID("FallingUpgrade"));
    private boolean isUpgraded = false;

    public FallingUpgrade(Falling event) {
        super(event, new FallingUpgradeCondition());
    }

    private static class FallingUpgradeCondition implements UpgradeCondition {
        @Override
        public boolean canUpgrade(AbstractEvent event) {
            Falling fallingEvent = (Falling)event;
            Enum<?> curScreen = ReflectionHacks.getPrivate(fallingEvent, Falling.class, "screen");
            return "CHOICE".equals(curScreen.name());
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
        Falling fallingEvent = (Falling)event;
        Enum<?> curScreen = ReflectionHacks.getPrivate(fallingEvent, Falling.class, "screen");

        if ("CHOICE".equals(curScreen.name())) {
            boolean hasSkill = ReflectionHacks.getPrivate(fallingEvent, Falling.class, "skill");
            boolean hasPower = ReflectionHacks.getPrivate(fallingEvent, Falling.class, "power");
            boolean hasAttack = ReflectionHacks.getPrivate(fallingEvent, Falling.class, "attack");
            AbstractCard skillCard = ReflectionHacks.getPrivate(fallingEvent, Falling.class, "skillCard");
            AbstractCard powerCard = ReflectionHacks.getPrivate(fallingEvent, Falling.class, "powerCard");
            AbstractCard attackCard = ReflectionHacks.getPrivate(fallingEvent, Falling.class, "attackCard");

            fallingEvent.imageEventText.clearAllDialogs();

            if (!hasSkill && !hasPower && !hasAttack) {
                fallingEvent.imageEventText.setDialogOption(Falling.OPTIONS[8]);
            } else {
                if (hasSkill) {
                    fallingEvent.imageEventText.setDialogOption(
                            uiStrings.TEXT[1] + FontHelper.colorString(skillCard.name, "g"),
                            skillCard.makeStatEquivalentCopy());
                } else {
                    fallingEvent.imageEventText.setDialogOption(Falling.OPTIONS[2], true);
                }

                if (hasPower) {
                    fallingEvent.imageEventText.setDialogOption(
                            uiStrings.TEXT[2] + FontHelper.colorString(powerCard.name, "g"),
                            powerCard.makeStatEquivalentCopy());
                } else {
                    fallingEvent.imageEventText.setDialogOption(Falling.OPTIONS[4], true);
                }

                if (hasAttack) {
                    fallingEvent.imageEventText.setDialogOption(
                            uiStrings.TEXT[0] + FontHelper.colorString(attackCard.name, "g"),
                            attackCard.makeStatEquivalentCopy());
                } else {
                    fallingEvent.imageEventText.setDialogOption(Falling.OPTIONS[6], true);
                }
            }
        }
    }
}