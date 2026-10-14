package battle.bullets;

// The one place to tune how much harder the game gets. Level 1 is the base amount of bullets; every level
// after that adds EXTRA_PER_LEVEL on top, until MAX_MULTIPLIER is reached. There is no last level, so the
// cap is what keeps an endless run from becoming impossible.
public final class BulletScaling {
   // Extra bullets per level, as a fraction of the base amount.
   // 0.05 -> level 2 has 5% more bullets than level 1, level 11 has 50% more, level 21 has twice as many.
   // Set it to 0 to turn scaling off (handy for testing).
   public static final double EXTRA_PER_LEVEL = 0.05;

   // Upper limit for the multiplier (2.0 = twice the base amount of bullets).
   public static final double MAX_MULTIPLIER = 2.0;

   private BulletScaling() {
   }

   // 1.0 = base amount of bullets, 2.0 = twice as many.
   public static double multiplier(int level) {
      double multiplier = 1.0 + EXTRA_PER_LEVEL * (level - 1);
      return Math.max(1.0, Math.min(MAX_MULTIPLIER, multiplier));
   }
}
