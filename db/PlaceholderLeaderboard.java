package db;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// In-memory stand-in for the players table, used when MySQL/XAMPP isn't reachable.
// Seeded with sample scores; anything saved here is lost when the game closes.
final class PlaceholderLeaderboard {
   private static final Map<String, Integer> LEVELS = new LinkedHashMap<>();

   static {
      LEVELS.put("SANS", 15);
      LEVELS.put("PAPYRUS", 12);
      LEVELS.put("UNDYNE", 10);
      LEVELS.put("TORIEL", 8);
      LEVELS.put("ALPHYS", 6);
      LEVELS.put("NAPSTABLOOK", 4);
      LEVELS.put("FLOWEY", 2);
   }

   private PlaceholderLeaderboard() {
   }

   // Same ordering as TOP_PLAYERS_SQL: level DESC, username ASC.
   static synchronized List<LeaderboardEntry> getTopPlayers(int limit) {
      List<Map.Entry<String, Integer>> sorted = new ArrayList<>(LEVELS.entrySet());
      sorted.sort(Map.Entry.<String, Integer>comparingByValue().reversed().thenComparing(Map.Entry.comparingByKey()));
      List<LeaderboardEntry> entries = new ArrayList<>();
      int rank = 1;

      for (Map.Entry<String, Integer> e : sorted.subList(0, Math.min(limit, sorted.size()))) {
         entries.add(new LeaderboardEntry(rank++, e.getKey(), e.getValue()));
      }

      return entries;
   }

   // Never lowers a level, like SAVE_HIGHEST_LEVEL_SQL.
   static synchronized void saveHighestLevel(String username, int level) {
      LEVELS.merge(username, level, Math::max);
   }
}
