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
        return (day >= 5 && day <= 6) || (day >= 12 && day <= 13);
    }

    @Override
    protected void applyEffects(Player player, TimeSystem timeSystem) {
        hasOccurred = true;
        player.changeStat(StatType.INTELLIGENCE, 25);
    }

    @Override
    public String getName() { return "ได้รับ \"Source Code\" จากรุ่นพี่"; }
    @Override
    public String getDescription() { return "รุ่นพี่ใจดีเดินเอาไฟล์สรุปและแนวข้อสอบมาให้ประหนึ่งแสงสว่างที่ปลายอุโมงค์\n\n" +
                                        "ผลกระทบ: Int +15";
                                    }
    @Override public double getChance() { return 0.1; }
}