package upgradeableevents.patches.Shrines;

import basemod.ReflectionHacks;
import com.badlogic.gdx.math.MathUtils;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.events.shrines.GremlinWheelGame;
import com.megacrit.cardcrawl.helpers.Hitbox;
import com.megacrit.cardcrawl.helpers.controller.CInputActionSet;
import com.megacrit.cardcrawl.helpers.input.InputHelper;
import com.megacrit.cardcrawl.relics.Ectoplasm;

import java.util.ArrayList;

@SpirePatch(clz = GremlinWheelGame.class, method = "update")
public class GremlinWheelUpdatePatch {
    @SpirePrefixPatch
    public static void Prefix(GremlinWheelGame __instance) {
        boolean upgraded = GremlinWheelUpgradedField.upgraded.get(__instance);
        if (!upgraded) return;

        boolean startSpin = ReflectionHacks.getPrivate(__instance, GremlinWheelGame.class, "startSpin");
        boolean buttonPressed = ReflectionHacks.getPrivate(__instance, GremlinWheelGame.class, "buttonPressed");
        float bounceTimer = ReflectionHacks.getPrivate(__instance, GremlinWheelGame.class, "bounceTimer");
        Hitbox buttonHb = ReflectionHacks.getPrivate(__instance, GremlinWheelGame.class, "buttonHb");

        // This is the exact moment when the wheel spin is initiated
        if (bounceTimer == 0.0F && startSpin && !buttonPressed &&
                (buttonHb.hovered && InputHelper.justClickedLeft || CInputActionSet.proceed.isJustPressed())) {

            // Calculate result based on conditions
            float healthPercentage = (float) AbstractDungeon.player.currentHealth / (float)AbstractDungeon.player.maxHealth;
            boolean hasEctoplasm = AbstractDungeon.player.hasRelic(Ectoplasm.ID);

            // Create list of possible results
            ArrayList<Integer> possibleResults = new ArrayList<>();

            // Add possible results based on conditions
            if (!hasEctoplasm) {
                possibleResults.add(0);  // Gold
            }
            possibleResults.add(1);      // Relic
            if (healthPercentage < 0.8f) {
                possibleResults.add(2);  // Heal
            }
            possibleResults.add(4);      // Remove

            // Select result
            int result;
            if (healthPercentage <= 0.3f && possibleResults.contains(2)) {
                result = 2;  // Force heal for low HP
            } else {
                result = possibleResults.get(AbstractDungeon.miscRng.random(possibleResults.size() - 1));
            }

            // Calculate final angle
            float resultAngle = (float)result * 60.0F + MathUtils.random(-10.0F, 10.0F);

            // Set the result and angle
            ReflectionHacks.setPrivate(__instance, GremlinWheelGame.class, "result", result);
            ReflectionHacks.setPrivate(__instance, GremlinWheelGame.class, "resultAngle", resultAngle);
        }

        // Log state during spin
        if ((startSpin || (boolean)ReflectionHacks.getPrivate(__instance, GremlinWheelGame.class, "finishSpin")) && upgraded) {
        }
    }
}