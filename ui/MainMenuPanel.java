package ui;

import db.SyncService;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.Timer;

public class MainMenuPanel extends JPanel {
   JPanel menuPanel = new JPanel();
   JPanel artPanel = new JPanel();
   JPanel btnPanel = new JPanel();
   private int selectedBtnIndex = 0;
   private MenuButton[] buttons;
   private Runnable[] menuActions;
   private boolean leaderboardOpen = false;
   private boolean optionsOpen = false;
   private final Main main;
   private static final Font SMALL_FONT = UIUtils.undertaleFont.deriveFont(16.0F);
   private static final Color OFFLINE_COLOR = new Color(255, 140, 60);
   private final JLabel statusLabel = new JLabel(" ");
   private final Timer statusTimer = new Timer(2000, e -> this.refreshStatus());

   public MainMenuPanel(Main main) {
      this.main = main;
      this.setLayout(new BorderLayout());
      this.setBackground(Color.BLACK);
      this.menuPanel.setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 30));
      this.btnPanel.setLayout(new BoxLayout(this.btnPanel, 1));
      this.btnPanel.setOpaque(false);
      this.menuPanel.setOpaque(false);
      this.artPanel.setOpaque(false);
      this.menuPanel.setBackground(Color.RED);
      this.artPanel.setBackground(Color.BLUE);
      this.menuPanel.setLayout(new GridBagLayout());
      MenuButton startBtn = new MenuButton("START");
      MenuButton leaderboardBtn = new MenuButton("LEADERBOARD");
      MenuButton optionsBtn = new MenuButton("OPTIONS");
      MenuButton exitBtn = new MenuButton("LOG OUT");
      startBtn.setAlignmentX(0.5F);
      leaderboardBtn.setAlignmentX(0.5F);
      optionsBtn.setAlignmentX(0.5F);
      exitBtn.setAlignmentX(0.5F);
      this.buttons = new MenuButton[]{startBtn, leaderboardBtn, optionsBtn, exitBtn};
      this.menuActions = new Runnable[]{main::showBattlePanel, this::showLeaderboard, this::optionsPlaceholder, main::showLogin};
      this.btnPanel.add(startBtn);
      this.btnPanel.add(Box.createVerticalStrut(16));
      this.btnPanel.add(leaderboardBtn);
      this.btnPanel.add(Box.createVerticalStrut(16));
      this.btnPanel.add(optionsBtn);
      this.btnPanel.add(Box.createVerticalStrut(16));
      this.btnPanel.add(exitBtn);
      this.btnPanel.setAlignmentX(0.5F);
      this.menuPanel.add(this.buildColumn());
      this.add(this.menuPanel, "West");
      this.buildArt();
      this.add(this.artPanel, "Center");
      this.updateSelection();
      InputMap inputMap = this.getInputMap(2);
      ActionMap actionMap = this.getActionMap();
      inputMap.put(KeyStroke.getKeyStroke("UP"), "moveUp");
      inputMap.put(KeyStroke.getKeyStroke("DOWN"), "moveDown");
      inputMap.put(KeyStroke.getKeyStroke(90, 0), "select");
      actionMap.put("moveUp", new AbstractAction() {
         @Override
         public void actionPerformed(ActionEvent e) {
            if (!MainMenuPanel.this.leaderboardOpen && !MainMenuPanel.this.optionsOpen) {
               MainMenuPanel.this.selectedBtnIndex--;
               if (MainMenuPanel.this.selectedBtnIndex < 0) {
                  MainMenuPanel.this.selectedBtnIndex = MainMenuPanel.this.buttons.length - 1;
               }

               MainMenuPanel.this.updateSelection();
            }
         }
      });
      actionMap.put("moveDown", new AbstractAction() {
         @Override
         public void actionPerformed(ActionEvent e) {
            if (!MainMenuPanel.this.leaderboardOpen && !MainMenuPanel.this.optionsOpen) {
               MainMenuPanel.this.selectedBtnIndex++;
               if (MainMenuPanel.this.selectedBtnIndex > MainMenuPanel.this.buttons.length - 1) {
                  MainMenuPanel.this.selectedBtnIndex = 0;
               }

               MainMenuPanel.this.updateSelection();
            }
         }
      });
      actionMap.put("select", new AbstractAction() {
         @Override
         public void actionPerformed(ActionEvent e) {
            if (!MainMenuPanel.this.leaderboardOpen && !MainMenuPanel.this.optionsOpen) {
               MainMenuPanel.this.menuActions[MainMenuPanel.this.selectedBtnIndex].run();
            }
         }
      });
      this.setVisible(true);
   }

   private void showLeaderboard() {
      if (!this.leaderboardOpen) {
         this.leaderboardOpen = true;
         LeaderboardOverlay overlay = new LeaderboardOverlay(this::hideLeaderboard);
         this.main.getRootPane().setGlassPane(overlay);
         overlay.setVisible(true);
         overlay.requestFocusInWindow();
      }
   }

   private void hideLeaderboard() {
      this.leaderboardOpen = false;
      this.main.getRootPane().getGlassPane().setVisible(false);
      this.requestFocusInWindow();
   }

   private void optionsPlaceholder() {
      if (!this.optionsOpen) {
         this.optionsOpen = true;
         OptionsOverlay overlay = new OptionsOverlay(this::hideOptions);
         this.main.getRootPane().setGlassPane(overlay);
         overlay.setVisible(true);
      }
   }

   private void hideOptions() {
      this.optionsOpen = false;
      this.main.getRootPane().getGlassPane().setVisible(false);
      this.requestFocusInWindow();
   }

   // Logo and player name above the buttons; sync status and controls below, matching the login screen.
   private JPanel buildColumn() {
      JPanel column = new JPanel();
      column.setLayout(new BoxLayout(column, 1));
      column.setOpaque(false);
      ImageIcon logo = new ImageIcon(this.getClass().getResource("/resources/logo.png"));
      int logoW = 340;
      int logoH = (int)Math.round((double)logo.getIconHeight() / logo.getIconWidth() * logoW);
      JLabel logoLabel = new JLabel(new ImageIcon(logo.getImage().getScaledInstance(logoW, logoH, Image.SCALE_SMOOTH)));
      logoLabel.setAlignmentX(0.5F);
      JLabel welcome = UIUtils.createLabel("WELCOME, " + this.main.getCurrentUser());
      welcome.setForeground(Color.YELLOW);
      welcome.setAlignmentX(0.5F);
      this.statusLabel.setFont(SMALL_FONT);
      this.statusLabel.setAlignmentX(0.5F);
      JLabel hint = UIUtils.createLabel("ARROWS + Z");
      hint.setFont(SMALL_FONT);
      hint.setForeground(Color.GRAY);
      hint.setAlignmentX(0.5F);
      column.add(logoLabel);
      column.add(welcome);
      column.add(Box.createVerticalStrut(30));
      column.add(this.btnPanel);
      column.add(Box.createVerticalStrut(30));
      column.add(this.statusLabel);
      column.add(Box.createVerticalStrut(10));
      column.add(hint);
      return column;
   }

   // Shows whether progress is reaching the server or waiting on this device.
   private void refreshStatus() {
      boolean online = SyncService.isOnline();
      int pending = SyncService.pendingCount();
      String changes = pending + (pending == 1 ? " CHANGE" : " CHANGES");
      if (online && pending == 0) {
         this.statusLabel.setText("ONLINE - PROGRESS SYNCED");
         this.statusLabel.setForeground(Color.GREEN);
      } else if (online) {
         this.statusLabel.setText("ONLINE - SYNCING " + changes + "...");
         this.statusLabel.setForeground(Color.YELLOW);
      } else if (pending == 0) {
         this.statusLabel.setText("OFFLINE - PROGRESS SAVES ON THIS DEVICE");
         this.statusLabel.setForeground(OFFLINE_COLOR);
      } else {
         this.statusLabel.setText("OFFLINE - " + changes + " WAITING TO SYNC");
         this.statusLabel.setForeground(OFFLINE_COLOR);
      }
   }

   @Override
   public void addNotify() {
      super.addNotify();
      this.refreshStatus();
      this.statusTimer.start();
      SyncService.syncInBackground();
   }

   @Override
   public void removeNotify() {
      this.statusTimer.stop();
      super.removeNotify();
   }

   private void buildArt() {
      ImageIcon original = new ImageIcon(this.getClass().getResource("/resources/mikudayo.png"));
      int targetH = 640;
      int targetW = (int)Math.round((double)original.getIconWidth() / original.getIconHeight() * targetH);
      JLabel art = new JLabel(new ImageIcon(original.getImage().getScaledInstance(targetW, targetH, Image.SCALE_SMOOTH)));
      this.artPanel.setLayout(new GridBagLayout());
      this.artPanel.add(art);
   }

   private void updateSelection() {
      for (int i = 0; i < this.buttons.length; i++) {
         this.buttons[i].setSelected(i == this.selectedBtnIndex);
      }
   }
}
