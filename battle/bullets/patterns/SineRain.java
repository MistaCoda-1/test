package battle.bullets.patterns;

import java.awt.Image;
import java.util.concurrent.ThreadLocalRandom;

import battle.bullets.Bullet;
import battle.bullets.BulletPattern;

// Bullets drift down while swaying left and right like a wave.
public class SineRain extends BulletPattern {
   private static final int SPAWN_EVERY_TICKS = 16;
   private static final int BULLET_SIZE = 18;
   private static final double FALL_SPEED = 3.0;
   private static final double SWAY = 40.0;
   private static final double SWAY_SPEED = 0.08;
   private static final int DAMAGE = 3;
   private final Image bulletSprite;

   public SineRain(int arenaWidth, int arenaHeight, Image bulletSprite, double density) {
      super(arenaWidth, arenaHeight, 300, density);
      this.bulletSprite = bulletSprite;
   }

   @Override
   protected void spawnTick(int elapsedTicks) {
      if (this.spawnsOn(elapsedTicks, SPAWN_EVERY_TICKS)) {
         ThreadLocalRandom random = ThreadLocalRandom.current();
         double centerX = random.nextDouble(SWAY, Math.max(SWAY + 1, this.arenaWidth - SWAY - BULLET_SIZE));
         this.bullets.add(new WaveBullet(centerX, -20.0, FALL_SPEED, random.nextDouble(0.0, Math.PI * 2), this.bulletSprite));
      }
   }

   // A bullet that moves differently from the plain Bullet: its advance() adds the sideways sway.
   private static class WaveBullet extends Bullet {
      private final double centerX;
      private final double startAngle;
      private int age = 0;

      WaveBullet(double centerX, double y, double fallSpeed, double startAngle, Image sprite) {
         super(centerX, y, 0.0, fallSpeed, BULLET_SIZE, BULLET_SIZE, DAMAGE, sprite);
         this.centerX = centerX;
         this.startAngle = startAngle;
      }

      @Override
      public void advance() {
         this.age++;
         this.x = this.centerX + SWAY * Math.sin(this.startAngle + this.age * SWAY_SPEED);
         this.y = this.y + this.vy;
      }
   }
}
