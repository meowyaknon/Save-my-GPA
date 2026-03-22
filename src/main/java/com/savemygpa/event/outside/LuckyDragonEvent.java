package com.savemygpa.event.outside;

import com.savemygpa.core.TimeOfDay;
import com.savemygpa.core.TimeSystem;
import com.savemygpa.event.Event;
import com.savemygpa.event.EventContext;
import com.savemygpa.event.Location;
import com.savemygpa.player.Player;
import com.savemygpa.player.StatType;
import com.savemygpa.player.effect.buff.AuraOfLuckBuff;

public class LuckyDragonEvent extends Event {

    @Override
    protected void applyEffects(Player player, TimeSystem timeSystem) {
        player.changeStat(StatType.MOOD, +20);
        player.addEffect(new AuraOfLuckBuff());
    }

    @Override
    protected String getName() {
        return "น้องเงินทอง";
    }

    @Override
    protected String getDescription() {
        return "A golden cat naps in the sun. Today feels lucky!";
    }

    @Override
    public boolean canOccur(Player player, TimeSystem timeSystem, EventContext context) {
        return context.getLocation() == Location.OUTSIDE
                && timeSystem.getTimeOfDay() == TimeOfDay.AFTERNOON;
    }

    @Override
    public double getChance() {
        return 0.4;
    }
}