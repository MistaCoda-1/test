package db;

public class LeaderboardEntry {
   private final int rank;
   private final String username;
   private final int highestLevel;

   public LeaderboardEntry(int rank, String username, int highestLevel) {
      this.rank = rank;
      this.username = username;
      this.highestLevel = highestLevel;
   }

   public int getRank() {
      return this.rank;
   }

   public String getUsername() {
      return this.username;
   }

   public int getHighestLevel() {
      return this.highestLevel;
   }
}
