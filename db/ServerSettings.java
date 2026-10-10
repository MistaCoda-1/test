package db;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

// Which MySQL server this device uses. Saved per device in heartbreak-settings.properties in the user's home folder.
// The admin's own computer keeps the defaults (localhost / root); player devices point at the admin's IP.
public final class ServerSettings {
   private static final Path FILE = Paths.get(System.getProperty("user.home"), "heartbreak-settings.properties");
   private static final String DEFAULT_HOST = "localhost";
   private static final int DEFAULT_PORT = 3306;
   private static final String DEFAULT_USER = "root";
   private static final String DEFAULT_PASSWORD = "";

   private static String host = DEFAULT_HOST;
   private static int port = DEFAULT_PORT;
   private static String user = DEFAULT_USER;
   private static String password = DEFAULT_PASSWORD;

   static {
      load();
   }

   private ServerSettings() {
   }

   public static synchronized String getHost() {
      return host;
   }

   public static synchronized int getPort() {
      return port;
   }

   public static synchronized String getUser() {
      return user;
   }

   public static synchronized String getPassword() {
      return password;
   }

   public static synchronized void update(String newHost, int newPort, String newUser, String newPassword) throws IOException {
      host = newHost.isBlank() ? DEFAULT_HOST : newHost.trim();
      port = newPort;
      user = newUser.isBlank() ? DEFAULT_USER : newUser.trim();
      password = newPassword;
      Properties props = new Properties();
      props.setProperty("host", host);
      props.setProperty("port", String.valueOf(port));
      props.setProperty("user", user);
      props.setProperty("password", password);

      try (OutputStream out = Files.newOutputStream(FILE)) {
         props.store(out, "HeartBreak server settings");
      }
   }

   private static void load() {
      if (!Files.exists(FILE)) {
         return;
      }

      Properties props = new Properties();
      try (InputStream in = Files.newInputStream(FILE)) {
         props.load(in);
         host = props.getProperty("host", DEFAULT_HOST);
         port = Integer.parseInt(props.getProperty("port", String.valueOf(DEFAULT_PORT)));
         user = props.getProperty("user", DEFAULT_USER);
         password = props.getProperty("password", DEFAULT_PASSWORD);
      } catch (IOException | NumberFormatException e) {
         System.out.println("Could not read " + FILE + ", using defaults: " + e.getMessage());
      }
   }
}
