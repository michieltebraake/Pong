import processing.core.PApplet;
import processing.net.Client;
import processing.net.Server;

public class PongServer extends PApplet {
    private int width = 500;
    private int height = 400;

    private Server s;
    private Client client1;
    private Client client2;

    private Buffer buffer = new Buffer();

    private int tick = 0;

    private boolean gameStarted = false;

    private Paddle paddle1 = new Paddle(width / 2);
    private Paddle paddle2 = new Paddle(width / 2);
    private Ball ball = new Ball(width / 2, height / 2);

    public static void main(String[] args) {
        PApplet.main(PongServer.class);
    }

    public void settings() {
        size(width, height);
    }

    public void setup() {
        rectMode(CENTER);
        ellipseMode(CENTER);

        s = new Server(this, 12345); // Start a simple server on a port
    }

    public void draw() {
        background(0);

        Client client = s.available();
        if (client != null) {
            String message = client.readString();
            if (message != null) {
                System.out.println(message);
                //If game has not yet started, accept client connection
                if (!gameStarted) {
                    if (client1 == null) {
                        println("Client 1 connected");
                        client1 = client;
                    } else if (client != client1 && client2 == null) {
                        println("Client 2 connected, starting game");
                        client2 = client;
                        //Both clients are connected, start match!
                        client1.write("[1]");
                        client2.write("[2]");
                        gameStarted = true;
                    }
                } else {
                    //If game has started, relay the message to the other client
                    //Override buffer, since server doesn't care about missing messages
                    buffer.setBuffer(message);
                    PongLogic.decodePacket(buffer);
                    String[] commands = buffer.getLatestCommand();
                    if (commands != null) {
                        float paddleX = Float.parseFloat(commands[1]);
                        if (client == client1) {
                            client2.write(message);
                            paddle1.setPaddleX(paddleX);
                        } else if (client == client2) {
                            client1.write(message);
                            paddle2.setPaddleX(paddleX);
                        } else {
                            println("ERROR: GOT MESSAGE FROM UNKNOWN CLIENT");
                        }
                    }
                }
            }
        }

        if (gameStarted) {
            int updateFromServer = PongLogic.processTick(ball, paddle1, paddle2, true, 0);
            tick++;
            if (updateFromServer != 0) {
                s.write("[" + paddle1.getPaddleX() + "," + paddle2.getPaddleX() + "," + ball.getX() + "," + ball.getY() + "," + ball.getDirection() + "," + ball.getDx() + "," + tick + "]");
            }
        }

        //Draw ball
        fill(255);
        ellipse(ball.getX(), ball.getY(), Ball.size, Ball.size);

        //Draw paddles
        fill(153);
        rect(paddle1.getPaddleX(), paddle1.getWallDistance(), paddle1.getPaddleWidth(), paddle1.getPaddleHeight());
        rect(paddle2.getPaddleX(), height - paddle2.getWallDistance(), paddle2.getPaddleWidth(), paddle2.getPaddleHeight());
    }
}
