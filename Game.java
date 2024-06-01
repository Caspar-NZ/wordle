import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import javax.swing.Timer;

public class Game extends GameEngine {
    private int letterCount = 5;
    private int currentLine = 0;
    private int currentBox = 0;
    private boolean[][] isFlipping;
    private boolean[][] isSolid;
    private double[][] flipProgress;
    private double flipSpeed = 2;
    private char[][] letters;
    private Color[][] boxColors;
    private Map<Character, Character> keyboard = new HashMap<>();
    private String targetWord;
    private boolean gameOver = false;
    private boolean isFlippingInProgress = false;
    private Set<String> allowableWords = new HashSet<>();
    private boolean invalidWord = false;

    private double boxWidth = 50;
    private double boxHeight = 50;
    private double startY = 50;

    private Font font = new Font("Arial", Font.BOLD, 40);

    private final String[] keyboardRows = {"QWERTYUIOP", "ASDFGHJKL", "ZXCVBNM"};
    private double keyboardStartY = 500;

    private enum GameState { MENU, PLAYING, GAME_OVER, SUCCESS }
    private GameState gameState = GameState.MENU;

    private Timer gameOverTimer;
    private List<Integer> remainingOptionsAfterEachGuess = new ArrayList<>();

    public static void main(String[] args) {
        createGame(new Game());
    }

    @Override
    public void init() {
        setWindowSize(500, 700);
        loadAllowableWords(letterCount);
        chooseRandomTargetWord();
    }

    private void loadAllowableWords(int length) {
        allowableWords.clear();
        String fileName = length + "letter.txt";
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = br.readLine()) != null) {
                allowableWords.add(line.toUpperCase().trim());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void chooseRandomTargetWord() {
        int index = new Random().nextInt(allowableWords.size());
        targetWord = new ArrayList<>(allowableWords).get(index);
        targetWord = "KAMES";
        System.out.println("Target word: " + targetWord);
    }

    @Override
    public void update(double dt) {
        if (gameState == GameState.PLAYING) {
            for (int line = 0; line < 6; line++) {
                for (int i = 0; i < letterCount; i++) {
                    if (isFlipping[line][i]) {
                        flipProgress[line][i] += flipSpeed * dt;
                        if (flipProgress[line][i] >= 1) {
                            flipProgress[line][i] = 1;
                            isFlipping[line][i] = false;
                            isSolid[line][i] = true;
                        }
                    }
                }
            }
            isFlippingInProgress = checkFlippingInProgress();
        }
    }

    @Override
    public void paintComponent() {
        clearBackground(width(), height());

        switch (gameState) {
            case MENU:
                drawMenu();
                break;
            case PLAYING:
                drawGame();
                drawKeyboard();
                drawInvalidMessage();
                break;
            case GAME_OVER:
                drawGameOver();
                break;
            case SUCCESS:
                drawSuccess();
                break;
        }
    }

    private void drawMenu() {
        changeBackgroundColor(Color.BLACK);
        clearBackground(width(), height());
        changeColor(Color.WHITE);
        drawBoldText(50, height() / 2 - 90, "Press 4 for 4 Letter Words", "Arial", 30);
        drawBoldText(50, height() / 2 - 30, "Press 5 for 5 Letter Words", "Arial", 30);
        drawBoldText(50, height() / 2 + 30, "Press 6 for 6 Letter Words", "Arial", 30);
    }

    private void drawGameOver() {
        changeBackgroundColor(Color.BLACK);
        clearBackground(width(), height());
        changeColor(Color.WHITE);
        drawBoldText(20, 200, "Game Over", "Arial", 40);
        drawBoldText(20, 240, "The word was: " + targetWord, "Arial", 20);
        drawGameReview();
        drawBoldText(20, 500, "Press R to Restart", "Arial", 20);
        drawBoldText(20, 550, "Press M for Menu", "Arial", 20);
    }

    private void drawSuccess() {
        changeBackgroundColor(Color.BLACK);
        clearBackground(width(), height());
        changeColor(Color.GREEN);
        drawBoldText(20, 200, "Congratulations!", "Arial", 40);
        drawBoldText(20, 240, "You guessed the word!", "Arial", 20);
        drawGameReview();
        drawBoldText(20, 500, "Press R to Restart", "Arial", 20);
        drawBoldText(20, 550, "Press M for Menu", "Arial", 20);
    }

    private void drawGameReview() {
        changeColor(Color.WHITE);
        int yOffset = 300;
        for (int i = 0; i < remainingOptionsAfterEachGuess.size() - 1; i++) {
            drawBoldText(20, yOffset + (i * 20), "After guess " + (i + 1) + ": " + remainingOptionsAfterEachGuess.get(i) + " options remained", "Arial", 20);
        }
    }

    private void drawGame() {
        changeBackgroundColor(Color.BLACK);
        clearBackground(width(), height());
        double totalWidth = letterCount * boxWidth + (letterCount - 1) * 10;
        double startX = (width() - totalWidth) / 2;

        for (int line = 0; line < 6; line++) {
            for (int i = 0; i < letterCount; i++) {
                double currentX = startX + i * (boxWidth + 10);
                double currentY = startY + line * (boxHeight + 10);
                double heightScale = Math.abs(Math.cos(Math.PI * flipProgress[line][i]));
                double currentHeight = boxHeight * heightScale;
                double boxY = currentY + (boxHeight - currentHeight) / 2;

                Color boxColor = Color.WHITE;
                if (isSolid[line][i]) {
                    boxColor = boxColors[line][i];
                }

                if (isSolid[line][i] || (isFlipping[line][i] && flipProgress[line][i] > 0.5)) {
                    changeColor(boxColor);
                    drawSolidRectangle(currentX, boxY, boxWidth, currentHeight);
                } else {
                    changeColor(Color.WHITE);
                    drawRectangle(currentX, boxY, boxWidth, currentHeight);
                }

                if (letters[line][i] != '\0') {
                    saveCurrentTransform();
                    translate(currentX + boxWidth / 2, boxY + currentHeight / 2);
                    double angle = Math.PI * flipProgress[line][i];
                    double scaleY = Math.cos(angle);
                    if (flipProgress[line][i] > 0.5) {
                        scaleY = -scaleY;
                    }
                    scale(1, scaleY);
                    translate(-boxWidth / 2, -boxHeight / 2);

                    if (flipProgress[line][i] > 0.5) {
                        changeColor(Color.BLACK);
                    } else {
                        changeColor(Color.WHITE);
                    }

                    FontMetrics metrics = mGraphics.getFontMetrics(font);
                    int letterWidth = metrics.stringWidth(String.valueOf(letters[line][i]));
                    int letterHeight = metrics.getAscent();
                    drawText((boxWidth - letterWidth) / 2, (boxHeight + letterHeight) / 2 - 3, String.valueOf(letters[line][i]), font);

                    restoreLastTransform();
                }
            }
        }
    }

    private void drawInvalidMessage() {
        if (invalidWord) {
            changeColor(Color.RED);
            String message = "Invalid word!";
            Font font = new Font("Arial", Font.BOLD, 30);
            FontMetrics metrics = mGraphics.getFontMetrics(font);
            int messageWidth = metrics.stringWidth(message);
            drawText((width() - messageWidth) / 2, 460, message, font);
        }
    }

    private void drawKeyboard() {
        String[] rows = {"QWERTYUIOP", "ASDFGHJKL", "ZXCVBNM"};
        int startY = 500;
        int keyWidth = 40;
        int keyHeight = 50;
        int fontSize = 25;
        int spacingBetweenRows = 60;

        for (int i = 0; i < rows.length; i++) {
            int startX = (width() - rows[i].length() * (keyWidth + 5)) / 2;
            for (char letter : rows[i].toCharArray()) {
                char status = keyboard.getOrDefault(letter, 'U');
                switch (status) {
                    case 'G':
                        changeColor(Color.GREEN);
                        break;
                    case 'Y':
                        changeColor(Color.YELLOW);
                        break;
                    case 'B':
                        changeColor(Color.GRAY);
                        break;
                    default:
                        changeColor(Color.LIGHT_GRAY);
                        break;
                }
                drawSolidRectangle(startX, startY, keyWidth, keyHeight);
                changeColor(Color.BLACK);
                Font font = new Font("Arial", Font.BOLD, fontSize);
                FontMetrics metrics = mGraphics.getFontMetrics(font);
                int letterWidth = metrics.stringWidth(String.valueOf(letter));
                int letterHeight = metrics.getAscent();
                drawText(startX + (keyWidth - letterWidth) / 2, startY + (keyHeight + letterHeight) / 2 - 5, String.valueOf(letter), font);
                startX += keyWidth + 5;
            }
            startY += spacingBetweenRows;
        }
    }

    @Override
    public void keyPressed(KeyEvent event) {
        switch (gameState) {
            case MENU:
                handleMenuKeyPress(event);
                break;
            case PLAYING:
                handlePlayingKeyPress(event);
                break;
            case GAME_OVER:
            case SUCCESS:
                handleGameOverKeyPress(event);
                break;
        }

        if (event.getKeyCode() == KeyEvent.VK_ESCAPE) {
            gameState = GameState.MENU;
        }
    }

    private void handleMenuKeyPress(KeyEvent event) {
        switch (event.getKeyCode()) {
            case KeyEvent.VK_4:
                letterCount = 4;
                startGame();
                break;
            case KeyEvent.VK_5:
                letterCount = 5;
                startGame();
                break;
            case KeyEvent.VK_6:
                letterCount = 6;
                startGame();
                break;
        }
    }

    private void startGame() {
        if (gameOverTimer != null) {
            gameOverTimer.stop();
        }

        currentLine = 0;
        currentBox = 0;
        gameOver = false;
        isFlippingInProgress = false;
        invalidWord = false;
        isFlipping = new boolean[6][letterCount];
        isSolid = new boolean[6][letterCount];
        flipProgress = new double[6][letterCount];
        letters = new char[6][letterCount];
        boxColors = new Color[6][letterCount];
        keyboard.clear();
        loadAllowableWords(letterCount);
        chooseRandomTargetWord();
        gameState = GameState.PLAYING;
        remainingOptionsAfterEachGuess.clear();
    }

    private void handlePlayingKeyPress(KeyEvent event) {
        if (gameOver || isFlippingInProgress) {
            return;
        }

        char keyChar = Character.toUpperCase(event.getKeyChar());
        if (Character.isLetter(keyChar) && currentBox < letterCount) {
            letters[currentLine][currentBox] = keyChar;
            currentBox++;
            invalidWord = false;
        } else if (event.getKeyCode() == KeyEvent.VK_BACK_SPACE && currentBox > 0) {
            currentBox--;
            letters[currentLine][currentBox] = '\0';
            invalidWord = false;
        } else if (event.getKeyCode() == KeyEvent.VK_ENTER && currentBox == letterCount) {
            String guess = new String(letters[currentLine]).trim().toUpperCase();
            System.out.println("Guessed word: " + guess);
            if (allowableWords.contains(guess)) {
                invalidWord = false;
                determineBoxColors();
                startFlipping();
                calculateRemainingOptions();
            } else {
                invalidWord = true;
                System.out.println("Invalid guess. Word not found in word list.");
            }
        }
    }

    private void handleGameOverKeyPress(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.VK_R) {
            startGame();
        } else if (event.getKeyCode() == KeyEvent.VK_M) {
            gameState = GameState.MENU;
        }
    }

    private void determineBoxColors() {
        String inputWord = new String(letters[currentLine]);
        int[] targetLetterCounts = new int[26];
        int[] inputLetterCounts = new int[26];

        for (char c : targetWord.toCharArray()) {
            targetLetterCounts[c - 'A']++;
        }

        for (int i = 0; i < letterCount; i++) {
            char inputChar = letters[currentLine][i];
            if (inputChar == targetWord.charAt(i)) {
                boxColors[currentLine][i] = Color.GREEN;
                targetLetterCounts[inputChar - 'A']--;
                inputLetterCounts[inputChar - 'A']++;
                keyboard.put(inputChar, 'G');
            }
        }

        for (int i = 0; i < letterCount; i++) {
            if (boxColors[currentLine][i] != null) {
                continue;
            }

            char inputChar = letters[currentLine][i];
            if (targetLetterCounts[inputChar - 'A'] > 0) {
                boxColors[currentLine][i] = Color.YELLOW;
                targetLetterCounts[inputChar - 'A']--;
                inputLetterCounts[inputChar - 'A']++;
                if (keyboard.getOrDefault(inputChar, 'U') != 'G') {
                    keyboard.put(inputChar, 'Y');
                }
            } else {
                boxColors[currentLine][i] = Color.GRAY;
                if (!keyboard.containsKey(inputChar) || keyboard.get(inputChar) == 'U') {
                    keyboard.put(inputChar, 'B');
                }
            }
        }

        if (inputWord.equals(targetWord) || currentLine == 5) {
            gameOver = true;
            gameOverTimer = new Timer((letterCount + 1) * 500, e -> {
                gameState = inputWord.equals(targetWord) ? GameState.SUCCESS : GameState.GAME_OVER;
                mFrame.repaint();
            });
            gameOverTimer.setRepeats(false);
            gameOverTimer.start();
        }
    }
// Calculates how many possible words remain after each guess
    private void calculateRemainingOptions() {
        Set<String> remainingOptions = new HashSet<>(allowableWords);
        Map<Integer, Set<Character>> possibleLetters = new HashMap<>();
        Set<Character> knownLetters = new HashSet<>();

        // Initialise possible letters for each position
        for (int i = 0; i < letterCount; i++) {
            possibleLetters.put(i, new HashSet<>());
            for (char c = 'A'; c <= 'Z'; c++) {
                possibleLetters.get(i).add(c);
            }
        }

        // Collect all ruled out letters and required letters with counts
        for (int line = 0; line <= currentLine; line++) {
            for (int i = 0; i < letterCount; i++) {
                char guessChar = letters[line][i];
                if (boxColors[line][i] == Color.GRAY) {
                    for (int j = 0; j < letterCount; j++) {
                        possibleLetters.get(j).remove(guessChar);
                    }
                } else {
                    if (boxColors[line][i] == Color.GREEN) {
                        possibleLetters.get(i).clear();
                        possibleLetters.get(i).add(guessChar);
                    } else if (boxColors[line][i] == Color.YELLOW) {
                        possibleLetters.get(i).remove(guessChar);
                        knownLetters.add(guessChar);
                    }
                }
            }
        }

        // Filter remaining options based on possible letters for each position and required counts
        Set<String> validOptions = new HashSet<>();
        for (String word : remainingOptions) {
            if (isValidOption(word, possibleLetters, knownLetters)) {
                validOptions.add(word);
            }
        }

        remainingOptionsAfterEachGuess.add(validOptions.size());
        System.out.println("Remaining options after guess " + (currentLine + 1) + ": " + validOptions.size());

        if (validOptions.size() < 20) {
            System.out.println("Possible remaining words: " + validOptions);
        }

        // Debugging: output remaining possible letters for each position
        for (int i = 0; i < letterCount; i++) {
            System.out.println("Position " + i + ": " + possibleLetters.get(i));
        }

        // Update remaining options to be valid options
        remainingOptions = validOptions;
    }

    private boolean isValidOption(String word, Map<Integer, Set<Character>> possibleLetters, Set<Character> knownLetters) {
        int[] wordLetterCounts = new int[26];
        for (char c : word.toCharArray()) {
            wordLetterCounts[c - 'A']++;
        }

        // Check each position for valid letters
        for (int i = 0; i < letterCount; i++) {
            if (!possibleLetters.get(i).contains(word.charAt(i))) {
                return false;
            }
        }

        // Check if word contains all known letters
        for (char c : knownLetters) {
            if (wordLetterCounts[c - 'A'] == 0) {
                return false;
            }
        }

        return true;
    }


    private void startFlipping() {
        isFlippingInProgress = true;
        new Thread(() -> {
            try {
                for (int i = 0; i < letterCount; i++) {
                    playAudio(loadAudio("cardFlip.wav"));
                    isFlipping[currentLine][i] = true;
                    flipProgress[currentLine][i] = 0;
                    Thread.sleep(500);
                }
                isFlippingInProgress = false;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            currentBox = 0;
            currentLine++;
        }).start();
    }

    private boolean checkFlippingInProgress() {
        for (int line = 0; line < 6; line++) {
            for (int i = 0; i < letterCount; i++) {
                if (isFlipping[line][i]) {
                    return true;
                }
            }
        }
        return false;
    }
}
