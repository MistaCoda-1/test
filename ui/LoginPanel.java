package ui;

import db.AccountDAO;
import db.ServerSettings;
import db.SyncService;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingWorker;

public class LoginPanel extends JPanel {
   private final JTextField textField = new JTextField(20);
   private final JPasswordField passwordField = new JPasswordField(20);
   private final JButton submitButton = new JButton("DETERMINATION");
   private final JButton createAccountButton = new JButton("CREATE ACCOUNT");
   private final JButton serverButton = new JButton("SERVER SETTINGS");
   private final ImageIcon logo = new ImageIcon(this.getClass().getResource("/resources/logo.png"));
   private final JLabel label = new JLabel(this.logo);
   JLabel loginStatusLabel = UIUtils.createLabel("");
   private final JLabel serverStatusLabel = UIUtils.createLabel("");
   private final Main main;

   public LoginPanel(Main main) {
      this.main = main;
      this.setLayout(new BoxLayout(this, 1));
      this.setBackground(Color.BLACK);
      UIUtils.styleInputField(this.textField);
      UIUtils.styleInputField(this.passwordField);
      // The default bullet echo char is a tiny 4px dot in the 8-bit font, so typing looks like it does nothing.
      this.passwordField.setEchoChar('*');
      UIUtils.styleButton(this.submitButton);
      UIUtils.styleButton(this.createAccountButton);
      UIUtils.styleButton(this.serverButton);
      Font smallFont = UIUtils.undertaleFont.deriveFont(16.0F);
      this.createAccountButton.setPreferredSize(new Dimension(280, 40));
      this.createAccountButton.setMaximumSize(new Dimension(280, 40));
      this.serverButton.setFont(smallFont);
      this.serverButton.setPreferredSize(new Dimension(220, 30));
      this.serverButton.setMaximumSize(new Dimension(220, 30));
      this.serverStatusLabel.setFont(smallFont);
      JLabel usernameLabel = UIUtils.createLabel("USERNAME: ");
      JLabel passwordLabel = UIUtils.createLabel("PASSWORD: ");
      JPanel userRow = this.createRow(usernameLabel, this.textField);
      JPanel passRow = this.createRow(passwordLabel, this.passwordField);
      this.label.setAlignmentX(0.5F);
      userRow.setAlignmentX(0.5F);
      passRow.setAlignmentX(0.5F);
      this.submitButton.setAlignmentX(0.5F);
      this.createAccountButton.setAlignmentX(0.5F);
      this.serverButton.setAlignmentX(0.5F);
      this.add(this.label);
      this.add(userRow);
      this.add(Box.createVerticalStrut(5));
      this.add(passRow);
      this.add(Box.createVerticalStrut(20));
      this.add(this.submitButton);
      this.add(Box.createVerticalStrut(10));
      this.add(this.createAccountButton);
      this.add(Box.createVerticalStrut(15));
      this.loginStatusLabel.setAlignmentX(0.5F);
      this.add(this.loginStatusLabel);
      this.add(Box.createVerticalStrut(10));
      this.serverStatusLabel.setAlignmentX(0.5F);
      this.add(this.serverStatusLabel);
      this.add(Box.createVerticalStrut(5));
      this.add(this.serverButton);
      this.add(Box.createVerticalGlue());
      this.submitButton.addActionListener(e -> this.login());
      this.textField.addActionListener(e -> this.login());
      this.passwordField.addActionListener(e -> this.login());
      this.createAccountButton.addActionListener(e -> this.createAccount());
      this.serverButton.addActionListener(e -> this.openServerSettings());
      this.checkServer();
   }

   private void login() {
      String username = this.textField.getText().trim();
      char[] password = this.passwordField.getPassword();
      if (username.isEmpty() || password.length == 0) {
         this.loginStatusLabel.setText("ENTER USERNAME AND PASSWORD");
         return;
      }

      this.setBusy(true);
      this.loginStatusLabel.setText("CHECKING...");
      (new SwingWorker<AccountDAO.LoginResult, Void>() {
         protected AccountDAO.LoginResult doInBackground() {
            return new AccountDAO().login(username, password);
         }

         @Override
         protected void done() {
            try {
               switch (this.get()) {
                  case OK -> LoginPanel.this.enterGame(AccountDAO.normalize(username));
                  case OK_OFFLINE -> LoginPanel.this.enterGame(AccountDAO.normalize(username));
                  case FAILED -> LoginPanel.this.fail(SyncService.isOnline() ? "LOGIN FAILED!" : "LOGIN FAILED! (OFFLINE)");
                  case NAME_TAKEN_ON_SERVER -> LoginPanel.this.fail("NAME BELONGS TO SOMEONE ELSE ON THE SERVER");
               }
            } catch (ExecutionException | InterruptedException e) {
               System.out.println("Login error: " + e);
               LoginPanel.this.fail("SOMETHING WENT WRONG!");
            } finally {
               Arrays.fill(password, '0');
               LoginPanel.this.showServerStatus(SyncService.isOnline());
            }
         }
      }).execute();
   }

   // Works offline too: the account is saved on this device and uploaded when the server is reachable.
   private void createAccount() {
      String username = this.textField.getText().trim();
      char[] password = this.passwordField.getPassword();
      this.setBusy(true);
      this.loginStatusLabel.setText("CREATING ACCOUNT...");
      (new SwingWorker<AccountDAO.RegisterResult, Void>() {
         protected AccountDAO.RegisterResult doInBackground() {
            return new AccountDAO().register(username, password);
         }

         @Override
         protected void done() {
            try {
               switch (this.get()) {
                  case CREATED, CREATED_OFFLINE -> LoginPanel.this.enterGame(AccountDAO.normalize(username));
                  case NAME_TAKEN -> LoginPanel.this.fail("NAME ALREADY TAKEN!");
                  case INVALID_NAME -> LoginPanel.this.fail("NAME: 3-20 LETTERS, NUMBERS OR _");
                  case PASSWORD_TOO_SHORT -> LoginPanel.this.fail("PASSWORD: AT LEAST 4 CHARACTERS");
               }
            } catch (ExecutionException | InterruptedException e) {
               System.out.println("Create account error: " + e);
               LoginPanel.this.fail("SOMETHING WENT WRONG!");
            } finally {
               Arrays.fill(password, '0');
               LoginPanel.this.showServerStatus(SyncService.isOnline());
            }
         }
      }).execute();
   }

   private void enterGame(String username) {
      this.submitButton.setEnabled(false);
      this.main.setCurrentUser(username);
      this.main.showMainMenu();
   }

   private void fail(String message) {
      this.loginStatusLabel.setText(message);
      this.setBusy(false);
   }

   private void setBusy(boolean busy) {
      this.submitButton.setEnabled(!busy);
      this.createAccountButton.setEnabled(!busy);
      this.serverButton.setEnabled(!busy);
   }

   // Checks in the background whether this device can reach the server in ServerSettings.
   private void checkServer() {
      this.serverStatusLabel.setForeground(Color.LIGHT_GRAY);
      this.serverStatusLabel.setText("SERVER " + ServerSettings.getHost() + ": CHECKING...");
      (new SwingWorker<Boolean, Void>() {
         protected Boolean doInBackground() {
            return SyncService.syncNow();
         }

         @Override
         protected void done() {
            try {
               LoginPanel.this.showServerStatus(this.get());
            } catch (ExecutionException | InterruptedException e) {
               LoginPanel.this.showServerStatus(false);
            }
         }
      }).execute();
   }

   private void showServerStatus(boolean online) {
      this.serverStatusLabel.setForeground(online ? Color.GREEN : new Color(255, 140, 60));
      this.serverStatusLabel.setText(online
         ? "SERVER " + ServerSettings.getHost() + ": ONLINE"
         : "SERVER " + ServerSettings.getHost() + ": OFFLINE - ACCOUNTS SAVE ON THIS DEVICE");
   }

   private void openServerSettings() {
      ServerSettingsOverlay overlay = new ServerSettingsOverlay(this::closeServerSettings, this::checkServer);
      this.main.getRootPane().setGlassPane(overlay);
      overlay.setVisible(true);
      overlay.focusFirstField();
   }

   private void closeServerSettings() {
      this.main.getRootPane().getGlassPane().setVisible(false);
      this.textField.requestFocusInWindow();
   }

   private JPanel createRow(JLabel label, JTextField field) {
      JPanel row = new JPanel(new FlowLayout(1, 1, 0));
      row.setOpaque(false);
      row.setPreferredSize(new Dimension(400, 45));
      row.setMaximumSize(new Dimension(400, 45));
      row.add(label);
      row.add(field);
      return row;
   }
}
