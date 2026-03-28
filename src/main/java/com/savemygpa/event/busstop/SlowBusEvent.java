package com.savemygpa.event.busstop;

import com.savemygpa.core.TimeSystem;
import com.savemygpa.event.Event;
import com.savemygpa.event.EventContext;
import com.savemygpa.event.Location;
import com.savemygpa.player.Player;
import com.savemygpa.player.StatType;

public class SlowBusEvent extends Event {

    @Override public boolean isVisitTriggered() { return false; }

    @Override
    public boolean canOccur(Player player, TimeSystem timeSystem, EventContext context) {
        return context.getLocation() == Location.BUS_STOP;
    }

    @Override
    protected void applyEffects(Player player, TimeSystem timeSystem) {
        timeSystem.advanceTime(2);
        player.changeStat(StatType.ENERGY, -1);
        player.changeStat(StatType.MOOD, -15);
    }

    @Override
    public String getName() { return "\"รถพระจอม\" ในตำนานไม่มาสักที"; }
    @Override
    public String getDescription() { return "รอนานจนเหงื่อซึม รถบัสที่รอคอยไม่โผล่มาเสียที แผนการอ่านหนังสือที่วางไว้หายไป 2 ชั่วโมง\n\n" + 
                                    "ผลกระทบ: Time -2, Energy -1, Mood -15"; }
    @Override public double getChance() { return 0.25; }
}