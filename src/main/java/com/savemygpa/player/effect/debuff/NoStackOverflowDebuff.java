package com.savemygpa.player.effect.debuff;

import com.savemygpa.player.Player;
import com.savemygpa.player.StatType;
import com.savemygpa.player.effect.StatusEffect;

public class NoStackOverflowDebuff extends StatusEffect {

    private static final int INT_PENALTY = 5;
    private static final int MOOD_CAP = 75;

    public NoStackOverflowDebuff() {
        super(99);
    }

    @Override
    public void onApply(Player player) {
        int currentMood = player.getStat(StatType.MOOD);
        if (currentMood > MOOD_CAP) {
            player.changeStat(StatType.MOOD, MOOD_CAP - currentMood);
        }
        System.out.println("[Debuff] No StackOverflow — can't look anything up. Brain is foggy.");
    }

    @Override
    public void onExpire(Player player) {
        System.out.println("[Debuff] No StackOverflow cleared — internet is back.");
    }

    @Override
    public int modifyIntelligenceGain(int base) {
        return Math.max(0, base - INT_PENALTY);
    }

    @Override
    public int modifyStatCap(StatType type, int currentCap) {
        if (type == StatType.MOOD) {
            return Math.min(currentCap, MOOD_CAP);
        }
        return currentCap;
    }

    @Override
    public String getName() {
        return "No StackOverflow";
    }
}