package com.bya.server.game;

import com.bya.model.*;
import com.bya.server.game.observer.GameSubject;
import com.bya.server.patterns.state.NormalState;
import com.bya.server.patterns.state.VillainState;
import com.bya.server.patterns.strategy.AttackStrategy;
import com.bya.server.patterns.strategy.SimpleStrikeStrategy;

public class GameState extends GameSubject {

    private static GameState instance;

    private int villainHp;
    private boolean isGameOver;
    private VillainState state;
    private AttackStrategy attackStrategy;

    private GameState() {
        this.villainHp = 100;
        this.isGameOver = false;
        this.state = new NormalState();
        this.attackStrategy = new SimpleStrikeStrategy();
    }

    public static synchronized GameState getInstance() {
        if (instance == null) {
            instance = new GameState();
        }
        return instance;
    }

    public synchronized void processHeroAttack(HeroAttackData attackData) {
        if (isGameOver) return;
        state.takeDamage(this, attackData);

        AttackConfirmationData confirmationData = new AttackConfirmationData();
        confirmationData.message = "Вы успешно атаковали злодея!";
        confirmationData.villainHp = this.villainHp;

        ServerEvent confirmationEvent = new ServerEvent();
        confirmationEvent.eventType = "attackConfirmation";
        confirmationEvent.data = confirmationData;
        notifyObservers(confirmationEvent);

        if(isGameOver){
            GameOverData gameOverData = new GameOverData();
            gameOverData.winner = "Герои";

            ServerEvent gameOverEvent = new ServerEvent();
            gameOverEvent.eventType = "gameOver";
            gameOverEvent.data = gameOverData;
            notifyObservers(gameOverEvent);
        }
    }

    public synchronized void processVillainAttack() {
        if (isGameOver) return;

        ServerEvent attackEvent = new ServerEvent();
        attackEvent.eventType = "villainAttack";
        attackEvent.data = attackStrategy.executeAttack(this.villainHp);
        notifyObservers(attackEvent);
    }

    public int getVillainHp() {
        return villainHp;
    }

    public void setVillainHp(int villainHp) {
        this.villainHp = villainHp;
    }

    public void setGameOver(boolean gameOver) {
        isGameOver = gameOver;
    }

    public void setState(VillainState state) {
        this.state = state;
    }

    public void setAttackStrategy(AttackStrategy attackStrategy) {
        this.attackStrategy = attackStrategy;
    }
}