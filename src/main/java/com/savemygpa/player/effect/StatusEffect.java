package com.savemygpa.player.effect;

import com.savemygpa.player.Player;
import com.savemygpa.player.StatType;

public abstract class StatusEffect {

    private int duration;

    public StatusEffect(int duration) {
        this.duration = duration;
    }

    public void onApply(Player player) {}

    public void onExpire(Player player) {}

    public void onTransition(Player player) {
        duration--;
    }

    public boolean isExpired() {
        return duration <= 0;
    }

    public int modifyIntelligenceGain(int base) {
        return base;
    }

    public int modifyStatCap(StatType type, int currentCap) {
        return currentCap;
    }

    public double getEventChanceMultiplier() {
        return 1.0;
    }

    public int getRemainingDuration() {
        return duration;
    }

    public void setRemainingDuration(int duration) {
        this.duration = duration;
    }

    public abstract String getName();

    public String getDescription() {
        int dur = getRemainingDuration();
        return "⏳ เหลือ " + (dur >= 99 ? "ยาวนาน" : dur + " turns") + "\n🗑 หมดเองตามเวลา";
    }
}