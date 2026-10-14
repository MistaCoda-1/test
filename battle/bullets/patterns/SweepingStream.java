package battle.bullets.patterns;

import java.awt.Image;

import battle.bullets.BulletPattern;

// A shooter slides back and forth along the top wall, dropping a steady stream of bullets as it goes.
public class SweepingStream extends BulletPattern {
   private static final int SPAWN_EVERY_TICKS = 15;
   private static final int SWEEP_TICKS = 120;
   private static final int BULLET_SIZE = 12;
   private static final double FALL_SPEED = 3.0;
   private static final int DAMAGE = 2;
   private final Image bulletSprite;

   public SweepingStream(int arenaWidth, int arenaHeight, Image bulletSprite, double density) {
      super(arenaWidth, arenaHeight, 300, density);
      this.bulletSprite = bulletSprite;
   }

   @Override
   protected void spawnTick(int elapsedTicks) {
      if (this.spawnsOn(elapsedTicks, SPAWN_EVERY_TICKS)) {
         // 0 -> 1 -> 0 over SWEEP_TICKS: the shooter's position along the wall as a fraction of its width.
         double phase = (elapsedTicks % SWEEP_TICKS) / (double)SWEEP_TICKS;
         double position = phase < 0.5 ? phase * 2 : 2 - phase * 2;
         double x = position * (this.arenaWidth - BULLET_SIZE);
         this.bullets.add(this.makeBullet(x, -20.0, 0.0, FALL_SPEED, BULLET_SIZE, BULLET_SIZE, DAMAGE, this.bulletSprite));
      }
   }
}
