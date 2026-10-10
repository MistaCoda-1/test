package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public final class Database {
   // connectTimeout keeps the game from hanging when the server isn't reachable.
   private static final String OPTIONS = "?connectTimeout=3000&allowPublicKeyRetrieval=true";
   private static final String DB_NAME = "HeartBreak";
   private static boolean ready = false;

   private Database() {
   }

   // The server comes from ServerSettings: localhost on the admin's computer, the admin's IP on other devices.
   private static String serverUrl() {
      return "jdbc:mysql://" + ServerSettings.getHost() + ":" + ServerSettings.getPort() + "/" + OPTIONS;
   }

   private static String dbUrl() {
      return "jdbc:mysql://" + ServerSettings.getHost() + ":" + ServerSettings.getPort() + "/" + DB_NAME + OPTIONS;
   }

   public static Connection getConnection() throws SQLException {
      return DriverManager.getConnection(dbUrl(), ServerSettings.getUser(), ServerSettings.getPassword());
   }

   // Creates the database and tables if they don't exist yet. Returns false if the server can't be reached.
   public static synchronized boolean initialize() {
      try {
         ensureReady();
         return true;
      } catch (SQLException e) {
         System.out.println("Database unavailable at " + ServerSettings.getHost() + " (is XAMPP running?): " + firstLine(e.getMessage()));
         return false;
      }
   }

   // Same as initialize(), but throws so callers can tell the player the server is offline.
   public static synchronized void ensureReady() throws SQLException {
      if (!ready) {
         initializeDB();
         initializeTables();
         ready = true;
         System.out.println("Database ready at " + ServerSettings.getHost() + ".");
      }
   }

   // Tries a server without saving it as the setting (for the TEST button in server settings).
   public static void testConnection(String host, int port, String user, String password) throws SQLException {
      String url = "jdbc:mysql://" + host + ":" + port + "/" + OPTIONS;
      DriverManager.getConnection(url, user, password).close();
   }

   // Call after the server settings change so the next connection sets up the new server.
   public static synchronized void reset() {
      ready = false;
   }

   private static void initializeDB() throws SQLException {
      try (
         Connection conn = DriverManager.getConnection(serverUrl(), ServerSettings.getUser(), ServerSettings.getPassword());
         Statement stmt = conn.createStatement();
      ) {
         stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS HeartBreak");
      }
   }

   private static void initializeTables() throws SQLException {
      String USERS_SQL = "CREATE TABLE IF NOT EXISTS users( uid INT AUTO_INCREMENT PRIMARY KEY, username VARCHAR(50) NOT NULL UNIQUE, pasword_hash varchar(255) NOT NULL );";
      String PLAYERS_SQL = "CREATE TABLE IF NOT EXISTS players ( id INT AUTO_INCREMENT PRIMARY KEY, username VARCHAR(50) NOT NULL UNIQUE, level INT DEFAULT 0, FOREIGN KEY (username) REFERENCES users(username) ON DELETE CASCADE ON UPDATE CASCADE);";

      try (
         Connection conn = getConnection();
         Statement stmt = conn.createStatement();
      ) {
         stmt.execute(USERS_SQL);
         stmt.execute(PLAYERS_SQL);
      }
   }

   static String firstLine(String message) {
      return message == null ? "" : message.lines().findFirst().orElse("");
   }
}
