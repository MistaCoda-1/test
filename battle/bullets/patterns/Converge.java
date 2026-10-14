package battle.bullets.patterns;

import java.awt.Image;
import java.util.concurrent.ThreadLocalRandom;

import battle.bullets.BulletPattern;

// A ring of bullets closes in on the middle of the arena. The safe spot is between two of them.
public class Converge extends BulletPattern {
   private static final int RING_EVERY_TICKS = 100;
   private static final int BULLETS_PER_RING = 8;
   private static final int BULLET_SIZE = 14;
   private static final double SPEED = 2.2;
   private static final int DAMAGE = 2;
   // How far outside the arena edge the ring starts. Must stay under the 20px buffer in Bullet.isOffscreen,
   // or the bullets would be removed the moment they spawn.
   private static final int START_OUTSIDE = 15;
   private final Image bulletSprite;

   public Converge(int arenaWidth, int arenaHeight, Image bulletSprite, double density) {
      super(arenaWidth, arenaHeight, 300, density);
      this.bulletSprite = bulletSprite;
   }

   @Override
   protected void spawnTick(int elapsedTicks) {
      if (this.spawnsOn(elapsedTicks, RING_EVERY_TICKS)) {
         double centerX = this.arenaWidth / 2.0;
         double centerY = this.arenaHeight / 2.0;
         double offset = ThreadLocalRandom.current().nextDouble(0.0, Math.PI * 2);
         for (int i = 0; i < BULLETS_PER_RING; i++) {
            double angle = offset + i * (Math.PI * 2 / BULLETS_PER_RING);
            double dirX = Math.cos(angle);
            double dirY = Math.sin(angle);
            // Walk from the center along this direction until we hit the (slightly enlarged) arena edge.
            double distX = (centerX + START_OUTSIDE) / Math.max(1.0E-6, Math.abs(dirX));
            double distY = (centerY + START_OUTSIDE) / Math.max(1.0E-6, Math.abs(dirY));
            double dist = Math.min(distX, distY);
            double x = centerX + dirX * dist - BULLET_SIZE / 2.0;
            double y = centerY + dirY * dist - BULLET_SIZE / 2.0;
            this.bullets.add(this.makeBullet(x, y, -dirX * SPEED, -dirY * SPEED, BULLET_SIZE, BULLET_SIZE, DAMAGE, this.bulletSprite));
         }
      }
   }
}
