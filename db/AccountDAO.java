package db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.Arrays;
import java.util.regex.Pattern;

// Player accounts. Works offline: accounts are always kept on this device (LocalStore) and
// uploaded to the server's users table when it's reachable (here, or later by SyncService).
public class AccountDAO {
   private static final Pattern VALID_USERNAME = Pattern.compile("[A-Za-z0-9_]{3,20}");
   private static final int MIN_PASSWORD_LENGTH = 4;
   static final String FIND_HASH_SQL = "SELECT pasword_hash FROM users WHERE username = ?";
   static final String CREATE_USER_SQL = "INSERT INTO users (username, pasword_hash) VALUES (?, ?)";
   // Older builds created users with an empty hash when saving a score; let someone set a password on those.
   static final String CLAIM_USER_SQL = "UPDATE users SET pasword_hash = ? WHERE username = ? AND pasword_hash = ''";

   public enum RegisterResult {
      CREATED,
      CREATED_OFFLINE,
      NAME_TAKEN,
      INVALID_NAME,
      PASSWORD_TOO_SHORT;
   }

   public enum LoginResult {
      OK,
      OK_OFFLINE,
      FAILED,
      NAME_TAKEN_ON_SERVER;
   }

   // Usernames are stored in capitals, so "dom" and "DOM" are the same player.
   public static String normalize(String username) {
      return username.trim().toUpperCase();
   }

   public RegisterResult register(String username, char[] password) {
      if (!VALID_USERNAME.matcher(username.trim()).matches()) {
         return RegisterResult.INVALID_NAME;
      }

      if (password.length < MIN_PASSWORD_LENGTH) {
         return RegisterResult.PASSWORD_TOO_SHORT;
      }

      String name = normalize(username);
      LocalStore.Account local = LocalStore.get(name);
      if (local != null && local.hash() != null) {
         return RegisterResult.NAME_TAKEN;
      }

      String hash = PasswordHasher.hash(password);
      try {
         Database.ensureReady();
         try (Connection conn = Database.getConnection()) {
            if (!insertOrClaim(conn, name, hash)) {
               return RegisterResult.NAME_TAKEN;
            }
         }

         LocalStore.putAccount(name, hash, true);
         return RegisterResult.CREATED;
      } catch (SQLException e) {
         // Server unreachable: keep the account on this device; SyncService uploads it later.
         LocalStore.putAccount(name, hash, false);
         return RegisterResult.CREATED_OFFLINE;
      }
   }

   public LoginResult login(String username, char[] password) {
      String name = normalize(username);
      LocalStore.Account local = LocalStore.get(name);
      boolean localMatch = local != null && local.hash() != null && PasswordHasher.verify(password, local.hash());
      String serverHash;

      try {
         Database.ensureReady();
         try (Connection conn = Database.getConnection()) {
            serverHash = findHash(conn, name);
            if (serverHash != null && PasswordHasher.verify(password, serverHash)) {
               // Remember the account so this device can log in offline next time.
               LocalStore.putAccount(name, serverHash, true);
               SyncService.syncInBackground();
               return LoginResult.OK;
            }

            if (localMatch && (serverHash == null || serverHash.isEmpty())) {
               // Made offline on this device and not on the server yet: upload it now.
               insertOrClaim(conn, name, local.hash());
               LocalStore.markSynced(name);
               SyncService.syncInBackground();
               return LoginResult.OK;
            }

            if (localMatch) {
               LocalStore.markConflict(name);
               return LoginResult.NAME_TAKEN_ON_SERVER;
            }

            return LoginResult.FAILED;
         }
      } catch (SQLException e) {
         if (localMatch) {
            return LoginResult.OK_OFFLINE;
         }

         // The original built-in login, still allowed offline.
         if (name.equals("DOM") && Arrays.equals("123".toCharArray(), password)) {
            return LoginResult.OK_OFFLINE;
         }

         return LoginResult.FAILED;
      }
   }

   // Returns false if the name belongs to someone else on the server.
   private static boolean insertOrClaim(Connection conn, String name, String hash) throws SQLException {
      try (PreparedStatement create = conn.prepareStatement(CREATE_USER_SQL)) {
         create.setString(1, name);
         create.setString(2, hash);
         create.executeUpdate();
         return true;
      } catch (SQLIntegrityConstraintViolationException e) {
         return claim(conn, name, hash);
      }
   }

   static boolean claim(Connection conn, String name, String hash) throws SQLException {
      try (PreparedStatement claim = conn.prepareStatement(CLAIM_USER_SQL)) {
         claim.setString(1, hash);
         claim.setString(2, name);
         return claim.executeUpdate() == 1;
      }
   }

   // null if the server has no such user.
   static String findHash(Connection conn, String name) throws SQLException {
      try (PreparedStatement stmt = conn.prepareStatement(FIND_HASH_SQL)) {
         stmt.setString(1, name);
         try (ResultSet rs = stmt.executeQuery()) {
            return rs.next() ? rs.getString(1) : null;
         }
      }
   }
}
