package battle.bullets.patterns;

import java.awt.Image;
import java.util.concurrent.ThreadLocalRandom;

import battle.bullets.BulletPattern;

// Every so often a spot on the top wall fires a fan of bullets that spread out as they fall.
public class FanShot extends BulletPattern {
   private static final int SHOT_EVERY_TICKS = 60;
   private static final int BULLETS_PER_FAN = 5;
   private static final double SPREAD_DEGREES = 20.0;
   private static final int BULLET_SIZE = 16;
   private static final double SPEED = 3.2;
   private static final int EDGE_MARGIN = 40;
   private static final int DAMAGE = 3;
   private final Image bulletSprite;

   public FanShot(int arenaWidth, int arenaHeight, Image bulletSprite, double density) {
      super(arenaWidth, arenaHeight, 300, density);
      this.bulletSprite = bulletSprite;
   }

   @Override
   protected void spawnTick(int elapsedTicks) {
      if (this.spawnsOn(elapsedTicks, SHOT_EVERY_TICKS)) {
         double x = ThreadLocalRandom.current().nextDouble(EDGE_MARGIN, this.arenaWidth - EDGE_MARGIN);
         int middle = BULLETS_PER_FAN / 2;
         for (int i = 0; i < BULLETS_PER_FAN; i++) {
            // 0 degrees is straight down; the fan is centered on it.
            double angle = Math.toRadians((i - middle) * SPREAD_DEGREES);
            this.bullets.add(this.makeBullet(x, -20.0, SPEED * Math.sin(angle), SPEED * Math.cos(angle), BULLET_SIZE, BULLET_SIZE, DAMAGE, this.bulletSprite));
         }
      }
   }
}
