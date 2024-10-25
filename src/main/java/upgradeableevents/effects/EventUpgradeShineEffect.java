package upgradeableevents.effects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.ScreenShake;
import com.megacrit.cardcrawl.vfx.AbstractGameEffect;
import com.megacrit.cardcrawl.vfx.UpgradeHammerImprintEffect;
import com.megacrit.cardcrawl.vfx.UpgradeShineParticleEffect;

public class EventUpgradeShineEffect extends AbstractGameEffect {
    private float x;
    private float y;
    private boolean clang1 = false;
    private boolean clang2 = false;
    private int numImprints;
    private float horizontalSpacing;
    private float verticalSpacing;

    public EventUpgradeShineEffect(float x, float y, int numImprints, float horizontalSpacing, float verticalSpacing) {
        this.x = x;
        this.y = y;
        this.duration = 0.8F;
        this.numImprints = numImprints;
        this.horizontalSpacing = horizontalSpacing;
        this.verticalSpacing = verticalSpacing;
    }

    public void update() {
        if (this.duration < 0.6F && !this.clang1) {
            CardCrawlGame.sound.play("CARD_UPGRADE");
            this.clang1 = true;
            this.clank(
                    this.x - this.horizontalSpacing * Settings.scale,
                    this.y + 0.0F * Settings.scale
            );
            CardCrawlGame.screenShake.shake(ScreenShake.ShakeIntensity.HIGH, ScreenShake.ShakeDur.SHORT, false);
        }

        if (this.duration < 0.2F && !this.clang2) {
            this.clang2 = true;
            this.clank(
                    this.x + this.horizontalSpacing * Settings.scale,
                    this.y - this.verticalSpacing * Settings.scale
            );
            CardCrawlGame.screenShake.shake(ScreenShake.ShakeIntensity.HIGH, ScreenShake.ShakeDur.SHORT, false);
        }

        this.duration -= Gdx.graphics.getDeltaTime();
        if (this.duration < 0.0F) {
            this.clank(
                    this.x + (this.horizontalSpacing / 3.0F) * Settings.scale,
                    this.y + this.verticalSpacing * Settings.scale
            );
            this.isDone = true;
            CardCrawlGame.screenShake.shake(ScreenShake.ShakeIntensity.HIGH, ScreenShake.ShakeDur.SHORT, false);
        }
    }

    private void clank(float x, float y) {
        for (int i = 0; i < this.numImprints; i++) {
            AbstractDungeon.topLevelEffectsQueue.add(new UpgradeHammerImprintEffect(x, y));
        }

        if (!Settings.DISABLE_EFFECTS) {
            for(int i = 0; i < 30; ++i) {
                AbstractDungeon.topLevelEffectsQueue.add(new UpgradeShineParticleEffect(
                        x + MathUtils.random(-10.0F, 10.0F) * Settings.scale,
                        y + MathUtils.random(-10.0F, 10.0F) * Settings.scale
                ));
            }
        }
    }

    public void render(SpriteBatch sb) {
    }

    public void dispose() {
    }
}