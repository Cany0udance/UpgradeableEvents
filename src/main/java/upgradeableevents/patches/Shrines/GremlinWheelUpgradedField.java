package upgradeableevents.patches.Shrines;

import com.evacipated.cardcrawl.modthespire.lib.SpireField;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.megacrit.cardcrawl.events.shrines.GremlinWheelGame;

@SpirePatch(clz = GremlinWheelGame.class, method = SpirePatch.CLASS)
public class GremlinWheelUpgradedField {
    public static SpireField<Boolean> upgraded = new SpireField<>(() -> false);
}