import processing.core.PApplet;
import processing.net.Client;

public class Pong extends PApplet {
    public static int width = 500;
    public static int height = 400;

    private Client c;
    private Buffer buffer = new Buffer();
    private int tick = 0;
    private float playerDir = 0;

    private boolean gameStarted = false;
    private int playerId;

    private Paddle paddle1 = new Paddle(width / 2);
    private Paddle paddle2 = new Paddle(width / 2);
    private Ball ball = new Ball(width / 2, height / 2);

    private Paddle playerPaddle;
    private Paddle opponentPaddle;

    public static void main(String[] args) {
        PApplet.main(Pong.class);
    }

    public void settings() {
        size(width, height);
    }

    public void setup() {
        rectMode(CENTER);
        ellipseMode(CENTER);
        c = new Client(this, "127.0.0.1", 12345); // Connect to local server
        c.write("Hey server, how you doin'? ;)");
    }

    public void draw() {
        background(0);

        while (c.available() > 0) {
            String message = c.readString();
            buffer.appendBuffer(message);

            while (PongLogic.containsPacket(buffer)) {
                PongLogic.decodePacket(buffer);
                String[] commands = buffer.getLatestCommand();
                if (commands != null) {
                    if (!gameStarted) {
                        playerId = Integer.parseInt(commands[0]);
                        gameStarted = true;
                        println("Received game start message, player id: " + playerId);
                        if (playerId == 1) {
                            playerPaddle = paddle1;
                            opponentPaddle = paddle2;
                        } else {
                            playerPaddle = paddle2;
                            opponentPaddle = paddle1;
                        }
                    } else {
                        //If just a simple packet with info from other paddle, process that
                        if (commands.length == 2) {
                            try {
                                int tick = Integer.parseInt(commands[0]);
                            } catch (NumberFormatException e) {
                                System.out.println("WARNING: Could not read message: " + message);
                            }
                            float paddleX = Float.parseFloat(commands[1]);
                            opponentPaddle.setPaddleX(paddleX);
                        } else {
                            //Otherwise it's a packet containing the game state, process that then
                            if (playerId == 1) {
                                paddle2.setPaddleX(Float.parseFloat(commands[1]));
                            } else {
                                paddle1.setPaddleX(Float.parseFloat(commands[0]));
                            }
                            ball.setX(Float.parseFloat(commands[2]));
                            ball.setY(Float.parseFloat(commands[3]));
                            ball.setDirection(Float.parseFloat(commands[4]));
                            ball.setDx(Float.parseFloat(commands[5]));
                            System.out.println("Update from server applied to game state");
                        }
                    }
                }
            }
        }

        if (gameStarted) {
            playerPaddle.setPaddleDir(playerDir);
            int status = PongLogic.processTick(ball, paddle1, paddle2, false, playerId);

            c.write("[" + tick + "," + +playerPaddle.getPaddleX() + "]");
        }

        //Draw ball
        fill(255);
        ellipse(ball.getX(), ball.getY(), Ball.size, Ball.size);

        //Draw paddles
        fill(153);
        rect(paddle1.getPaddleX(), paddle1.getWallDistance(), paddle1.getPaddleWidth(), paddle1.getPaddleHeight());
        rect(paddle2.getPaddleX(), height - paddle2.getWallDistance(), paddle2.getPaddleWidth(), paddle2.getPaddleHeight());
    }

    public void keyPressed() {
        if (key == 'a') {
            playerDir = -1;
        } else if (key == 'd') {
            playerDir = 1;
        }
    }

    public void keyReleased() {
        if (key == 'a' || key == 'd') {
            playerDir = 0;
        }
    }
}

//TODO Make sound on every bounce & point scored
//TODO Javadoc
//TODO Keep score
