package com.savemygpa.event.canteen;

import com.savemygpa.core.TimeSystem;
import com.savemygpa.event.Event;
import com.savemygpa.event.EventContext;
import com.savemygpa.event.Location;
import com.savemygpa.player.Player;
import com.savemygpa.player.StatType;

public class NoSeatEvent extends Event {

    @Override
    public boolean canOccur(Player player, TimeSystem timeSystem, EventContext context) {
        return context.getLocation() == Location.CANTEEN;
    }

    @Override
    protected void applyEffects(Player player, TimeSystem timeSystem) {
        timeSystem.advanceTime(1); // time -1
        player.changeStat(StatType.MOOD, -20);
        System.out.println("[Event] โรงอาหารแน่น! ช่างน่าสงสาร ไม่เหลือที่นั่งให้คนอย่างคุณแล้ว รอตั้งนานพอคนหาย ข้าวก็หมด ชีวิตช่างน่าเศร้า");
    }

    @Override
    protected String getName() { return "โรงอาหารแน่นมาก"; }

    @Override
    protected String getDescription() {
        return "ช่างน่าสงสาร ไม่เหลือที่นั่งให้คนอย่างคุณแล้ว รอตั้งนานพอคนหาย ข้าวก็หมด ชีวิตช่างน่าเศร้า";
    }

    @Override
    public double getChance() { return 0.3; }
}