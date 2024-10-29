package upgradeableevents.eventupgrades.TheCity;

import basemod.ReflectionHacks;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.events.AbstractEvent;
import com.megacrit.cardcrawl.events.shrines.Nloth;
import com.megacrit.cardcrawl.helpers.RelicLibrary;
import com.megacrit.cardcrawl.localization.UIStrings;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.relics.Circlet;
import com.megacrit.cardcrawl.relics.NlothsGift;
import upgradeableevents.interfaces.AbstractEventUpgrade;
import upgradeableevents.interfaces.UpgradeCondition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;
import java.util.stream.Collectors;

import static upgradeableevents.UpgradeableEvents.makeID;

public class NlothUpgrade extends AbstractEventUpgrade {
    private static final UIStrings uiStrings = CardCrawlGame.languagePack.getUIString(makeID("NlothUpgrade"));
    private boolean isUpgraded = false;
    private AbstractRelic bonusRelic1;
    private AbstractRelic bonusRelic2;

    public NlothUpgrade(Nloth event) {
        super(event, new NlothUpgradeCondition());
        // Generate two random relics the player doesn't have
        generateBonusRelics();
    }

    private void generateBonusRelics() {
        ArrayList<AbstractRelic> possibleRelics = new ArrayList<>();

        // Add relics player doesn't have from each tier
        for (AbstractRelic.RelicTier tier : AbstractRelic.RelicTier.values()) {
            if (tier != AbstractRelic.RelicTier.SPECIAL) {
                ArrayList<String> relicPool = new ArrayList<>(AbstractDungeon.commonRelicPool);
                for (String relicId : relicPool) {
                    if (!AbstractDungeon.player.hasRelic(relicId)) {
                        AbstractRelic relic = RelicLibrary.getRelic(relicId);
                        if (relic != null) {
                            possibleRelics.add(relic);
                        }
                    }
                }
            }
        }

        // Shuffle and pick two different relics
        Collections.shuffle(possibleRelics, new Random(AbstractDungeon.miscRng.randomLong()));
        if (possibleRelics.size() >= 2) {
            bonusRelic1 = possibleRelics.get(0).makeCopy();
            bonusRelic2 = possibleRelics.get(1).makeCopy();
        } else {
            // Fallback if not enough unique relics available
            bonusRelic1 = new Circlet();
            bonusRelic2 = new Circlet();
        }
    }

    private static class NlothUpgradeCondition implements UpgradeCondition {
        @Override
        public boolean canUpgrade(AbstractEvent event) {
            Nloth nlothEvent = (Nloth)event;
            int screenNum = ReflectionHacks.getPrivate(nlothEvent, Nloth.class, "screenNum");
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
        Nloth nlothEvent = (Nloth)event;

        // Make entire relic name green by adding #g before each word
        String greenRelic1 = Arrays.stream(bonusRelic1.name.split(" "))
                .map(word -> "#g" + word)
                .collect(Collectors.joining(" "));

        String greenRelic2 = Arrays.stream(bonusRelic2.name.split(" "))
                .map(word -> "#g" + word)
                .collect(Collectors.joining(" "));

        // Combine text pieces in correct order: "Obtain (green relic name) and a special relic"
        nlothEvent.imageEventText.setDialogOption(
                uiStrings.TEXT[0] + greenRelic1 + uiStrings.TEXT[1],
                new NlothsGift());

        nlothEvent.imageEventText.setDialogOption(
                uiStrings.TEXT[0] + greenRelic2 + uiStrings.TEXT[1],
                new NlothsGift());

        nlothEvent.imageEventText.setDialogOption(Nloth.OPTIONS[2]);
    }
}