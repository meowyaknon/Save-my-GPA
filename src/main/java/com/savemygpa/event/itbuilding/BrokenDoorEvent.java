package com.savemygpa.event.itbuilding;

import com.savemygpa.core.TimeSystem;
import com.savemygpa.event.Event;
import com.savemygpa.event.EventContext;
import com.savemygpa.event.Location;
import com.savemygpa.player.Player;
import com.savemygpa.player.StatType;

public class BrokenDoorEvent extends Event {

    @Override
    public boolean canOccur(Player player, TimeSystem timeSystem, EventContext context) {
        return context.getLocation() == Location.IT_BUILDING;
    }

    @Override
    protected void applyEffects(Player player, TimeSystem timeSystem) {
        player.changeStat(StatType.MOOD, -15);
        timeSystem.advanceTime(1); // time -1
        System.out.println("[Event] ประตูอัตโนมัติไม่เปิด! เสียเวลาและอารมณ์ไปกับประตูแสนซน");
    }

    @Override
    protected String getName() { return "ประตูอัตโนมัติไม่เปิด"; }

    @Override
    protected String getDescription() {
        return "คุณเดินตรงดิ้งไปด้วยความรีบ แต่เซนเซอร์ประตูเจ้ากรรมดันไม่ทำงาน ชนประตูกระดังปัง!";
    }

    @Override
    public double getChance() { return 0.25; }
}