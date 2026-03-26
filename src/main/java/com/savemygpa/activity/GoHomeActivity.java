package com.savemygpa.activity;

import com.savemygpa.core.TimeSystem;
import com.savemygpa.event.EventManager;
import com.savemygpa.player.Player;

public class GoHomeActivity extends Activity {

    @Override
    public RequirementReason canPerform(Player player, TimeSystem timeSystem) {
        return null;
    }

    @Override
    public String getFailMessage(RequirementReason reason) {
        return null;
    }

    @Override
    protected void applyEffects(Player player, TimeSystem timeSystem) {
    }

    @Override
    protected int getTimeCost() {
        return 0;
    }

    @Override
    protected String getName() {
        return "Going Home";
    }

    @Override
    protected void afterActivity(Player player, TimeSystem timeSystem, EventManager eventManager) {
    }
}