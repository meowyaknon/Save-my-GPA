package com.savemygpa.event;

import com.savemygpa.core.TimeSystem;
import com.savemygpa.player.Player;

public abstract class Event {

    public final void occurEvent(Player player, TimeSystem timeSystem, boolean activityCompleted) {

    }

    protected abstract void applyEffects(Player player, TimeSystem timeSystem);
    protected abstract String getName();
    protected abstract String getDescription();
    public abstract boolean canOccur(Player player, TimeSystem timeSystem, boolean activityCompleted);
}
