package ui;

import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import javax.swing.*;

public class LeaderboardOverlay extends UIUtils.DimmedOverlay {
   public LeaderboardOverlay(final Runnable onClose) {
      this.setLayout(new GridBagLayout());
      this.setFocusable(true);
      this.add(new LeaderboardPanel());
      this.addMouseListener(new MouseAdapter() {});
      InputMap inputMap = this.getInputMap(2);
      ActionMap actionMap = this.getActionMap();
      inputMap.put(KeyStroke.getKeyStroke(88, 0), "close");
      actionMap.put("close", new AbstractAction() {
         @Override
         public void actionPerformed(ActionEvent e) {
            onClose.run();
         }
      });
   }
}
