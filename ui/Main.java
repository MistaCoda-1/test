package ui;

import battle.BattlePanel;
import db.SyncService;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class Main extends JFrame {

    private String currentUser = "";

    public Main() {
        this.setTitle("HeartBreak!");
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setSize(1000, 750);
        this.setLocationRelativeTo(null);
        this.setResizable(false);
        this.showLogin();
        this.setVisible(true);
    }

    public String getCurrentUser() {
        return this.currentUser;
    }

    public void setCurrentUser(String currentUser) {
        this.currentUser = currentUser;
    }

    public void showLogin() {
        JPanel login = new LoginPanel(this);
        this.setContentPane(login);
        this.validate();
        this.repaint();
        login.requestFocusInWindow();
    }

    public void showMainMenu() {
        JPanel mainMenu = new MainMenuPanel(this);
        this.setContentPane(mainMenu);
        this.validate();
        this.repaint();
        mainMenu.requestFocusInWindow();
    }

    public void showBattlePanel() {
        JPanel battle = new BattlePanel(this);
        this.setContentPane(battle);
        this.validate();
        this.repaint();
        battle.requestFocusInWindow();
    }

    public static void main(String[] args) {
        SyncService.start();
        SwingUtilities.invokeLater(Main::new);
    }
}