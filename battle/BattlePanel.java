package battle;

import battle.bullets.BulletPattern;
import battle.bullets.BulletScaling;

import db.LeaderboardDAO;

import ui.GameSettings;
import ui.Main;
import ui.UIUtils;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;

import javax.imageio.ImageIO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class BattlePanel extends JPanel {
   private static final int STAGE_W = 960;

   private static final int ACTS_TO_SPARE = 4;

   private BattlePanel.BattleState state = BattlePanel.BattleState.PLAYER_TURN;

   private final JPanel stage = new JPanel(null);

   private int selectedBtnIndex = 0;
   private CommandButton[] buttons;
   private Runnable[] commandActions;

   JPanel dialoguePanel = new JPanel(new BorderLayout());
   private JLabel dialogueLabel;
   private FightMinigamePanel fightMinigame;
   private Component dialogueSlot;
   private String[] dialogueLines;
   private int dialogueIndex;
   private Runnable onDialogueDone;

   private final ImageIcon heartIcon = new ImageIcon(this.getClass().getResource("/resources/player_soul.png"));
   Font gameFont = UIUtils.undertaleFont;

   private final String playerName;

   private int enemyNumber = 0;
   private int fightsWithEnemy = 1;
   private int currentLvl = 1;
   private int currentHp = 30;
   private int maxHp = 30;

   private int enemyHp;
   private int enemyMaxHp;
   private EnemyView enemyView;

   private PlayerStatusRow playerStatus;

   private Image BULLET_SPRITE;
   private Image needleSprite;
   private PatternPicker patternPicker;

   private ArenaPanel arena;

   private int spareProgress = 0;

   private final List<Item> inventory = new ArrayList<>(List.of(
         new Item("Monster White - 10HP", 10), new Item("Malunggay Pandesal - 20HP", 20), new Item("Sting - 20HP", 20),
         new Item("KCO Chips - 30HP", 30)));

   private JLabel[] menuLabels;
   private int menuIndex;
   private IntConsumer onMenuPick;
   private final ImageIcon blankIcon = new ImageIcon(
         new BufferedImage(this.heartIcon.getIconWidth(), this.heartIcon.getIconHeight(), BufferedImage.TYPE_INT_ARGB));
   private final Main main;

   public BattlePanel(Main main) {
      this.main = main;
      this.playerName = main.getCurrentUser();

      this.setLayout(null);
      this.setBackground(Color.BLACK);
      this.stage.setBackground(Color.BLACK);
      this.add(this.stage);

      this.enemyMaxHp = this.currentEnemy().maxHp();
      this.enemyHp = this.enemyMaxHp;

      this.loadBullet();

      this.patternPicker = new PatternPicker(300, 250, this.BULLET_SPRITE, this.needleSprite);
      
      this.buildEnemy();
      this.buildDialogueBox();
      this.buildPlayerStatus();
      this.buildCommandButtons();
      this.buildKeyBindings();
      this.updateSelection();
   }

   public void loadBullet() {
      try {
         URL resource = this.getClass().getResource("/resources/bullet2.png");
         if (resource != null) {
            this.BULLET_SPRITE = ImageIO.read(resource);
         } else {
            System.err.println("Bullet sprite not found");
         }
      } catch (IOException var2) {
         System.err.println("Error reading bullet asset file: " + var2.getMessage());
         this.BULLET_SPRITE = null;
      }

      try {
         URL needle = this.getClass().getResource("/resources/bullet1.png");
         this.needleSprite = needle != null ? ImageIO.read(needle) : null;
      } catch (IOException e) {
         System.err.println("Error reading needle asset file: " + e.getMessage());
         this.needleSprite = null;
      }
   }

   @Override
   public void doLayout() {
      this.stage.setBounds((this.getWidth() - 960) / 2, (this.getHeight() - 700) / 2, 960, 700);
   }

   private void buildEnemy() {
      this.enemyView = new EnemyView(STAGE_W, this.currentEnemy(), this.enemyHp, this.enemyMaxHp);
      this.stage.add(this.enemyView);
   }

   private void buildDialogueBox() {
      this.dialoguePanel.setBackground(Color.BLACK);
      this.dialoguePanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.WHITE),
            new EmptyBorder(15, 20, 15, 20)));
      this.dialogueLabel = this.makeLabel(this.currentEnemy().greeting());
      this.dialogueLabel.setHorizontalAlignment(0);
      this.dialoguePanel.add(this.dialogueLabel, "Center");
      this.setDialogueSlot(this.dialoguePanel, 25, 328, 910, 265);
   }

   private void buildPlayerStatus() {
      this.playerStatus = new PlayerStatusRow(this.playerName, STAGE_W, this.currentLvl, this.currentHp, this.maxHp);
      this.stage.add(this.playerStatus);
   }

   private void buildCommandButtons() {
      String[] names = new String[] { "FIGHT", "ACT", "ITEM", "MERCY" };
      this.buttons = new CommandButton[names.length];
      int totalW = names.length * 212 + (names.length - 1) * 20;
      int startX = (960 - totalW) / 2;

      for (int i = 0; i < names.length; i++) {
         this.buttons[i] = new CommandButton(names[i], this.heartIcon);
         this.buttons[i].setBounds(startX + i * 232, 635, 212, 50);
         this.stage.add(this.buttons[i]);
      }

      this.commandActions = new Runnable[] { this::startFightSequence, this::actButton, this::itemButton,
            this::startMercy };
   }

   private void buildKeyBindings() {
      InputMap inputMap = this.getInputMap(2);
      ActionMap actionMap = this.getActionMap();
      inputMap.put(KeyStroke.getKeyStroke("LEFT"), "moveLeft");
      inputMap.put(KeyStroke.getKeyStroke("RIGHT"), "moveRight");
      inputMap.put(KeyStroke.getKeyStroke(90, 0), "select");
      actionMap.put("moveLeft", new AbstractAction() {
         @Override
         public void actionPerformed(ActionEvent e) {
            if (BattlePanel.this.state == BattlePanel.BattleState.PLAYER_TURN) {
               BattlePanel.this.selectedBtnIndex--;
               if (BattlePanel.this.selectedBtnIndex < 0) {
                  BattlePanel.this.selectedBtnIndex = BattlePanel.this.buttons.length - 1;
               }

               BattlePanel.this.updateSelection();
            }
         }
      });
      actionMap.put("moveRight", new AbstractAction() {
         @Override
         public void actionPerformed(ActionEvent e) {
            if (BattlePanel.this.state == BattlePanel.BattleState.PLAYER_TURN) {
               BattlePanel.this.selectedBtnIndex++;
               if (BattlePanel.this.selectedBtnIndex > BattlePanel.this.buttons.length - 1) {
                  BattlePanel.this.selectedBtnIndex = 0;
               }

               BattlePanel.this.updateSelection();
            }
         }
      });
      actionMap.put("select", new AbstractAction() {
         @Override
         public void actionPerformed(ActionEvent e) {
            if (BattlePanel.this.state == BattlePanel.BattleState.DIALOGUE) {
               BattlePanel.this.advanceDialogue();
            } else if (BattlePanel.this.state == BattlePanel.BattleState.PLAYER_TURN) {
               BattlePanel.this.commandActions[BattlePanel.this.selectedBtnIndex].run();
            } else if (BattlePanel.this.state == BattlePanel.BattleState.MENU) {
               BattlePanel.this.pickMenuOption();
            }
         }
      });
      inputMap.put(KeyStroke.getKeyStroke("UP"), "menuUp");
      inputMap.put(KeyStroke.getKeyStroke("DOWN"), "menuDown");
      inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_X, 0), "menuBack");
      actionMap.put("menuUp", new AbstractAction() {
         @Override
         public void actionPerformed(ActionEvent e) {
            if (BattlePanel.this.state == BattlePanel.BattleState.MENU) {
               BattlePanel.this.moveMenu(-1);
            }
         }
      });
      actionMap.put("menuDown", new AbstractAction() {
         @Override
         public void actionPerformed(ActionEvent e) {
            if (BattlePanel.this.state == BattlePanel.BattleState.MENU) {
               BattlePanel.this.moveMenu(1);
            }
         }
      });
      actionMap.put("menuBack", new AbstractAction() {
         @Override
         public void actionPerformed(ActionEvent e) {
            if (BattlePanel.this.state == BattlePanel.BattleState.MENU) {
               BattlePanel.this.cancelMenu();
            }
         }
      });
   }

   private JLabel makeLabel(String text) {
      JLabel label = new JLabel(text);
      label.setFont(this.gameFont);
      label.setForeground(Color.WHITE);
      return label;
   }

   private void setDialogueSlot(Component occupant, int x, int y, int w, int h) {
      if (this.dialogueSlot != null) {
         this.stage.remove(this.dialogueSlot);
      }

      this.dialogueSlot = occupant;
      occupant.setBounds(x, y, w, h);
      this.stage.add(occupant);
      this.stage.revalidate();
      this.stage.repaint();
   }

   private AnimateBox placeAnimator(int fromW, int fromH, int toW, int toH, Runnable onDone) {
      AnimateBox animator = new AnimateBox(fromW, fromH, toW, toH, onDone);
      this.setDialogueSlot(animator, 21, 324, 918, 273);
      return animator;
   }

   // ACT: "Check" shows the enemy's info; the enemy's own act counts toward
   // sparing it.
   private void actButton() {
      Enemy enemy = this.currentEnemy();
      this.showMenu(new String[] { "Check", enemy.actName() }, choice -> {
         if (choice == 0) {
            this.startDialogue(this::showBattleBox, enemy.checkText());
         } else {
            this.spareProgress++;
            if (this.spareProgress >= ACTS_TO_SPARE) {
               this.startDialogue(this::showBattleBox, enemy.actLine(),
                     enemy.name() + " doesn't want to fight anymore.");
            } else {
               this.startDialogue(this::showBattleBox, enemy.actLine());
            }
         }
      });
   }

   // ITEM: each item heals once and is used up. Using one takes your turn.
   private void itemButton() {
      if (this.inventory.isEmpty()) {
         this.startDialogue(this::restoreDialogueBox, "You have no items left.");
         return;
      }

      String[] names = this.inventory.stream().map(Item::name).toArray(String[]::new);
      this.showMenu(names, choice -> {
         Item item = this.inventory.remove(choice);
         int before = this.currentHp;
         this.updatePlayerStats(this.currentHp + item.heal(), this.currentLvl);
         String result = this.currentHp >= this.maxHp ? "Your HP was maxed out."
               : "You recovered " + (this.currentHp - before) + " HP!";
         this.startDialogue(this::showBattleBox, "You ate the " + item.name() + ".", result);
      });
   }

   // MERCY: works after enough ACTs, or once the enemy is at 1/4 HP or less.
   private void startMercy() {
      if (this.isSpareable()) {
         this.state = BattlePanel.BattleState.DIALOGUE;
         this.onEnemyDefeated(true);
      } else {
         this.startDialogue(this::showBattleBox, "'You think you can spare me?!'");
      }
   }

   private void showMenu(String[] options, IntConsumer onPick) {
      this.state = BattlePanel.BattleState.MENU;
      this.clearSelectionIcons();
      this.onMenuPick = onPick;
      this.menuIndex = 0;
      this.menuLabels = new JLabel[options.length];
      JPanel list = new JPanel(new GridLayout(0, 1, 0, 4));
      list.setOpaque(false);

      for (int i = 0; i < options.length; i++) {
         this.menuLabels[i] = this.makeLabel("* " + options[i]);
         this.menuLabels[i].setIconTextGap(12);
         list.add(this.menuLabels[i]);
      }

      JPanel holder = new JPanel(new BorderLayout());
      holder.setOpaque(false);
      holder.add(list, "North");
      JLabel backHint = this.makeLabel("Z: choose   X: back");
      backHint.setForeground(Color.GRAY);
      holder.add(backHint, "South");
      this.dialoguePanel.removeAll();
      this.dialoguePanel.add(holder, "Center");
      this.updateMenuSelection();
      this.dialoguePanel.revalidate();
      this.dialoguePanel.repaint();
   }

   private void moveMenu(int delta) {
      this.menuIndex = Math.floorMod(this.menuIndex + delta, this.menuLabels.length);
      this.updateMenuSelection();
   }

   private void updateMenuSelection() {
      for (int i = 0; i < this.menuLabels.length; i++) {
         boolean selected = i == this.menuIndex;
         this.menuLabels[i].setIcon(selected ? this.heartIcon : this.blankIcon);
         this.menuLabels[i].setForeground(selected ? Color.YELLOW : Color.WHITE);
      }
   }

   private void pickMenuOption() {
      IntConsumer pick = this.onMenuPick;
      this.onMenuPick = null;
      this.menuLabels = null;
      pick.accept(this.menuIndex);
   }

   private void cancelMenu() {
      this.onMenuPick = null;
      this.menuLabels = null;
      this.restoreDialogueBox();
   }

   private void startFightSequence() {
      if (this.state == BattlePanel.BattleState.PLAYER_TURN) {
         this.state = BattlePanel.BattleState.FIGHT_MINIGAME;
         this.clearSelectionIcons();
         this.dialoguePanel.removeAll();
         this.fightMinigame = new FightMinigamePanel(this::onHitEnemy);
         this.dialoguePanel.add(this.fightMinigame, "Center");
         this.dialoguePanel.revalidate();
         this.dialoguePanel.repaint();
         this.fightMinigame.start();
      }
   }

   private void onHitEnemy(int damage) {
      this.enemyHp = Math.clamp((long) (this.enemyHp - damage), 0, this.enemyMaxHp);
      this.enemyView.showHp(this.enemyHp, this.enemyMaxHp);
      Timer resultPause = new Timer(1200, e -> {
         if (this.enemyHp <= 0) {
            this.onEnemyDefeated();
         } else {
            this.showBattleBox();
         }
      });
      resultPause.setRepeats(false);
      resultPause.start();
   }

   private void showBattleBox() {
      this.placeAnimator(910, 265, 300, 250, this::finalizeBattleBox).startAnimation();
   }

   private void finalizeBattleBox() {
      this.arena = new ArenaPanel(300, 250, this.heartIcon, this::onPlayerHit, this::returnToPlayerTurn);
      this.setDialogueSlot(this.arena, 330, 335, 300, 250);
      this.state = BattlePanel.BattleState.ENEMY_TURN;
      this.arena.startTurn(this.createPattern());
   }

   private void onPlayerHit(int damage) {
      this.updatePlayerStats(this.currentHp - GameSettings.scaleDamage(damage), this.currentLvl);
      if (this.currentHp <= 0 && this.state == BattlePanel.BattleState.ENEMY_TURN) {
         this.onPlayerDefeated();
      }
   }

   private void onPlayerDefeated() {
      this.arena.endTurn();
      this.startDialogue(this::returnToMainMenu, "Your SOUL shatters...",
            "GAME OVER. You reached LEVEL " + this.currentLvl + ".");
   }

   private void saveProgress() {
      String username = this.playerName;
      int level = this.currentLvl;
      Thread saver = new Thread(() -> {
         try {
            new LeaderboardDAO().saveHighestLevel(username, level);
         } catch (SQLException e) {
            System.out.println("Could not save score (is XAMPP running?): " + e.getMessage());
         }
      });
      saver.setDaemon(true);
      saver.start();
   }

   private void returnToPlayerTurn() {
      this.placeAnimator(300, 250, 910, 265, this::restoreDialogueBox).startAnimation();
   }

   private void restoreDialogueBox() {
      Enemy enemy = this.currentEnemy();
      String text;
      if (this.spareProgress >= ACTS_TO_SPARE) {
         text = enemy.name() + " doesn't want to fight anymore. (Try MERCY)";
      } else if (this.isSpareable()) {
         text = enemy.name() + " looks ready to give up. (Try MERCY)";
      } else {
         text = enemy.greeting();
      }

      this.showDialogueText(text);
      this.state = BattlePanel.BattleState.PLAYER_TURN;
      this.updateSelection();
   }

   private Enemy currentEnemy() {
      return Enemies.at(this.enemyNumber);
   }

   private void loadEnemy() {
      Enemy enemy = this.currentEnemy();
      this.enemyMaxHp = enemy.maxHp();
      this.enemyHp = this.enemyMaxHp;
      this.enemyView.showHp(this.enemyHp, this.enemyMaxHp);
      this.enemyView.setEnemy(enemy);
      this.spareProgress = 0;
   }

   private void onEnemyDefeated() {
      this.onEnemyDefeated(false);
   }

   // Shared by killing (FIGHT) and sparing (MERCY): both clear the level. The game
   // is endless,
   // so there is no last level; a run only ends when the player is defeated.
   private void onEnemyDefeated(boolean spared) {
      String name = this.currentEnemy().name();
      // Sparing ends the whole stretch with this enemy; killing only does after its
      // last fight.
      boolean nextEnemy = spared || this.fightsWithEnemy == Enemies.FIGHTS_PER_ENEMY;
      // Level up: record the new highest level. HP carries over, there is no free
      // heal between levels.
      this.updatePlayerStats(this.currentHp, this.currentLvl + 1);
      this.saveProgress();
      if (nextEnemy) {
         this.enemyNumber++;
         this.fightsWithEnemy = 1;
         String first = spared ? "You spared " + name + ". It hops away peacefully."
               : "The enemy falls over and does not get back up.";
         this.startDialogue(() -> {
            this.loadEnemy();
            this.startDialogue(this::restoreDialogueBox, "You move on, but something else waits ahead.");
         }, first);
      } else {
         this.fightsWithEnemy++;
         String first = "The enemy falls over and you move on to the next level.";
         this.startDialogue(() -> {
            this.loadEnemy();
            this.startDialogue(this::restoreDialogueBox, "It seems it still had some fight in it and followed you.");
         }, first);
      }
   }

   private boolean isSpareable() {
      return this.spareProgress >= ACTS_TO_SPARE || this.enemyHp <= this.enemyMaxHp / 4;
   }

   private BulletPattern createPattern() {
      return this.patternPicker.next(BulletScaling.multiplier(this.currentLvl));
   }

   private void returnToMainMenu() {
      this.main.showMainMenu();
   }

   private void startDialogue(Runnable onDone, String... lines) {
      this.state = BattlePanel.BattleState.DIALOGUE;
      this.clearSelectionIcons();
      this.dialogueLines = lines;
      this.dialogueIndex = 0;
      this.onDialogueDone = onDone;
      this.showDialogueText(lines[0]);
   }

   private void advanceDialogue() {
      if (this.dialogueLines != null) {
         this.dialogueIndex++;
         if (this.dialogueIndex < this.dialogueLines.length) {
            this.showDialogueText(this.dialogueLines[this.dialogueIndex]);
         } else {
            Runnable done = this.onDialogueDone;
            this.dialogueLines = null;
            this.onDialogueDone = null;
            if (done != null) {
               done.run();
            }
         }
      }
   }

   private void showDialogueText(String text) {
      this.dialogueLabel.setText(text);
      this.dialoguePanel.removeAll();
      this.dialoguePanel.add(this.dialogueLabel, "Center");
      if (this.dialogueSlot != this.dialoguePanel) {
         this.setDialogueSlot(this.dialoguePanel, 25, 328, 910, 265);
      } else {
         this.dialoguePanel.revalidate();
         this.dialoguePanel.repaint();
      }
   }

   public void updatePlayerStats(int newHp, int level) {
      this.currentHp = Math.clamp((long) newHp, 0, this.maxHp);
      this.currentLvl = Math.max(1, level);
      if (this.playerStatus != null) {
         this.playerStatus.update(this.currentLvl, this.currentHp, this.maxHp);
      }
   }

   private void updateSelection() {
      for (int i = 0; i < this.buttons.length; i++) {
         this.buttons[i].setSelected(i == this.selectedBtnIndex);
      }
   }

   private void clearSelectionIcons() {
      for (CommandButton button : this.buttons) {
         button.setSelected(false);
      }
   }

   static enum BattleState {
      PLAYER_TURN,
      FIGHT_MINIGAME,
      ENEMY_TURN,
      DIALOGUE,
      MENU;
   }
}