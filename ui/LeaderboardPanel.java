package ui;

import db.LeaderboardDAO;
import db.LeaderboardEntry;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;

public class LeaderboardPanel extends JPanel {
   Font gameFont = UIUtils.undertaleFont;
   private final JPanel tablePanel = new JPanel(new GridBagLayout());
   private JLabel titleLabel;

   public LeaderboardPanel() {
      this.setPreferredSize(new Dimension(520, 480));
      this.setBackground(Color.BLACK);
      this.setOpaque(true);
      this.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.WHITE, 2), new EmptyBorder(20, 30, 15, 30)));
      this.setLayout(new BorderLayout());
      JLabel title = new JLabel("LEADERBOARD", 0);
      title.setForeground(Color.WHITE);
      title.setFont(this.gameFont);
      title.setBorder(new EmptyBorder(0, 0, 15, 0));
      this.add(title, "North");
      this.titleLabel = title;
      this.tablePanel.setOpaque(false);
      this.add(this.tablePanel, "Center");
      JLabel returnLabel = new JLabel("Press X to return to Main Menu", 0);
      returnLabel.setForeground(Color.LIGHT_GRAY);
      returnLabel.setFont(this.gameFont);
      returnLabel.setBorder(new EmptyBorder(15, 0, 0, 0));
      this.add(returnLabel, "South");
      this.showMessage("Loading...");
      this.loadScores();
   }

   private void loadScores() {
      LeaderboardDAO dao = new LeaderboardDAO();
      (new SwingWorker<List<LeaderboardEntry>, Void>() {
         protected List<LeaderboardEntry> doInBackground() throws Exception {
            return dao.getTopPlayers(10);
         }

         @Override
         protected void done() {
            try {
               LeaderboardPanel.this.showEntries(this.get());
               if (dao.isOffline()) {
                  LeaderboardPanel.this.titleLabel.setText("LEADERBOARD (OFFLINE)");
               }
            } catch (ExecutionException | InterruptedException var2) {
               System.out.println("Open XAMPP first.");
               LeaderboardPanel.this.showMessage("Could not load leaderboard.");
            }
         }
      }).execute();
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
         this.addFiller(row);
      } else {
         this.addFiller(row);
      }

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
      this.addCell(rank, 0, row, 0.2, 2, color);
      this.addCell(username, 1, row, 0.5, 2, color);
      this.addCell(level, 2, row, 0.3, 4, color);
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
      gbc.fill = 2;
      this.tablePanel.add(label, gbc);
   }

   private void addSpanningMessage(int row, String message) {
      JLabel label = new JLabel(message, 0);
      label.setForeground(Color.LIGHT_GRAY);
      label.setFont(this.gameFont);
      label.setBorder(new EmptyBorder(20, 0, 0, 0));
      GridBagConstraints gbc = new GridBagConstraints();
      gbc.gridx = 0;
      gbc.gridy = row;
      gbc.gridwidth = 3;
      gbc.fill = 2;
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
