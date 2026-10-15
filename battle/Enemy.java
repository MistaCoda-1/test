package battle;

import java.awt.Color;

record Enemy(String spritePath, int maxHp, String greeting, String name, Color tint,
             String checkText, String actName, String actLine) {
}