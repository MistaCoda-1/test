package battle;

import battle.bullets.Bullet;
import battle.bullets.BulletPattern;

import java.awt.event.ActionEvent;
import java.util.function.IntConsumer;

import java.awt.*;
import javax.swing.*;

public class ArenaPanel extends JPanel {
   // Used to draw the player
   private final Image heartImage;
   private final int heartWidth;
   private final int heartHeight;

   // Used to center the player
   private int soulX;
   private int soulY;

   private Timer gameLoopTimer;

   private boolean moveUp;
   private boolean moveDown;
   private boolean moveLeft;
   private boolean moveRight;

   // Focus logic from undertale
   // Holding X slows the player down
   private boolean focusActive;

   private final IntConsumer onPlayerHit;
   private final Runnable onTurnEnd;

   private BulletPattern pattern;
   private int invincibleTicks = 0;

   public ArenaPanel(int width, int height, ImageIcon heartIcon, IntConsumer onPlayerHit, Runnable onTurnEnd) {
      this.heartImage = heartIcon.getImage();
      this.heartWidth = heartIcon.getIconWidth();
      this.heartHeight = heartIcon.getIconHeight();
      this.onPlayerHit = onPlayerHit;
      this.onTurnEnd = onTurnEnd;
      this.setBackground(Color.BLACK);
      this.setBorder(BorderFactory.createLineBorder(Color.WHITE, 3));
      this.setPreferredSize(new Dimension(width, height));
      this.soulX = (width - this.heartWidth) / 2;
      this.soulY = (height - this.heartHeight) / 2;
      this.setupInputMappings();
   }

   // Paints/Fires the bullet pattern
   @Override
   protected void paintComponent(Graphics g) {
      super.paintComponent(g);
      if (this.pattern != null) {
         for (Bullet bullet : this.pattern.getBullets()) {
            if (bullet.sprite != null) {
               g.drawImage(bullet.sprite, (int) bullet.x, (int) bullet.y, bullet.width, bullet.height, this);
            } else {
               g.setColor(bullet.color);
               g.fillRect((int) bullet.x, (int) bullet.y, bullet.width, bullet.height);
            }
         }
      }

      boolean drawHeart = this.invincibleTicks <= 0 || this.invincibleTicks / 4 % 2 == 0;
      if (drawHeart) {
         g.drawImage(this.heartImage, this.soulX, this.soulY, this.heartWidth, this.heartHeight, this);
      }
   }

/**
 * User movement done with dx and dy (change in x/change in y)
 * When a key is held it constantly increments/decrements dx and/or dy
 * which translates to player movement.
 * Also sets arena bounds so the player doesn't go out of bounds.
 */
   public void startTurn(BulletPattern pattern) {
      if (this.gameLoopTimer == null || !this.gameLoopTimer.isRunning()) {
         this.pattern = pattern;
         this.invincibleTicks = 0;
         this.gameLoopTimer = new Timer(16, e -> {
            double dx = 0.0;
            double dy = 0.0;
            if (this.moveLeft) {
               dx--;
            }

            if (this.moveRight) {
               dx++;
            }

            if (this.moveUp) {
               dy--;
            }

            if (this.moveDown) {
               dy++;
            }

            if (dx != 0.0 && dy != 0.0) {
               dx *= 0.7071;
               dy *= 0.7071;
            }

            int activeSpeed = this.focusActive ? 2 : 4;
            this.soulX = this.soulX + (int) Math.round(dx * activeSpeed);
            this.soulY = this.soulY + (int) Math.round(dy * activeSpeed);
            int maxW = this.getWidth() - this.heartWidth - 3;
            int maxH = this.getHeight() - this.heartHeight - 3;
            this.soulX = Math.clamp((long) this.soulX, 3, maxW);
            this.soulY = Math.clamp((long) this.soulY, 3, maxH);
            this.pattern.update();
            this.checkCollisions();

            if (this.pattern == null) {
               return;
            }

            if (this.pattern.isOver()) {
               this.endTurn();
               if (this.onTurnEnd != null) {
                  this.onTurnEnd.run();
               }
            } else {
               this.repaint();
            }
         });
         this.gameLoopTimer.start();
         this.requestFocusInWindow();
      }
   }

/**
 * Defines the players hitbox then checks if any of the bullets in the current pattern
 * enters/intersects `heartBounds` then registers it as a hit. Very cool!
 */
   private void checkCollisions() {
      if (this.invincibleTicks > 0) {
         this.invincibleTicks--;
      } else {
         Rectangle heartBounds = new Rectangle(this.soulX + 4, this.soulY + 4, this.heartWidth - 8,
               this.heartHeight - 8);

         for (Bullet bullet : this.pattern.getBullets()) {
            if (heartBounds.intersects(bullet.getBounds())) {
               if (this.onPlayerHit != null) {
                  this.onPlayerHit.accept(bullet.damage);
               }

               this.invincibleTicks = 30;
               break;
            }
         }
      }
   }

   public void endTurn() {
      if (this.gameLoopTimer != null) {
         this.gameLoopTimer.stop();
      }

      this.moveUp = this.moveDown = this.moveLeft = this.moveRight = this.focusActive = false;
      this.pattern = null;
   }

   private void setupInputMappings() {
      InputMap im = this.getInputMap(2);
      ActionMap am = this.getActionMap();
      im.put(KeyStroke.getKeyStroke("UP"), "pressUp");
      am.put("pressUp", new AbstractAction() {
         @Override
         public void actionPerformed(ActionEvent e) {
            ArenaPanel.this.moveUp = true;
         }
      });
      im.put(KeyStroke.getKeyStroke("DOWN"), "pressDown");
      am.put("pressDown", new AbstractAction() {
         @Override
         public void actionPerformed(ActionEvent e) {
            ArenaPanel.this.moveDown = true;
         }
      });
      im.put(KeyStroke.getKeyStroke("LEFT"), "pressLeft");
      am.put("pressLeft", new AbstractAction() {
         @Override
         public void actionPerformed(ActionEvent e) {
            ArenaPanel.this.moveLeft = true;
         }
      });
      im.put(KeyStroke.getKeyStroke("RIGHT"), "pressRight");
      am.put("pressRight", new AbstractAction() {
         @Override
         public void actionPerformed(ActionEvent e) {
            ArenaPanel.this.moveRight = true;
         }
      });
      im.put(KeyStroke.getKeyStroke("X"), "pressX");
      am.put("pressX", new AbstractAction() {
         @Override
         public void actionPerformed(ActionEvent e) {
            ArenaPanel.this.focusActive = true;
         }
      });
      im.put(KeyStroke.getKeyStroke("released UP"), "releaseUp");
      am.put("releaseUp", new AbstractAction() {
         @Override
         public void actionPerformed(ActionEvent e) {
            ArenaPanel.this.moveUp = false;
         }
      });
      im.put(KeyStroke.getKeyStroke("released DOWN"), "releaseDown");
      am.put("releaseDown", new AbstractAction() {
         @Override
         public void actionPerformed(ActionEvent e) {
            ArenaPanel.this.moveDown = false;
         }
      });
      im.put(KeyStroke.getKeyStroke("released LEFT"), "releaseLeft");
      am.put("releaseLeft", new AbstractAction() {
         @Override
         public void actionPerformed(ActionEvent e) {
            ArenaPanel.this.moveLeft = false;
         }
      });
      im.put(KeyStroke.getKeyStroke("released RIGHT"), "releaseRight");
      am.put("releaseRight", new AbstractAction() {
         @Override
         public void actionPerformed(ActionEvent e) {
            ArenaPanel.this.moveRight = false;
         }
      });
      im.put(KeyStroke.getKeyStroke("released X"), "releaseX");
      am.put("releaseX", new AbstractAction() {
         @Override
         public void actionPerformed(ActionEvent e) {
            ArenaPanel.this.focusActive = false;
         }
      });
   }
}