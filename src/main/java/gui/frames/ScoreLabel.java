package main.java.gui.frames;

import main.java.gui.entities.GameTimer;

import javax.swing.*;
import java.awt.*;
import java.util.Timer;
import java.util.TimerTask;

public class ScoreLabel extends JLabel {
    private final Timer m_timer = initTimer();
    private final GameTimer gameTimer;

    private static java.util.Timer initTimer() {
        java.util.Timer timer = new java.util.Timer("events generator", true);
        return timer;
    }

    public ScoreLabel (GameTimer gameTimer) {
        super("0");
        this.gameTimer = gameTimer;
        setFont(new Font("Sitka Text", Font.BOLD, 30));
        //setBorder(BorderFactory.createTitledBorder("Score"));
        m_timer.schedule(new TimerTask() {
            @Override
            public void run() {
                onRedrawEvent();
            }
        }, 0, 50);
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        int maxTime = gameTimer.getMaxTime();
        setText(String.format("%02d:%02d", maxTime / 60, maxTime % 60));
    }

    protected void onRedrawEvent() {
        EventQueue.invokeLater(this::repaint);
    }
}
