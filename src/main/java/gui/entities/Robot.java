package main.java.gui.entities;

public class Robot {
    public volatile double posX;
    public volatile double posY;
    public volatile double direction;

    public Robot(double x, double y, double direction) {
        posX = x;
        posY = y;
        this.direction = direction;
    }

    public void moveRobotStraight(double velocity, double angleToTarget, double duration) {
        posX += velocity * duration * Math.cos(angleToTarget);
        posY += velocity * duration * Math.sin(angleToTarget);
        direction = angleToTarget;
    }
}
