package ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics;
import java.awt.GraphicsEnvironment;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.InputStream;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public final class UIUtils {
   public static Font undertaleFont = loadFont("/resources/8bitoperator-jve/8bitoperator_jve.ttf", 24.0F);

   private UIUtils() {
   }

   public static Font loadFont(String path, float size) {
      try {
         Font var4;
         try (InputStream is = UIUtils.class.getResourceAsStream(path)) {
            if (is == null) {
               throw new IOException("File not found at: " + path);
            }

            Font font = Font.createFont(0, is).deriveFont(1, size);
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(font);
            var4 = font;
         }

         return var4;
      } catch (FontFormatException | IOException var7) {
         System.err.println("Font error: " + var7.getMessage() + ". Defaulting to Monospaced.");
         return new Font("Monospaced", 1, (int)size);
      }
   }

   public static void styleButton(final JButton button) {
      button.setPreferredSize(new Dimension(200, 40));
      button.setMaximumSize(new Dimension(200, 40));
      button.setAlignmentX(0.5F);
      button.setBackground(Color.BLACK);
      button.setForeground(Color.WHITE);
      button.setBorder(BorderFactory.createLineBorder(Color.WHITE, 1));
      button.setFocusPainted(false);
      button.setContentAreaFilled(false);
      button.setOpaque(true);
      button.setFont(undertaleFont);
      button.addMouseListener(new MouseAdapter() {
         @Override
         public void mouseEntered(MouseEvent evt) {
            button.setForeground(Color.YELLOW);
            button.setBorder(BorderFactory.createLineBorder(Color.YELLOW, 1));
         }

         @Override
         public void mouseExited(MouseEvent evt) {
            button.setForeground(Color.WHITE);
            button.setBorder(BorderFactory.createLineBorder(Color.WHITE, 1));
         }
      });
   }

   public static void styleInputField(JTextField field) {
      field.setPreferredSize(new Dimension(200, 35));
      field.setMaximumSize(new Dimension(200, 35));
      field.setBackground(Color.BLACK);
      field.setForeground(Color.WHITE);
      field.setCaretColor(Color.WHITE);
      field.setBorder(BorderFactory.createLineBorder(Color.WHITE, 1));
      field.setHorizontalAlignment(0);
      field.setFont(undertaleFont);
   }

   public static JLabel createLabel(String text) {
      JLabel lbl = new JLabel(text);
      lbl.setForeground(Color.WHITE);
      lbl.setFont(undertaleFont);
      return lbl;
   }

   // Base for pop-up screens (options, leaderboard, server settings): darkens whatever is behind them.
   public static class DimmedOverlay extends JPanel {
      private static final Color DIM = new Color(0, 0, 0, 190);

      public DimmedOverlay() {
         this.setOpaque(false);
      }

      @Override
      protected void paintComponent(Graphics g) {
         g.setColor(DIM);
         g.fillRect(0, 0, this.getWidth(), this.getHeight());
         super.paintComponent(g);
      }
   }
}