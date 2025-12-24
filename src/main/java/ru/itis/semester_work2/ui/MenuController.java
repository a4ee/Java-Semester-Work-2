package ru.itis.semester_work2.ui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import ru.itis.semester_work2.network.GameClient;

import java.io.IOException;

public class MenuController {
    @FXML private TextField playerNameField;
    @FXML private TextField hostField;
    @FXML private TextField portField;
    @FXML private Label statusLabel;

    @FXML
    private void onConnect() {
        String playerName = playerNameField.getText().trim();
        String host = hostField.getText().trim();
        String portText = portField.getText().trim();

        if (playerName.isEmpty()) {
            statusLabel.setText("Введите имя игрока!");
            return;
        }

        int port;
        try {
            port = Integer.parseInt(portText);
        } catch (NumberFormatException e) {
            statusLabel.setText("Неверный порт!");
            return;
        }

        try {
            GameClient client = new GameClient();
            client.connect(host, port);

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ru/itis/semester_work2/fxml/lobby.fxml"));
            Scene scene = new Scene(loader.load(), 800, 600);

            LobbyController lobbyController = loader.getController();
            lobbyController.init(client, playerName);

            Stage stage = (Stage) playerNameField.getScene().getWindow();
            stage.setScene(scene);

        } catch (IOException e) {
            statusLabel.setText("Ошибка подключения: " + e.getMessage());
        }
    }
}

