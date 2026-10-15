package battle.bullets;

// The one place to tune how much harder the game gets. Level 1 is the base amount of bullets; every level
// after that adds EXTRA_PER_LEVEL on top, until MAX_MULTIPLIER is reached. There is no last level, so the
// cap is what keeps an endless run from becoming impossible.
public final class BulletScaling {
   // Extra bullets per level, as a fraction of the base amount.
   // Set to 0 to turn scaling off (for testing).
   public static final double EXTRA_PER_LEVEL = 0.05;

   public static final double MAX_MULTIPLIER = 4.5;

   private BulletScaling() {
   }

   public static double multiplier(int level) {
      double multiplier = 1.0 + EXTRA_PER_LEVEL * (level - 1);
      return Math.max(1.0, Math.min(MAX_MULTIPLIER, multiplier));
   }
}
