package com.savemygpa.event.classroom;

import com.savemygpa.core.TimeSystem;
import com.savemygpa.event.Event;
import com.savemygpa.event.EventContext;
import com.savemygpa.event.Location;
import com.savemygpa.player.Player;
import com.savemygpa.player.StatType;

public class PerfectCompileEvent extends Event {

    @Override
    public boolean canOccur(Player player, TimeSystem timeSystem, EventContext context) {
        return context.getLocation() == Location.CLASSROOM;
    }

    @Override
    protected void applyEffects(Player player, TimeSystem timeSystem) {
        player.changeStat(StatType.MOOD, 45);
        player.changeStat(StatType.INTELLIGENCE, 2);
        System.out.println("[Event] Compile ผ่าน! ความมหัศจรรย์ที่น่านันๆ จะเกิดที่ โค้ดที่เขียนมาอย่างยาวเที้ยมดัน Run ผ่านฉลุยแบบไม่มี Error");
    }

    @Override
    protected String getName() { return "Compile ผ่านในครั้งเดียว"; }

    @Override
    protected String getDescription() {
        return "ความมหัศจรรย์ที่น่านันๆ จะเกิดที่ โค้ดที่เขียนมาอย่างยาวเที้ยมดัน Run ผ่านฉลุยแบบไม่มี Error ความฟินระดับนี้ทำให้ค่า Mood พุ่งทะลุหลอด";
    }

    @Override
    public double getChance() { return 0.2; }
}