package main.java.gui.entities;

import javax.swing.*;
import java.awt.*;
import java.util.Observable;
import java.util.Observer;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

public class GameTimer implements Observer {

    private AtomicInteger m_time = new AtomicInteger(0);
    private volatile boolean isStopped = true;
    private AtomicInteger maxTime = new AtomicInteger(0);
    private volatile Timer timer;

    public GameTimer(Timer timer) {
        this.timer = timer;
        timer.scheduleAtFixedRate(timerTask, 0, 1000);
    }

    public boolean getIsStopped() {
        return isStopped;
    }

    public int getTime() {
        return m_time.intValue();
    }

    public int getMaxTime() {
        return maxTime.intValue();
    }

    private TimerTask timerTask = new TimerTask() {
        @Override
        public void run() {
            if (!isStopped)
                m_time.incrementAndGet();
        }
    };

    private void start() {
        isStopped = false;
    }

    private void stop() {
        maxTime.accumulateAndGet(m_time.intValue(), Math::max);
        m_time.set(0);
        isStopped = true;
    }

    @Override
    public void update(Observable o, Object arg) {
        if ((arg).equals("timestart"))
            start();
        else if ((arg).equals("timestop"))
            stop();
    }
}
