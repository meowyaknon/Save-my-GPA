package com.savemygpa.player.effect.buff;

import com.savemygpa.player.Player;
import com.savemygpa.player.StatType;
import com.savemygpa.player.effect.StatusEffect;

public class SeniorNoteBuff extends StatusEffect {

    private int daysRemaining;
    private static final int INT_REWARD = 2;

    public SeniorNoteBuff() {
        super(0);
        this.daysRemaining = 2;
    }

    @Override
    public void onApply(Player player) {
        System.out.println("[Buff] Senior's Note received — int +2 will arrive in 2 days!");
    }

    public void tickDay(Player player) {
        if (daysRemaining <= 0) return;
        daysRemaining--;
        if (daysRemaining <= 0) {
            player.changeStat(StatType.INTELLIGENCE, INT_REWARD);
            System.out.println("[Buff] Senior's Note activated — int +2!");
            daysRemaining = 0;
        }
    }

    @Override
    public void onExpire(Player player) {
        System.out.println("[Buff] Senior's Note expired.");
    }

    public int getDaysRemaining() {
        return Math.max(0, daysRemaining);
    }

    public void setDaysRemaining(int daysRemaining) {
        this.daysRemaining = Math.max(0, daysRemaining);
    }

    @Override
    public String getName() {
        return "Senior's Note";
    }

    @Override
    public boolean isExpired() {
        return daysRemaining <= 0;
    }

    @Override
    public int getRemainingDuration() {
        return getDaysRemaining();
    }

    @Override
    public String getDescription() {
        return "📖 บันทึกจากรุ่นพี่\n" +
                "🧠 INT +2 จะมาถึงใน " + getDaysRemaining() + " วัน\n" +
                "🗑 หมดเองเมื่อ INT ถูกส่งมอบแล้ว";
    }
}