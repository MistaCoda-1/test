package battle.bullets.patterns;

import java.awt.Color;
import java.awt.Image;
import java.util.concurrent.ThreadLocalRandom;

import battle.bullets.Bullet;
import battle.bullets.BulletPattern;

public class Rain extends BulletPattern {
   private static final int SPAWN_EVERY_TICKS = 10;
   private final Image bulletSprite;

   public Rain(int arenaWidth, int arenaHeight, Image bulletSprite, double density) {
      super(arenaWidth, arenaHeight, 300, density);
      this.bulletSprite = bulletSprite;
   }

   @Override
   protected void spawnTick(int elapsedTicks) {
      if (this.spawnsOn(elapsedTicks, SPAWN_EVERY_TICKS)) {
         double x = ThreadLocalRandom.current().nextDouble(0.0, Math.max(1, this.arenaWidth - 20));
         if (this.bulletSprite != null) {
            this.bullets.add(new Bullet(x, -20.0, 0.0, 4.5, 20, 20, 2, this.bulletSprite));
         } else {
            this.bullets.add(new Bullet(x, -20.0, 0.0, 4.5, 20, 20, 2, Color.WHITE));
         }
      }
   }
}
