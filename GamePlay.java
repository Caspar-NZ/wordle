import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;

public class GamePlay {
    static Map<Character, Character> keyboard = new HashMap<>();

    public static void drawGame(Game game) {
        game.changeBackgroundColor(Color.BLACK);
        game.clearBackground(game.width(), game.height());
        double boxWidth = 50;
        double boxHeight = 50;
        double startY = 50;
        double totalWidth = game.letterCount * boxWidth + (game.letterCount - 1) * 10;
        double startX = (game.width() - totalWidth) / 2;

        for (int line = 0; line < 6; line++) {
            for (int i = 0; i < game.letterCount; i++) {
                double currentX = startX + i * (boxWidth + 10);
                double currentY = startY + line * (boxHeight + 10);
                double heightScale = Math.abs(Math.cos(Math.PI * game.flipProgress[line][i]));
                double currentHeight = boxHeight * heightScale;
                double boxY = currentY + (boxHeight - currentHeight) / 2;

                Color boxColor = Color.WHITE;
                if (game.isSolid[line][i]) {
                    boxColor = game.boxColors[line][i];
                }

                if (game.isSolid[line][i] || (game.isFlipping[line][i] && game.flipProgress[line][i] > 0.5)) {
                    game.changeColor(boxColor);
                    game.drawSolidRectangle(currentX, boxY, boxWidth, currentHeight);
                } else {
                    game.changeColor(Color.WHITE);
                    game.drawRectangle(currentX, boxY, boxWidth, currentHeight);
                }

                if (game.letters[line][i] != '\0') {
                    game.saveCurrentTransform();
                    game.translate(currentX + boxWidth / 2, boxY + currentHeight / 2);
                    double angle = Math.PI * game.flipProgress[line][i];
                    double scaleY = Math.cos(angle);
                    if (game.flipProgress[line][i] > 0.5) {
                        scaleY = -scaleY;
                    }
                    game.scale(1, scaleY);
                    game.translate(-boxWidth / 2, -boxHeight / 2);

                    if (game.flipProgress[line][i] > 0.5) {
                        game.changeColor(Color.BLACK);
                    } else {
                        game.changeColor(Color.WHITE);
                    }

                    FontMetrics metrics = game.mGraphics.getFontMetrics(game.font);
                    int letterWidth = metrics.stringWidth(String.valueOf(game.letters[line][i]));
                    int letterHeight = metrics.getAscent();
                    game.drawText((boxWidth - letterWidth) / 2, (boxHeight + letterHeight) / 2 - 3, String.valueOf(game.letters[line][i]), game.font);

                    game.restoreLastTransform();
                }
            }
        }
    }

    public static void drawKeyboard(Game game) {
        game.keyBounds.clear();
        String[] rows = {"QWERTYUIOP", "ASDFGHJKL", "ZXCVBNM"};
        int startY = 500;
        int keyWidth = 40;
        int keyHeight = 50;
        int fontSize = 25;
        int spacingBetweenRows = 55;

        Font font = null;
        for (int i = 0; i < rows.length; i++) {
            int startX = (game.width() - rows[i].length() * (keyWidth + 5)) / 2;
            for (char letter : rows[i].toCharArray()) {
                char status = keyboard.getOrDefault(letter, 'U');
                switch (status) {
                    case 'G':
                        game.changeColor(Color.GREEN);
                        break;
                    case 'Y':
                        game.changeColor(Color.YELLOW);
                        break;
                    case 'B':
                        game.changeColor(Color.GRAY);
                        break;
                    default:
                        game.changeColor(Color.LIGHT_GRAY);
                        break;
                }
                game.drawSolidRectangle(startX, startY, keyWidth, keyHeight);
                game.changeColor(Color.BLACK);
                font = new Font("Arial", Font.BOLD, fontSize);
                FontMetrics metrics = game.mGraphics.getFontMetrics(font);
                int letterWidth = metrics.stringWidth(String.valueOf(letter));
                int letterHeight = metrics.getAscent();
                game.drawText(startX + (keyWidth - letterWidth) / 2, startY + (keyHeight + letterHeight) / 2 , String.valueOf(letter), font);

                game.keyBounds.put(new Rectangle(startX, startY, keyWidth, keyHeight), letter);

                startX += keyWidth + 5;
            }
            startY += spacingBetweenRows;
        }

        // Draw the back key
        int backKeyX = game.width() / 2 - 223;
        int backKeyY = 610;
        int backKeyWidth = 60;
        int backKeyHeight = keyHeight;
        game.changeColor(Color.LIGHT_GRAY);
        game.drawSolidRectangle(backKeyX, backKeyY, backKeyWidth, backKeyHeight);
        game.changeColor(Color.BLACK);
        game.drawText(backKeyX + (backKeyWidth - fontSize) / 2, backKeyY + (backKeyHeight + fontSize) / 2 - 2, "\u232B", font);
        game.keyBounds.put(new Rectangle(backKeyX, backKeyY, backKeyWidth, backKeyHeight), '<');

        // Draw the enter key
        int enterKeyX = (game.width() / 2 + 157);
        int enterKeyY = 610;
        int enterKeyWidth = 60;
        int enterKeyHeight = keyHeight;
        game.changeColor(Color.LIGHT_GRAY);
        game.drawSolidRectangle(enterKeyX, enterKeyY, enterKeyWidth, enterKeyHeight);
        game.changeColor(Color.BLACK);
        game.drawText(enterKeyX + (enterKeyWidth - fontSize) / 2, enterKeyY + (enterKeyHeight + fontSize) / 2, "\u23CE", font);
        game.keyBounds.put(new Rectangle(enterKeyX, enterKeyY, enterKeyWidth, enterKeyHeight), '\n');
    }

    public static void drawInvalidMessage(Game game) {
        if (game.invalidWord) {
            game.changeColor(Color.RED);
            game.drawCenteredBoldText(game.width() / 2, 460, "Invalid word!", "Arial", 30);
        }
    }

    public static void handlePlayingKeyPress(Game game, KeyEvent event) {
        if (game.gameOver || game.isFlippingInProgress) {
            return;
        }

        char keyChar = Character.toUpperCase(event.getKeyChar());
        if (Character.isLetter(keyChar) && game.currentBox < game.letterCount) {
            game.letters[game.currentLine][game.currentBox] = keyChar;
            game.currentBox++;
            game.invalidWord = false;
        } else if (event.getKeyCode() == KeyEvent.VK_BACK_SPACE && game.currentBox > 0) {
            game.currentBox--;
            game.letters[game.currentLine][game.currentBox] = '\0';
            game.invalidWord = false;
        } else if (event.getKeyCode() == KeyEvent.VK_ENTER && game.currentBox == game.letterCount) {
            String guess = new String(game.letters[game.currentLine]).trim().toUpperCase();
            if (game.allowableWords.contains(guess)) {
                game.invalidWord = false;
                game.determineBoxColors();
                startFlipping(game);
                game.calculateRemainingOptions();
            } else {
                game.invalidWord = true;
            }
        }
    }

    public static void handleMouseClick(Game game, MouseEvent event) {
        Point point = event.getPoint();
        for (Map.Entry<Rectangle, Character> entry : game.keyBounds.entrySet()) {
            if (entry.getKey().contains(point)) {
                char keyChar = entry.getValue();
                if (keyChar == '<') {
                    handlePlayingKeyPress(game, new KeyEvent(game.mFrame, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_BACK_SPACE, KeyEvent.CHAR_UNDEFINED));
                } else if (keyChar == '\n') {
                    handlePlayingKeyPress(game, new KeyEvent(game.mFrame, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_ENTER, KeyEvent.CHAR_UNDEFINED));
                } else {
                    handlePlayingKeyPress(game, new KeyEvent(game.mFrame, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_UNDEFINED, keyChar));
                }
                break;
            }
        }
    }

    public static void updateFlipping(Game game, double dt) {
        for (int line = 0; line < 6; line++) {
            for (int i = 0; i < game.letterCount; i++) {
                if (game.isFlipping[line][i]) {
                    game.flipProgress[line][i] += 2 * dt;  // Adjust the flipping speed here
                    if (game.flipProgress[line][i] >= 1) {
                        game.flipProgress[line][i] = 1;
                        game.isFlipping[line][i] = false;
                        game.isSolid[line][i] = true;
                    }
                }
            }
        }
        game.isFlippingInProgress = checkFlippingInProgress(game);
    }

    private static void startFlipping(Game game) {
        game.isFlippingInProgress = true;
        new Thread(() -> {
            try {
                for (int i = 0; i < game.letterCount; i++) {
                    game.playAudio(game.loadAudio("Resources/Sounds/cardFlip.wav"));
                    game.isFlipping[game.currentLine][i] = true;
                    game.flipProgress[game.currentLine][i] = 0;
                    Thread.sleep(500);  // Control the delay between flips here
                }
                game.isFlippingInProgress = false;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            game.currentBox = 0;
            game.currentLine++;
        }).start();
    }

    private static boolean checkFlippingInProgress(Game game) {
        for (int line = 0; line < 6; line++) {
            for (int i = 0; i < game.letterCount; i++) {
                if (game.isFlipping[line][i]) {
                    return true;
                }
            }
        }
        return false;
    }
}
