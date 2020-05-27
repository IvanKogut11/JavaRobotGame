package main.java.gui.entities;

import javax.swing.*;
import java.awt.*;
import java.util.Observable;
import java.util.Observer;
import java.util.Timer;
import java.util.TimerTask;

public class GameTimer implements Observer {

    private volatile int time = 0;
    private volatile boolean isStopped = true;
    private volatile int maxTime = 0;
    private volatile Timer timer;

    public GameTimer(Timer timer) {
        this.timer = timer;
        timer.scheduleAtFixedRate(timerTask, 0, 1000);
    }

    public boolean getIsStopped() {
        return isStopped;
    }

    public int getTime() {
        return time;
    }

    public int getMaxTime() {
        return maxTime;
    }

    private TimerTask timerTask = new TimerTask() {
        @Override
        public void run() {
            if (!isStopped)
                time++;
        }
    };

    private void start() {
        isStopped = false;
    }

    private void stop() {
        maxTime = Math.max(time, maxTime);
        time = 0;
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
