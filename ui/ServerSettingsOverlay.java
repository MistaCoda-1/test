package ui;

import db.Database;
import db.ServerSettings;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.concurrent.ExecutionException;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;

// SERVER SETTINGS screen shown over the login screen, styled like the rest of the game.
public class ServerSettingsOverlay extends UIUtils.DimmedOverlay {
   private static final Font FIELD_FONT = UIUtils.undertaleFont.deriveFont(20.0F);
   private static final Font SMALL_FONT = UIUtils.undertaleFont.deriveFont(16.0F);
   private final JTextField hostField = new JTextField(ServerSettings.getHost());
   private final JTextField portField = new JTextField(String.valueOf(ServerSettings.getPort()));
   private final JTextField userField = new JTextField(ServerSettings.getUser());
   private final JPasswordField passwordField = new JPasswordField(ServerSettings.getPassword());
   private final JLabel statusLabel = new JLabel(" ", SwingConstants.CENTER);
   private final JButton testButton = new JButton("TEST");
   private final JButton saveButton = new JButton("SAVE");
   private final JButton cancelButton = new JButton("CANCEL");
   private final Runnable onClose;
   private final Runnable onSaved;

   public ServerSettingsOverlay(Runnable onClose, Runnable onSaved) {
      this.onClose = onClose;
      this.onSaved = onSaved;
      this.setLayout(new GridBagLayout());
      this.addMouseListener(new MouseAdapter() {});

      JPanel box = new JPanel(new BorderLayout());
      box.setPreferredSize(new Dimension(640, 480));
      box.setBackground(Color.BLACK);
      box.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.WHITE, 2), new EmptyBorder(20, 30, 20, 30)));

      JPanel header = new JPanel();
      header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
      header.setOpaque(false);
      header.add(this.centered(UIUtils.createLabel("SERVER SETTINGS"), Color.WHITE, UIUtils.undertaleFont));
      header.add(this.centered(new JLabel(" "), Color.GRAY, SMALL_FONT));
      header.add(this.centered(new JLabel("ADMIN'S COMPUTER (RUNS XAMPP): localhost + root"), Color.LIGHT_GRAY, SMALL_FONT));
      header.add(this.centered(new JLabel("OTHER DEVICES: ADMIN'S IP + heartbreak ACCOUNT"), Color.LIGHT_GRAY, SMALL_FONT));
      box.add(header, BorderLayout.NORTH);

      JPanel form = new JPanel(new GridBagLayout());
      form.setOpaque(false);
      this.addRow(form, 0, "SERVER IP", this.hostField);
      this.addRow(form, 1, "PORT", this.portField);
      this.addRow(form, 2, "DB USER", this.userField);
      this.passwordField.setEchoChar('*');
      this.addRow(form, 3, "DB PASSWORD", this.passwordField);
      box.add(form, BorderLayout.CENTER);

      JPanel footer = new JPanel();
      footer.setLayout(new BoxLayout(footer, BoxLayout.Y_AXIS));
      footer.setOpaque(false);
      this.statusLabel.setFont(SMALL_FONT);
      this.statusLabel.setAlignmentX(0.5F);
      footer.add(this.statusLabel);
      JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 10));
      buttons.setOpaque(false);
      for (JButton button : new JButton[]{this.testButton, this.saveButton, this.cancelButton}) {
         UIUtils.styleButton(button);
         button.setFont(FIELD_FONT);
         button.setPreferredSize(new Dimension(150, 40));
         buttons.add(button);
      }

      footer.add(buttons);
      footer.add(this.centered(new JLabel("ENTER: SAVE    ESC: CANCEL"), Color.GRAY, SMALL_FONT));
      box.add(footer, BorderLayout.SOUTH);
      this.add(box);

      this.testButton.addActionListener(e -> this.test());
      this.saveButton.addActionListener(e -> this.save());
      this.cancelButton.addActionListener(e -> this.onClose.run());
      for (JTextField field : new JTextField[]{this.hostField, this.portField, this.userField, this.passwordField}) {
         field.addActionListener(e -> this.save());
      }

      this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cancel");
      this.getActionMap().put("cancel", new AbstractAction() {
         @Override
         public void actionPerformed(ActionEvent e) {
            ServerSettingsOverlay.this.onClose.run();
         }
      });
   }

   // Focus the first field once the overlay is on screen.
   public void focusFirstField() {
      this.hostField.requestFocusInWindow();
      this.hostField.selectAll();
   }

   private void addRow(JPanel form, int row, String text, JTextField field) {
      JLabel label = UIUtils.createLabel(text);
      label.setFont(FIELD_FONT);
      UIUtils.styleInputField(field);
      field.setFont(FIELD_FONT);
      field.setPreferredSize(new Dimension(280, 35));
      GridBagConstraints c = new GridBagConstraints();
      c.gridy = row;
      c.insets = new Insets(5, 0, 5, 14);
      c.anchor = GridBagConstraints.EAST;
      form.add(label, c);
      c.gridx = 1;
      c.insets = new Insets(5, 0, 5, 0);
      c.anchor = GridBagConstraints.WEST;
      form.add(field, c);
   }

   private JLabel centered(JLabel label, Color color, Font font) {
      label.setForeground(color);
      label.setFont(font);
      label.setAlignmentX(0.5F);
      return label;
   }

   private Integer parsePort() {
      try {
         int port = Integer.parseInt(this.portField.getText().trim());
         return port > 0 && port <= 65535 ? port : null;
      } catch (NumberFormatException e) {
         return null;
      }
   }

   private void showStatus(String text, Color color) {
      this.statusLabel.setText(text);
      this.statusLabel.setForeground(color);
   }

   private void test() {
      Integer port = this.parsePort();
      if (port == null) {
         this.showStatus("PORT MUST BE A NUMBER (USUALLY 3306)", Color.RED);
         return;
      }

      String host = this.hostField.getText().trim();
      String user = this.userField.getText().trim();
      String password = new String(this.passwordField.getPassword());
      this.testButton.setEnabled(false);
      this.showStatus("TESTING " + host + "...", Color.LIGHT_GRAY);
      (new SwingWorker<Void, Void>() {
         protected Void doInBackground() throws Exception {
            Database.testConnection(host, port, user, password);
            return null;
         }

         @Override
         protected void done() {
            ServerSettingsOverlay.this.testButton.setEnabled(true);
            try {
               this.get();
               ServerSettingsOverlay.this.showStatus("CONNECTED! PRESS SAVE TO USE THIS SERVER.", Color.GREEN);
            } catch (ExecutionException | InterruptedException e) {
               ServerSettingsOverlay.this.showStatus(ServerSettingsOverlay.reason(e), Color.RED);
            }
         }
      }).execute();
   }

   private static String reason(Exception e) {
      String message = e.getCause() instanceof SQLException sql ? sql.getMessage() : String.valueOf(e.getCause());
      if (message != null && message.contains("Access denied")) {
         return "WRONG DB USER OR PASSWORD";
      }

      return "CAN'T CONNECT - CHECK THE IP AND THAT XAMPP IS RUNNING";
   }

   private void save() {
      Integer port = this.parsePort();
      if (port == null) {
         this.showStatus("PORT MUST BE A NUMBER (USUALLY 3306)", Color.RED);
         return;
      }

      try {
         ServerSettings.update(this.hostField.getText(), port, this.userField.getText(), new String(this.passwordField.getPassword()));
      } catch (IOException e) {
         this.showStatus("COULD NOT SAVE SETTINGS: " + e.getMessage(), Color.RED);
         return;
      }

      Database.reset();
      this.onClose.run();
      this.onSaved.run();
   }
}
