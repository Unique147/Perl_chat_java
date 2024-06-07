package com.example.chat;

import javafx.application.Platform;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class SessionManager {
    private static final long SESSION_TIMEOUT = 60000; // 1 час в миллисекундах
    private static final long CONFIRMATION_TIMEOUT = 60000; // 1 минута в миллисекундах

    private Map<String, Timer> userTimers = new HashMap<>();
    private ChatInterface chatInterface;

    public SessionManager(ChatInterface chatInterface) {
        this.chatInterface = chatInterface;
    }

    public void startSession(String userCode) {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> chatInterface.showConfirmationScene(userCode));
            }
        }, SESSION_TIMEOUT);

        userTimers.put(userCode, timer);
    }

    public void stopSession(String userCode) {
        Timer timer = userTimers.remove(userCode);
        if (timer != null) {
            timer.cancel();
        }
    }

    public void startConfirmationTimer(String userCode) {
        Timer confirmationTimer = new Timer();
        confirmationTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> chatInterface.logoutUser());
            }
        }, CONFIRMATION_TIMEOUT);
    }
}