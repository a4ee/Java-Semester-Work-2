package ru.itis.semester_work2.model;

import java.util.ArrayList;
import java.util.List;

public class GameState {
    private Ship ship1, ship2;
    private List<Bullet> bullets = new ArrayList<>();
    private List<Mine> mines = new ArrayList<>();
    private List<Island> islands = new ArrayList<>();
    private boolean gameOver;
    private int winnerId;

    public Ship getShip1() { return ship1; }
    public void setShip1(Ship ship1) { this.ship1 = ship1; }

    public Ship getShip2() { return ship2; }
    public void setShip2(Ship ship2) { this.ship2 = ship2; }

    public List<Bullet> getBullets() { return bullets; }
    public void setBullets(List<Bullet> bullets) { this.bullets = bullets; }

    public List<Mine> getMines() { return mines; }
    public void setMines(List<Mine> mines) { this.mines = mines; }

    public List<Island> getIslands() { return islands; }
    public void setIslands(List<Island> islands) { this.islands = islands; }

    public boolean isGameOver() { return gameOver; }
    public void setGameOver(boolean gameOver) { this.gameOver = gameOver; }

    public int getWinnerId() { return winnerId; }
    public void setWinnerId(int winnerId) { this.winnerId = winnerId; }
}
