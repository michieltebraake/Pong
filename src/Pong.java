import processing.core.PApplet;
import processing.net.Client;
import processing.serial.Serial;

/**
 * Main Pong class
 * Handles the connection to the server, serial input from the Arduino and the repeating draw function
 * Calls methods in PongLogic that process the game's logic
 *
 * Created by Michiel te Braake and Rick Fontein
 */
public class Pong extends PApplet {
    public static int width = 500;
    public static int height = 400;

    private static int NEWLINE_N = 10;
    private static int NEWLINE_R = 13;
    private String serialBuffer;

    private Client c;
    private Serial port;
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
        try {
            port = new Serial(this, Serial.list()[0], 9600);
        } catch (RuntimeException e) {
            System.out.println("Serial port was busy, only keyboard input available");
        }
        rectMode(CENTER);
        ellipseMode(CENTER);
        c = new Client(this, "127.0.0.1", 12345); // Connect to local server
        c.write("Hey server, how you doin'? ;)");
    }

    public void draw() {
        background(0);

        if (port != null) {
            //Read serial input
            while (port.available() > 0) {
                serialEvent(port.read()); // read data
            }
        }

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
                            switch (Integer.parseInt(commands[0])) {
                                case 5:
                                    int scoreId = Integer.parseInt(commands[1]);
                                    if (scoreId == playerId) {
                                        makeSound(1);
                                    } else {
                                        makeSound(2);
                                    }
                                    score.addPoint(scoreId);
                                    break;
                                default:
                                    //If just a simple packet with info from other paddle, process that
                                    try {
                                        int tick = Integer.parseInt(commands[0]);
                                    } catch (NumberFormatException e) {
                                        System.out.println("WARNING: Could not read message: " + message);
                                    }
                                    float paddleX = Float.parseFloat(commands[1]);
                                    opponentPaddle.setX(paddleX);
                                    break;
                            }
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
                makeSound(0);
            }

            c.write("[" + tick + "," + +playerPaddle.getX() + "]");
        }

        PongLogic.drawScreen(this, ball, paddle1, paddle2, score);
    }

    private void serialEvent(int serial) {
        try { // try-catch because of transmission errors
            if (serial != NEWLINE_R && serial != NEWLINE_N) {
                serialBuffer += (char) serial;
            } else {
                switch (serialBuffer) {
                    case "L":
                        System.out.println("Pressed left");
                        playerPaddle.setDirection(-1);
                        break;
                    case "R":
                        playerPaddle.setDirection(1);
                        break;
                    case "N":
                        playerPaddle.setDirection(0);
                        break;
                }
                serialBuffer = ""; // Clear the value of "serialBuffer"
            }
        } catch (Exception e) {
            println("no valid data");
        }
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
     * @param type Type of the sound (0 = bounce, 1 = score point, 2 = opponent scores point
     */
    private void makeSound(int type) {
        if (port != null) {
            port.write(type);
        }
    }
}