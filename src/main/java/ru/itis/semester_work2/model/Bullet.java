package ru.itis.semester_work2.model;

public class Bullet {
    private double x, y;
    private double angle;
    private int ownerId;

    public Bullet() {}

    public Bullet(double x, double y, double angle, int ownerId) {
        this.x = x;
        this.y = y;
        this.angle = angle;
        this.ownerId = ownerId;
    }

    public double getX() { return x; }
    public void setX(double x) { this.x = x; }

    public double getY() { return y; }
    public void setY(double y) { this.y = y; }

    public double getAngle() { return angle; }
    public void setAngle(double angle) { this.angle = angle; }

    public int getOwnerId() { return ownerId; }
    public void setOwnerId(int ownerId) { this.ownerId = ownerId; }
}
