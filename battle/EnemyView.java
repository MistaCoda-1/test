package battle;

import java.awt.Color;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.FilteredImageSource;
import java.awt.image.RGBImageFilter;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import ui.StatusBar;
import ui.UIUtils;

class EnemyView extends JPanel {
   private static final int HEIGHT = 322;
   private static final int PANEL_W = 700;
   private static final int PANEL_H = 280;
   private static final int PANEL_Y = 8;
   private static final int SPRITE_H = 250;
   private static final int HP_Y = 292;

   private final EnemyBG background = new EnemyBG(new Color(0, 255, 60), 140);
   private final JLabel sprite = new JLabel();
   private final StatusBar hpBar;

   EnemyView(int stageWidth, Enemy enemy, int hp, int maxHp) {
      this.setLayout(null);
      this.setOpaque(false);
      this.setBounds(0, 0, stageWidth, HEIGHT);

      this.background.setLayout(null);
      this.background.setBounds((stageWidth - PANEL_W) / 2, PANEL_Y, PANEL_W, PANEL_H);
      this.background.add(this.sprite);
      this.add(this.background);

      JLabel marker = UIUtils.createLabel("HP");
      int markerW = marker.getPreferredSize().width;
      int gap = 8;
      int x = (stageWidth - (markerW + gap + 110)) / 2;
      marker.setBounds(x, HP_Y, markerW, 30);
      this.hpBar = new StatusBar(hp, maxHp);
      this.hpBar.setBounds(x + markerW + gap, HP_Y + 5, 110, 20);
      this.add(marker);
      this.add(this.hpBar);

      this.setEnemy(enemy);
   }

   void setEnemy(Enemy enemy) {
      ImageIcon originalIcon = new ImageIcon(this.getClass().getResource(enemy.spritePath()));
      int targetH = SPRITE_H;
      int targetW = (int)Math.round((double)originalIcon.getIconWidth() / originalIcon.getIconHeight() * targetH);
      Image image = originalIcon.getImage();
      if (enemy.tint() != null) {
         image = tinted(image, enemy.tint());
      }

      Image scaledImage = image.getScaledInstance(targetW, targetH, Image.SCALE_SMOOTH);
      this.sprite.setIcon(new ImageIcon(scaledImage));
      this.sprite.setBounds((PANEL_W - targetW) / 2, (PANEL_H - targetH) / 2, targetW, targetH);
   }

   void showHp(int hp, int maxHp) {
      this.hpBar.updateHp(hp, maxHp);
   }

   private static Image tinted(Image image, Color tint) {
      RGBImageFilter filter = new RGBImageFilter() {
         @Override
         public int filterRGB(int x, int y, int argb) {
            int r = ((argb >> 16) & 0xFF) * tint.getRed() / 255;
            int g = ((argb >> 8) & 0xFF) * tint.getGreen() / 255;
            int b = (argb & 0xFF) * tint.getBlue() / 255;
            return (argb & 0xFF000000) | (r << 16) | (g << 8) | b;
         }
      };
      return Toolkit.getDefaultToolkit().createImage(new FilteredImageSource(image.getSource(), filter));
   }
}
