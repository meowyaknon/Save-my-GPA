package com.savemygpa.activity;

import com.savemygpa.core.TimeSystem;
import com.savemygpa.event.EventManager;
import com.savemygpa.player.Player;

public abstract class Activity {

    public final void performActivity(Player player, TimeSystem timeSystem) {

        applyEffects(player,  timeSystem);

        timeSystem.advanceTime(getTimeCost());

        System.out.println(getName() + " completed.");
    }

    protected abstract void applyEffects(Player player, TimeSystem timeSystem);
    protected abstract int getTimeCost();
    protected abstract String getName();
    public abstract RequirementReason canPerform(Player player, TimeSystem timeSystem);
    public abstract String getFailMessage(RequirementReason reason);
}
