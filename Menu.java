import java.awt.Color;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Menu {
    private static Sprite idleSprite;
    private static Sprite runSprite;
    private static Sprite jumpSprite;
    private static boolean isRunning = false;
    private static boolean isJumping = false;
    private static boolean isGameOver = false;
    private static boolean isPlaying = false;
    private static double jumpTime = 0;
    private static double jumpVelocity = -250; // Initial jump velocity (negative for upward movement)
    private static double gravity = 600; // Gravity pulling the character down
    private static double jumpYPosition = 0; // Current vertical position of the jump
    private static final double MAX_JUMP_TIME = 1.1;
    private static List<Obstacle> obstacles = new ArrayList<>();
    private static double obstacleSpawnTimer = 0;
    private static double obstacleSpawnInterval = 1.2; // Minimum interval between obstacles
    private static double obstacleSpeed = 200; // Initial speed of obstacles
    private static int score = 0;
    private static double timeAlive = 0;
    private static Random rand = new Random();

    public static void init(Game game) {
        Image idleSheet = game.loadImage("Resources/Pictures/Idle_2.png");
        Image runSheet = game.loadImage("Resources/Pictures/Run.png");
        Image jumpSheet = game.loadImage("Resources/Pictures/Jump.png");

        idleSprite = new Sprite(idleSheet, 128, 128, 7, 0.2);
        runSprite = new Sprite(runSheet, 128, 128, 8, 0.07);
        jumpSprite = new Sprite(jumpSheet, 128, 128, 12, 0.08);
    }

    public static void update(Game game, double dt) {
        if (isGameOver) {
            idleSprite.update(dt);
            return;
        }

        if (isPlaying) {
            timeAlive += dt;
            score = (int) (timeAlive * 10);

            // Handle obstacles
            obstacleSpawnTimer += dt;
            if (obstacleSpawnTimer >= obstacleSpawnInterval) {
                spawnObstacle(game);
                obstacleSpawnTimer = 0;
                obstacleSpawnInterval = 1.2 + rand.nextDouble(); // Random interval between 1.2 and 2.2 seconds
                obstacleSpeed += 10; // Increase obstacle speed gradually
            }

            for (Obstacle obstacle : obstacles) {
                obstacle.update(dt);
            }

            // Remove off-screen obstacles
            obstacles.removeIf(Obstacle::isOffScreen);

            // Check for collisions
            int playerX = game.width() / 2 - 10; // Adjust collision box to the middle 20 pixels of the sprite
            int playerY = game.height() - 128 + (int) jumpYPosition;
            int playerWidth = 20; // Width of the collision box
            int playerHeight = 128;

            for (Obstacle obstacle : obstacles) {
                if (obstacle.collidesWith(playerX, playerY, playerWidth, playerHeight)) {
                    isGameOver = true;
                    isPlaying = false;
                    isRunning = false;
                    isJumping = false;
                    jumpYPosition = 0;
                    obstacles.clear();
                    idleSprite.reset();
                    break;
                }
            }
        }

        if (isJumping) {
            jumpTime += dt;
            jumpYPosition += jumpVelocity * dt;
            jumpVelocity += gravity * dt;

            if (jumpTime >= MAX_JUMP_TIME) {
                jumpTime = 0;
                jumpVelocity = -250; // Reset jump velocity
                jumpYPosition = 0; // Reset jump position
                isJumping = false;
                isRunning = true;
                runSprite.reset();
            } else {
                jumpSprite.update(dt);
            }
        } else if (isRunning) {
            runSprite.update(dt);
        } else {
            idleSprite.update(dt);
        }
    }

    private static void spawnObstacle(Game game) {
        int startY = game.height() - 30; // Position along the bottom of the screen
        int width = 20;
        int height = 30; // Height of the obstacles
        double speed = obstacleSpeed;
        int startX = game.width();
        obstacles.add(new Obstacle(startX, startY, width, height, speed));
    }

    public static void drawMenu(Game game) {
        game.changeBackgroundColor(game.black);
        game.clearBackground(game.width(), game.height());
        game.changeColor(game.white);
        game.drawCenteredBoldText(game.width() / 2, game.height() / 2 - 160, "Expanded Wordle", "Arial", 40);
        game.drawCenteredBoldText(game.width() / 2, game.height() / 2 - 90, "Press 4 for 4 Letter Words", "Arial", 30);
        game.drawCenteredBoldText(game.width() / 2, game.height() / 2 - 30, "Press 5 for 5 Letter Words", "Arial", 30);
        game.drawCenteredBoldText(game.width() / 2, game.height() / 2 + 30, "Press 6 for 6 Letter Words", "Arial", 30);
        game.drawCenteredBoldText(game.width() / 2, game.height() - 180, "Little mini game for when you get bored", "Arial", 20);
        game.drawCenteredBoldText(game.width() / 2, game.height() - 150, "Press SPACE to Start Running", "Arial", 20);

        if (isGameOver) {
            game.drawCenteredBoldText(game.width() / 2, game.height() - 120, "Game Over! Score: " + score, "Arial", 16);
            game.drawCenteredBoldText(game.width() / 2, game.height() - 100, "Press SPACE to Restart", "Arial", 16);
        }

        Image currentFrame;
        if (isJumping) {
            currentFrame = jumpSprite.getCurrentFrame();
        } else if (isRunning) {
            currentFrame = runSprite.getCurrentFrame();
        } else {
            currentFrame = idleSprite.getCurrentFrame();
        }

        // Adjust the Y position based on jump
        int baseY = game.height() - 128;
        int adjustedY = baseY;
        if ((int) jumpYPosition < 0) {
            adjustedY = baseY + (int) jumpYPosition;
        }
        int xOffset = 64;
        if (isJumping) {
            xOffset = 56;
        }
        game.drawImage(currentFrame, game.width() / 2 - xOffset, adjustedY, 128, 128);

        // Draw obstacles
        game.changeColor(game.red);
        for (Obstacle obstacle : obstacles) {
            game.drawSolidRectangle(obstacle.getX(), obstacle.getY(), obstacle.getWidth(), obstacle.getHeight());
        }

        // Draw score
        if (isPlaying) {
            game.changeColor(game.blue);
            game.drawText(400, game.height() - 120, "Score: " + score, "Arial", 16);
        }
    }

    public static void keyPressed(Game game, KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.VK_SPACE) {
            if (isGameOver || !isPlaying) {
                isGameOver = false;
                isPlaying = true;
                isRunning = true;
                isJumping = false;
                score = 0;
                timeAlive = 0;
                obstacleSpeed = 200;
                obstacleSpawnInterval = 1.2; // Reset the minimum interval
                jumpVelocity = -250; // Reset jump velocity
                jumpYPosition = 0; // Reset jump position
                obstacles.clear();
                runSprite.reset();
            } else if (isRunning) {
                isJumping = true;
                isRunning = false;
                jumpSprite.reset();
                jumpVelocity = -250; // Initial jump velocity
            }
        } else if (event.getKeyCode() == KeyEvent.VK_4 || event.getKeyCode() == KeyEvent.VK_5 || event.getKeyCode() == KeyEvent.VK_6) {
            resetMiniGame();
            game.handleMenuKeyPress(event);
        }
    }

    private static void resetMiniGame() {
        isGameOver = false;
        isPlaying = false;
        isRunning = false;
        score = 0;
        timeAlive = 0;
        obstacleSpeed = 200;
        obstacleSpawnInterval = 1.2;
        jumpVelocity = -250;
        jumpYPosition = 0;
        obstacles.clear();
        idleSprite.reset();
    }
}
