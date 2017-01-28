public class Paddle {
    public static int width = 80;
    public static int height = 20;

    //Distance between wall and paddle center
    public static int wallDistance = 15;

    //Speed (pixels per tick) of the paddle
    public static float speed = 10;

    //X coordinate of paddle
    private float x;

    //Direction of paddle (-1, 0 or 1)
    private float direction = 0;

    public Paddle(int x) {
        this.x = x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public void setDirection(float direction) {
        this.direction = direction;
    }

    public int getPaddleWidth() {
        return width;
    }

    public int getPaddleHeight() {
        return height;
    }

    public int getWallDistance() {
        return wallDistance;
    }

    public float getX() {
        return x;
    }

    public float getPaddleSpeed() {
        return speed;
    }

    public float getDirection() {
        return direction;
    }
}
