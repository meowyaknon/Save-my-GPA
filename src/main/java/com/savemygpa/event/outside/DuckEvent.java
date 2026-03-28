package com.savemygpa.event.outside;

import com.savemygpa.core.TimeOfDay;
import com.savemygpa.core.TimeSystem;
import com.savemygpa.event.Event;
import com.savemygpa.event.EventContext;
import com.savemygpa.event.Location;
import com.savemygpa.player.Player;
import com.savemygpa.player.StatType;

public class DuckEvent extends Event {

    @Override public boolean isVisitTriggered() { return true; }

    @Override
    public boolean canOccur(Player player, TimeSystem timeSystem, EventContext context) {
        return context.getLocation() == Location.OUTSIDE
                && timeSystem.getTimeOfDay() == TimeOfDay.MORNING;
    }

    @Override
    protected void applyEffects(Player player, TimeSystem timeSystem) {
        timeSystem.advanceTime(1);
        player.changeStat(StatType.MOOD, 15);
    }

    @Override
    public String getName() { return "กองทัพ \"เป็ด\" บุกเลนจักรยาน"; }
    @Override
    public String getDescription() { return "น้องเป็ดเดินพาเหรดข้ามถนนแบบไม่รีบเร่ง คุณต้องหยุดรอจนกว่าขบวนจะหมด ทำให้เข้าเรียนสายนิดหน่อยแต่ก็ได้ความน่ารักปลอบใจ\n\n" + 
                                    "ผลกระทบ: Time -1, Mood +15"; }
    @Override public double getChance() { return 0.35; }
}