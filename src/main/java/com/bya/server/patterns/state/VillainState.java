package com.bya.server.patterns.state;

import com.bya.model.HeroAttackData;
import com.bya.server.game.GameState;

public interface VillainState {
    void takeDamage(GameState gameState, HeroAttackData attackData);
}