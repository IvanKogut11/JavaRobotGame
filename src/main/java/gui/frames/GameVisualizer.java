package main.java.gui.frames;

import main.java.gui.entities.Game;
import main.java.gui.entities.GameTimer;
import main.java.gui.frames.GameWindow;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.util.*;

import javax.swing.JPanel;

public class GameVisualizer extends JPanel {
    private final Timer m_timer = initTimer();
    private final GameWindow gameWindow;
    private final Game game;

    private static Timer initTimer() {
        Timer timer = new Timer("events generator", true);
        return timer;
    }

    private static final int robotDiam1 = 30;
    private static final int robotDiam2 = 10;

    private static final int fenceLen1 = 5;
    private static final int fenceLen2 = 30;

    private static volatile double maxX = 500;
    private static final double minX = 0;
    private static volatile double maxY = 500;
    private static final double minY = 0;


    public GameVisualizer(GameWindow gameWindow, GameTimer gameTimer) {
        game = new Game(this);
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
                if (e.getButton() == MouseEvent.BUTTON1 && !game.getIsTargetSelected()) {
                    game.addTargetPoint(p.x, p.y);
                    repaint();
                } else if (e.getButton() == MouseEvent.BUTTON3) {
                    game.addFence(p);
                    repaint();
                }
            }
        });
        game.addObserver(gameTimer);
        setDoubleBuffered(true);
        this.gameWindow = gameWindow;
    }

    protected void onRedrawEvent() {
        EventQueue.invokeLater(this::repaint);
    }

    public int getRobotDiam1() {
        return robotDiam1;
    }

    public int getRobotDiam2() {
        return robotDiam2;
    }

    public int getFenceLen1() {
        return fenceLen1;
    }

    public int getFenceLen2() {
        return fenceLen2;
    }

    public double getMinX() {
        return minX;
    }

    public double getMaxX() {
        return maxX;
    }

    public double getMinY() {
        return minY;
    }

    public double getMaxY() {
        return maxY;
    }

    private void updateWidthAndHeight() {
        maxX = gameWindow.getWidth();
        maxY = gameWindow.getHeight();
    }


    protected void onModelUpdateEvent() {
        updateWidthAndHeight();
        game.updateIfNeeded();
    }

    private static int round(double value) {
        return (int) (value + 0.5);
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        Graphics2D g2d = (Graphics2D) g;
        drawRobot(g2d, round(game.getRobotPosX()), round(game.getRobotPosY()), game.getRobotDirection());
        drawTarget(g2d, round(game.getTargetPosX()), round(game.getTargetPosY()));
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
        ArrayList<Point> fences = game.getFencesCentres();
        for (Point center : fences) {
            AffineTransform t = AffineTransform.getRotateInstance(0, 0, 0);
            g.setTransform(t);
            g.setColor(Color.RED);
            fillRect(g, center.x, center.y, fenceLen1, fenceLen2);
            g.setColor(Color.BLACK);
            drawRect(g, center.x, center.y, fenceLen1, fenceLen2);
        }
    }

    private void drawRobot(Graphics2D g, int x, int y, double direction) {
        int robotCenterX = round(game.getRobotPosX());
        int robotCenterY = round(game.getRobotPosY());
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
