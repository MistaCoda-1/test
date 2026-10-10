package db;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeSet;

// Accounts and scores kept on this device, so creating accounts, logging in and saving scores all work
// without the server. SyncService uploads anything the server doesn't have yet.
// Saved in heartbreak-local-data.properties in the user's home folder.
final class LocalStore {
   private static final Path FILE = Paths.get(System.getProperty("user.home"), "heartbreak-local-data.properties");
   private static final String PREFIX = "account.";
   private static final Properties DATA = new Properties();

   // hash == null means a name that only has a local score (the built-in offline login), never uploaded.
   record Account(String name, String hash, int level, int syncedLevel, boolean synced, boolean conflict) {
   }

   static {
      load();
   }

   private LocalStore() {
   }

   static synchronized Account get(String name) {
      if (!DATA.containsKey(key(name, "level")) && !DATA.containsKey(key(name, "hash"))) {
         return null;
      }

      return new Account(
         name,
         DATA.getProperty(key(name, "hash")),
         intValue(key(name, "level")),
         intValue(key(name, "syncedLevel")),
         Boolean.parseBoolean(DATA.getProperty(key(name, "synced"), "false")),
         Boolean.parseBoolean(DATA.getProperty(key(name, "conflict"), "false"))
      );
   }

   static synchronized List<Account> accounts() {
      TreeSet<String> names = new TreeSet<>();
      for (String k : DATA.stringPropertyNames()) {
         if (k.startsWith(PREFIX)) {
            names.add(k.substring(PREFIX.length(), k.lastIndexOf('.')));
         }
      }

      List<Account> result = new ArrayList<>();
      for (String name : names) {
         result.add(get(name));
      }

      return result;
   }

   static synchronized void putAccount(String name, String hash, boolean synced) {
      DATA.setProperty(key(name, "hash"), hash);
      DATA.setProperty(key(name, "synced"), String.valueOf(synced));
      DATA.remove(key(name, "conflict"));
      DATA.putIfAbsent(key(name, "level"), "0");
      save();
   }

   static synchronized void markSynced(String name) {
      DATA.setProperty(key(name, "synced"), "true");
      DATA.remove(key(name, "conflict"));
      save();
   }

   static synchronized void markConflict(String name) {
      DATA.setProperty(key(name, "conflict"), "true");
      save();
   }

   // Never lowers a level.
   static synchronized void saveLevel(String name, int level) {
      if (level > intValue(key(name, "level"))) {
         DATA.setProperty(key(name, "level"), String.valueOf(level));
         save();
      }
   }

   // The server now has this level (or higher), so it no longer needs uploading.
   static synchronized void markLevelSynced(String name, int serverLevel) {
      if (serverLevel > intValue(key(name, "level"))) {
         DATA.setProperty(key(name, "level"), String.valueOf(serverLevel));
      }

      DATA.setProperty(key(name, "syncedLevel"), String.valueOf(serverLevel));
      save();
   }

   // Changes made on this device that the server doesn't have yet.
   static synchronized int pendingCount() {
      int count = 0;
      for (Account a : accounts()) {
         if (a.hash() == null || a.conflict()) {
            continue;
         }

         if (!a.synced()) {
            count++;
         }

         if (a.level() > a.syncedLevel()) {
            count++;
         }
      }

      return count;
   }

   static synchronized void cacheBoard(List<LeaderboardEntry> entries) {
      StringBuilder sb = new StringBuilder();
      for (LeaderboardEntry e : entries) {
         sb.append(e.getUsername()).append(':').append(e.getHighestLevel()).append(';');
      }

      DATA.setProperty("board.cache", sb.toString());
      save();
   }

   // Last leaderboard seen from the server, merged with this device's own scores.
   static synchronized List<LeaderboardEntry> offlineBoard(int limit) {
      Map<String, Integer> levels = new HashMap<>();
      for (String item : DATA.getProperty("board.cache", "").split(";")) {
         int colon = item.lastIndexOf(':');
         if (colon > 0) {
            levels.merge(item.substring(0, colon), Integer.parseInt(item.substring(colon + 1)), Math::max);
         }
      }

      for (Account a : accounts()) {
         if (a.level() > 0) {
            levels.merge(a.name(), a.level(), Math::max);
         }
      }

      List<Map.Entry<String, Integer>> sorted = new ArrayList<>(levels.entrySet());
      sorted.sort(Map.Entry.<String, Integer>comparingByValue().reversed().thenComparing(Map.Entry.comparingByKey()));
      List<LeaderboardEntry> entries = new ArrayList<>();
      int rank = 1;
      for (Map.Entry<String, Integer> e : sorted.subList(0, Math.min(limit, sorted.size()))) {
         entries.add(new LeaderboardEntry(rank++, e.getKey(), e.getValue()));
      }

      return entries;
   }

   private static String key(String name, String field) {
      return PREFIX + name + "." + field;
   }

   private static int intValue(String key) {
      try {
         return Integer.parseInt(DATA.getProperty(key, "0"));
      } catch (NumberFormatException e) {
         return 0;
      }
   }

   private static void load() {
      if (Files.exists(FILE)) {
         try (InputStream in = Files.newInputStream(FILE)) {
            DATA.load(in);
         } catch (IOException e) {
            System.out.println("Could not read " + FILE + ": " + e.getMessage());
         }
      }
   }

   private static void save() {
      try (OutputStream out = Files.newOutputStream(FILE)) {
         DATA.store(out, "HeartBreak accounts and scores saved on this device");
      } catch (IOException e) {
         System.out.println("Could not save " + FILE + ": " + e.getMessage());
      }
   }
}
