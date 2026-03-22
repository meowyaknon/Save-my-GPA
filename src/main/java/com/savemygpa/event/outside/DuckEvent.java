package com.savemygpa.event.outside;

import com.savemygpa.core.TimeOfDay;
import com.savemygpa.core.TimeSystem;
import com.savemygpa.event.Event;
import com.savemygpa.event.EventContext;
import com.savemygpa.event.Location;
import com.savemygpa.player.Player;
import com.savemygpa.player.StatType;

public class DuckEvent extends Event {

    @Override
    public boolean canOccur(Player player, TimeSystem timeSystem, EventContext context) {
        return context.getLocation() == Location.OUTSIDE
                && timeSystem.getTimeOfDay() == TimeOfDay.MORNING;
    }

    @Override
    protected void applyEffects(Player player, TimeSystem timeSystem) {
        timeSystem.advanceTime(1); // time -1
        player.changeStat(StatType.MOOD, 15);
        System.out.println("[Event] กองทัพเป็ดบุกเลน! หยุดรอจนกว่าขบวนจะหมด ได้ความน่ารักปลอบใจ");
    }

    @Override
    protected String getName() { return "กองทัพเป็ด"; }

    @Override
    protected String getDescription() {
        return "น้องเป็ดเดินพาเหรดข้ามถนนแบบไม่รีบเร่ง คุณต้องหยุดรอจนกว่าขบวนจะหมด แต่ก็ได้ความน่ารักปลอบใจ";
    }

    @Override
    public double getChance() { return 0.35; }
}