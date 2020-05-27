package main.java.gui.entities;

import main.java.gui.frames.GameVisualizer;

import java.awt.*;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedDeque;

public class Game extends Observable {
    private static final int robotDiam1 = 30;
    private static final int robotDiam2 = 10;

    private Random rand = new Random();
    private ConcurrentLinkedDeque<Point> pointsToVisit = new ConcurrentLinkedDeque<>();
    private final GameVisualizer gameVisualizer;

    private final Robot robot = new Robot(100, 100, 0);

    private volatile boolean isTargetSelected = false;
    private volatile double minDistBetweenFenceAndTarget = 50;

    private volatile double targetPosX = 150;
    private volatile double targetPosY = 100;

    private volatile double pointToVisitX = 150;
    private volatile double pointToVisitY = 100;

    private volatile ArrayList<Point> fences = new ArrayList<>();

    private static final double maxVelocity = 0.1;

    public Game (GameVisualizer gameVisualizer) {
        this.gameVisualizer = gameVisualizer;
    }

    public double getRobotPosX() {
        return robot.posX;
    }

    public double getRobotPosY() {
        return robot.posY;
    }

    public double getRobotDirection() {
        return robot.direction;
    }

    public boolean getIsTargetSelected() {
        return isTargetSelected;
    }

    public double getTargetPosX() {
        return targetPosX;
    }

    public double getTargetPosY() {
        return targetPosY;
    }

    public ArrayList<Point> getFencesCentres() {
        return fences;
    }

    public void addTargetPoint(int targetX, int targetY) {
        //TODO start timer
        setChanged();
        notifyObservers("timestart");
        addPointToVisit(targetX, targetY);
    }

    private void addPointToVisit(int pX, int pY) {
        Point pointToVisit = new Point(pX, pY);
        if (pointsToVisit.size() == 0) {
            isTargetSelected = true;
            setTargetPosition(pointToVisit);
        }
        pointsToVisit.addLast(pointToVisit);
        pointToVisitX = pX;
        pointToVisitY = pY;
        if (pointsToVisit.size() > 25) {
            pointsToVisit.clear();
            pointsToVisit.addLast(new Point((int)targetPosX, (int)targetPosY));
            pointToVisitX = targetPosX;
            pointToVisitY = targetPosY;
        }
    }

    private void pollPointToVisit() {
        if (pointsToVisit.size() == 0)
            return;
        pointsToVisit.pollLast();
        if (pointsToVisit.size() == 0) {
            isTargetSelected = false;
            setChanged();
            notifyObservers("scoreinc");
            setChanged();
            notifyObservers("timestop");
            //notifyObservers();//TODO incrementScore
            return;
        }
        Point curP = pointsToVisit.peekLast();
        pointToVisitX = curP.x;
        pointToVisitY = curP.y;
    }

    public void addFence(Point p) {
        if (calcDistance(targetPosX, targetPosY, p.x, p.y) >= minDistBetweenFenceAndTarget &&
                !isPointInRobot(p.x, p.y))
            fences.add(p);
    }

    private boolean isPointInFence(double pX, double pY, int fenceCenterX, int fenceCenterY) {
        int leftX = fenceCenterX - gameVisualizer.getFenceLen1() / 2,
                rightX = fenceCenterX + gameVisualizer.getFenceLen1() / 2;
        int topY = fenceCenterY - gameVisualizer.getFenceLen2() / 2,
                bottomY = fenceCenterY + gameVisualizer.getFenceLen2() / 2;
        return leftX <= pX && pX <= rightX &&
                topY <= pY && pY <= bottomY;
    }

    private boolean isPointInAnyFence(double pX, double pY) { //TODO change
        for (Point fenceCenter : fences) {
            if (isPointInFence(pX, pY, fenceCenter.x, fenceCenter.y)) {
                Point pointToAvoidFence = getPointToAvoidFence(fenceCenter.x, fenceCenter.y);
                addPointToVisit(pointToAvoidFence.x, pointToAvoidFence.y);
                return true;
            }
        }
        return false;
    }

    private Point getPointToAvoidFence(int fenceCenterX, int fenceCenterY) { //Can be edited
        int type = rand.nextInt(3);
        int shift = 15;
        if (type == 0)
            return new Point((int) getRobotPosX(),
                    (int) applyLimits(fenceCenterY + gameVisualizer.getFenceLen2() / 2. + shift,
                            gameVisualizer.getMinY(), gameVisualizer.getMaxY()));
        if (type == 1)
            return new Point((int) getRobotPosX(),
                    (int) applyLimits(fenceCenterY + gameVisualizer.getFenceLen2() / 2. - shift,
                            gameVisualizer.getMinY(), gameVisualizer.getMaxY()));
        double angleToTarget = calcAngleTo(getRobotPosX(), getRobotPosY(), pointToVisitX, pointToVisitY);
        if (angleToTarget >= Math.PI / 2. && angleToTarget <= Math.PI * 3. / 2.)
            return new Point((int) applyLimits(getRobotPosX() + shift, gameVisualizer.getMinX(), gameVisualizer.getMaxX()),
                    (int) getRobotPosY());
        return new Point((int) applyLimits(getRobotPosX() - shift, gameVisualizer.getMinX(), gameVisualizer.getMaxX()),
                (int) getRobotPosY());
    }

    private boolean isPointInRobot(double pX, double pY) {
        return Math.pow(pX - getRobotPosX(), 2) / Math.pow(robotDiam1, 2) +
                Math.pow(pY - getRobotPosY(), 2) / Math.pow(robotDiam2, 2) <= 1;
    }

    protected void setTargetPosition(Point p) {
        targetPosX = p.x;
        targetPosY = p.y;
    }

    private static double calcDistance(double x1, double y1, double x2, double y2) {
        double diffX = x1 - x2;
        double diffY = y1 - y2;
        return Math.sqrt(diffX * diffX + diffY * diffY);
    }

    private static double calcAngleTo(double fromX, double fromY, double toX, double toY) {
        double diffX = toX - fromX;
        double diffY = toY - fromY;

        return asNormalizedRadians(Math.atan2(diffY, diffX));
    }

    private void makeModelsInsideWindow() {
        robot.posX = applyLimits(robot.posX, gameVisualizer.getMinX(), gameVisualizer.getMaxX());
        robot.posY = applyLimits(robot.posY, gameVisualizer.getMinY(), gameVisualizer.getMaxY());
        targetPosX = applyLimits(targetPosX, gameVisualizer.getMinX(), gameVisualizer.getMaxX());
        targetPosY = applyLimits(targetPosY, gameVisualizer.getMinY(), gameVisualizer.getMaxY());
    }

    private static double applyLimits(double value, double min, double max) {
        if (value < min)
            return min;
        if (value > max)
            return max;
        return value;
    }

    public void updateIfNeeded() {
        makeModelsInsideWindow();
        double distance = calcDistance(pointToVisitX, pointToVisitY,
                getRobotPosX(), getRobotPosY());
        if (distance < 0.5) {
            pollPointToVisit();
            if (pointsToVisit.size() == 0) {
                fences.clear();
            }
            return;
        }
        double velocity = maxVelocity;
        double angleToTarget = calcAngleTo(getRobotPosX(), getRobotPosY(), pointToVisitX, pointToVisitY);

        moveRobotStraight(velocity, angleToTarget, 10);
    }

    private void moveRobotStraight(double velocity, double angleToTarget, double duration) { //TODO
        double prevRobotX = getRobotPosX();
        double prevRobotY = getRobotPosY();
        double prevRobotDir = getRobotDirection();
        robot.moveRobotStraight(velocity, angleToTarget, duration);
        if (!isPointInAnyFence(getRobotPosX(), getRobotPosY())) {
            robot.posX = applyLimits(robot.posX, gameVisualizer.getMinX(), gameVisualizer.getMaxX());
            robot.posY = applyLimits(robot.posY, gameVisualizer.getMinY(), gameVisualizer.getMaxY());
            robot.direction = angleToTarget;
        }
        else {
            robot.posX = prevRobotX;
            robot.posY = prevRobotY;
            robot.direction = prevRobotDir;
        }
    }

    private static double asNormalizedRadians(double angle) {
        while (angle < 0) {
            angle += 2 * Math.PI;
        }
        while (angle >= 2 * Math.PI) {
            angle -= 2 * Math.PI;
        }
        return angle;
    }
}
