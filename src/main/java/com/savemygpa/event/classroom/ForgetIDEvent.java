package com.savemygpa.event.classroom;

import com.savemygpa.core.TimeSystem;
import com.savemygpa.event.Event;
import com.savemygpa.event.EventContext;
import com.savemygpa.event.Location;
import com.savemygpa.player.Player;
import com.savemygpa.player.StatType;

public class ForgetIDEvent extends Event {

    @Override public boolean isVisitTriggered() { return false; }

    @Override
    public boolean canOccur(Player player, TimeSystem timeSystem, EventContext context) {
        if (context.getLocation() != Location.CLASSROOM) return false;
        int day = timeSystem.getCurrentDay();
        return day == 6 || day == 7 || day == 13 || day == 14;
    }

    @Override
    protected void applyEffects(Player player, TimeSystem timeSystem) {
        player.changeStat(StatType.MOOD, -30);
        player.changeStat(StatType.ENERGY, -6);
    }

    @Override protected String getName() { return "บัตร นศ. หาย"; }
    @Override protected String getDescription() { return "ตกใจสุดขีด ต้องรีบไปทำเรื่องหน้าห้องสอบ เสียพลังงานและขวัญกำลังใจ"; }
    @Override public double getChance() { return 0.25; }
}