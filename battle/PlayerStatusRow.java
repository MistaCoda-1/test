package battle;

import javax.swing.JLabel;
import javax.swing.JPanel;
import ui.StatusBar;
import ui.UIUtils;

class PlayerStatusRow extends JPanel {
   private static final int TOP = 599;
   private static final int ROW_H = 30;
   private static final int LEFT = 25;

   private final String playerName;
   private final JLabel statsLabel = UIUtils.createLabel("");
   private final JLabel hpMarker = UIUtils.createLabel("HP");
   private final StatusBar healthBar;
   private final JLabel hpNumbers = UIUtils.createLabel("");

   PlayerStatusRow(String playerName, int stageWidth, int level, int hp, int maxHp) {
      this.playerName = playerName;
      this.setLayout(null);
      this.setOpaque(false);
      this.setBounds(0, TOP, stageWidth, ROW_H);
      this.healthBar = new StatusBar(hp, maxHp);
      this.add(this.statsLabel);
      this.add(this.hpMarker);
      this.add(this.healthBar);
      this.add(this.hpNumbers);
      this.update(level, hp, maxHp);
   }

   void update(int level, int hp, int maxHp) {
      this.statsLabel.setText(this.playerName + "   LEVEL " + level + " / ENDLESS");
      this.hpNumbers.setText(hp + " / " + maxHp);
      int x = LEFT;
      x = this.placeLabel(this.statsLabel, x) + 24;
      x = this.placeLabel(this.hpMarker, x) + 8;
      this.healthBar.setBounds(x, 5, 110, 20);
      x += 122;
      this.placeLabel(this.hpNumbers, x);
      this.healthBar.updateHp(hp, maxHp);
   }

   private int placeLabel(JLabel label, int x) {
      int w = label.getPreferredSize().width;
      label.setBounds(x, 0, w, ROW_H);
      return x + w;
   }
}
