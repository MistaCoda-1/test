package battle.bullets;

import java.awt.Color;
import java.awt.Image;
import java.util.concurrent.ThreadLocalRandom;

public class BulletRain extends BulletPattern {
   private static final int SPAWN_EVERY_TICKS = 10;
   private static final int BULLET_SIZE = 20;
   private static final double FALL_SPEED = 4.5;
   private static final int DAMAGE = 2;
   private final Image bulletSprite;

   public BulletRain(int arenaWidth, int arenaHeight, Image bulletSprite) {
      super(arenaWidth, arenaHeight, 300);
      this.bulletSprite = bulletSprite;
   }

   @Override
   protected void spawnTick(int elapsedTicks) {
      if (elapsedTicks % 10 == 0) {
         double x = ThreadLocalRandom.current().nextDouble(0.0, Math.max(1, this.arenaWidth - 20));
         if (this.bulletSprite != null) {
            this.bullets.add(new Bullet(x, -20.0, 0.0, 4.5, 20, 20, 2, this.bulletSprite));
         } else {
            this.bullets.add(new Bullet(x, -20.0, 0.0, 4.5, 20, 20, 2, Color.WHITE));
         }
      }
   }
}
