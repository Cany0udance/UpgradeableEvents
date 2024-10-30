package upgradeableevents.eventupgrades.BetterEvents;

import basemod.ReflectionHacks;
import betterAltar.potions.AltarPotion;
import betterAltar.relics.BloodRelic;
import betterAltar.util.AbstractEventDialog;
import com.badlogic.gdx.math.MathUtils;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.events.AbstractEvent;
import com.megacrit.cardcrawl.localization.UIStrings;
import com.megacrit.cardcrawl.relics.BloodVial;
import com.megacrit.cardcrawl.relics.BloodyIdol;
import upgradeableevents.interfaces.AbstractEventUpgrade;

import static upgradeableevents.UpgradeableEvents.makeID;

@SpirePatch(
        optional = true,
        cls = "betterAltar.events.BetterAltarEvent",
        method = SpirePatch.CLASS
)
public class BetterAltarUpgrade extends AbstractEventUpgrade {
    private static final String BETTER_ALTAR_CLASS = "betterAltar.BetterAltar";
    private static final String BETTER_ALTAR_EVENT_CLASS = "betterAltar.events.BetterAltarEvent";
    public static boolean isBetterAltarLoaded = false;
    public static Class<?> eventClass = null;

    static {
        try {
            Class.forName(BETTER_ALTAR_CLASS);
            eventClass = Class.forName(BETTER_ALTAR_EVENT_CLASS);
            isBetterAltarLoaded = true;
        } catch (Exception e) {
            isBetterAltarLoaded = false;
        }
    }

    private static final UIStrings uiStrings = CardCrawlGame.languagePack.getUIString(makeID("BetterAltarUpgrade"));
    private boolean isUpgraded = false;

    public BetterAltarUpgrade(AbstractEvent event) {
        super(event, (AbstractEvent e) -> {
            if (!isBetterAltarLoaded) return false;
            int screenNum = ReflectionHacks.getPrivate(e, AbstractEvent.class, "screenNum");
            return screenNum == 0;
        });
    }

    public void upgrade() {
        if (!canBeUpgraded()) return;
        isUpgraded = true;
        clearAndRebuildOptions();
    }

    public boolean isUpgraded() {
        return isUpgraded;
    }

    protected void rebuildOptions() {
        if (!isBetterAltarLoaded) return;

        String[] OPTIONS = ReflectionHacks.getPrivateStatic(eventClass, "OPTIONS");
        boolean idol = ReflectionHacks.getPrivate(event, eventClass, "idol");
        boolean vial = ReflectionHacks.getPrivate(event, eventClass, "vial");
        AbstractEventDialog eventText = ReflectionHacks.getPrivate(event, eventClass, "EventText");

        // Calculate new HP loss values
        int reducedHpLoss1 = MathUtils.round(AbstractDungeon.player.maxHealth * 0.25F); // 25% for no idol
        int reducedHpLoss2 = MathUtils.round(AbstractDungeon.player.maxHealth * 0.13F); // 13% for no vial
        int reducedHpLoss3 = MathUtils.round(AbstractDungeon.player.maxHealth * 0.12F); // 12% for potion
        int smallHpLoss = MathUtils.round(AbstractDungeon.player.maxHealth * 0.05F); // 5% for defying

        // Clear both dialog sets
        eventText.clearAllDialogs();
        event.imageEventText.clearAllDialogs();

        // Bloody Idol option
        if (idol) {
            eventText.setDialogOption(uiStrings.TEXT[0], new BloodyIdol());
        } else {
            eventText.setDialogOption(OPTIONS[0] + reducedHpLoss1 + OPTIONS[1], new BloodyIdol());
        }

        // Blood Relic option
        if (vial) {
            eventText.setDialogOption(uiStrings.TEXT[1], new BloodRelic());
        } else {
            eventText.setDialogOption(OPTIONS[0] + reducedHpLoss2 + OPTIONS[2], new BloodVial());
        }

        // Altar Potion option
        eventText.setDialogOption(OPTIONS[0] + reducedHpLoss3 + OPTIONS[5], new AltarPotion());

        // Replace Decay with small HP loss option
        eventText.setDialogOption(uiStrings.TEXT[2] + smallHpLoss + uiStrings.TEXT[3]);
    }
}
