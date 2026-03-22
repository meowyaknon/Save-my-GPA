package com.savemygpa.event.busstop;

import com.savemygpa.core.TimeSystem;
import com.savemygpa.event.Event;
import com.savemygpa.event.EventContext;
import com.savemygpa.event.Location;
import com.savemygpa.player.Player;
import com.savemygpa.player.StatType;

public class GoodSeatEvent extends Event {

    @Override public boolean isVisitTriggered() { return false; }

    @Override
    public boolean canOccur(Player player, TimeSystem timeSystem, EventContext context) {
        return context.getLocation() == Location.BUS_STOP;
    }

    @Override
    protected void applyEffects(Player player, TimeSystem timeSystem) {
        player.changeStat(StatType.MOOD, 15);
        player.changeStat(StatType.INTELLIGENCE, 5);
    }

    @Override protected String getName() { return "ได้นั่งมุมโปรด"; }
    @Override protected String getDescription() { return "แอร์เย็นฉ่ำ ปลั๊กไฟพร้อม ที่นั่งว่างพอดี สมาธิมาเต็มร้อย"; }
    @Override public double getChance() { return 0.3; }
}