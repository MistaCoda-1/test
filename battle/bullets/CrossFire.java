package battle.bullets;

import java.awt.Color;
import java.awt.Image;
import java.util.concurrent.ThreadLocalRandom;

// Fast falling needles combined with bullets from the side walls.
public class CrossFire extends BulletPattern {
   private static final int NEEDLE_EVERY_TICKS = 12;
   private static final int SIDE_EVERY_TICKS = 22;
   private static final int NEEDLE_W = 12;
   private static final int NEEDLE_H = 27;
   private static final int SIDE_W = 20;
   private static final int SIDE_H = 16;
   private static final double NEEDLE_SPEED = 5.5;
   private static final double SIDE_SPEED = 3.5;
   private static final int DAMAGE = 4;
   private final Image needleSprite;
   private final Image sideSprite;

   public CrossFire(int arenaWidth, int arenaHeight, Image needleSprite, Image sideSprite, double density) {
      super(arenaWidth, arenaHeight, 300, density);
      this.needleSprite = needleSprite;
      this.sideSprite = sideSprite;
   }

   @Override
   protected void spawnTick(int elapsedTicks) {
      ThreadLocalRandom random = ThreadLocalRandom.current();
      if (this.spawnsOn(elapsedTicks, NEEDLE_EVERY_TICKS)) {
         double x = random.nextDouble(0.0, Math.max(1, this.arenaWidth - NEEDLE_W));
         this.bullets.add(this.make(x, -20.0, 0.0, NEEDLE_SPEED, NEEDLE_W, NEEDLE_H, this.needleSprite));
      }

      if (this.spawnsOn(elapsedTicks, SIDE_EVERY_TICKS)) {
         double y = random.nextDouble(0.0, Math.max(1, this.arenaHeight - SIDE_H));
         boolean fromLeft = random.nextBoolean();
         double x = fromLeft ? -SIDE_W : this.arenaWidth;
         this.bullets.add(this.make(x, y, fromLeft ? SIDE_SPEED : -SIDE_SPEED, 0.0, SIDE_W, SIDE_H, this.sideSprite));
      }
   }

   private Bullet make(double x, double y, double vx, double vy, int w, int h, Image sprite) {
      return sprite != null ? new Bullet(x, y, vx, vy, w, h, DAMAGE, sprite) : new Bullet(x, y, vx, vy, w, h, DAMAGE, Color.WHITE);
   }
}
