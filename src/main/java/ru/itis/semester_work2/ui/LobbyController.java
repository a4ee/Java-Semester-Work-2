package ru.itis.semester_work2.ui;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import ru.itis.semester_work2.network.ClientEventListener;
import ru.itis.semester_work2.network.GameClient;
import ru.itis.semester_work2.protocol.ConnectMessage;
import ru.itis.semester_work2.protocol.GameOverMessage;
import ru.itis.semester_work2.protocol.GameStartMessage;
import ru.itis.semester_work2.protocol.GameStateMessage;

import java.io.IOException;

public class LobbyController implements ClientEventListener {
    @FXML private Label statusLabel;

    private GameClient client;
    private String playerName;
    private volatile boolean running = true;

    public void init(GameClient client, String playerName) {
        this.client = client;
        this.playerName = playerName;
        client.setListener(this);

        Thread networkThread = new Thread(this::networkLoop);
        networkThread.setDaemon(true);
        networkThread.start();
    }

    private void networkLoop() {
        while (running) {
            try {
                client.update();
                Thread.sleep(16);
            } catch (IOException | InterruptedException e) {
                break;
            }
        }
    }

    @Override
    public void onConnected() {
        client.send(new ConnectMessage(playerName));
        Platform.runLater(() -> statusLabel.setText("Подключено! Ожидание второго игрока..."));
    }

    @Override
    public void onDisconnected() {
        running = false;
        Platform.runLater(() -> statusLabel.setText("Отключено от сервера"));
    }

    @Override
    public void onGameStart(GameStartMessage message) {
        running = false;

        try {
            Thread.sleep(50);
        } catch (InterruptedException ignored) {}

        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/ru/itis/semester_work2/fxml/game.fxml"));
                Scene scene = new Scene(loader.load(), 800, 700);

                GameController gameController = loader.getController();
                gameController.init(client, playerName, message.getPlayerId());

                Stage stage = (Stage) statusLabel.getScene().getWindow();
                stage.setScene(scene);

            } catch (IOException e) {
                statusLabel.setText("Ошибка загрузки игры: " + e.getMessage());
            }
        });
    }

    @Override
    public void onGameState(GameStateMessage message) {}

    @Override
    public void onGameOver(GameOverMessage message) {}
}

