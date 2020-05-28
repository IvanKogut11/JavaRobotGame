package main.java.gui.entities;

import java.util.concurrent.atomic.AtomicReference;

public class Robot {
    public AtomicReference<Double> posX = new AtomicReference<>(0.);
    public AtomicReference<Double> posY = new AtomicReference<>(0.);
    public volatile double direction;

    public Robot(double x, double y, double direction) {
        posX.set(x);
        posY.set(y);
        this.direction = direction;
    }

    public void moveRobotStraight(double velocity, double angleToTarget, double duration) {
        posX.updateAndGet((x) -> x + velocity * duration * Math.cos(angleToTarget));
        posY.updateAndGet((y) -> y + velocity * duration * Math.sin(angleToTarget));
        direction = angleToTarget;
    }
}
