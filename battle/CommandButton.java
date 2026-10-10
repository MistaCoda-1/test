package battle;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import ui.UIUtils;

public class CommandButton extends JPanel {
   private final JLabel textLabel;
   private final JLabel iconLabel;
   private final ImageIcon heartIcon;
   private ImageIcon commandIcon;
   private boolean selected = false;

   public CommandButton(String text, ImageIcon heartIcon) {
      this.heartIcon = heartIcon;
      this.commandIcon = blankIcon(heartIcon);
      this.setLayout(new BorderLayout());
      this.setPreferredSize(new Dimension(200, 40));
      this.setBackground(Color.BLACK);
      this.textLabel = UIUtils.createLabel(text);
      this.textLabel.setBorder(new EmptyBorder(0, 0, 0, 10));
      this.iconLabel = new JLabel(this.commandIcon);
      this.iconLabel.setBorder(new EmptyBorder(0, 10, 0, 0));
      this.add(this.textLabel, "East");
      this.add(this.iconLabel, "West");
   }

   public void setCommandIcon(ImageIcon icon) {
      this.commandIcon = icon;
      if (!this.selected) {
         this.iconLabel.setIcon(this.commandIcon);
      }
   }

   public void setSelected(boolean selected) {
      this.selected = selected;
      Color highlight = selected ? Color.YELLOW : new Color(224, 132, 66);
      this.textLabel.setForeground(highlight);
      this.setBorder(BorderFactory.createLineBorder(highlight, 3));
      this.iconLabel.setIcon(selected ? this.heartIcon : this.commandIcon);
   }

   private static ImageIcon blankIcon(ImageIcon sizedLike) {
      return new ImageIcon(new BufferedImage(sizedLike.getIconWidth(), sizedLike.getIconHeight(), 2));
   }
}
