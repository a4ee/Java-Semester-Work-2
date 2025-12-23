package ru.itis.semester_work2.protocol;

public class ConnectMessage extends Message {
    private String playerName;

    public ConnectMessage() {
        super(MessageType.CONNECT);
    }

    public ConnectMessage(String playerName) {
        super(MessageType.CONNECT);
        this.playerName = playerName;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }
}

