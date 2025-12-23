package ru.itis.semester_work2.network;

import ru.itis.semester_work2.protocol.GameOverMessage;
import ru.itis.semester_work2.protocol.GameStartMessage;
import ru.itis.semester_work2.protocol.GameStateMessage;

public interface ClientEventListener {
    void onConnected();
    void onDisconnected();
    void onGameStart(GameStartMessage message);
    void onGameState(GameStateMessage message);
    void onGameOver(GameOverMessage message);
}

