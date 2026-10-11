package ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

// Main menu button: white box, turns yellow with the heart cursor when selected (like the battle buttons).
public class MenuButton extends JPanel {
   private static final ImageIcon HEART = new ImageIcon(MenuButton.class.getResource("/resources/player_soul.png"));
   private static final ImageIcon BLANK = new ImageIcon(new BufferedImage(HEART.getIconWidth(), HEART.getIconHeight(), BufferedImage.TYPE_INT_ARGB));
   private static final int ICON_PAD = 14;
   private final JLabel textLabel;
   private final JLabel iconLabel = new JLabel(BLANK);
   private boolean selected = false;

   public MenuButton(String text) {
      this.setLayout(new BorderLayout());
      this.setPreferredSize(new Dimension(260, 44));
      this.setMaximumSize(new Dimension(260, 44));
      this.setBackground(Color.BLACK);
      this.setBorder(BorderFactory.createLineBorder(Color.WHITE, 1));
      this.textLabel = UIUtils.createLabel(text);
      this.textLabel.setForeground(Color.WHITE);
      this.textLabel.setHorizontalAlignment(0);
      this.iconLabel.setBorder(new EmptyBorder(0, ICON_PAD, 0, 0));
      // Same width as the heart on the right, so the text stays centered.
      JLabel spacer = new JLabel();
      spacer.setPreferredSize(new Dimension(HEART.getIconWidth() + ICON_PAD, 1));
      this.add(this.iconLabel, "West");
      this.add(this.textLabel, "Center");
      this.add(spacer, "East");
   }

   public void setSelected(boolean selected) {
      this.selected = selected;
      Color highlight = selected ? Color.YELLOW : Color.WHITE;
      this.textLabel.setForeground(highlight);
      this.setBorder(BorderFactory.createLineBorder(highlight, selected ? 2 : 1));
      this.iconLabel.setIcon(selected ? HEART : BLANK);
   }
}
