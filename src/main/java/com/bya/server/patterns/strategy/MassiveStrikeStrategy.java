package com.bya.server.patterns.strategy;

import com.bya.model.VillainAttackData;

public class MassiveStrikeStrategy implements AttackStrategy {
    @Override
    public VillainAttackData executeAttack(int currentHp) {
        VillainAttackData data = new VillainAttackData();
        data.damageDealt = 25;
        data.villainHp = currentHp;
        data.attackType = "ENRAGED_STRIKE";
        return data;
    }
}