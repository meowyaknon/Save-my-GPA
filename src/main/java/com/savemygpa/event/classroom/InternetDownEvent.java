package com.savemygpa.event.classroom;

import com.savemygpa.core.TimeSystem;
import com.savemygpa.event.Event;
import com.savemygpa.event.EventContext;
import com.savemygpa.event.Location;
import com.savemygpa.player.Player;
import com.savemygpa.player.StatType;
import com.savemygpa.player.effect.debuff.NoStackOverflowDebuff;

public class InternetDownEvent extends Event {

    @Override
    public boolean canOccur(Player player, TimeSystem timeSystem, EventContext context) {
        return context.getLocation() == Location.CLASSROOM
                && !player.hasEffect(NoStackOverflowDebuff.class);
    }

    @Override
    protected void applyEffects(Player player, TimeSystem timeSystem) {
        player.changeStat(StatType.MOOD, -25);
        player.addEffect(new NoStackOverflowDebuff());
        System.out.println("[Event] Internet ล่ม! เน็ตล่ม กู้โค้ดไม่ได้ ค้นหาวิธีแก้ Error ไม่ได้ ทำให้การเรียนรู้วันนี้ติดขัดไปหมด");
    }

    @Override
    protected String getName() { return "Internet ล่ม"; }

    @Override
    protected String getDescription() {
        return "เน็ตล่ม กู้โค้ดไม่ได้ ค้นหาวิธีแก้ Error ไม่ได้ ทำให้การเรียนรู้วันนี้ติดขัดไปหมด";
    }

    @Override
    public double getChance() { return 0.3; }
}