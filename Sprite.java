import java.awt.Image;

public class Sprite {
    private Image sheet;
    private Image[] frames;
    private int frameCount;
    private int currentFrame;
    private double frameDuration;
    private double animTime;

    public Sprite(Image sheet, int frameWidth, int frameHeight, int frameCount, double frameDuration) {
        this.sheet = sheet;
        this.frameCount = frameCount;
        this.frameDuration = frameDuration;
        frames = new Image[frameCount];

        for (int i = 0; i < frameCount; i++) {
            int x = (i * frameWidth) % sheet.getWidth(null);
            int y = (i * frameWidth / sheet.getWidth(null)) * frameHeight;
            frames[i] = GameEngine.subImage(sheet, x, y, frameWidth, frameHeight);
        }
    }

    public void update(double dt) {
        animTime += dt;
        currentFrame = (int) ((animTime / frameDuration) % frameCount);
    }

    public void reset() {
        animTime = 0;
        currentFrame = 0;
    }

    public Image getCurrentFrame() {
        return frames[currentFrame];
    }
}
