package com.savemygpa.player.effect.debuff;

import com.savemygpa.player.Player;
import com.savemygpa.player.StatType;
import com.savemygpa.player.effect.StatusEffect;

public class WetFeetDebuff extends StatusEffect {

    public WetFeetDebuff() {
        super(99);
    }

    @Override
    public void onApply(Player player) {
        System.out.println("[Debuff] Wet Feet — soaked socks, energy drains per step.");
    }

    @Override
    public void onExpire(Player player) {
        System.out.println("[Debuff] Wet Feet cleared.");
    }

    @Override
    public void onTransition(Player player) {
        player.changeStat(StatType.ENERGY, -1);
    }

    @Override
    public String getName() {
        return "Wet Feet";
    }

    @Override
    public String getDescription() {
        return "-> 💧 รองเท้าเปียก!\n" +
                "-> ส่งผล : ⚡ Energy -1 ทุกครั้งที่เปลี่ยนสถานที่\n" +
                "-> หยุดทำงานเมื่อ : 🗑 หมดเองเมื่อสิ้นวัน (กลับบ้าน)";
    }
}