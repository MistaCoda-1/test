package battle;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JPanel;
import javax.swing.Timer;

public class AnimateBox extends JPanel {
   private final int startWidth;
   private final int startHeight;
   private final int targetWidth;
   private final int targetHeight;
   private final Runnable onComplete;
   private int boxWidth;
   private int boxHeight;
   private Timer timer;

   public AnimateBox(int startWidth, int startHeight, int targetWidth, int targetHeight, Runnable onComplete) {
      this.startWidth = startWidth;
      this.startHeight = startHeight;
      this.targetWidth = targetWidth;
      this.targetHeight = targetHeight;
      this.onComplete = onComplete;
      this.boxWidth = startWidth;
      this.boxHeight = startHeight;
      this.setBackground(Color.BLACK);
   }

   public void startAnimation() {
      if (this.timer == null || !this.timer.isRunning()) {
         this.timer = new Timer(15, e -> {
            boolean widthDone = false;
            boolean heightDone = false;
            int stepX = 25;
            int stepY = 15;
            if (this.boxWidth > this.targetWidth) {
               this.boxWidth = Math.max(this.targetWidth, this.boxWidth - stepX);
            } else if (this.boxWidth < this.targetWidth) {
               this.boxWidth = Math.min(this.targetWidth, this.boxWidth + stepX);
            } else {
               widthDone = true;
            }

            if (this.boxHeight > this.targetHeight) {
               this.boxHeight = Math.max(this.targetHeight, this.boxHeight - stepY);
            } else if (this.boxHeight < this.targetHeight) {
               this.boxHeight = Math.min(this.targetHeight, this.boxHeight + stepY);
            } else {
               heightDone = true;
            }

            this.repaint();
            if (widthDone && heightDone) {
               this.timer.stop();
               if (this.onComplete != null) {
                  this.onComplete.run();
               }
            }
         });
         this.timer.start();
      }
   }

   @Override
   protected void paintComponent(Graphics g) {
      super.paintComponent(g);
      Graphics2D g2d = (Graphics2D)g;
      int centerX = (this.getWidth() - this.boxWidth) / 2;
      int centerY = (this.getHeight() - this.boxHeight) / 2;
      g2d.setColor(Color.BLACK);
      g2d.fillRect(centerX, centerY, this.boxWidth, this.boxHeight);
      g2d.setColor(Color.WHITE);
      g2d.setStroke(new BasicStroke(3.0F));
      g2d.drawRect(centerX, centerY, this.boxWidth - 1, this.boxHeight - 1);
   }
}
