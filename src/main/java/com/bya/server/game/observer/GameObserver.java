package com.bya.server.game.observer;

import com.bya.model.ServerEvent;

public interface GameObserver {
    void update(ServerEvent event);
}