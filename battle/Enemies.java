package battle;

import java.awt.Color;

// The enemy line-up and the level math: which enemy you face on a level, and which fight of its stretch it is.
// The game is endless, so after the last enemy the line-up starts over from the first.
final class Enemies {
   static final int FIGHTS_PER_ENEMY = 2;

   private static final Enemy[] ROSTER = {
      new Enemy("/resources/enemy.png", 20, "*A seemingly angry frog hops towards you.*",
         "Froggit", null,
         "Froggit - HP 20. It looks angry.",
         "Sing along", "You sing along. Froggit croaks happily."),
      new Enemy("/resources/enemy.png", 30, "'Ribbit. Who is you, cuz?'",
         "Fragsta", new Color(90, 170, 255),
         "Fragsta - HP 30. His aura is overflowing.",
         "Compliment", "You compliment Fragsta's drip. He daps you up"),
      new Enemy("/resources/enemy.png", 40, "'Kneel before me!'",
         "King Frog", new Color(255, 90, 40),
         "King Frog - HP 40. Thinks it rules the pond.",
         "Bow", "You bow to King Frog. It accepts your loyalty.")
   };

   private Enemies() {
   }

   static Enemy forLevel(int level) {
      return ROSTER[((level - 1) / FIGHTS_PER_ENEMY) % ROSTER.length];
   }

   // 1 to FIGHTS_PER_ENEMY: how far into the current enemy's stretch this level is.
   static int fightNumber(int level) {
      return (level - 1) % FIGHTS_PER_ENEMY + 1;
   }
}
