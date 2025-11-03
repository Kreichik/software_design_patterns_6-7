package com.bya.server.patterns.state;

import com.bya.model.HeroAttackData;
import com.bya.server.game.GameState;
import com.bya.server.patterns.strategy.MassiveStrikeStrategy;

public class NormalState implements VillainState {
    @Override
    public void takeDamage(GameState gameState, HeroAttackData attackData) {
        int currentHp = gameState.getVillainHp();
        currentHp -= attackData.attackDamage;
        gameState.setVillainHp(currentHp);

        if (currentHp <= 0) {
            System.out.println("Злодей переходит во вторую фазу!");
            gameState.setState(new EnragedState());
            gameState.setVillainHp(150);
            gameState.setAttackStrategy(new MassiveStrikeStrategy());
        }
    }
}