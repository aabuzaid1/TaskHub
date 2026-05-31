package service;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import java.util.function.Consumer;

public class TimerService {
    private Timeline timeline;
    private int secondsRemaining;
    private boolean running = false;
    private Consumer<String> onTick;
    private Runnable onFinished;

    public void startTimer(int minutes, Consumer<String> onTick, Runnable onFinished) {
        if (running) {
            stopTimer();
        }
        
        this.secondsRemaining = minutes * 60;
        this.onTick = onTick;
        this.onFinished = onFinished;
        this.running = true;

        timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            secondsRemaining--;
            if (secondsRemaining <= 0) {
                stopTimer();
                if (this.onFinished != null) {
                    this.onFinished.run();
                }
            } else {
                if (this.onTick != null) {
                    this.onTick.accept(getFormattedTime());
                }
            }
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
        
        // Initial tick
        if (this.onTick != null) {
            this.onTick.accept(getFormattedTime());
        }
    }

    public void stopTimer() {
        if (timeline != null) {
            timeline.stop();
        }
        running = false;
    }

    public boolean isRunning() {
        return running;
    }

    public String getFormattedTime() {
        int m = secondsRemaining / 60;
        int s = secondsRemaining % 60;
        return String.format("%02d:%02d", m, s);
    }
}
