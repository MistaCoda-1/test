package battle;

import java.awt.Color;

// The enemy line-up and the level math: which enemy you face on a level, and which fight of its stretch it is.
final class Enemies {
   static final int FIGHTS_PER_ENEMY = 5;

   private static final Enemy[] ROSTER = {
      new Enemy("/resources/enemy.png", 30, "*A seemingly angry frog hops towards you.*",
         "FROG", null, Enemy.BulletStyle.CROSSFIRE,
         "FROG - ATK 2/HP 30. Only knows one song.",
         "Sing along", "You sing along. FROG croaks happily."),
      new Enemy("/resources/enemy.png", 50, "'Ribbit. You again?'",
         "BLUE FROG", new Color(90, 170, 255), Enemy.BulletStyle.SIDES,
         "BLUE FROG - ATK 3/HP 50. Attacks from both sides.",
         "Compliment", "You compliment BLUE FROG's color. It blushes... purple."),
      new Enemy("/resources/enemy.png", 100, "'I am the KING of this pond!'",
         "RED FROG", new Color(255, 90, 90), Enemy.BulletStyle.CROSSFIRE,
         "RED FROG - ATK 4/HP 100. Thinks it rules the pond.",
         "Bow", "You bow to RED FROG. It accepts your loyalty.")
   };

   static final int TOTAL_LEVELS = ROSTER.length * FIGHTS_PER_ENEMY;

   private Enemies() {
   }

   static Enemy forLevel(int level) {
      return ROSTER[Math.min((level - 1) / FIGHTS_PER_ENEMY, ROSTER.length - 1)];
   }

   // 1 to FIGHTS_PER_ENEMY: how far into the current enemy's stretch this level is.
   static int fightNumber(int level) {
      return (level - 1) % FIGHTS_PER_ENEMY + 1;
   }
}
