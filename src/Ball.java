public class Ball {
    //Radius of the ball
    public static float size = 20;
    //Speed (vertical pixels per tick) of the ball
    public static float speed = 2;
    //Max horizontal movement the ball can get
    public static int maxDx = 5;

    private float x;
    private float y;
    private float dx = 0;

    //Direction of the ball (-1 or 1)
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
