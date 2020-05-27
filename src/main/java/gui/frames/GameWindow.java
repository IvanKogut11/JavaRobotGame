package main.java.gui.frames;

import main.java.gui.entities.GameTimer;

import java.awt.BorderLayout;
import java.util.Timer;

import javax.swing.JInternalFrame;
import javax.swing.JPanel;

public class GameWindow extends JInternalFrame //OK
{
    private final GameVisualizer m_visualizer;
    private final ScoreLabel m_scoreLabel;
    private final TimerLabel m_timerLabel;

    public GameWindow() {
        super("Игровое поле", true, true, true, true);
        JPanel topPanel = new JPanel(new BorderLayout());
        GameTimer gameTimer = new GameTimer(new Timer());
        m_scoreLabel = new ScoreLabel(gameTimer);
        topPanel.add(m_scoreLabel, BorderLayout.WEST);
        m_timerLabel = new TimerLabel(gameTimer);
        topPanel.add(m_timerLabel, BorderLayout.EAST);
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(topPanel, BorderLayout.NORTH);
        m_visualizer = new GameVisualizer(this, gameTimer);
        mainPanel.add(m_visualizer, BorderLayout.CENTER);
        getContentPane().add(mainPanel);
        pack();
    }
}
