public class Score {
    private int player1Score = 0;
    private int player2Score = 0;

    public static int xDistance = 15;
    public static int yDistance = 15;

    public Score() {

    }

    public int addPoint(int playerId) {
        if (playerId == 1) {
            player1Score++;
            return player1Score;
        } else if (playerId == 2) {
            player2Score++;
            return player2Score;
        }
        return 0;
    }

    public int getPlayer1Score() {
        return player1Score;
    }

    public int getPlayer2Score() {
        return player2Score;
    }
}
