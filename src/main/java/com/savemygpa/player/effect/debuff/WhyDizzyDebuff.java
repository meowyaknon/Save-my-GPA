package com.savemygpa.player.effect.debuff;

import com.savemygpa.player.Player;
import com.savemygpa.player.effect.StatusEffect;

public class WhyDizzyDebuff extends StatusEffect {

    private static final int INT_PENALTY = 2;

    public WhyDizzyDebuff() {
        super(99);
    }

    @Override
    public void onApply(Player player) {
        System.out.println("[Debuff] Why Dizzy? — head spinning, hard to focus.");
    }

    @Override
    public void onExpire(Player player) {
        System.out.println("[Debuff] Why Dizzy? cleared — class helped settle the head.");
    }

    @Override
    public int modifyIntelligenceGain(int base) {
        return Math.max(0, base - INT_PENALTY);
    }

    @Override
    public String getName() {
        return "Why Dizzy?";
    }

    @Override
    public String getDescription() {
        return "-> 😵 หัวหมุน!\n" +
                "-> ส่งผล : 🧠 INT gain -2 ต่อกิจกรรม\n" +
                "-> หยุดทำงานเมื่อ : 🗑 เข้าห้องเรียน (random event)";
    }
}