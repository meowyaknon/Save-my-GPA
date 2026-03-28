package com.savemygpa.player.effect.debuff;

import com.savemygpa.player.Player;
import com.savemygpa.player.StatType;
import com.savemygpa.player.effect.StatusEffect;

public class StackOverflowDownDebuff extends StatusEffect {

    private static final int INT_PENALTY = 5;
    private static final int MOOD_CAP    = 75;

    public StackOverflowDownDebuff() {
        super(99);
    }

    @Override
    public void onApply(Player player) {
        // Immediately clamp mood if already above cap
        if (player.getStat(StatType.MOOD) > MOOD_CAP) {
            player.changeStat(StatType.MOOD, MOOD_CAP - player.getStat(StatType.MOOD));
        }
        System.out.println("[Debuff] StackOverflow Down — internet is down. Brain foggy.");
    }

    @Override
    public void onExpire(Player player) {
        System.out.println("[Debuff] StackOverflow Down cleared — internet is back.");
    }

    @Override
    public void onTransition(Player player) {
    }

    @Override
    public int modifyIntelligenceGain(int base) {
        return Math.max(0, base - INT_PENALTY);
    }

    @Override
    public int modifyStatCap(StatType type, int currentCap) {
        if (type == StatType.MOOD) return Math.min(currentCap, MOOD_CAP);
        return currentCap;
    }

    @Override
    public String getName() { return "StackOverflow Down"; }
}