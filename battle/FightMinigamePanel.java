package battle;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.Timer;

public class FightMinigamePanel extends JPanel {
   private final Timer tickTimer;
   private final FightMinigamePanel.DamageListener listener;
   private double linePos = 0.0;
   private double lineSpeed = 7.0;
   private boolean locked = false;
   private int lastDamage = 0;

   public FightMinigamePanel(FightMinigamePanel.DamageListener listener) {
      this.listener = listener;
      this.setPreferredSize(new Dimension(790, 90));
      this.setOpaque(false);
      this.setFocusable(true);
      this.tickTimer = new Timer(15, e -> {
         this.linePos = this.linePos + this.lineSpeed;
         if (this.linePos >= 750.0) {
            this.linePos = 750.0;
            this.lockIn();
         } else {
            this.repaint();
         }
      });
      InputMap inputMap = this.getInputMap(2);
      ActionMap actionMap = this.getActionMap();
      inputMap.put(KeyStroke.getKeyStroke(90, 0), "lockIn");
      actionMap.put("lockIn", new AbstractAction() {
         @Override
         public void actionPerformed(ActionEvent e) {
            FightMinigamePanel.this.lockIn();
         }
      });
   }

   public void start() {
      this.locked = false;
      this.lastDamage = 0;
      this.linePos = 0.0;
      this.tickTimer.start();
      this.requestFocusInWindow();
   }

   private void lockIn() {
      if (!this.locked) {
         this.locked = true;
         this.tickTimer.stop();
         double center = 375.0;
         double distanceFromCenter = Math.abs(this.linePos - center);
         double accuracy = Math.max(0.0, 1.0 - distanceFromCenter / center);
         this.lastDamage = (int)Math.round(accuracy * 10.0);
         this.repaint();
         if (this.listener != null) {
            this.listener.onDamageDealt(this.lastDamage);
         }
      }
   }

   @Override
   protected void paintComponent(Graphics g) {
      super.paintComponent(g);
      Graphics2D g2 = (Graphics2D)g;
      int barX = (this.getWidth() - 750) / 2;
      g2.setColor(Color.DARK_GRAY);
      g2.fillRect(barX, 100, 750, 20);
      g2.setColor(Color.WHITE);
      g2.fillRect(barX + 375 - 15, 100, 30, 20);
      g2.setColor(Color.RED);
      int lineX = barX + (int)this.linePos;
      g2.fillRect(lineX - 2, 0, 4, 250);
      g2.setColor(Color.WHITE);
      g2.drawRect(barX, 100, 750, 20);
      g2.setFont(this.getFont().deriveFont(1, 16.0F));
      g2.setColor(Color.WHITE);
      String message = "Press Z in the white zone!";
      if (this.locked) {
         message = this.lastDamage > 0 ? "DAMAGE: " + this.lastDamage : "MISS!";
      }

      int textWidth = g2.getFontMetrics().stringWidth(message);
      g2.drawString(message, barX + 375 - textWidth / 2, 150);
   }

   public interface DamageListener {
      void onDamageDealt(int var1);
   }
}
