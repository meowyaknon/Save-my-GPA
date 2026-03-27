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
    public String getName() { return "รถพระจอมไม่มา"; }
    @Override
    public String getDescription() { return "รอนานจนเกือบซึม รถบัสที่รอคอยไม่โผล่มา"; }
    @Override public double getChance() { return 0.4; }
}