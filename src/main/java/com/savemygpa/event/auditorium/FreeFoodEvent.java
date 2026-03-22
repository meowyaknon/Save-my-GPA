package com.savemygpa.event.auditorium;

import com.savemygpa.core.TimeSystem;
import com.savemygpa.event.Event;
import com.savemygpa.event.EventContext;
import com.savemygpa.event.Location;
import com.savemygpa.player.Player;
import com.savemygpa.player.StatType;
import com.savemygpa.player.effect.buff.IndefatigableBuff;

public class FreeFoodEvent extends Event {

    @Override
    public boolean canOccur(Player player, TimeSystem timeSystem, EventContext context) {
        return context.getLocation() == Location.AUDITORIUM
                && !player.hasEffect(IndefatigableBuff.class);
    }

    @Override
    protected void applyEffects(Player player, TimeSystem timeSystem) {
        player.changeStat(StatType.ENERGY, 5);
        player.addEffect(new IndefatigableBuff());
        System.out.println("[Event] ไก่ทอดฟรี! สวรรค์มาโปรด รุ่นพี่แสนใจดีสั่งไก่ทอดผู้พันมาเลี้ยง ทั้งอร่อย อิ่มกาย และอิ่มใจ");
    }

    @Override
    protected String getName() { return "รุ่นพี่เลี้ยงไก่ทอด"; }

    @Override
    protected String getDescription() {
        return "สวรรค์มาโปรด รุ่นพี่แสนใจดีสั่งไก่ทอดผู้พันมาเลี้ยง ทั้งอร่อย อิ่มกาย และอิ่มใจ อยากมาแบบนี้อีกจัง";
    }

    @Override
    public double getChance() { return 0.3; }
}