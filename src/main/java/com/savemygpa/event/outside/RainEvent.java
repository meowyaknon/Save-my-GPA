package com.savemygpa.event.outside;

import com.savemygpa.core.TimeSystem;
import com.savemygpa.event.Event;
import com.savemygpa.event.EventContext;
import com.savemygpa.event.Location;
import com.savemygpa.player.Player;
import com.savemygpa.player.StatType;
import com.savemygpa.player.effect.debuff.WetFeetDebuff;

public class RainEvent extends Event {

    @Override public boolean isVisitTriggered() { return true; }

    @Override
    public boolean canOccur(Player player, TimeSystem timeSystem, EventContext context) {
        return context.getLocation() == Location.OUTSIDE
                && !player.hasEffect(WetFeetDebuff.class);
    }

    @Override
    protected void applyEffects(Player player, TimeSystem timeSystem) {
        player.changeStat(StatType.MOOD, -10);
        player.addEffect(new WetFeetDebuff());
    }

    @Override
    public String getName() { return "ฝนตกหนัก"; }
    @Override
    public String getDescription() { 
    return "ฝนตกหนักที่เขตลาดกระบังคือเรื่องปกติ ถุงเท้าเปียกๆ คู่นี้ ทำให้อารมณ์ไม่ดีชะมัด\n\n" +
           "MOOD -10\n\n" +
           "Debuff: Wet Feet (เสีย energy เพิ่มขึ้น 1 หน่วย เมื่อเปลี่ยนสถานที่) ทั้งวัน";
    }
    @Override public double getChance() { return 0.3; }
}