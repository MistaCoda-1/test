package battle.bullets.patterns;

import java.awt.Image;
import java.util.concurrent.ThreadLocalRandom;

import battle.bullets.BulletPattern;

// A wall of bullets falls from the top with a gap in it. Each gap is only a few steps away from the last
// one, so the player can always get through
public class GapWall extends BulletPattern {
   private static final int SPAWN_EVERY_TICKS = 55;
   private static final int SLOT = 20;
   private static final int GAP_SLOTS = 4;
   private static final int MAX_GAP_SHIFT = 3;
   private static final double FALL_SPEED = 2.5;
   private static final int DAMAGE = 2;
   private final Image bulletSprite;
   private final int slots;
   private int gapStart;

   public GapWall(int arenaWidth, int arenaHeight, Image bulletSprite, double density) {
      super(arenaWidth, arenaHeight, 300, density);
      this.bulletSprite = bulletSprite;
      this.slots = (int)Math.ceil((double)arenaWidth / SLOT);
      this.gapStart = (this.slots - GAP_SLOTS) / 2;
   }

   @Override
   protected void spawnTick(int elapsedTicks) {
      if (this.spawnsOn(elapsedTicks, SPAWN_EVERY_TICKS)) {
         int shift = ThreadLocalRandom.current().nextInt(-MAX_GAP_SHIFT, MAX_GAP_SHIFT + 1);
         this.gapStart = Math.clamp((long)(this.gapStart + shift), 0, this.slots - GAP_SLOTS);
         for (int slot = 0; slot < this.slots; slot++) {
            boolean inGap = slot >= this.gapStart && slot < this.gapStart + GAP_SLOTS;
            if (!inGap) {
               this.bullets.add(this.makeBullet(slot * SLOT, -20.0, 0.0, FALL_SPEED, SLOT, SLOT, DAMAGE, this.bulletSprite));
            }
         }
      }
   }
}
