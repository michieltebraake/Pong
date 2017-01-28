import java.util.Random;

public class PongLogic {

    public static int processTick(Ball ball, Paddle paddle1, Paddle paddle2, boolean server, int paddleToMove) {
        //Whenever the ball is hit by a paddle or goes off screen, the game state is sent to the client from the server
        //This is done to prevent the clients from becoming desynchronized
        int updateFromServer = 0;

        //Calculate new location ball
        ball.setX(ball.getX() + ball.getDx());
        ball.setY(ball.getY() + ball.getDirection() * Ball.speed);

        //Calculate new location for paddles
        if (paddleToMove == 1) {
            float newPaddleLoc = paddle1.getPaddleX() + paddle1.getPaddleDir() * paddle1.getPaddleSpeed();
            if (isPaddleOnScreen(newPaddleLoc)) {
                paddle1.setPaddleX(newPaddleLoc);
            }
        } else if (paddleToMove == 2) {
            float newPaddleLoc = paddle2.getPaddleX() + paddle2.getPaddleDir() * paddle2.getPaddleSpeed();
            if (isPaddleOnScreen(newPaddleLoc)) {
                paddle2.setPaddleX(newPaddleLoc);
            }
        }

        //To check for collisions, first test if the ball is at the right y for a possible collision
        boolean atCorrectY = false;
        float paddle_x; //Select the paddle that we need to test intersections for based on the direction of the ball
        if (ball.getDirection() == 1) {
            paddle_x = paddle2.getPaddleX();
            if (ball.getY() > Pong.height - Paddle.wallDistance - (Paddle.paddleHeight / 2) - Ball.size &&
                    ball.getY() < Pong.height - Paddle.wallDistance + (Paddle.paddleHeight / 2) - Ball.size) {
                atCorrectY = true;
            }
        } else {
            paddle_x = paddle1.getPaddleX();
            if (ball.getY() > Paddle.wallDistance - (Paddle.paddleHeight / 2) + Ball.size &&
                    ball.getY() < Paddle.wallDistance + (Paddle.paddleHeight / 2) + Ball.size) {
                atCorrectY = true;
            }
        }

        if (atCorrectY) {
            float distance_from_center = ball.getX() - paddle_x;
            if (distance_from_center < (Paddle.paddleWidth / 2) + Ball.size && distance_from_center > (-Paddle.paddleWidth / 2) - Ball.size) {
                ball.setDirection(ball.getDirection() * -1);

                float normalized_intersection = distance_from_center / Paddle.paddleWidth;
                float bounce_angle = normalized_intersection * Ball.maxDx;
                ball.setDx(bounce_angle);
                updateFromServer = 1;
            }
        }


        //If the ball went off screen, reset the ball
        if (ball.getY() < -Ball.size || ball.getY() > Pong.height + Ball.size) {
            if (server) {
                ball.setX(Pong.width / 2);
                ball.setY(Pong.height / 2);
                ball.setDx(new Random().nextInt(2 * Ball.maxDx) - Ball.maxDx);
            }
            updateFromServer = 2;
        }

        //If the ball hits a wall, bounce it back
        if (ball.getX() > Pong.width - Ball.size || ball.getX() < Ball.size) {
            ball.setDx(ball.getDx() * -1);
        }
        return updateFromServer;
    }

    private static boolean isPaddleOnScreen(float paddleX) {
        return paddleX > Paddle.paddleWidth / 2 && paddleX < Pong.width - (Paddle.paddleWidth / 2);
    }

    public static void decodePacket(Buffer buffer) {
        String message = buffer.getBuffer();
        if (message != null) {
            int startIndex = message.indexOf("[");
            if (startIndex != -1) {
                int endIndex = message.indexOf("]", startIndex);
                if (endIndex != -1) {
                    String command = message.substring(startIndex + 1, endIndex);
                    buffer.setLatestCommand(command.split(","));
                    buffer.setBuffer(message.substring(endIndex, message.length()));
                }
            }
        }
    }

    public static boolean containsPacket(Buffer buffer) {
        String message = buffer.getBuffer();
        int startIndex = message.indexOf("[");
        if (startIndex != -1) {
            int endIndex = message.indexOf("]", startIndex);
            if (endIndex != -1) {
                return true;
            }
        }
        return false;
    }
}
