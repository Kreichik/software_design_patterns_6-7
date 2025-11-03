package com.bya.server.patterns.strategy;

import com.bya.model.VillainAttackData;

public class SimpleStrikeStrategy implements AttackStrategy {
    @Override
    public VillainAttackData executeAttack(int currentHp) {
        VillainAttackData data = new VillainAttackData();
        data.damageDealt = 10;
        data.villainHp = currentHp;
        return data;
    }
}