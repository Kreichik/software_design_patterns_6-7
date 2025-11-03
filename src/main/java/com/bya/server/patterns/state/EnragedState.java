package com.bya.server.patterns.state;

import com.bya.model.HeroAttackData;
import com.bya.server.game.GameState;

public class EnragedState implements VillainState {
    @Override
    public void takeDamage(GameState gameState, HeroAttackData attackData) {
        int currentHp = gameState.getVillainHp();
        currentHp -= attackData.attackDamage;
        gameState.setVillainHp(currentHp);

        if (currentHp <= 0) {
            System.out.println("Злодей окончательно повержен!");
            gameState.setGameOver(true);
        }
    }
}