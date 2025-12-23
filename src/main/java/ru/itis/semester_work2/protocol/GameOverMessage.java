package ru.itis.semester_work2.protocol;

public class GameOverMessage extends Message {
    private int winnerId;
    private String winnerName;

    public GameOverMessage() {
        super(MessageType.GAME_OVER);
    }

    public GameOverMessage(int winnerId, String winnerName) {
        super(MessageType.GAME_OVER);
        this.winnerId = winnerId;
        this.winnerName = winnerName;
    }

    public int getWinnerId() {
        return winnerId;
    }

    public void setWinnerId(int winnerId) {
        this.winnerId = winnerId;
    }

    public String getWinnerName() {
        return winnerName;
    }

    public void setWinnerName(String winnerName) {
        this.winnerName = winnerName;
    }
}

