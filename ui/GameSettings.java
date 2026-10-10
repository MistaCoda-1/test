package ui;

// Settings chosen in the OPTIONS menu. Kept for the whole session.
public final class GameSettings {
   public enum Difficulty {
      EASY(0.5),
      NORMAL(1.0),
      HARD(1.5);

      private final double damageMultiplier;

      Difficulty(double damageMultiplier) {
         this.damageMultiplier = damageMultiplier;
      }
   }

   private static Difficulty difficulty = Difficulty.NORMAL;

   private GameSettings() {
   }

   public static Difficulty getDifficulty() {
      return difficulty;
   }

   public static void cycleDifficulty(int direction) {
      Difficulty[] values = Difficulty.values();
      int next = Math.floorMod(difficulty.ordinal() + direction, values.length);
      difficulty = values[next];
   }

   // Bullet damage after the difficulty multiplier. A hit always does at least 1 damage.
   public static int scaleDamage(int damage) {
      return Math.max(1, (int)Math.round(damage * difficulty.damageMultiplier));
   }
}
