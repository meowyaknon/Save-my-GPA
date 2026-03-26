package com.savemygpa.event.coworkingspace;

import com.savemygpa.core.TimeSystem;
import com.savemygpa.event.Event;
import com.savemygpa.event.EventContext;
import com.savemygpa.event.Location;
import com.savemygpa.player.Player;
import com.savemygpa.player.StatType;
import com.savemygpa.player.effect.debuff.WhyDizzyDebuff;

public class HurtHeadEvent extends Event {

    @Override public boolean isVisitTriggered() { return false; }

    @Override
    public boolean canOccur(Player player, TimeSystem timeSystem, EventContext context) {
        return context.getLocation() == Location.COWORKING
                && !player.hasEffect(WhyDizzyDebuff.class);
    }

    @Override
    protected void applyEffects(Player player, TimeSystem timeSystem) {
        player.changeStat(StatType.INTELLIGENCE, -5);
        player.addEffect(new WhyDizzyDebuff());
    }

    @Override protected String getName() { return "โป๊กกระได"; }
    @Override protected String getDescription() { return "ก้าวเข้ามาแล้วโขกบรรไดเจ้ากรรม มึนตึบเลย"; }
    @Override public double getChance() { return 0.25; }
}