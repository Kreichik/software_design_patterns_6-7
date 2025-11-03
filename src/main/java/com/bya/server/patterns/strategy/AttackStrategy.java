package com.bya.server.patterns.strategy;

import com.bya.model.VillainAttackData;

public interface AttackStrategy {
    VillainAttackData executeAttack(int currentHp);
}