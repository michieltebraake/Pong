import processing.core.PApplet;
import processing.net.Client;

public class Pong extends PApplet {
    public static int width = 500;
    public static int height = 400;

    private Client c;
    private Buffer buffer = new Buffer();
    private int tick = 0;

    private boolean gameStarted = false;
    //Id of player (1 or 2), set when game starts
    private int playerId;

    private Paddle paddle1 = new Paddle(width / 2);
    private Paddle paddle2 = new Paddle(width / 2);
    private Ball ball = new Ball(width / 2, height / 2);

    //References to the players and opponents paddle for easy access
    private Paddle playerPaddle;
    private Paddle opponentPaddle;

    private Score score = new Score();

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

            //As long as the buffer contains packets, keep processing them
            while (PongLogic.containsPacket(buffer)) {
                PongLogic.decodePacket(buffer);
                String[] commands = buffer.getLatestCommand();
                if (commands != null) {
                    if (!gameStarted) {
                        //If the game has not yet started, start it now
                        playerId = Integer.parseInt(commands[0]);
                        gameStarted = true;
                        println("Received game start message, player id: " + playerId);
                        //Set the player and opponent paddle references for easy access
                        if (playerId == 1) {
                            playerPaddle = paddle1;
                            opponentPaddle = paddle2;
                        } else {
                            playerPaddle = paddle2;
                            opponentPaddle = paddle1;
                        }
                    } else {
                        if (commands.length == 2) {
                            //If just a simple packet with info from other paddle, process that
                            try {
                                int tick = Integer.parseInt(commands[0]);
                            } catch (NumberFormatException e) {
                                System.out.println("WARNING: Could not read message: " + message);
                            }
                            float paddleX = Float.parseFloat(commands[1]);
                            opponentPaddle.setX(paddleX);
                        } else {
                            //Otherwise it's a packet containing the full game state, process that then
                            if (playerId == 1) {
                                paddle2.setX(Float.parseFloat(commands[1]));
                            } else {
                                paddle1.setX(Float.parseFloat(commands[0]));
                            }
                            ball.setX(Float.parseFloat(commands[2]));
                            ball.setY(Float.parseFloat(commands[3]));
                            ball.setDirection(Float.parseFloat(commands[4]));
                            ball.setDx(Float.parseFloat(commands[5]));
                        }
                    }
                }
            }
        }

        //If the game has started, process a game tick
        if (gameStarted) {
            int status = PongLogic.processTick(ball, paddle1, paddle2, score, false, playerId);

            if (status == 1) {
                //Make a sound that indicates bouncing on a paddle
                makeSound(1);
            } else if (status == 2) {
                //Make a sound that indicates that the ball went off the screen
                makeSound(2);
            }

            c.write("[" + tick + "," + +playerPaddle.getX() + "]");
        }

        PongLogic.drawScreen(this, ball, paddle1, paddle2, score);
    }

    public void keyPressed() {
        //Set the paddle direction based on the key that was pressed
        if (key == 'a') {
            playerPaddle.setDirection(-1);
        } else if (key == 'd') {
            playerPaddle.setDirection(1);
        }
    }

    public void keyReleased() {
        //Stop paddle when key is released
        if (key == 'a' || key == 'd') {
            playerPaddle.setDirection(0);
        }
    }

    /**
     * Sends a signal to the Arduino which will then make a sound with it's speaker
     *
     * @param pitch Pitch of the sound
     */
    private void makeSound(int pitch) {
        //TODO Implement this
    }
}

//TODO Make sound on every bounce & point scored
//TODO Keep score
