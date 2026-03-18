package com.savemygpa.activity;

import com.savemygpa.core.TimeSystem;
import com.savemygpa.player.Player;
import com.savemygpa.player.StatType;

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
        player.changeStat(StatType.MOOD, 10 + (timeSystem.getCurrentHour() * 5));
        player.changeStat(StatType.ENERGY, 3 + ((int) player.getStat(StatType.MOOD) / 20) + timeSystem.getCurrentHour());
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
    protected void afterActivity(Player player, TimeSystem timeSystem) {
        timeSystem.endDay();
        System.out.println("The day has ended. A new day begins.");
    }
}