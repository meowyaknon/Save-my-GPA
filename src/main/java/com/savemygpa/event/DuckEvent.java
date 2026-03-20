package com.savemygpa.event;

import com.savemygpa.core.TimeOfDay;
import com.savemygpa.core.TimeSystem;
import com.savemygpa.player.Player;

public class DuckEvent extends Event {

    @Override
    public boolean canOccur(Player player, TimeSystem timeSystem, boolean activityCompleted) {
        return timeSystem.getTimeOfDay() == TimeOfDay.MORNING
                && activityCompleted;
    }

    @Override
    protected void applyEffects(Player player, TimeSystem timeSystem) {

    }

    @Override
    protected String getName() {
        return "";
    }

}
