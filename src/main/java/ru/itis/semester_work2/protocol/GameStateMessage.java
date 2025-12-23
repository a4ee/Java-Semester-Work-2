package ru.itis.semester_work2.protocol;

import ru.itis.semester_work2.model.GameState;

public class GameStateMessage extends Message {
    private GameState gameState;

    public GameStateMessage() {
        super(MessageType.GAME_STATE);
    }

    public GameStateMessage(GameState gameState) {
        super(MessageType.GAME_STATE);
        this.gameState = gameState;
    }

    public GameState getGameState() {
        return gameState;
    }

    public void setGameState(GameState gameState) {
        this.gameState = gameState;
    }
}

