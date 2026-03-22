package com.savemygpa.player.effect.buff;

import com.savemygpa.player.Player;
import com.savemygpa.player.effect.StatusEffect;

public class AuraOfLuckBuff extends StatusEffect {

    private static final double LUCK_MULTIPLIER = 1.5;

    public AuraOfLuckBuff() {
        super(2);
    }

    @Override
    public void onApply(Player player) {
        System.out.println("[Buff] Aura of Luck activated — feeling lucky for 2 transitions!");
    }

    @Override
    public void onExpire(Player player) {
        System.out.println("[Buff] Aura of Luck faded.");
    }

    @Override
    public double getEventChanceMultiplier() {
        return LUCK_MULTIPLIER;
    }

    @Override
    public String getName() {
        return "Aura of Luck";
    }
}