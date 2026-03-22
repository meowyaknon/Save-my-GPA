package com.savemygpa.player.effect.debuff;

import com.savemygpa.player.Player;
import com.savemygpa.player.effect.StatusEffect;

public class WhyDizzyDebuff extends StatusEffect {

    private static final int INT_PENALTY_PER_STUDY = 2;

    public WhyDizzyDebuff() {
        super(99);
    }

    @Override
    public void onApply(Player player) {
        System.out.println("[Debuff] Why Dizzy? — your head is spinning. Hard to focus.");
    }

    @Override
    public void onExpire(Player player) {
        System.out.println("[Debuff] Why Dizzy? cleared.");
    }

    @Override
    public int modifyIntelligenceGain(int base) {
        return Math.max(0, base - INT_PENALTY_PER_STUDY);
    }

    @Override
    public String getName() {
        return "Why Dizzy?";
    }
}