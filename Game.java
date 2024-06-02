import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.List;
import javax.swing.Timer;

public class Game extends GameEngine {
    int letterCount = 5;
    int currentLine = 0;
    int currentBox = 0;
    boolean[][] isFlipping;
    boolean[][] isSolid;
    double[][] flipProgress;
    char[][] letters;
    Color[][] boxColors;
    String targetWord;
    boolean gameOver = false;
    boolean isFlippingInProgress = false;
    boolean invalidWord = false;
    Font font = new Font("Arial", Font.BOLD, 40);
    Set<String> allowableWords = new HashSet<>();
    private GameState gameState = GameState.MENU;
    private Timer gameOverTimer;
    List<Integer> remainingOptionsAfterEachGuess = new ArrayList<>();
    Map<Rectangle, Character> keyBounds = new HashMap<>(); // Store key bounds and their characters

    private enum GameState { MENU, PLAYING, GAME_OVER }

    public static void main(String[] args) {
        createGame(new Game());
    }

    @Override
    public void init() {
        setWindowSize(500, 700);
        loadAllowableWords(letterCount);
        chooseRandomTargetWord();
        Menu.init(this);
    }

    private void loadAllowableWords(int length) {
        allowableWords.clear();
        String fileName = "Resources/" + length + "letter.txt";
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
        System.out.println("Target word: " + targetWord);
    }

    @Override
    public void update(double dt) {
        if (gameState == GameState.MENU) {
            Menu.update(this, dt);
        } else if (gameState == GameState.PLAYING) {
            GamePlay.updateFlipping(this, dt);
        }
    }

    @Override
    public void paintComponent() {
        clearBackground(width(), height());

        switch (gameState) {
            case MENU:
                Menu.drawMenu(this);
                break;
            case PLAYING:
                GamePlay.drawGame(this);
                GamePlay.drawKeyboard(this);
                GamePlay.drawInvalidMessage(this);
                break;
            case GAME_OVER:
                GameOver.drawGameOver(this);
                break;
        }
    }

    @Override
    public void keyPressed(KeyEvent event) {
        if (gameState == GameState.MENU) {
            Menu.keyPressed(this, event);
        } else {
            switch (gameState) {
                case PLAYING:
                    GamePlay.handlePlayingKeyPress(this, event);
                    break;
                case GAME_OVER:
                    handleGameOverKeyPress(event);
                    break;
            }

            if (event.getKeyCode() == KeyEvent.VK_ESCAPE) {
                gameState = GameState.MENU;
            }
        }
    }
    public void mouseClicked(MouseEvent event) {
        if (gameState == GameState.PLAYING && !isFlippingInProgress) {
            GamePlay.handleMouseClick(this, event);
        }
    }

    void handleMenuKeyPress(KeyEvent event) {
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
        GamePlay.keyboard.clear();
        loadAllowableWords(letterCount);
        chooseRandomTargetWord();
        gameState = GameState.PLAYING;
        remainingOptionsAfterEachGuess.clear();
    }

    private void handleGameOverKeyPress(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.VK_R) {
            startGame();
        } else if (event.getKeyCode() == KeyEvent.VK_M) {
            gameState = GameState.MENU;
        }
    }

    void determineBoxColors() {
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
                GamePlay.keyboard.put(inputChar, 'G');
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
                if (GamePlay.keyboard.getOrDefault(inputChar, 'U') != 'G') {
                    GamePlay.keyboard.put(inputChar, 'Y');
                }
            } else {
                boxColors[currentLine][i] = Color.GRAY;
                if (!GamePlay.keyboard.containsKey(inputChar) || GamePlay.keyboard.get(inputChar) == 'U') {
                    GamePlay.keyboard.put(inputChar, 'B');
                }
            }
        }

        if (inputWord.equals(targetWord) || currentLine == 5) {
            gameOver = true;
            gameOverTimer = new Timer((letterCount + 1) * 500, e -> {
                gameState = GameState.GAME_OVER;
                mFrame.repaint();
            });
            gameOverTimer.setRepeats(false);
            gameOverTimer.start();
        }
    }

    void calculateRemainingOptions() {
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

}
