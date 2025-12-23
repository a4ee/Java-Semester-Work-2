package ru.itis.semester_work2.protocol;

public class PlayerInputMessage extends Message {
    private int ownerId;
    private boolean forward;  // W
    private boolean backward; // S
    private boolean left;     // A
    private boolean right;    // D
    private boolean shoot;    // SPACE
    private boolean mine;     // E

    public PlayerInputMessage() {
        super(MessageType.PLAYER_INPUT);
    }

    public PlayerInputMessage(int ownerId) {
        super(MessageType.PLAYER_INPUT);
        this.ownerId = ownerId;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(int ownerId) {
        this.ownerId = ownerId;
    }

    public boolean isForward() {
        return forward;
    }

    public void setForward(boolean forward) {
        this.forward = forward;
    }

    public boolean isBackward() {
        return backward;
    }

    public void setBackward(boolean backward) {
        this.backward = backward;
    }

    public boolean isLeft() {
        return left;
    }

    public void setLeft(boolean left) {
        this.left = left;
    }

    public boolean isRight() {
        return right;
    }

    public void setRight(boolean right) {
        this.right = right;
    }

    public boolean isShoot() {
        return shoot;
    }

    public void setShoot(boolean shoot) {
        this.shoot = shoot;
    }

    public boolean isMine() {
        return mine;
    }

    public void setMine(boolean mine) {
        this.mine = mine;
    }
}
