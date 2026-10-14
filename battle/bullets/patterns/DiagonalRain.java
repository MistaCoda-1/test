package battle.bullets.patterns;

import java.awt.Image;
import java.util.concurrent.ThreadLocalRandom;

import battle.bullets.BulletPattern;

// Bullets slant down across the arena, alternating between leaning right and leaning left.
public class DiagonalRain extends BulletPattern {
   private static final int SPAWN_EVERY_TICKS = 12;
   private static final int BULLET_SIZE = 18;
   private static final double SIDE_SPEED = 2.2;
   private static final double FALL_SPEED = 3.6;
   private static final int DAMAGE = 3;
   private final Image bulletSprite;
   private boolean leaningRight = true;

   public DiagonalRain(int arenaWidth, int arenaHeight, Image bulletSprite, double density) {
      super(arenaWidth, arenaHeight, 300, density);
      this.bulletSprite = bulletSprite;
   }

   @Override
   protected void spawnTick(int elapsedTicks) {
      if (this.spawnsOn(elapsedTicks, SPAWN_EVERY_TICKS)) {
         // Leaning right bullets start on the left half, leaning left ones on the right half.
         double half = this.arenaWidth / 2.0;
         double x = ThreadLocalRandom.current().nextDouble(0.0, half) + (this.leaningRight ? 0.0 : half - BULLET_SIZE);
         double vx = this.leaningRight ? SIDE_SPEED : -SIDE_SPEED;
         this.bullets.add(this.makeBullet(x, -20.0, vx, FALL_SPEED, BULLET_SIZE, BULLET_SIZE, DAMAGE, this.bulletSprite));
         this.leaningRight = !this.leaningRight;
      }
   }
}
