package ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

// OPTIONS screen shown over the main menu: difficulty setting and a list of controls.
public class OptionsOverlay extends UIUtils.DimmedOverlay {
   private final JLabel difficultyLabel = UIUtils.createLabel("");

   public OptionsOverlay(final Runnable onClose) {
      this.setLayout(new GridBagLayout());
      this.addMouseListener(new MouseAdapter() {});

      JPanel box = new JPanel(new BorderLayout());
      box.setPreferredSize(new Dimension(520, 480));
      box.setBackground(Color.BLACK);
      box.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.WHITE, 2), new EmptyBorder(20, 30, 15, 30)));

      JLabel title = UIUtils.createLabel("OPTIONS");
      title.setHorizontalAlignment(SwingConstants.CENTER);
      title.setBorder(new EmptyBorder(0, 0, 15, 0));
      box.add(title, BorderLayout.NORTH);

      JPanel body = new JPanel(new GridLayout(0, 1, 0, 6));
      body.setOpaque(false);
      this.difficultyLabel.setForeground(Color.YELLOW);
      this.difficultyLabel.setHorizontalAlignment(SwingConstants.CENTER);
      body.add(this.difficultyLabel);
      body.add(this.hint("LEFT / RIGHT to change"));
      body.add(new JLabel(" "));
      body.add(this.hint("CONTROLS"));
      body.add(this.hint("Arrows - move / choose"));
      body.add(this.hint("Z - confirm"));
      body.add(this.hint("X - back / slow move"));
      box.add(body, BorderLayout.CENTER);

      JLabel returnLabel = this.hint("Press X to return to Main Menu");
      returnLabel.setBorder(new EmptyBorder(15, 0, 0, 0));
      box.add(returnLabel, BorderLayout.SOUTH);
      this.add(box);
      this.refresh();

      InputMap inputMap = this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
      ActionMap actionMap = this.getActionMap();
      inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_X, 0), "close");
      inputMap.put(KeyStroke.getKeyStroke("LEFT"), "prev");
      inputMap.put(KeyStroke.getKeyStroke("RIGHT"), "next");
      actionMap.put("close", new AbstractAction() {
         @Override
         public void actionPerformed(ActionEvent e) {
            onClose.run();
         }
      });
      actionMap.put("prev", new AbstractAction() {
         @Override
         public void actionPerformed(ActionEvent e) {
            GameSettings.cycleDifficulty(-1);
            OptionsOverlay.this.refresh();
         }
      });
      actionMap.put("next", new AbstractAction() {
         @Override
         public void actionPerformed(ActionEvent e) {
            GameSettings.cycleDifficulty(1);
            OptionsOverlay.this.refresh();
         }
      });
   }

   private void refresh() {
      this.difficultyLabel.setText("DIFFICULTY:  < " + GameSettings.getDifficulty() + " >");
   }

   private JLabel hint(String text) {
      JLabel label = UIUtils.createLabel(text);
      label.setForeground(Color.LIGHT_GRAY);
      label.setHorizontalAlignment(SwingConstants.CENTER);
      return label;
   }
}
