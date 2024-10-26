package upgradeableevents.patches;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.potions.AbstractPotion;
import com.megacrit.cardcrawl.potions.BlessingOfTheForge;
import upgradeableevents.UpgradeEventManager;

import java.util.HashMap;
import java.util.Map;

@SpirePatch(clz = AbstractPotion.class, method = "render")
public class PotionRenderPatch {
    private static final float PULSE_SPEED = 3f;
    private static final int NUM_ORBS = 3;
    private static final float ORB_RADIUS = 0.3f;
    private static final float SPARKLE_SIZE = 12f;
    private static final float ROTATION_SPEED = 120f;

    // Map to store timer for each potion instance
    private static final Map<AbstractPotion, Float> pulseTimers = new HashMap<>();

    @SpirePostfixPatch
    public static void Postfix(AbstractPotion __instance, SpriteBatch sb) {
        // Clear timers if player has no Blessing of the Forge potions
        if (!AbstractDungeon.player.hasPotion(BlessingOfTheForge.POTION_ID) && !pulseTimers.isEmpty()) {
            pulseTimers.clear();
        }

        if (__instance instanceof BlessingOfTheForge && UpgradeEventManager.canUpgradeCurrentEvent()) {
            float pulseTimer = pulseTimers.getOrDefault(__instance, 0f);
            pulseTimer += Gdx.graphics.getDeltaTime() * PULSE_SPEED;
            pulseTimers.put(__instance, pulseTimer);

            float alpha = (MathUtils.sin(pulseTimer) + 1.0f) * 0.25f + 0.25f;
            int srcFunc = sb.getBlendSrcFunc();
            int dstFunc = sb.getBlendDstFunc();
            Color oldColor = sb.getColor();
            try {
                sb.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
                for (int i = 0; i < NUM_ORBS; i++) {
                    float angle = (pulseTimer * ROTATION_SPEED + i * (360f / NUM_ORBS)) % 360f;
                    float sparkleX = __instance.posX + MathUtils.cosDeg(angle) * __instance.hb.width * ORB_RADIUS;
                    float sparkleY = __instance.posY + MathUtils.sinDeg(angle) * __instance.hb.height * ORB_RADIUS;
                    sb.setColor(1.0f, 0.9f, 0.2f, alpha);
                    sb.draw(ImageMaster.GLOW_SPARK_2,
                            sparkleX - SPARKLE_SIZE/2f,
                            sparkleY - SPARKLE_SIZE/2f,
                            SPARKLE_SIZE/2f,
                            SPARKLE_SIZE/2f,
                            SPARKLE_SIZE,
                            SPARKLE_SIZE,
                            1.0f,
                            1.0f,
                            0.0f);
                }
            } finally {
                sb.setBlendFunction(srcFunc, dstFunc);
                sb.setColor(oldColor);
            }
        }
    }
}