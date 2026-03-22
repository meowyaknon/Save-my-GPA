package com.savemygpa.event.outside;

import com.savemygpa.core.TimeSystem;
import com.savemygpa.event.Event;
import com.savemygpa.event.EventContext;
import com.savemygpa.event.Location;
import com.savemygpa.player.Player;
import com.savemygpa.player.StatType;
import com.savemygpa.player.effect.debuff.WetFeetDebuff;

public class RainEvent extends Event {

    @Override
    public boolean canOccur(Player player, TimeSystem timeSystem, EventContext context) {
        return context.getLocation() == Location.OUTSIDE
                && !player.hasEffect(WetFeetDebuff.class);
    }

    @Override
    protected void applyEffects(Player player, TimeSystem timeSystem) {
        player.changeStat(StatType.MOOD, -10);
        player.addEffect(new WetFeetDebuff());
        System.out.println("[Event] ฝนตกหนัก! ถุงเท้าเปียกจนเดินไม่สะดวก อารมณ์ไม่ดีซะมัด");
    }

    @Override
    protected String getName() { return "ฝนตกหนัก"; }

    @Override
    protected String getDescription() {
        return "ฝนตกหนักที่เขตลาดกระบังคือเรื่องปกติ ถุงเท้าเปียกๆ คู่นี้ ทำให้อารมณ์ไม่ดีซะมัด";
    }

    @Override
    public double getChance() { return 0.3; }
}