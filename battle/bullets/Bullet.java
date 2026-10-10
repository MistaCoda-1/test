package battle.bullets;

import java.awt.Color;
import java.awt.Image;
import java.awt.Rectangle;

public class Bullet {
   public double x;
   public double y;
   public final double vx;
   public final double vy;
   public final int width;
   public final int height;
   public final int damage;
   public final Color color;
   public final Image sprite;

   public Bullet(double x, double y, double vx, double vy, int width, int height, int damage, Image sprite) {
      this.x = x;
      this.y = y;
      this.vx = vx;
      this.vy = vy;
      this.width = width;
      this.height = height;
      this.damage = damage;
      this.sprite = sprite;
      this.color = Color.WHITE;
   }

   public Bullet(double x, double y, double vx, double vy, int width, int height, int damage, Color color) {
      this.x = x;
      this.y = y;
      this.vx = vx;
      this.vy = vy;
      this.width = width;
      this.height = height;
      this.damage = damage;
      this.color = color;
      this.sprite = null;
   }

   public void advance() {
      this.x = this.x + this.vx;
      this.y = this.y + this.vy;
   }

   public Rectangle getBounds() {
      return new Rectangle((int)this.x, (int)this.y, this.width, this.height);
   }

   public boolean isOffscreen(int arenaWidth, int arenaHeight) {
      int buffer = 20;
      return this.x < -buffer || this.y < -buffer || this.x > arenaWidth + buffer || this.y > arenaHeight + buffer;
   }
}
