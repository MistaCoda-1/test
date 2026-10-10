package battle;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JPanel;

public class EnemyBG extends JPanel {
   private final Color gridColor;
   private final int cellSize;

   public EnemyBG(Color gridColor, int cellSize) {
      this.gridColor = gridColor;
      this.cellSize = cellSize;
      this.setOpaque(true);
   }

   @Override
   protected void paintComponent(Graphics g) {
      super.paintComponent(g);
      g.setColor(Color.BLACK);
      g.fillRect(0, 0, this.getWidth(), this.getHeight());
      Graphics2D g2d = (Graphics2D)g;
      g2d.setColor(this.gridColor);
      g2d.setStroke(new BasicStroke(1.0F));

      for (int x = this.cellSize; x < this.getWidth(); x += this.cellSize) {
         g2d.drawLine(x, 0, x, this.getHeight());
      }

      for (int y = this.cellSize; y < this.getHeight(); y += this.cellSize) {
         g2d.drawLine(0, y, this.getWidth(), y);
      }

      g2d.drawRect(0, 0, this.getWidth() - 1, this.getHeight() - 1);
   }
}
