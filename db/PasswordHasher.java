package db;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

// Salted PBKDF2 password hashes, so the database never stores real passwords.
// Stored format: pbkdf2$<iterations>$<salt base64>$<hash base64>
final class PasswordHasher {
   private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
   private static final int ITERATIONS = 100_000;
   private static final int SALT_BYTES = 16;
   private static final int HASH_BITS = 256;
   private static final SecureRandom RANDOM = new SecureRandom();

   private PasswordHasher() {
   }

   static String hash(char[] password) {
      byte[] salt = new byte[SALT_BYTES];
      RANDOM.nextBytes(salt);
      byte[] hash = pbkdf2(password, salt, ITERATIONS);
      Base64.Encoder b64 = Base64.getEncoder();
      return "pbkdf2$" + ITERATIONS + "$" + b64.encodeToString(salt) + "$" + b64.encodeToString(hash);
   }

   static boolean verify(char[] password, String stored) {
      String[] parts = stored == null ? new String[0] : stored.split("\\$");
      if (parts.length != 4 || !parts[0].equals("pbkdf2")) {
         return false;
      }

      try {
         int iterations = Integer.parseInt(parts[1]);
         byte[] salt = Base64.getDecoder().decode(parts[2]);
         byte[] expected = Base64.getDecoder().decode(parts[3]);
         return MessageDigest.isEqual(expected, pbkdf2(password, salt, iterations));
      } catch (IllegalArgumentException e) {
         return false;
      }
   }

   private static byte[] pbkdf2(char[] password, byte[] salt, int iterations) {
      PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, HASH_BITS);
      try {
         return SecretKeyFactory.getInstance(ALGORITHM).generateSecret(spec).getEncoded();
      } catch (GeneralSecurityException e) {
         throw new IllegalStateException("PBKDF2 not available", e);
      } finally {
         spec.clearPassword();
      }
   }
}
