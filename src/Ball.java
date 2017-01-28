public class Ball {
    public static float size = 20; // Radius of the ball
    public static float speed = 2;
    public static int maxDx = 5; //Max horizontal movement the ball can get

    private float x;
    private float y;
    private float dx = 0;
    private float direction = 1;

    public Ball(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getDx() {
        return dx;
    }

    public void setDx(float dx) {
        this.dx = dx;
    }

    public float getDirection() {
        return direction;
    }

    public void setDirection(float direction) {
        this.direction = direction;
    }
}
