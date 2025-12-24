package ru.itis.semester_work2.game;

import ru.itis.semester_work2.model.*;
import ru.itis.semester_work2.protocol.PlayerInputMessage;

import java.util.*;

import static ru.itis.semester_work2.game.GameConstants.*;

public class GameEngine {
    private final GameState gameState;
    private final Map<Integer, PlayerInputMessage> playerInputs = new HashMap<>();

    public GameEngine() {
        this.gameState = new GameState();
        initGame();
    }

    private void initGame() {
        Ship ship1 = new Ship();
        ship1.setPlayerId(1);
        ship1.setX(100);
        ship1.setY(MAP_HEIGHT / 2.0);
        ship1.setAngle(90);
        ship1.setHp(SHIP_HP);

        Ship ship2 = new Ship();
        ship2.setPlayerId(2);
        ship2.setX(MAP_WIDTH - 100);
        ship2.setY(MAP_HEIGHT / 2.0);
        ship2.setAngle(270);
        ship2.setHp(SHIP_HP);

        gameState.setShip1(ship1);
        gameState.setShip2(ship2);

        generateIslands();
    }

    private void generateIslands() {
        Random rand = new Random();
        List<Island> islands = new ArrayList<>();

        for (int i = 0; i < ISLAND_COUNT; i++) {
            int width = 60 + rand.nextInt(40);
            int height = 60 + rand.nextInt(40);

            double x = MAP_WIDTH / 4.0 + rand.nextInt(MAP_WIDTH / 2);
            double y = MAP_HEIGHT / 4.0 + rand.nextInt(MAP_HEIGHT / 2);

            islands.add(new Island(x, y, width, height));
        }

        gameState.setIslands(islands);
    }

    public void setPlayerInput(int playerId, PlayerInputMessage input) {
        playerInputs.put(playerId, input);
    }

    public void update(double deltaTime) {
        if (gameState.isGameOver()) return;

        PlayerInputMessage input1 = playerInputs.get(1);
        PlayerInputMessage input2 = playerInputs.get(2);

        updateShip(gameState.getShip1(), input1, deltaTime);
        updateShip(gameState.getShip2(), input2, deltaTime);

        updateBullets(deltaTime);

        checkCollisions();

        checkGameOver();
    }

    private void updateShip(Ship ship, PlayerInputMessage input, double deltaTime) {
        if (ship == null || input == null) return;

        if (input.isLeft()) {
            ship.setAngle(ship.getAngle() - SHIP_ROTATION_SPEED * deltaTime);
        }
        if (input.isRight()) {
            ship.setAngle(ship.getAngle() + SHIP_ROTATION_SPEED * deltaTime);
        }

        double angle = ship.getAngle() % 360;
        if (angle < 0) angle += 360;
        ship.setAngle(angle);

        double radians = Math.toRadians(ship.getAngle());
        double dx = 0, dy = 0;

        if (input.isForward()) {
            dx += Math.cos(radians) * SHIP_SPEED * deltaTime;
            dy += Math.sin(radians) * SHIP_SPEED * deltaTime;
        }
        if (input.isBackward()) {
            dx -= Math.cos(radians) * SHIP_SPEED * deltaTime * 0.5;
            dy -= Math.sin(radians) * SHIP_SPEED * deltaTime * 0.5;
        }


        double newX = ship.getX() + dx;
        double newY = ship.getY() + dy;

        //границы карты
        newX = Math.max(SHIP_SIZE / 2.0, Math.min(MAP_WIDTH - SHIP_SIZE / 2.0, newX));
        newY = Math.max(SHIP_SIZE / 2.0, Math.min(MAP_HEIGHT - SHIP_SIZE / 2.0, newY));

        if (!collidesWithIsland(newX, newY, SHIP_SIZE / 2.0)) {
            ship.setX(newX);
            ship.setY(newY);
        }

        if (input.isShoot()) {
            tryShoot(ship);
        }

        if (input.isMine()) {
            tryPlaceMine(ship);
        }
    }

    private void tryShoot(Ship ship) {
        long now = System.currentTimeMillis();
        if (now - ship.getLastShotTime() >= SHOOT_COOLDOWN) {
            ship.setLastShotTime(now);

            double radians = Math.toRadians(ship.getAngle());
            double bulletX = ship.getX() + Math.cos(radians) * (SHIP_SIZE / 2.0 + 10);
            double bulletY = ship.getY() + Math.sin(radians) * (SHIP_SIZE / 2.0 + 10);

            Bullet bullet = new Bullet(bulletX, bulletY, ship.getAngle(), ship.getPlayerId());
            gameState.getBullets().add(bullet);
        }
    }

    private void tryPlaceMine(Ship ship) {
        long now = System.currentTimeMillis();
        if (now - ship.getLastMineTime() >= MINE_COOLDOWN) {
            ship.setLastMineTime(now);

            double radians = Math.toRadians(ship.getAngle());
            double mineX = ship.getX() - Math.cos(radians) * (SHIP_SIZE / 2.0 + 15);
            double mineY = ship.getY() - Math.sin(radians) * (SHIP_SIZE / 2.0 + 15);

            Mine mine = new Mine(mineX, mineY, ship.getPlayerId());
            gameState.getMines().add(mine);
        }
    }

    private void updateBullets(double deltaTime) {
        Iterator<Bullet> iterator = gameState.getBullets().iterator();

        while (iterator.hasNext()) {
            Bullet bullet = iterator.next();

            double radians = Math.toRadians(bullet.getAngle());
            bullet.setX(bullet.getX() + Math.cos(radians) * BULLET_SPEED * deltaTime);
            bullet.setY(bullet.getY() + Math.sin(radians) * BULLET_SPEED * deltaTime);

            if (bullet.getX() < 0 || bullet.getX() > MAP_WIDTH ||
                bullet.getY() < 0 || bullet.getY() > MAP_HEIGHT) {
                iterator.remove();
                continue;
            }

            if (collidesWithIsland(bullet.getX(), bullet.getY(), BULLET_SIZE / 2.0)) {
                iterator.remove();
            }
        }
    }

    private void checkCollisions() {
        // проверка на то что снаряд попал в корабль
        Iterator<Bullet> bulletIterator = gameState.getBullets().iterator();
        while (bulletIterator.hasNext()) {
            Bullet bullet = bulletIterator.next();

            Ship target = (bullet.getOwnerId() == 1) ? gameState.getShip2() : gameState.getShip1();

            if (target != null && circlesCollide(
                    bullet.getX(), bullet.getY(), BULLET_SIZE / 2.0,
                    target.getX(), target.getY(), SHIP_SIZE / 2.0)) {
                target.setHp(target.getHp() - BULLET_DAMAGE);
                bulletIterator.remove();
            }
        }

        // проверка на то что корабль наступил на мину (собственный корабль тоже может)
        Iterator<Mine> mineIterator = gameState.getMines().iterator();
        while (mineIterator.hasNext()) {
            Mine mine = mineIterator.next();

            Ship ship1 = gameState.getShip1();
            Ship ship2 = gameState.getShip2();
            Ship[] ships = {ship1, ship2};
            boolean exploded = false;

            for (Ship ship: ships) {

                exploded = false;

                if (ship != null && circlesCollide(
                        mine.getX(), mine.getY(), MINE_SIZE / 2.0,
                        ship.getX(), ship.getY(), SHIP_SIZE / 2.0)) {
                    ship.setHp(ship.getHp() - MINE_DAMAGE);
                    exploded = true;
                }
            }

            if (exploded) {
                mineIterator.remove();
            }
        }
    }

    private boolean circlesCollide(double x1, double y1, double r1, double x2, double y2, double r2) {
        double dx = x1 - x2;
        double dy = y1 - y2;
        double distance = Math.sqrt(dx * dx + dy * dy);
        return distance < (r1 + r2);
    }

    private boolean collidesWithIsland(double x, double y, double radius) {
        for (Island island : gameState.getIslands()) {
            double closestX = Math.max(island.getX(), Math.min(x, island.getX() + island.getWidth()));
            double closestY = Math.max(island.getY(), Math.min(y, island.getY() + island.getHeight()));

            double dx = x - closestX;
            double dy = y - closestY;

            if ((dx * dx + dy * dy) < (radius * radius)) {
                return true;
            }
        }
        return false;
    }

    private void checkGameOver() {
        Ship ship1 = gameState.getShip1();
        Ship ship2 = gameState.getShip2();

        if (ship1.getHp() <= 0) {
            gameState.setGameOver(true);
            gameState.setWinnerId(2);
        } else if (ship2.getHp() <= 0) {
            gameState.setGameOver(true);
            gameState.setWinnerId(1);
        }
    }

    public GameState getGameState() {
        return gameState;
    }

    public boolean isGameOver() {
        return gameState.isGameOver();
    }

    public int getWinnerId() {
        return gameState.getWinnerId();
    }
}

