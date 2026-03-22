package com.savemygpa.player.effect.buff;

import com.savemygpa.player.Player;
import com.savemygpa.player.StatType;
import com.savemygpa.player.effect.StatusEffect;

public class SeniorNoteBuff extends StatusEffect {

    private int daysRemaining;
    private static final int INT_REWARD = 4;

    public SeniorNoteBuff() {
        super(Integer.MAX_VALUE); // base duration won't expire; we manage it ourselves
        this.daysRemaining = 2;
    }

    @Override
    public void onApply(Player player) {
        System.out.println("[Buff] Senior's Note received — int +4 will arrive in 2 days!");
    }

    public void tickDay(Player player) {
        daysRemaining--;
        if (daysRemaining <= 0) {
            player.changeStat(StatType.INTELLIGENCE, INT_REWARD);
            System.out.println("[Buff] Senior's Note activated — int +4!");
            forceExpire();
        }
    }

    private void forceExpire() {
        while (!isExpired()) {
            super.onTransition(null);
        }
    }

    @Override
    public void onTransition(Player player) {
    }

    @Override
    public void onExpire(Player player) {
        System.out.println("[Buff] Senior's Note expired.");
    }

    @Override
    public String getName() {
        return "Senior's Note";
    }
}