package ru.itis.semester_work2.ui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;

public class GameOverController {
    @FXML private Label winnerLabel;
    @FXML private Label resultLabel;

    private int winnerId;
    private String winnerName;
    private int myPlayerId;

    public void init(int winnerId, String winnerName, int myPlayerId) {
        this.winnerId = winnerId;
        this.winnerName = winnerName;
        this.myPlayerId = myPlayerId;

        winnerLabel.setText("Победитель: " + winnerName);

        if (winnerId == myPlayerId) {
            resultLabel.setText("Поздравляем! Вы победили!");
            resultLabel.setStyle("-fx-text-fill: #2ecc71; -fx-font-size: 16px;");
        } else {
            resultLabel.setText("Вы проиграли. Повезёт в следующий раз!");
            resultLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 16px;");
        }
    }

    @FXML
    private void onBackToMenu() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ru/itis/semester_work2/fxml/menu.fxml"));
            Scene scene = new Scene(loader.load(), 800, 600);

            Stage stage = (Stage) winnerLabel.getScene().getWindow();
            stage.setScene(scene);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

