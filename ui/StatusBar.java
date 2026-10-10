package ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import javax.swing.JPanel;

public class StatusBar extends JPanel {
   private int currentHp;
   private int maxHp;

   public StatusBar(int currentHp, int maxHp) {
      this.currentHp = currentHp;
      this.maxHp = maxHp;
      Dimension size = new Dimension(110, 20);
      this.setPreferredSize(size);
      this.setMinimumSize(size);
      this.setMaximumSize(size);
      this.setBackground(Color.BLACK);
   }

   public void updateHp(int currentHp, int maxHp) {
      this.currentHp = currentHp;
      this.maxHp = maxHp;
      this.repaint();
   }

   @Override
   protected void paintComponent(Graphics g) {
      super.paintComponent(g);
      if (this.maxHp > 0) {
         double hpPercentage = (double)this.currentHp / this.maxHp;
         int coloredWidth = (int)(this.getWidth() * hpPercentage);
         g.setColor(Color.RED);
         g.fillRect(0, 0, this.getWidth(), this.getHeight());
         g.setColor(Color.YELLOW);
         g.fillRect(0, 0, coloredWidth, this.getHeight());
      }
   }
}
