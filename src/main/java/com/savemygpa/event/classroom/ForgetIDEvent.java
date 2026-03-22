package com.savemygpa.event.classroom;

import com.savemygpa.core.TimeSystem;
import com.savemygpa.event.Event;
import com.savemygpa.event.EventContext;
import com.savemygpa.event.Location;
import com.savemygpa.player.Player;
import com.savemygpa.player.StatType;

public class ForgetIDEvent extends Event {

    @Override
    public boolean canOccur(Player player, TimeSystem timeSystem, EventContext context) {
        if (context.getLocation() != Location.CLASSROOM) return false;

        int day = timeSystem.getCurrentDay();
        boolean isExamDay = (day == 6 || day == 7 || day == 11 || day == 12);
        return isExamDay && context.isAfterActivity();
    }

    @Override
    protected void applyEffects(Player player, TimeSystem timeSystem) {
        player.changeStat(StatType.MOOD, -30);
        player.changeStat(StatType.ENERGY, -6);
        System.out.println("[Event] บัตร นศ. หาย! ตกใจสุดขีดต้องรีบไปทำเรื่องหน้าห้องสอบ เสียพลังงานและขวัญกำลังใจก่อนเริ่มทำข้อสอบ");
    }

    @Override
    protected String getName() { return "บัตร นศ. หาย"; }

    @Override
    protected String getDescription() {
        return "ตกใจสุดขีดต้องรีบไปทำเรื่องหน้าห้องสอบ เสียพลังงานและขวัญกำลังใจก่อนเริ่มทำข้อสอบ";
    }

    @Override
    public double getChance() { return 0.25; }
}