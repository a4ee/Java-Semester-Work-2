package ru.itis.semester_work2.protocol;

public class GameStartMessage extends Message {
    private int playerId;
    private int mapWidth;
    private int mapHeight;

    public GameStartMessage() {
        super(MessageType.GAME_START);
    }

    public GameStartMessage(int playerId, int mapWidth, int mapHeight) {
        super(MessageType.GAME_START);
        this.playerId = playerId;
        this.mapWidth = mapWidth;
        this.mapHeight = mapHeight;
    }

    public int getPlayerId() {
        return playerId;
    }

    public void setPlayerId(int playerId) {
        this.playerId = playerId;
    }

    public int getMapWidth() {
        return mapWidth;
    }

    public void setMapWidth(int mapWidth) {
        this.mapWidth = mapWidth;
    }

    public int getMapHeight() {
        return mapHeight;
    }

    public void setMapHeight(int mapHeight) {
        this.mapHeight = mapHeight;
    }
}

