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

    @Override
    public String getName() { return "บัตร นศ. หายในห้องสอบ"; }
    @Override
    public String getDescription() { return "ตกใจสุดขีดต้องรีบไปทำเรื่องหน้าห้องสอบ เสียพลังงานและขวัญกำลังใจก่อนเริ่มทำข้อสอบ\n\n" + 
                                    "ผลกระทบ: Mood -30, Energy -6"; }
    @Override public double getChance() { return 0.2; }
}