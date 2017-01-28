public class Paddle {
    public static int paddleWidth = 80;
    public static int paddleHeight = 20;
    public static int wallDistance = 15;
    public static float paddleSpeed = 10;

    private float paddleX;
    private float paddleDir = 0;

    public Paddle(int paddleX) {
        this.paddleX = paddleX;
    }

    public void setPaddleX(float paddleX) {
        this.paddleX = paddleX;
    }

    public void setPaddleDir(float paddleDir) {
        this.paddleDir = paddleDir;
    }

    public int getPaddleWidth() {
        return paddleWidth;
    }

    public int getPaddleHeight() {
        return paddleHeight;
    }

    public int getWallDistance() {
        return wallDistance;
    }

    public float getPaddleX() {
        return paddleX;
    }

    public float getPaddleSpeed() {
        return paddleSpeed;
    }

    public float getPaddleDir() {
        return paddleDir;
    }
}
