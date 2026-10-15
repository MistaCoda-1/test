package battle;

import battle.bullets.patterns.*;
import battle.bullets.BulletPattern;

import java.awt.Image;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.DoubleFunction;

// Picks a bullet pattern for each enemy turn, never the same one twice in a row.
class PatternPicker {
   // Each entry builds a new pattern for a given density (see BulletScaling).
   private final List<DoubleFunction<BulletPattern>> pool;
   private int lastIndex = -1;

   PatternPicker(int arenaWidth, int arenaHeight, Image bulletSprite, Image needleSprite) {
      this.pool = List.of(
         density -> new Rain(arenaWidth, arenaHeight, bulletSprite, density),
         density -> new SideSweep(arenaWidth, arenaHeight, bulletSprite, density),
         density -> new CrossFire(arenaWidth, arenaHeight, needleSprite, bulletSprite, density),
         density -> new GapWall(arenaWidth, arenaHeight, bulletSprite, density),
         density -> new SideWall(arenaWidth, arenaHeight, bulletSprite, density),
         density -> new SineRain(arenaWidth, arenaHeight, bulletSprite, density),
         density -> new DiagonalRain(arenaWidth, arenaHeight, bulletSprite, density),
         density -> new FanShot(arenaWidth, arenaHeight, bulletSprite, density),
         density -> new Converge(arenaWidth, arenaHeight, bulletSprite, density),
         density -> new SweepingStream(arenaWidth, arenaHeight, bulletSprite, density)
      );
   }

   BulletPattern next(double density) {
      ThreadLocalRandom random = ThreadLocalRandom.current();
      int index;
      do {
         index = random.nextInt(this.pool.size());
      } while (index == this.lastIndex);
      this.lastIndex = index;
      return this.pool.get(index).apply(density);
   }
}
