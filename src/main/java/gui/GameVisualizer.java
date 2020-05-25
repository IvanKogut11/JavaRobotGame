package main.java.gui;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.*;

import javax.swing.JPanel;

public class GameVisualizer extends JPanel {
    private final Timer m_timer = initTimer();
    private final GameWindow gameWindow;

    private static Timer initTimer() {
        Timer timer = new Timer("events generator", true);
        return timer;
    }

    private Random rand = new Random();
    private ConcurrentLinkedDeque<Point> pointsToVisit = new ConcurrentLinkedDeque<>();

    private volatile double m_robotPositionX = 100;
    private volatile double m_robotPositionY = 100;
    private volatile double m_robotDirection = 0;
    private static final int robotDiam1 = 30;
    private static final int robotDiam2 = 10;

    private volatile double m_targetPositionX = 150;
    private volatile double m_targetPositionY = 100;
    private volatile boolean isTargetSelected = false;
    private volatile double minDistBetweenFenceAndTarget = 50;

    private volatile double pointToVisitX = 150;
    private volatile double pointToVisitY = 100;

    private volatile ArrayList<Point> m_fences = new ArrayList<>();
    private static final int fenceLen1 = 5;
    private static final int fenceLen2 = 30;

    private static final double maxVelocity = 0.1;
    private static final double maxAngularVelocity = 0.001;
    private static volatile double maxX = 500;
    private static final double minX = 0;
    private static volatile double maxY = 500;
    private static final double minY = 0;

    public GameVisualizer(GameWindow gameWindow) {
        m_timer.schedule(new TimerTask() {
            @Override
            public void run() {
                onRedrawEvent();
            }
        }, 0, 50);
        m_timer.schedule(new TimerTask() {
            @Override
            public void run() {
                onModelUpdateEvent();
            }
        }, 0, 10);
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Point p = e.getPoint();
                if (e.getButton() == MouseEvent.BUTTON1 && !isTargetSelected) {
                    addPointToVisit(p.x, p.y);
                    repaint();
                } else if (e.getButton() == MouseEvent.BUTTON3) {
                    addFence(p);
                    repaint();
                }
            }
        });
        setDoubleBuffered(true);
        this.gameWindow = gameWindow;
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
    }

    private void pollPointToVisit() {
        if (pointsToVisit.size() == 0)
            return;
        pointsToVisit.pollLast();
        if (pointsToVisit.size() == 0) {
            isTargetSelected = false;
            return;
        }
        Point curP = pointsToVisit.peekLast();
        pointToVisitX = curP.x;
        pointToVisitY = curP.y;
    }


    protected void addFence(Point p) {
        if (calcDistance(m_targetPositionX, m_targetPositionY, p.x, p.y) >= minDistBetweenFenceAndTarget &&
                !isPointInRobot(p.x, p.y))
            m_fences.add(p);
    }

    private boolean isPointInFence(double pX, double pY, int fenceCenterX, int fenceCenterY) {
        int leftX = fenceCenterX - fenceLen1 / 2, rightX = fenceCenterX + fenceLen1 / 2;
        int topY = fenceCenterY - fenceLen2 / 2, bottomY = fenceCenterY + fenceLen2 / 2;
        return leftX <= pX && pX <= rightX &&
                topY <= pY && pY <= bottomY;
    }

    private boolean isPointInAnyFence(double pX, double pY) { //TODO change
        for (Point fenceCenter : m_fences) {
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
        int shift = 10;
        if (type == 0)
            return new Point((int) m_robotPositionX,
                    (int) applyLimits(fenceCenterY + fenceLen2 / 2. + shift, minY, maxY));
        if (type == 1)
            return new Point((int) m_robotPositionX,
                    (int) applyLimits(fenceCenterY + fenceLen2 / 2. - shift, minY, maxY));
        double angleToTarget = calcAngleTo(m_robotPositionX, m_robotPositionY, pointToVisitX, pointToVisitY);
        if (angleToTarget >= Math.PI / 2. && angleToTarget <= Math.PI * 3. / 2.)
            return new Point((int) applyLimits(m_robotPositionX + shift, minX, maxX),
                    (int) m_robotPositionY);
        return new Point((int) applyLimits(m_robotPositionX + shift, minX, maxX),
                (int) m_robotPositionY);
    }

    private boolean isPointInRobot(double pX, double pY) {
        return Math.pow(pX - m_robotPositionX, 2) / Math.pow(robotDiam1, 2) +
                Math.pow(pY - m_robotPositionY, 2) / Math.pow(robotDiam2, 2) <= 1;
    }

    protected void setTargetPosition(Point p) {
        m_targetPositionX = p.x;
        m_targetPositionY = p.y;
    }

    protected void onRedrawEvent() {
        EventQueue.invokeLater(this::repaint);
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

    private void updateWidthAndHeight() {
        maxX = gameWindow.getWidth();
        maxY = gameWindow.getHeight();
    }

    private void makeModelsInsideWindow() {
        updateWidthAndHeight();
        m_robotPositionX = applyLimits(m_robotPositionX, minX, maxX);
        m_robotPositionY = applyLimits(m_robotPositionY, minY, maxY);
        m_targetPositionX = applyLimits(m_targetPositionX, minX, maxX);
        m_targetPositionY = applyLimits(m_targetPositionY, minY, maxY);
    }

    protected void onModelUpdateEvent() {
        makeModelsInsideWindow();
        double distance = calcDistance(pointToVisitX, pointToVisitY,
                m_robotPositionX, m_robotPositionY);
        if (distance < 0.5) {
            pollPointToVisit();
            return;
        }
        double velocity = maxVelocity;
        double angleToTarget = calcAngleTo(m_robotPositionX, m_robotPositionY, pointToVisitX, pointToVisitY);
/*        double angularVelocity = 0;
        if (angleToTarget > m_robotDirection)
            angularVelocity = maxAngularVelocity;
        if (angleToTarget < m_robotDirection)
            angularVelocity = -maxAngularVelocity;*/

        moveRobotStraight(velocity, angleToTarget, 10);
        //moveRobotStraight(velocity, angleToTarget, 10);
    }

    private static double applyLimits(double value, double min, double max) {
        if (value < min)
            return min;
        if (value > max)
            return max;
        return value;
    }

    private void moveRobotStraight(double velocity, double angleToTarget, double duration) {
        velocity = applyLimits(velocity, 0, maxVelocity);
        double newX = m_robotPositionX + velocity * duration * Math.cos(angleToTarget);
        double newY = m_robotPositionY + velocity * duration * Math.sin(angleToTarget);
        if (!isPointInAnyFence(newX, newY)) {
            m_robotPositionX = applyLimits(newX, minX, maxX);
            m_robotPositionY = applyLimits(newY, minY, maxY);
            m_robotDirection = angleToTarget;
        }
    }
    
    /*private void moveRobot(double velocity, double angularVelocity, double duration) {
        velocity = applyLimits(velocity, 0, maxVelocity);
        angularVelocity = applyLimits(angularVelocity, -maxAngularVelocity, maxAngularVelocity);

        double newX = m_robotPositionX + velocity / angularVelocity * 
            (Math.sin(m_robotDirection  + angularVelocity * duration) -
                Math.sin(m_robotDirection));
        if (!Double.isFinite(newX))
            newX = m_robotPositionX + velocity * duration * Math.cos(m_robotDirection);

        double newY = m_robotPositionY - velocity / angularVelocity * 
            (Math.cos(m_robotDirection  + angularVelocity * duration) -
                Math.cos(m_robotDirection));
        if (!Double.isFinite(newY))
            newY = m_robotPositionY + velocity * duration * Math.sin(m_robotDirection);
        if (!isPointInAnyFence(newX, newY)) {
            m_robotPositionX = applyLimits(newX, minX, maxX);
            m_robotPositionY = applyLimits(newY, minY, maxY);
        }
        *//*m_robotPositionX = applyLimits(newX, minX, maxX);
        m_robotPositionY = applyLimits(newY, minY, maxY);*//*
        double newDirection = asNormalizedRadians(m_robotDirection + angularVelocity * duration); 
        m_robotDirection = newDirection;
    }*/

    private static double asNormalizedRadians(double angle) {
        while (angle < 0) {
            angle += 2 * Math.PI;
        }
        while (angle >= 2 * Math.PI) {
            angle -= 2 * Math.PI;
        }
        return angle;
    }

    private static int round(double value) {
        return (int) (value + 0.5);
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        Graphics2D g2d = (Graphics2D) g;
        drawRobot(g2d, round(m_robotPositionX), round(m_robotPositionY), m_robotDirection);
        drawTarget(g2d, round(m_targetPositionX), round(m_targetPositionY));
        drawFences(g2d);
    }

    private static void fillOval(Graphics g, int centerX, int centerY, int diam1, int diam2) {
        g.fillOval(centerX - diam1 / 2, centerY - diam2 / 2, diam1, diam2);
    }

    private static void drawOval(Graphics g, int centerX, int centerY, int diam1, int diam2) {
        g.drawOval(centerX - diam1 / 2, centerY - diam2 / 2, diam1, diam2);
    }

    private static void fillRect(Graphics g, int centerX, int centerY, int len1, int len2) {
        g.fillRect(centerX - len1 / 2, centerY - len2 / 2, len1, len2);
    }

    private static void drawRect(Graphics g, int centerX, int centerY, int len1, int len2) {
        g.drawRect(centerX - len1 / 2, centerY - len2 / 2, len1, len2);
    }

    private void drawFences(Graphics2D g) {
        for (Point center : m_fences) {
            AffineTransform t = AffineTransform.getRotateInstance(0, 0, 0);
            g.setTransform(t);
            g.setColor(Color.RED);
            fillRect(g, center.x, center.y, fenceLen1, fenceLen2);
            g.setColor(Color.BLACK);
            drawRect(g, center.x, center.y, fenceLen1, fenceLen2);
        }
    }

    private void drawRobot(Graphics2D g, int x, int y, double direction) {
        int robotCenterX = round(m_robotPositionX);
        int robotCenterY = round(m_robotPositionY);
        AffineTransform t = AffineTransform.getRotateInstance(direction, robotCenterX, robotCenterY);
        g.setTransform(t);
        g.setColor(Color.MAGENTA);
        fillOval(g, robotCenterX, robotCenterY, robotDiam1, robotDiam2);
        g.setColor(Color.BLACK);
        drawOval(g, robotCenterX, robotCenterY, robotDiam1, robotDiam2);
        g.setColor(Color.WHITE);
        fillOval(g, robotCenterX + 10, robotCenterY, 5, 5);
        g.setColor(Color.BLACK);
        drawOval(g, robotCenterX + 10, robotCenterY, 5, 5);
    }

    private void drawTarget(Graphics2D g, int x, int y) {
        AffineTransform t = AffineTransform.getRotateInstance(0, 0, 0);
        g.setTransform(t);
        g.setColor(Color.GREEN);
        fillOval(g, x, y, 5, 5);
        g.setColor(Color.BLACK);
        drawOval(g, x, y, 5, 5);
    }
}
