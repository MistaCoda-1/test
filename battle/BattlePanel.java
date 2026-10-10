package battle;

import battle.bullets.BulletPattern;
import battle.bullets.BulletRain;
import battle.bullets.CrossFire;
import battle.bullets.SideSweep;
import db.LeaderboardDAO;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.RGBImageFilter;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;
import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import ui.GameSettings;
import ui.Main;
import ui.StatusBar;
import ui.UIUtils;

public class BattlePanel extends JPanel {
   private static final int STAGE_W = 960;
   private static final int STAGE_H = 700;
   private static final int ENEMY_W = 700;
   private static final int ENEMY_H = 280;
   private static final int ENEMY_X = 130;
   private static final int ENEMY_Y = 8;
   private static final int ENEMY_SPRITE = 250;
   private static final int ROW_H = 30;
   private static final int BAR_W = 110;
   private static final int BAR_H = 20;
   private static final int ENEMY_HP_Y = 292;
   private static final int BOX_X = 25;
   private static final int BOX_Y = 328;
   private static final int BOX_W = 910;
   private static final int BOX_H = 265;
   private static final int BOX_PAD = 4;
   private static final int ARENA_W = 300;
   private static final int ARENA_H = 250;
   private static final int STATUS_Y = 599;
   private static final int BTN_W = 212;
   private static final int BTN_H = 50;
   private static final int BTN_GAP = 20;
   private static final int BTN_Y = 635;
   private static final int FIGHTS_PER_ENEMY = 5;
   private static final BattlePanel.Enemy[] ENEMIES = new BattlePanel.Enemy[]{
      new BattlePanel.Enemy("/resources/enemy.png", 1, "'SEEEEKAAAAAAIIII DEEEEEE!!!'",
         "FROG", null, BattlePanel.BulletStyle.RAIN,
         "FROG - ATK 2. Only knows one song.",
         "Sing along", "You sing along. FROG croaks happily."),
      new BattlePanel.Enemy("/resources/enemy.png", 1, "'Ribbit. You again?'",
         "BLUE FROG", new Color(90, 170, 255), BattlePanel.BulletStyle.SIDES,
         "BLUE FROG - ATK 3. Attacks from both sides.",
         "Compliment", "You compliment BLUE FROG's color. It blushes... purple."),
      new BattlePanel.Enemy("/resources/enemy.png", 1, "'I am the KING of this pond!'",
         "RED FROG", new Color(255, 90, 90), BattlePanel.BulletStyle.CROSSFIRE,
         "RED FROG - ATK 4. Thinks it rules the pond.",
         "Bow", "You bow to RED FROG. It accepts your loyalty.")
   };
   private static final int ACTS_TO_SPARE = 2;
   private static final String LINE_DEFEATED = "The enemy falls over and you move on to the next level.";
   private static final String LINE_FOLLOWED = "It seems it still wanted to fight and followed you.";
   private static final String LINE_KILLED = "The enemy falls over and does not get back up.";
   private static final String LINE_NEW_ENEMY = "You move on, but something else waits ahead.";
   private static final String LINE_VICTORY = "There is no one left to fight.";
   private static final String LINE_MERCY = "'You think you can spare me?!'";
   private BattlePanel.BattleState state = BattlePanel.BattleState.PLAYER_TURN;
   private final JPanel stage = new JPanel(null);
   private int selectedBtnIndex = 0;
   private CommandButton[] buttons;
   private Runnable[] commandActions;
   EnemyBG enemyPanel = new EnemyBG(new Color(0, 255, 60), 140);
   JPanel dialoguePanel = new JPanel(new BorderLayout());
   private JLabel dialogueLabel;
   private FightMinigamePanel fightMinigame;
   private Component dialogueSlot;
   private String[] dialogueLines;
   private int dialogueIndex;
   private Runnable onDialogueDone;
   private JLabel enemySprite;
   private final ImageIcon heartIcon = new ImageIcon(this.getClass().getResource("/resources/player_soul.png"));
   Font gameFont = UIUtils.undertaleFont;
   private final String playerName;
   private int currentLvl = 1;
   private int currentHp = 20;
   private int maxHp = 20;
   private final int totalGameLvl = 15;
   private int enemyHp;
   private int enemyMaxHp;
   private JLabel statsTextLabel;
   private JLabel hpMarker;
   private StatusBar healthBar;
   private JLabel hpNumericLabel;
   private StatusBar enemyHealthBar;
   private Image BULLET_SPRITE;
   private Image needleSprite;
   private ArenaPanel arena;
   private int spareProgress = 0;
   private final List<BattlePanel.Item> inventory = new ArrayList<>(List.of(
      new BattlePanel.Item("Monster Candy", 10), new BattlePanel.Item("Spider Donut", 12), new BattlePanel.Item("Butterscotch Pie", 99)
   ));
   private JLabel[] menuLabels;
   private int menuIndex;
   private IntConsumer onMenuPick;
   private final ImageIcon blankIcon = new ImageIcon(new BufferedImage(this.heartIcon.getIconWidth(), this.heartIcon.getIconHeight(), BufferedImage.TYPE_INT_ARGB));
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
      this.buildEnemy();
      this.buildEnemyStatus();
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
      this.enemyPanel.setLayout(null);
      this.enemyPanel.setBounds(130, 8, 700, 280);
      this.enemySprite = new JLabel();
      this.enemyPanel.add(this.enemySprite);
      this.stage.add(this.enemyPanel);
      this.setEnemySprite(this.currentEnemy());
   }

   private void setEnemySprite(BattlePanel.Enemy enemy) {
      ImageIcon originalIcon = new ImageIcon(this.getClass().getResource(enemy.spritePath()));
      int targetH = 250;
      int targetW = (int)Math.round((double)originalIcon.getIconWidth() / originalIcon.getIconHeight() * targetH);
      Image image = originalIcon.getImage();
      if (enemy.tint() != null) {
         image = tinted(image, enemy.tint());
      }

      Image scaledImage = image.getScaledInstance(targetW, targetH, 4);
      this.enemySprite.setIcon(new ImageIcon(scaledImage));
      this.enemySprite.setBounds((700 - targetW) / 2, (280 - targetH) / 2, targetW, targetH);
   }

   // Multiplies every pixel by the tint color (the enemy sprite is white, so it simply becomes that color).
   private static Image tinted(Image image, Color tint) {
      RGBImageFilter filter = new RGBImageFilter() {
         @Override
         public int filterRGB(int x, int y, int argb) {
            int r = ((argb >> 16) & 0xFF) * tint.getRed() / 255;
            int g = ((argb >> 8) & 0xFF) * tint.getGreen() / 255;
            int b = (argb & 0xFF) * tint.getBlue() / 255;
            return (argb & 0xFF000000) | (r << 16) | (g << 8) | b;
         }
      };
      return Toolkit.getDefaultToolkit().createImage(new FilteredImageSource(image.getSource(), filter));
   }

   private void buildEnemyStatus() {
      JLabel marker = this.makeLabel("HP");
      int markerW = marker.getPreferredSize().width;
      int gap = 8;
      int x = (960 - (markerW + gap + 110)) / 2;
      marker.setBounds(x, 292, markerW, 30);
      this.enemyHealthBar = new StatusBar(this.enemyHp, this.enemyMaxHp);
      this.enemyHealthBar.setBounds(x + markerW + gap, 297, 110, 20);
      this.stage.add(marker);
      this.stage.add(this.enemyHealthBar);
   }

   private void buildDialogueBox() {
      this.dialoguePanel.setBackground(Color.BLACK);
      this.dialoguePanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.WHITE), new EmptyBorder(15, 20, 15, 20)));
      this.dialogueLabel = this.makeLabel(this.currentEnemy().greeting());
      this.dialogueLabel.setHorizontalAlignment(0);
      this.dialoguePanel.add(this.dialogueLabel, "Center");
      this.setDialogueSlot(this.dialoguePanel, 25, 328, 910, 265);
   }

   private void buildPlayerStatus() {
      this.statsTextLabel = this.makeLabel(this.statsText());
      this.hpMarker = this.makeLabel("HP");
      this.healthBar = new StatusBar(this.currentHp, this.maxHp);
      this.hpNumericLabel = this.makeLabel(this.hpText());
      this.stage.add(this.statsTextLabel);
      this.stage.add(this.hpMarker);
      this.stage.add(this.healthBar);
      this.stage.add(this.hpNumericLabel);
      this.layoutStatusRow();
   }

   private void buildCommandButtons() {
      String[] names = new String[]{"FIGHT", "ACT", "ITEM", "MERCY"};
      this.buttons = new CommandButton[names.length];
      int totalW = names.length * 212 + (names.length - 1) * 20;
      int startX = (960 - totalW) / 2;

      for (int i = 0; i < names.length; i++) {
         this.buttons[i] = new CommandButton(names[i], this.heartIcon);
         this.buttons[i].setBounds(startX + i * 232, 635, 212, 50);
         this.stage.add(this.buttons[i]);
      }

      this.commandActions = new Runnable[]{this::startFightSequence, this::actPlaceholder, this::itemPlaceholder, this::startMercy};
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

   // ACT: "Check" shows the enemy's info; the enemy's own act counts toward sparing it.
   private void actPlaceholder() {
      BattlePanel.Enemy enemy = this.currentEnemy();
      this.showMenu(new String[]{"Check", enemy.actName()}, choice -> {
         if (choice == 0) {
            this.startDialogue(this::showBattleBox, enemy.checkText());
         } else {
            this.spareProgress++;
            if (this.spareProgress >= ACTS_TO_SPARE) {
               this.startDialogue(this::showBattleBox, enemy.actLine(), enemy.name() + " doesn't want to fight anymore.");
            } else {
               this.startDialogue(this::showBattleBox, enemy.actLine());
            }
         }
      });
   }

   // ITEM: each item heals once and is used up. Using one takes your turn.
   private void itemPlaceholder() {
      if (this.inventory.isEmpty()) {
         this.startDialogue(this::restoreDialogueBox, "You have no items left.");
         return;
      }

      String[] names = this.inventory.stream().map(BattlePanel.Item::name).toArray(String[]::new);
      this.showMenu(names, choice -> {
         BattlePanel.Item item = this.inventory.remove(choice);
         int before = this.currentHp;
         this.updatePlayerStats(this.currentHp + item.heal(), this.currentLvl);
         String result = this.currentHp >= this.maxHp ? "Your HP was maxed out." : "You recovered " + (this.currentHp - before) + " HP!";
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
      this.enemyHp = Math.clamp((long)(this.enemyHp - damage), 0, this.enemyMaxHp);
      this.enemyHealthBar.updateHp(this.enemyHp, this.enemyMaxHp);
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
      this.startDialogue(this::returnToMainMenu, "Your SOUL shatters...", "GAME OVER. You reached LEVEL " + this.currentLvl + ".");
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
      BattlePanel.Enemy enemy = this.currentEnemy();
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

   private int fightNumber() {
      return (this.currentLvl - 1) % 5 + 1;
   }

   private BattlePanel.Enemy currentEnemy() {
      return ENEMIES[Math.min((this.currentLvl - 1) / 5, ENEMIES.length - 1)];
   }

   private void loadEnemy() {
      BattlePanel.Enemy enemy = this.currentEnemy();
      this.enemyMaxHp = enemy.maxHp();
      this.enemyHp = this.enemyMaxHp;
      this.enemyHealthBar.updateHp(this.enemyHp, this.enemyMaxHp);
      this.setEnemySprite(enemy);
      this.spareProgress = 0;
   }

   private void onEnemyDefeated() {
      this.onEnemyDefeated(false);
   }

   // Shared by killing (FIGHT) and sparing (MERCY): both clear the level.
   private void onEnemyDefeated(boolean spared) {
      boolean finalKill = this.fightNumber() == 5;
      boolean lastLevel = this.currentLvl >= 15;
      String name = this.currentEnemy().name();
      if (lastLevel) {
         String ending = spared ? "You spared " + name + ". It hops away peacefully." : "The enemy falls over and does not get back up.";
         this.startDialogue(this::returnToMainMenu, ending, "There is no one left to fight.");
      } else {
         // Level up: heal to full and record the new highest level.
         this.updatePlayerStats(this.maxHp, this.currentLvl + 1);
         this.saveProgress();
         if (finalKill) {
            String first = spared ? "You spared " + name + ". It hops away peacefully." : "The enemy falls over and does not get back up.";
            this.startDialogue(() -> {
               this.loadEnemy();
               this.startDialogue(this::restoreDialogueBox, "You move on, but something else waits ahead.");
            }, first);
         } else {
            String first = spared ? "You spared " + name + ". You move on to the next level." : "The enemy falls over and you move on to the next level.";
            this.startDialogue(() -> {
               this.loadEnemy();
               this.startDialogue(this::restoreDialogueBox, "It seems it still wanted to fight and followed you.");
            }, first);
         }
      }
   }

   private boolean isSpareable() {
      return this.spareProgress >= ACTS_TO_SPARE || this.enemyHp <= this.enemyMaxHp / 4;
   }

   private BulletPattern createPattern() {
      return switch (this.currentEnemy().bulletStyle()) {
         case RAIN -> new BulletRain(300, 250, this.BULLET_SPRITE);
         case SIDES -> new SideSweep(300, 250, this.BULLET_SPRITE);
         case CROSSFIRE -> new CrossFire(300, 250, this.needleSprite, this.BULLET_SPRITE);
      };
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

   private String statsText() {
      return this.playerName + "   LEVEL " + this.currentLvl + "/15";
   }

   private String hpText() {
      return this.currentHp + " / " + this.maxHp;
   }

   private void layoutStatusRow() {
      int x = 25;
      x = this.placeRowLabel(this.statsTextLabel, x) + 24;
      x = this.placeRowLabel(this.hpMarker, x) + 8;
      this.healthBar.setBounds(x, 604, 110, 20);
      x += 122;
      this.placeRowLabel(this.hpNumericLabel, x);
   }

   private int placeRowLabel(JLabel label, int x) {
      int w = label.getPreferredSize().width;
      label.setBounds(x, 599, w, 30);
      return x + w;
   }

   public void updatePlayerStats(int newHp, int level) {
      this.currentHp = Math.clamp((long)newHp, 0, this.maxHp);
      this.currentLvl = Math.clamp((long)level, 1, 15);
      if (this.statsTextLabel != null && this.hpNumericLabel != null && this.healthBar != null) {
         this.statsTextLabel.setText(this.statsText());
         this.hpNumericLabel.setText(this.hpText());
         this.layoutStatusRow();
         this.healthBar.updateHp(this.currentHp, this.maxHp);
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

   private static enum BulletStyle {
      RAIN,
      SIDES,
      CROSSFIRE;
   }

   // tint == null draws the sprite in its original colors.
   private record Enemy(String spritePath, int maxHp, String greeting, String name, Color tint, BulletStyle bulletStyle,
                        String checkText, String actName, String actLine) {
   }

   private record Item(String name, int heal) {
   }
}
