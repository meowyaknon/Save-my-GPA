package com.savemygpa.event.classroom;

import com.savemygpa.core.TimeSystem;
import com.savemygpa.event.Event;
import com.savemygpa.event.EventContext;
import com.savemygpa.event.Location;
import com.savemygpa.player.Player;
import com.savemygpa.player.StatType;

public class PerfectCompileEvent extends Event {

    @Override public boolean isVisitTriggered() { return false; }

    @Override
    public boolean canOccur(Player player, TimeSystem timeSystem, EventContext context) {
        return context.getLocation() == Location.CLASSROOM;
    }

    @Override
    protected void applyEffects(Player player, TimeSystem timeSystem) {
        player.changeStat(StatType.MOOD, 45);
        player.changeStat(StatType.INTELLIGENCE, 2);
    }

    @Override
    public String getName() { return "โปรเจค Compile ผ่านในครั้งเดียว!"; }
    @Override
    public String getDescription() { return "ความมหัศจรรย์ที่นานๆ จะเกิดที โค้ดที่เขียนมาอย่างยาวเหี้ยมดัน Run ผ่านฉลุยแบบไม่มี Error\n\n" + 
                                    "ผลกระทบ: Mood +45, Int +2\n\n"; }
    @Override public double getChance() { return 0.2; }
}