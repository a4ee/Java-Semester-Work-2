package ru.itis.semester_work2.ui;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import ru.itis.semester_work2.model.*;
import ru.itis.semester_work2.network.ClientEventListener;
import ru.itis.semester_work2.network.GameClient;
import ru.itis.semester_work2.protocol.*;

import java.io.IOException;
import java.util.*;

import static ru.itis.semester_work2.game.GameConstants.*;

public class GameController implements ClientEventListener {
    @FXML
    private Canvas gameCanvas;
    @FXML
    private StackPane gamePane;
    @FXML
    private Label player1Label;
    @FXML
    private Label player2Label;
    @FXML
    private Pane gameContainer;

    private Map<Integer, Sprite> shipSprites = new HashMap<>();
    private GameClient client;
    private String playerName;
    private int myPlayerId;
    private GameState currentState;
    private volatile boolean running = true;

    private Image imageShip1;
    private Image imageShip2;


    private final Set<KeyCode> pressedKeys = new HashSet<>();

    public void init(GameClient client, String playerName, int playerId) {
        this.client = client;
        this.playerName = playerName;
        this.myPlayerId = playerId;
        client.setListener(this);

        loadImages();
        setupInput();
        startGameLoop();
    }

    private void loadImages() {
        imageShip1 = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/ru/itis/semester_work2/sprites/ship1.png")));
        imageShip2 = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/ru/itis/semester_work2/sprites/ship2.png")));
    }

    private void setupInput() {
        Platform.runLater(() -> {
            Scene scene = gameCanvas.getScene();
            if (scene != null) {
                scene.setOnKeyPressed(e -> {
                    pressedKeys.add(e.getCode());
                    sendInput();
                });

                scene.setOnKeyReleased(e -> {
                    pressedKeys.remove(e.getCode());
                    sendInput();
                });
            }
        });
    }

    private void sendInput() {
        if (!running) return;

        PlayerInputMessage input = new PlayerInputMessage();
        input.setForward(pressedKeys.contains(KeyCode.W));
        input.setBackward(pressedKeys.contains(KeyCode.S));
        input.setLeft(pressedKeys.contains(KeyCode.A));
        input.setRight(pressedKeys.contains(KeyCode.D));
        input.setShoot(pressedKeys.contains(KeyCode.SPACE));
        input.setMine(pressedKeys.contains(KeyCode.E));

        client.send(input);
    }

    private void startGameLoop() {
        System.out.println("Запуск игрового потока...");
        Thread networkThread = new Thread(() -> {
            System.out.println("Игровой поток запущен!");
            while (running) {
                try {
                    client.update();
                    Thread.sleep(16);
                } catch (IOException | InterruptedException e) {
                    System.out.println("Ошибка в игровом потоке: " + e.getMessage());
                    break;
                }
            }
            System.out.println("Игровой поток завершён");
        });
        networkThread.setDaemon(true);
        networkThread.start();
    }

    private void render() {
        GraphicsContext gc = gameCanvas.getGraphicsContext2D();

        gc.setFill(Color.rgb(20, 60, 100));
        gc.fillRect(0, 0, MAP_WIDTH, MAP_HEIGHT);

        if (currentState == null) return;

        gc.setFill(Color.rgb(60, 40, 20));
        for (Island island : currentState.getIslands()) {
            gc.fillRect(island.getX(), island.getY(), island.getWidth(), island.getHeight());
        }

        gc.setFill(Color.rgb(50, 50, 50));
        for (Mine mine : currentState.getMines()) {
            gc.fillOval(mine.getX() - MINE_SIZE / 2.0, mine.getY() - MINE_SIZE / 2.0, MINE_SIZE, MINE_SIZE);
            gc.setFill(Color.RED);
            gc.fillOval(mine.getX() - 3, mine.getY() - 3, 6, 6);
            gc.setFill(Color.rgb(50, 50, 50));
        }

        gc.setFill(Color.YELLOW);
        for (Bullet bullet : currentState.getBullets()) {
            gc.fillOval(bullet.getX() - BULLET_SIZE / 2.0, bullet.getY() - BULLET_SIZE / 2.0, BULLET_SIZE, BULLET_SIZE);
        }
        gameContainer.getChildren().clear();
        shipSprites.clear();

        Ship ship1 = currentState.getShip1();
        Ship ship2 = currentState.getShip2();

        drawShip(ship1, imageShip1, ship1.getPlayerId());
        drawShip(ship2, imageShip2, ship2.getPlayerId());


        updateHpLabels();
    }


    private void drawShip(Ship ship, Image imageShip, int shipId) {
        ImageView shipView = new ImageView(imageShip);
        shipView.setFitWidth(SHIP_SIZE);
        shipView.setFitHeight(SHIP_SIZE);

        double centerX = ship.getX() - SHIP_SIZE / 2.0;
        double centerY = ship.getY() - SHIP_SIZE / 2.0;

        shipView.setLayoutX(centerX);
        shipView.setLayoutY(centerY);
        shipView.setRotate(ship.getAngle());

        shipView.setTranslateY(SHIP_SIZE / 2.0);
        shipView.setTranslateY(SHIP_SIZE / 2.0);


        gameContainer.getChildren().add(shipView);

        shipSprites.put(shipId, new Sprite(shipView, shipId));

        drawHpBar(ship);

    }
    private void drawHpBar(Ship ship) {
        GraphicsContext gc = gameCanvas.getGraphicsContext2D();

        double hpBarWidth = 40;
        double hpBarHeight = 5;
        double hpPercent = (double) ship.getHp() / SHIP_HP;

        gc.setFill(Color.DARKGRAY);
        gc.fillRect(ship.getX() - hpBarWidth / 2, ship.getY() - SHIP_SIZE / 2.0 - 15, hpBarWidth, hpBarHeight);

        gc.setFill(hpPercent > 0.3 ? Color.LIMEGREEN : Color.RED);
        gc.fillRect(ship.getX() - hpBarWidth / 2, ship.getY() - SHIP_SIZE / 2.0 - 15, hpBarWidth * hpPercent, hpBarHeight);
    }

    private void updateHpLabels() {
        if (currentState == null) return;

        Ship ship1 = currentState.getShip1();
        Ship ship2 = currentState.getShip2();

        Platform.runLater(() -> {
            if (ship1 != null) player1Label.setText("Игрок 1: HP " + ship1.getHp());
            if (ship2 != null) player2Label.setText("Игрок 2: HP " + ship2.getHp());
        });
    }

    @Override
    public void onConnected() {
    }

    @Override
    public void onDisconnected() {
        running = false;
    }

    @Override
    public void onGameStart(GameStartMessage message) {
    }

    @Override
    public void onGameState(GameStateMessage message) {
        this.currentState = message.getGameState();
        Platform.runLater(this::render);
    }

    @Override
    public void onGameOver(GameOverMessage message) {
        running = false;
        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/ru/itis/semester_work2/fxml/gameover.fxml"));
                Scene scene = new Scene(loader.load(), 800, 600);

                GameOverController controller = loader.getController();
                controller.init(message.getWinnerId(), message.getWinnerName(), myPlayerId);

                Stage stage = (Stage) gameCanvas.getScene().getWindow();
                stage.setScene(scene);

            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }


    private class Sprite{
        private ImageView imageView;
        private int id;

        public Sprite(ImageView imageView, int id) {
            this.imageView = imageView;
            this.id = id;
        }

        public void updatePosition(double x, double y, double angle) {
            imageView.setLayoutX(x);
            imageView.setLayoutY(y);
            imageView.setRotate(angle);
        }

        public void remove() {
            gameContainer.getChildren().remove(imageView);
        }
    }


}
