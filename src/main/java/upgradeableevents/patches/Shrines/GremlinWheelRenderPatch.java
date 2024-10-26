package upgradeableevents.patches.Shrines;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.events.shrines.GremlinWheelGame;
import upgradeableevents.util.ButtonEffectHelper;

@SpirePatch(clz = GremlinWheelGame.class, method = "render")
public class GremlinWheelRenderPatch {
    @SpirePostfixPatch
    public static void Postfix(GremlinWheelGame __instance, SpriteBatch sb) {
        ButtonEffectHelper.updateUpgradeAvailability();
    }
}