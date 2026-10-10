package db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

// Uploads accounts and scores made on this device while the server was unreachable.
// Runs every 15 seconds, on the login screen, and right after logging in.
public final class SyncService {
   private static final int INTERVAL_SECONDS = 15;
   private static final String FIND_LEVEL_SQL = "SELECT level FROM players WHERE username = ?";
   private static final ScheduledExecutorService EXECUTOR = Executors.newSingleThreadScheduledExecutor(r -> {
      Thread t = new Thread(r, "heartbreak-sync");
      t.setDaemon(true);
      return t;
   });
   private static volatile boolean online = false;
   private static volatile boolean started = false;

   private SyncService() {
   }

   public static synchronized void start() {
      if (!started) {
         started = true;
         EXECUTOR.scheduleWithFixedDelay(SyncService::syncNow, INTERVAL_SECONDS, INTERVAL_SECONDS, TimeUnit.SECONDS);
      }
   }

   public static void syncInBackground() {
      EXECUTOR.execute(SyncService::syncNow);
   }

   // Whether the last attempt reached the server.
   public static boolean isOnline() {
      return online;
   }

   public static int pendingCount() {
      return LocalStore.pendingCount();
   }

   // Returns true if the server was reachable.
   public static synchronized boolean syncNow() {
      try {
         Database.ensureReady();
         try (Connection conn = Database.getConnection()) {
            for (LocalStore.Account account : LocalStore.accounts()) {
               if (account.hash() == null || account.conflict()) {
                  continue;
               }

               if (!account.synced()) {
                  pushAccount(conn, account);
               }

               LocalStore.Account current = LocalStore.get(account.name());
               if (current.synced() && current.level() > current.syncedLevel()) {
                  pushLevel(conn, current.name(), current.level());
               }
            }
         }

         online = true;
         return true;
      } catch (SQLException e) {
         if (online) {
            System.out.println("Server unreachable, saving on this device: " + Database.firstLine(e.getMessage()));
         }

         online = false;
         return false;
      }
   }

   private static void pushAccount(Connection conn, LocalStore.Account account) throws SQLException {
      try (PreparedStatement insert = conn.prepareStatement(AccountDAO.CREATE_USER_SQL)) {
         insert.setString(1, account.name());
         insert.setString(2, account.hash());
         insert.executeUpdate();
         LocalStore.markSynced(account.name());
         System.out.println("Synced account " + account.name() + " to the server.");
      } catch (SQLIntegrityConstraintViolationException e) {
         String serverHash = AccountDAO.findHash(conn, account.name());
         if (account.hash().equals(serverHash) || ("".equals(serverHash) && AccountDAO.claim(conn, account.name(), account.hash()))) {
            LocalStore.markSynced(account.name());
         } else {
            // Someone else registered this name on the server. The player is told when they next log in online.
            LocalStore.markConflict(account.name());
            System.out.println("Account " + account.name() + " already exists on the server; not synced.");
         }
      }
   }

   static void pushLevel(Connection conn, String name, int level) throws SQLException {
      try (PreparedStatement save = conn.prepareStatement(LeaderboardDAO.SAVE_HIGHEST_LEVEL_SQL)) {
         save.setString(1, name);
         save.setInt(2, level);
         save.executeUpdate();
      }

      try (PreparedStatement find = conn.prepareStatement(FIND_LEVEL_SQL)) {
         find.setString(1, name);
         try (ResultSet rs = find.executeQuery()) {
            if (rs.next()) {
               LocalStore.markLevelSynced(name, rs.getInt(1));
            }
         }
      }
   }
}
