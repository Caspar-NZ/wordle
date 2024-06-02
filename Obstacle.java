import java.awt.Image;

public class Obstacle {
    private int x, y;
    private int width, height;
    private double speed;

    public Obstacle(int startX, int startY, int width, int height, double speed) {
        this.x = startX;
        this.y = startY;
        this.width = width;
        this.height = height;
        this.speed = speed;
    }

    public void update(double dt) {
        x -= speed * dt;
    }

    public void increaseSpeed(double increment) {
        speed += increment;
    }

    public boolean isOffScreen() {
        return x + width < 0;
    }

    public boolean collidesWith(int playerX, int playerY, int playerWidth, int playerHeight) {
        return playerX < x + width && playerX + playerWidth > x && playerY < y + height && playerY + playerHeight > y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
