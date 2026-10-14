package battle.bullets.patterns;

import java.awt.Color;
import java.awt.Image;
import java.util.concurrent.ThreadLocalRandom;

import battle.bullets.Bullet;
import battle.bullets.BulletPattern;

// Bullets fly in horizontally from the left and right walls at random heights.
public class SideSweep extends BulletPattern {
   private static final int SPAWN_EVERY_TICKS = 14;
   private static final int BULLET_W = 20;
   private static final int BULLET_H = 16;
   private static final double SPEED = 4.0;
   private static final int DAMAGE = 3;
   private final Image bulletSprite;

   public SideSweep(int arenaWidth, int arenaHeight, Image bulletSprite, double density) {
      super(arenaWidth, arenaHeight, 300, density);
      this.bulletSprite = bulletSprite;
   }

   @Override
   protected void spawnTick(int elapsedTicks) {
      if (this.spawnsOn(elapsedTicks, SPAWN_EVERY_TICKS)) {
         ThreadLocalRandom random = ThreadLocalRandom.current();
         double y = random.nextDouble(0.0, Math.max(1, this.arenaHeight - BULLET_H));
         boolean fromLeft = random.nextBoolean();
         double x = fromLeft ? -BULLET_W : this.arenaWidth;
         double vx = fromLeft ? SPEED : -SPEED;
         if (this.bulletSprite != null) {
            this.bullets.add(new Bullet(x, y, vx, 0.0, BULLET_W, BULLET_H, DAMAGE, this.bulletSprite));
         } else {
            this.bullets.add(new Bullet(x, y, vx, 0.0, BULLET_W, BULLET_H, DAMAGE, Color.WHITE));
         }
      }
   }
}
