package ru.itis.semester_work2.model;

import java.util.List;

public class GameState {
    private Ship ship1, ship2;
    private List<Bullet> bullets;
    private List<Mine> mines;
    private List<Island> islands;
    boolean gameOver;
    int winnerId;
}
