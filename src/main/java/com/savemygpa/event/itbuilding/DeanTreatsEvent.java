package com.savemygpa.event.itbuilding;

import com.savemygpa.core.TimeSystem;
import com.savemygpa.event.Event;
import com.savemygpa.event.EventContext;
import com.savemygpa.event.Location;
import com.savemygpa.player.Player;
import com.savemygpa.player.StatType;

public class DeanTreatsEvent extends Event {

    @Override
    public boolean canOccur(Player player, TimeSystem timeSystem, EventContext context) {
        return context.getLocation() == Location.IT_BUILDING;
    }

    @Override
    protected void applyEffects(Player player, TimeSystem timeSystem) {
        player.changeStat(StatType.MOOD, 40);
        player.changeStat(StatType.ENERGY, 10); // will be clamped to max by Player
        System.out.println("[Event] ท่านคณบดีเลี้ยงไอติม! เดินผ่านโถงคณะพอดี เจอท่านคณบดีเหมาไอติมไผ่XXXมาแจกเด็กๆ พลังใจเต็มเปี่ยมพร้อมปั้นโปรเจค");
    }

    @Override
    protected String getName() { return "ท่านคณบดีเลี้ยงไอติม"; }

    @Override
    protected String getDescription() {
        return "เดินผ่านโถงคณะพอดี เจอท่านคณบดีเหมาไอติมไผ่XXXมาแจกเด็กๆ พลังใจเต็มเปี่ยมพร้อมปั้นโปรเจค";
    }

    @Override
    public double getChance() { return 0.15; } // rare and powerful
}