import java.util.Random;

public class PongLogic {

    /**
     * Processes a game tick, which includes:
     * Moving the ball
     * Moving the paddles
     * Testing for collisions and bouncing the ball if a collision is found
     * Resetting the ball when it goes off screen (when a point is scored)
     * This method returns a status code, where 0 = nothing, 1 = ball bounced on paddle, 2 = ball went off screen
     *
     * @param ball the game ball
     * @param paddle1 the top paddle
     * @param paddle2 the bottom paddle
     * @param server indicates if method is being called from the server
     * @param paddleToMove id of the paddle that needs to be moved
     * @return int status
     */
    public static int processTick(Ball ball, Paddle paddle1, Paddle paddle2, boolean server, int paddleToMove) {
        //Whenever the ball is hit by a paddle or goes off screen, the game state is sent to the client from the server
        //This is done to prevent the clients from becoming desynchronized
        int updateFromServer = 0;

        //Calculate new location ball
        ball.setX(ball.getX() + ball.getDx());
        ball.setY(ball.getY() + ball.getDirection() * Ball.speed);

        //Calculate new location for paddles
        if (paddleToMove == 1) {
            float newPaddleLoc = paddle1.getX() + paddle1.getDirection() * paddle1.getPaddleSpeed();
            if (isPaddleOnScreen(newPaddleLoc)) {
                paddle1.setX(newPaddleLoc);
            }
        } else if (paddleToMove == 2) {
            float newPaddleLoc = paddle2.getX() + paddle2.getDirection() * paddle2.getPaddleSpeed();
            if (isPaddleOnScreen(newPaddleLoc)) {
                paddle2.setX(newPaddleLoc);
            }
        }

        //To check for collisions, first test if the ball is at the right y for a possible collision
        boolean atCorrectY = false;

        float paddle_x;
        //Select the paddle that we need to test intersections for based on the direction of the ball
        if (ball.getDirection() == 1) {
            //Set the x coordinate of the paddle that we're processing to the second paddles x
            paddle_x = paddle2.getX();
            //Calculate if the ball is within the y height of the second paddle
            if (ball.getY() > Pong.height - Paddle.wallDistance - (Paddle.height / 2) - Ball.size &&
                    ball.getY() < Pong.height - Paddle.wallDistance + (Paddle.height / 2) - Ball.size) {
                atCorrectY = true;
            }
        } else {
            //Same as above code, but now for the first paddle
            paddle_x = paddle1.getX();
            if (ball.getY() > Paddle.wallDistance - (Paddle.height / 2) + Ball.size &&
                    ball.getY() < Paddle.wallDistance + (Paddle.height / 2) + Ball.size) {
                atCorrectY = true;
            }
        }

        //If the ball is at the correct y coordinate, check if it intersects at the x coordinate
        if (atCorrectY) {
            float distance_from_center = ball.getX() - paddle_x;
            if (distance_from_center < (Paddle.width / 2) + Ball.size && distance_from_center > (-Paddle.width / 2) - Ball.size) {
                ball.setDirection(ball.getDirection() * -1);

                //Normalize the intersection distance so it is not dependent on the width of the paddle
                //The normalized distance runs from -1 to 1
                float normalized_intersection = distance_from_center / Paddle.width;
                //Multiply this normalized distance with the max angle the ball can have
                float bounce_angle = normalized_intersection * Ball.maxDx;
                ball.setDx(bounce_angle);

                //Indicate that a game update needs to be sent to properly synchronize the ball angles
                updateFromServer = 1;
            }
        }


        //If the ball went off screen, reset the ball
        if (ball.getY() < -Ball.size || ball.getY() > Pong.height + Ball.size) {
            if (server) {
                //If processing tick on the server, set the ball to the center of the screen
                ball.setX(Pong.width / 2);
                ball.setY(Pong.height / 2);
                //And randomize a start angle for the ball
                ball.setDx(new Random().nextInt(2 * Ball.maxDx) - Ball.maxDx);
            }
            //Indicate that a game update needs to be sent to synchronize the new ball angle
            updateFromServer = 2;
        }

        //If the ball hits a wall, bounce it back
        if (ball.getX() > Pong.width - Ball.size || ball.getX() < Ball.size) {
            ball.setDx(ball.getDx() * -1);
        }
        return updateFromServer;
    }

    /**
     * Returns whether or not the paddle would be on screen with the given x coordinate
     *
     * @param paddleX x coordinate of paddle
     * @return true if paddle would be visible on screen
     */
    private static boolean isPaddleOnScreen(float paddleX) {
        return paddleX > Paddle.width / 2 && paddleX < Pong.width - (Paddle.width / 2);
    }

    /**
     * Reads the first valid packet that is contained in the buffer
     * Sets this packet as the latest command in the buffer
     * Removes the packet from the buffer
     *
     * @param buffer the network buffer
     */
    public static void decodePacket(Buffer buffer) {
        String message = buffer.getBuffer();
        if (message != null) {
            //Get start index of the first packet
            int startIndex = message.indexOf("[");
            if (startIndex != -1) {
                //If start sign is found, get the first packet end sign that is AFTER the start index
                int endIndex = message.indexOf("]", startIndex);
                if (endIndex != -1) {
                    //If an end sign is found, we have a complete packet
                    String command = message.substring(startIndex + 1, endIndex);
                    //Split the packet on the ',' sign to get the separate commands
                    buffer.setLatestCommand(command.split(","));
                    //Update the buffer to remove the processed packet
                    buffer.setBuffer(message.substring(endIndex, message.length()));
                }
            }
        }
    }

    /**
     * Returns whether or not the buffer still contains a complete packet
     *
     * @param buffer the network buffer
     * @return true if the buffer contains a packe
     */
    public static boolean containsPacket(Buffer buffer) {
        String message = buffer.getBuffer();
        //Get start index of the first packet
        int startIndex = message.indexOf("[");
        if (startIndex != -1) {
            //If start sign is found, get the first packet end sign that is AFTER the start index
            int endIndex = message.indexOf("]", startIndex);
            if (endIndex != -1) {
                //If an end sign is found, we have a complete packet
                return true;
            }
        }
        return false;
    }
}
