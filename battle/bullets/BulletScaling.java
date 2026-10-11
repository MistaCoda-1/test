package battle.bullets;

// The one place to tune how much harder each fight gets. Fight 1 of every enemy is the base amount of
// bullets; each later fight adds EXTRA_PER_FIGHT on top (fightNumber() comes from the Enemies class).
public final class BulletScaling {
   // Extra bullets per fight, as a fraction of the base amount.
   // 0.15 -> fight 2 has 15% more bullets than fight 1, fight 3 has 30% more, ... fight 5 has 60% more.
   // Set it to 0 to turn scaling off (handy for testing).
   public static final double EXTRA_PER_FIGHT = 0.15;

   // Upper limit for the multiplier, so a big EXTRA_PER_FIGHT can't make a fight impossible.
   public static final double MAX_MULTIPLIER = 2.0;

   private BulletScaling() {
   }

   // 1.0 = base amount of bullets, 2.0 = twice as many.
   public static double multiplier(int fightNumber) {
      double multiplier = 1.0 + EXTRA_PER_FIGHT * (fightNumber - 1);
      return Math.max(1.0, Math.min(MAX_MULTIPLIER, multiplier));
   }
}
