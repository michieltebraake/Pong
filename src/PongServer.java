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
    private Score score = new Score();

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
                            paddle1.setX(paddleX);
                        } else if (client == client2) {
                            client1.write(message);
                            paddle2.setX(paddleX);
                        } else {
                            println("ERROR: GOT MESSAGE FROM UNKNOWN CLIENT");
                        }
                    }
                }
            }
        }

        //If the game has started, process a game tick
        if (gameStarted) {
            int updateFromServer = PongLogic.processTick(ball, paddle1, paddle2, score, true, 0);
            tick++;
            //If the ball bounced on a paddle or left the screen, send a game update to the clients
            if (updateFromServer != 0) {
                s.write("[" + paddle1.getX() + "," + paddle2.getX() + "," + ball.getX() + "," + ball.getY() + "," + ball.getDirection() + "," + ball.getDx() + "," + tick + "]");
            }

            if (updateFromServer == 2) {
                s.write("[5,1]");
            } else if (updateFromServer == 3) {
                s.write("[5,2]");
            }
        }

        PongLogic.drawScreen(this, ball, paddle1, paddle2, score);
    }
}
