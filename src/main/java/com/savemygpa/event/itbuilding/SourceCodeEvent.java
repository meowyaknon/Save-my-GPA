package com.savemygpa.event.itbuilding;

import com.savemygpa.core.TimeSystem;
import com.savemygpa.event.Event;
import com.savemygpa.event.EventContext;
import com.savemygpa.event.Location;
import com.savemygpa.player.Player;
import com.savemygpa.player.StatType;

public class SourceCodeEvent extends Event {

    private boolean hasOccurred = false;

    @Override public boolean isVisitTriggered() { return true; }

    @Override
    public boolean canOccur(Player player, TimeSystem timeSystem, EventContext context) {
        if (hasOccurred) return false;
        if (context.getLocation() != Location.IT_BUILDING) return false;
        int day = timeSystem.getCurrentDay();
        return (day >= 1 && day <= 5) || (day >= 8 && day <= 12);
    }

    @Override
    protected void applyEffects(Player player, TimeSystem timeSystem) {
        hasOccurred = true;
        player.changeStat(StatType.INTELLIGENCE, 25);
    }

    @Override protected String getName() { return "ได้รับ Source Code"; }
    @Override protected String getDescription() { return "รุ่นพีใจดีเดินเอาไฟล์สรุปและแนวข้อสอบมาให้"; }
    @Override public double getChance() { return 0.2; }
}