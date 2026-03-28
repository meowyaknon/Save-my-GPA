package com.savemygpa.event.itbuilding;

import com.savemygpa.core.TimeSystem;
import com.savemygpa.event.Event;
import com.savemygpa.event.EventContext;
import com.savemygpa.event.Location;
import com.savemygpa.player.Player;
import com.savemygpa.player.StatType;

/** VISIT-triggered — fires on arriving at the IT building. */
public class DeanTreatsEvent extends Event {

    @Override public boolean isVisitTriggered() { return true; }

    @Override
    public boolean canOccur(Player player, TimeSystem timeSystem, EventContext context) {
        return context.getLocation() == Location.IT_BUILDING;
    }

    @Override
    protected void applyEffects(Player player, TimeSystem timeSystem) {
        player.changeStat(StatType.MOOD, 40);
        player.changeStat(StatType.ENERGY, 10);
    }

    @Override
    public String getName() { return "ท่านคณบดีใจดี เลี้ยงไอติม"; }
    @Override
    public String getDescription() { return "เดินผ่านโถงคณะพอดี เจอท่านคณบดีเหมาไอติมไผ่XXXมาแจกเด็กๆ พลังใจเต็มเปี่ยมพร้อมปั่นโปรเจค\n\n" + 
                                    "ผลกระทบ: Mood +40, Energy +10"; }
    @Override public double getChance() { return 0.07; }
}