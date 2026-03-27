package com.savemygpa.event.itbuilding;

import com.savemygpa.core.TimeSystem;
import com.savemygpa.event.Event;
import com.savemygpa.event.EventContext;
import com.savemygpa.event.Location;
import com.savemygpa.player.Player;
import com.savemygpa.player.StatType;

public class BrokenDoorEvent extends Event {

    @Override public boolean isVisitTriggered() { return true; }

    @Override
    public boolean canOccur(Player player, TimeSystem timeSystem, EventContext context) {
        return context.getLocation() == Location.IT_BUILDING;
    }

    @Override
    protected void applyEffects(Player player, TimeSystem timeSystem) {
        player.changeStat(StatType.MOOD, -15);
        timeSystem.advanceTime(1);
    }

    @Override
    public String getName() { return "ประตูอัตโนมัติไม่เปิด"; }
    @Override
    public String getDescription() { return "ชนประตูกระดังปัง เซนเซอร์ไม่ทำงาน เสียเวลาและอารมณ์"; }
    @Override public double getChance() { return 0.25; }
}