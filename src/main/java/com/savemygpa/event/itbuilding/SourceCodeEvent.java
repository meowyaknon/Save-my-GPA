package com.savemygpa.event.itbuilding;

import com.savemygpa.config.GameConfig;
import com.savemygpa.core.TimeSystem;
import com.savemygpa.event.Event;
import com.savemygpa.event.EventContext;
import com.savemygpa.event.Location;
import com.savemygpa.player.Player;
import com.savemygpa.player.StatType;

public class SourceCodeEvent extends Event {

    private boolean hasOccurred = false;

    @Override
    public boolean canOccur(Player player, TimeSystem timeSystem, EventContext context) {
        if (hasOccurred) return false;
        if (context.getLocation() != Location.IT_BUILDING) return false;

        int day = timeSystem.getCurrentDay();
        boolean isNormalDay = (day >= 1 && day <= 5) || (day >= 8 && day <= 10);
        return isNormalDay;
    }

    @Override
    protected void applyEffects(Player player, TimeSystem timeSystem) {
        hasOccurred = true;
        player.changeStat(StatType.INTELLIGENCE, 25);
        System.out.println("[Event] ได้รับ Source Code! รุ่นพีใจดีเดินเอาไฟล์สรุปและแนวข้อสอบมาให้ ประหนึ่งแสงสว่างที่ปลายอุโมงค์");
    }

    @Override
    protected String getName() { return "ได้รับ Source Code"; }

    @Override
    protected String getDescription() {
        return "รุ่นพีใจดีเดินเอาไฟล์สรุปและแนวข้อสอบมาให้ ประหนึ่งแสงสว่างที่ปลายอุโมงค์";
    }

    @Override
    public double getChance() { return 0.2; }
}