package com.bya.server.game.observer;

import com.bya.model.ServerEvent;
import java.util.ArrayList;
import java.util.List;

public class GameSubject {
    private final List<GameObserver> observers = new ArrayList<>();

    public void addObserver(GameObserver observer) {
        observers.add(observer);
    }

    public void removeObserver(GameObserver observer) {
        observers.remove(observer);
    }

    public void notifyObservers(ServerEvent event) {
        for (GameObserver observer : observers) {
            observer.update(event);
        }
    }
}