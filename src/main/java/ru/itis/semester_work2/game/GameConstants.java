package ru.itis.semester_work2.game;

public class GameConstants {
    public static final int MAP_WIDTH = 800;
    public static final int MAP_HEIGHT = 600;

    public static final double SHIP_SPEED = 150;           // пикселей/сек
    public static final double SHIP_ROTATION_SPEED = 180;  // градусов/сек
    public static final int SHIP_HP = 3;
    public static final int SHIP_SIZE = 40;                // размер хитбокса

    public static final double BULLET_SPEED = 300;         // пикселей/сек
    public static final int BULLET_SIZE = 8;
    public static final int BULLET_DAMAGE = 1;
    public static final long SHOOT_COOLDOWN = 1000;        // мс

    public static final int MINE_SIZE = 20;
    public static final int MINE_DAMAGE = 2;
    public static final long MINE_COOLDOWN = 3000;         // мс

    public static final int ISLAND_COUNT = 5;
}

