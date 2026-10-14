package battle;

import java.awt.Color;

// One enemy type. tint == null draws the sprite in its original colors.
record Enemy(String spritePath, int maxHp, String greeting, String name, Color tint,
             String checkText, String actName, String actLine) {
}