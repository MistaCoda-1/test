package ui;

import db.LeaderboardDAO;
import db.LeaderboardEntry;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.swing.BorderFactory;
import javax.swing.border.EmptyBorder;
import javax.swing.*;

public class LeaderboardOverlay extends UIUtils.DimmedOverlay {
   private final Font gameFont = UIUtils.undertaleFont;
   private final JPanel tablePanel = new JPanel(new GridBagLayout());
   private JLabel titleLabel;

   public LeaderboardOverlay(final Runnable onClose) {
      this.setLayout(new GridBagLayout());
      this.setFocusable(true);
      this.addMouseListener(new MouseAdapter() {});

      JPanel box = new JPanel(new BorderLayout());
      box.setPreferredSize(new Dimension(520, 480));
      box.setBackground(Color.BLACK);
      box.setOpaque(true);
      box.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.WHITE, 2), 
            new EmptyBorder(20, 30, 15, 30)
      ));

      JLabel title = new JLabel("LEADERBOARD", 0);
      title.setForeground(Color.WHITE);
      title.setFont(this.gameFont);
      title.setBorder(new EmptyBorder(0, 0, 15, 0));
      box.add(title, BorderLayout.NORTH);
      this.titleLabel = title;

      this.tablePanel.setOpaque(false);
      box.add(this.tablePanel, BorderLayout.CENTER);

      JLabel returnLabel = new JLabel("Press X to return to Main Menu", 0);
      returnLabel.setForeground(Color.LIGHT_GRAY);
      returnLabel.setFont(this.gameFont);
      returnLabel.setBorder(new EmptyBorder(15, 0, 0, 0));
      box.add(returnLabel, BorderLayout.SOUTH);

      this.add(box);

      this.showMessage("Loading...");
      this.loadScores();
      this.setupKeyBindings(onClose);
   }

   private void setupKeyBindings(final Runnable onClose) {
      InputMap inputMap = this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
      ActionMap actionMap = this.getActionMap();
      
      inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_X, 0), "close");
      actionMap.put("close", new AbstractAction() {
         @Override
         public void actionPerformed(ActionEvent e) {
            onClose.run();
         }
      });
   }

   private void loadScores() {
      LeaderboardDAO dao = new LeaderboardDAO();
      new SwingWorker<List<LeaderboardEntry>, Void>() {
         @Override
         protected List<LeaderboardEntry> doInBackground() throws Exception {
            return dao.getTopPlayers(10);
         }

         @Override
         protected void done() {
            try {
               LeaderboardOverlay.this.showEntries(this.get());
               if (dao.isOffline()) {
                  LeaderboardOverlay.this.titleLabel.setText("LEADERBOARD (OFFLINE)");
               }
            } catch (ExecutionException | InterruptedException e) {
               System.out.println("Open XAMPP first.");
               LeaderboardOverlay.this.showMessage("Could not load leaderboard.");
            }
         }
      }.execute();
   }

   private void showEntries(List<LeaderboardEntry> entries) {
      this.tablePanel.removeAll();
      this.addRow(0, "RANK", "USERNAME", "HIGHEST LEVEL", Color.YELLOW);
      int row = 1;

      for (LeaderboardEntry entry : entries) {
         this.addRow(row++, String.valueOf(entry.getRank()), entry.getUsername(), String.valueOf(entry.getHighestLevel()), Color.WHITE);
      }

      if (entries.isEmpty()) {
         this.addSpanningMessage(row++, "No scores yet.");
      }
      
      this.addFiller(row);
      this.tablePanel.revalidate();
      this.tablePanel.repaint();
   }

   private void showMessage(String message) {
      this.tablePanel.removeAll();
      this.addRow(0, "RANK", "USERNAME", "HIGHEST LEVEL", Color.YELLOW);
      this.addSpanningMessage(1, message);
      this.addFiller(2);
      this.tablePanel.revalidate();
      this.tablePanel.repaint();
   }

   private void addRow(int row, String rank, String username, String level, Color color) {
      this.addCell(rank, 0, row, 0.2, JLabel.LEFT, color);
      this.addCell(username, 1, row, 0.5, JLabel.LEFT, color);
      this.addCell(level, 2, row, 0.3, JLabel.RIGHT, color);
   }

   private void addCell(String text, int col, int row, double weightX, int align, Color color) {
      JLabel label = new JLabel(text, align);
      label.setForeground(color);
      label.setFont(this.gameFont);
      label.setBorder(new EmptyBorder(4, 0, 4, 0));
      GridBagConstraints gbc = new GridBagConstraints();
      gbc.gridx = col;
      gbc.gridy = row;
      gbc.weightx = weightX;
      gbc.fill = GridBagConstraints.HORIZONTAL;
      this.tablePanel.add(label, gbc);
   }

   private void addSpanningMessage(int row, String message) {
      JLabel label = new JLabel(message, JLabel.CENTER);
      label.setForeground(Color.LIGHT_GRAY);
      label.setFont(this.gameFont);
      label.setBorder(new EmptyBorder(20, 0, 0, 0));
      GridBagConstraints gbc = new GridBagConstraints();
      gbc.gridx = 0;
      gbc.gridy = row;
      gbc.gridwidth = 3;
      gbc.fill = GridBagConstraints.HORIZONTAL;
      this.tablePanel.add(label, gbc);
   }

   private void addFiller(int row) {
      GridBagConstraints gbc = new GridBagConstraints();
      gbc.gridx = 0;
      gbc.gridy = row;
      gbc.gridwidth = 3;
      gbc.weighty = 1.0;
      this.tablePanel.add(Box.createGlue(), gbc);
   }
}