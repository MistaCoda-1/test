package db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class LeaderboardDAO {
   private static final String TOP_PLAYERS_SQL = "SELECT username, level FROM players ORDER BY level DESC, username ASC LIMIT ?";
   // Inserts the player, or raises their level if the new one is higher (never lowers it).
   static final String SAVE_HIGHEST_LEVEL_SQL = "INSERT INTO players (username, level) VALUES (?, ?) ON DUPLICATE KEY UPDATE level = GREATEST(level, VALUES(level))";
   private boolean offline = false;

   // Offline: the last leaderboard seen from the server plus this device's scores,
   // or PlaceholderLeaderboard if there's nothing saved yet.
   public List<LeaderboardEntry> getTopPlayers(int limit) throws SQLException {
      try {
         List<LeaderboardEntry> entries = this.getTopPlayersFromDb(limit);
         LocalStore.cacheBoard(entries);
         this.offline = false;
         return entries;
      } catch (SQLException e) {
         System.out.println("Database unavailable - showing leaderboard saved on this device.");
         this.offline = true;
         List<LeaderboardEntry> local = LocalStore.offlineBoard(limit);
         return local.isEmpty() ? PlaceholderLeaderboard.getTopPlayers(limit) : local;
      }
   }

   // Whether the last getTopPlayers() call couldn't reach the server.
   public boolean isOffline() {
      return this.offline;
   }

   // Always saved on this device first; uploaded now if the server is reachable, otherwise by SyncService later.
   public void saveHighestLevel(String username, int level) throws SQLException {
      LocalStore.saveLevel(username, level);
      try (Connection conn = Database.getConnection()) {
         SyncService.pushLevel(conn, username, level);
      } catch (SQLException e) {
         System.out.println("Score saved on this device; it will sync when the server is reachable.");
      }
   }

   private List<LeaderboardEntry> getTopPlayersFromDb(int limit) throws SQLException {
      List<LeaderboardEntry> entries = new ArrayList<>();

      try (
         Connection conn = Database.getConnection();
         PreparedStatement stmt = conn.prepareStatement(TOP_PLAYERS_SQL);
      ) {
         stmt.setInt(1, limit);

         try (ResultSet rs = stmt.executeQuery()) {
            int rank = 1;

            while (rs.next()) {
               entries.add(new LeaderboardEntry(rank++, rs.getString("username"), rs.getInt("level")));
            }
         }
      }

      return entries;
   }
}
