package battle.bullets;

import java.util.ArrayList;
import java.util.List;

public abstract class BulletPattern {
   protected final int arenaWidth;
   protected final int arenaHeight;
   protected final List<Bullet> bullets = new ArrayList<>();
   private final int durationTicks;
   private int elapsedTicks = 0;

   protected BulletPattern(int arenaWidth, int arenaHeight, int durationTicks) {
      this.arenaWidth = arenaWidth;
      this.arenaHeight = arenaHeight;
      this.durationTicks = durationTicks;
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

   public boolean isOver() {
      return this.elapsedTicks >= this.durationTicks && this.bullets.isEmpty();
   }

   public List<Bullet> getBullets() {
      return this.bullets;
   }
}
