package battle.bullets;

import java.awt.Image;
import java.util.ArrayList;
import java.util.List;

public abstract class BulletPattern {
   protected final int arenaWidth;
   protected final int arenaHeight;
   protected final List<Bullet> bullets = new ArrayList<>();
   private final int durationTicks;
   private final double density;
   private int elapsedTicks = 0;

   // density: 1.0 spawns the base amount of bullets, 2.0 twice as many (see BulletScaling).
   protected BulletPattern(int arenaWidth, int arenaHeight, int durationTicks, double density) {
      this.arenaWidth = arenaWidth;
      this.arenaHeight = arenaHeight;
      this.durationTicks = durationTicks;
      this.density = density;
   }

   public final void update() {
      if (this.elapsedTicks < this.durationTicks) {
         this.spawnTick(this.elapsedTicks);
      }

      for (Bullet bullet : this.bullets) {
         bullet.advance();
      }

      this.bullets.removeIf(b -> b.isOffscreen(this.arenaWidth, this.arenaHeight));
      this.elapsedTicks++;
   }

   protected abstract void spawnTick(int var1);

   // True on the ticks where something that spawns "every baseTicks" should spawn, sped up by the density.
   // With density 1.0 this is the same as tick % baseTicks == 0; fractional densities work too.
   protected final boolean spawnsOn(int tick, int baseTicks) {
      return Math.floor(tick * this.density / baseTicks) != Math.floor((tick - 1) * this.density / baseTicks);
   }

   // The one place new patterns build bullets. A null sprite draws a plain white box.
   protected final Bullet makeBullet(double x, double y, double vx, double vy, int width, int height, int damage, Image sprite) {
      return new Bullet(x, y, vx, vy, width, height, damage, sprite);
   }

   public boolean isOver() {
      return this.elapsedTicks >= this.durationTicks && this.bullets.isEmpty();
   }

   public List<Bullet> getBullets() {
      return this.bullets;
   }
}
