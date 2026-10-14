package battle.bullets.patterns;

import java.awt.Image;
import java.util.concurrent.ThreadLocalRandom;

import battle.bullets.BulletPattern;

// Like GapWall, but the wall slides in from the left or right wall and the gap is a band of rows.
public class SideWall extends BulletPattern {
   private static final int SPAWN_EVERY_TICKS = 60;
   private static final int SLOT = 20;
   private static final int GAP_SLOTS = 4;
   private static final int MAX_GAP_SHIFT = 3;
   private static final double SPEED = 3.0;
   private static final int DAMAGE = 2;
   private final Image bulletSprite;
   private final int slots;
   private int gapStart;

   public SideWall(int arenaWidth, int arenaHeight, Image bulletSprite, double density) {
      super(arenaWidth, arenaHeight, 300, density);
      this.bulletSprite = bulletSprite;
      this.slots = (int)Math.ceil((double)arenaHeight / SLOT);
      this.gapStart = (this.slots - GAP_SLOTS) / 2;
   }

   @Override
   protected void spawnTick(int elapsedTicks) {
      if (this.spawnsOn(elapsedTicks, SPAWN_EVERY_TICKS)) {
         ThreadLocalRandom random = ThreadLocalRandom.current();
         this.gapStart = Math.clamp((long)(this.gapStart + random.nextInt(-MAX_GAP_SHIFT, MAX_GAP_SHIFT + 1)), 0, this.slots - GAP_SLOTS);
         boolean fromLeft = random.nextBoolean();
         double x = fromLeft ? -SLOT : this.arenaWidth;
         for (int slot = 0; slot < this.slots; slot++) {
            boolean inGap = slot >= this.gapStart && slot < this.gapStart + GAP_SLOTS;
            if (!inGap) {
               this.bullets.add(this.makeBullet(x, slot * SLOT, fromLeft ? SPEED : -SPEED, 0.0, SLOT, SLOT, DAMAGE, this.bulletSprite));
            }
         }
      }
   }
}
