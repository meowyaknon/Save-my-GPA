package com.savemygpa.activity;

import com.savemygpa.core.TimeSystem;
import com.savemygpa.player.Player;

public abstract class Activity {

    public final void performActivity(Player player, TimeSystem timeSystem) {
        if (!canPerform(player, timeSystem)) {
            System.out.println("You can't perform this activity.");
            return;
        }

        applyEffects(player);

        timeSystem.advanceTime(getTimeCost());

        System.out.println(getName() + " completed.");
    }

    protected abstract boolean canPerform(Player player, TimeSystem timeSystem);
    protected abstract void applyEffects(Player player);
    protected abstract int getTimeCost();
    protected abstract String getName();
}
