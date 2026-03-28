package com.savemygpa.event.canteen;

import com.savemygpa.core.TimeSystem;
import com.savemygpa.event.Event;
import com.savemygpa.event.EventContext;
import com.savemygpa.event.Location;
import com.savemygpa.player.Player;
import com.savemygpa.player.StatType;

public class NoSeatEvent extends Event {

    @Override public boolean isVisitTriggered() { return false; }

    @Override
    public boolean canOccur(Player player, TimeSystem timeSystem, EventContext context) {
        return context.getLocation() == Location.CANTEEN;
    }

    @Override
    protected void applyEffects(Player player, TimeSystem timeSystem) {
        timeSystem.advanceTime(1);
        player.changeStat(StatType.MOOD, -20);
    }

    @Override
    public String getName() { return "โรงอาหารแน่นยิ่งกว่าปลาป๋อง"; }
    @Override
    public String getDescription() { return "ช่างน่าสงสาร ไม่เหลือที่นั่งให้คุณแล้ว\n\n" + 
                                    "ผลกระทบ: Time -1, Mood -20"; }
    @Override public double getChance() { return 0.3; }
}