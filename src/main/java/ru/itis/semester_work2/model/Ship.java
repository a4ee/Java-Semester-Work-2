package ru.itis.semester_work2.model;

public class Ship {
    private double x,y; // позиция
    private double angle; // угол поворота
    private int hp;
    private long lastShotTime; // время ласт выстрела
    private long lastMineTime; // время ласт мины
    int playerId; // 1 или 2




    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getAngle() {
        return angle;
    }

    public void setAngle(double angle) {
        this.angle = angle;
    }

    public int getHp() {
        return hp;
    }

    public void setHp(int hp) {
        this.hp = hp;
    }

    public long getLastShotTime() {
        return lastShotTime;
    }

    public void setLastShotTime(long lastShotTime) {
        this.lastShotTime = lastShotTime;
    }

    public long getLastMineTime() {
        return lastMineTime;
    }

    public void setLastMineTime(long lastMineTime) {
        this.lastMineTime = lastMineTime;
    }

    public int getPlayerId() {
        return playerId;
    }

    public void setPlayerId(int playerId) {
        this.playerId = playerId;
    }
}
