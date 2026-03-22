package com.savemygpa.event.canteen;

import com.savemygpa.core.TimeSystem;
import com.savemygpa.event.Event;
import com.savemygpa.event.EventContext;
import com.savemygpa.event.Location;
import com.savemygpa.player.Player;
import com.savemygpa.player.StatType;
import com.savemygpa.player.effect.buff.SeniorNoteBuff;

public class FreeMealEvent extends Event {

    @Override
    public boolean canOccur(Player player, TimeSystem timeSystem, EventContext context) {
        return context.getLocation() == Location.CANTEEN
                && !player.hasEffect(SeniorNoteBuff.class);
    }

    @Override
    protected void applyEffects(Player player, TimeSystem timeSystem) {
        player.changeStat(StatType.ENERGY, 10); // clamped to max by Player
        player.addEffect(new SeniorNoteBuff());
        System.out.println("[Event] เจอพี่รหัส! บังเอิญเจอพี่รหัส พี่เลยเลี้ยงข้าวชุดใหญ่พร้อมแนะแนวทางทำ Assignment ให้ง่ายขึ้น");
    }

    @Override
    protected String getName() { return "เจอพี่รหัสสายซัพ"; }

    @Override
    protected String getDescription() {
        return "บังเอิญเจอพี่รหัส พี่เลยเลี้ยงข้าวชุดใหญ่พร้อมแนะแนวทางทำ Assignment ให้ง่ายขึ้น";
    }

    @Override
    public double getChance() { return 0.25; }
}