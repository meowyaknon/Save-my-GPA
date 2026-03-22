package com.savemygpa.activity;

import com.savemygpa.config.GameConfig;
import com.savemygpa.config.StatConfig;
import com.savemygpa.core.TimeSystem;
import com.savemygpa.event.EventManager;
import com.savemygpa.player.Player;
import com.savemygpa.player.StatType;

public class EatActivity extends Activity {

    @Override
    public RequirementReason canPerform(Player player, TimeSystem timeSystem) {
        if (!timeSystem.isEnoughTime(getTimeCost())) {
            return RequirementReason.NOT_ENOUGH_TIME;
        }
        return null;
    }

    @Override
    public String getFailMessage(RequirementReason reason) {
        if  (reason == RequirementReason.NOT_ENOUGH_TIME) {
            return "No time to eat";
        }
        return null;
    }

    @Override
    protected void applyEffects(Player player,  TimeSystem timeSystem) {
        player.changeStat(StatType.MOOD, StatConfig.CANTEEN_MOOD_GAIN);
        player.changeStat(StatType.ENERGY, StatConfig.CANTEEN_ENERGY_GAIN);
    }

    @Override
    protected int getTimeCost() {
        return GameConfig.CANTEEN_TIME_COST;
    }

    @Override
    protected void afterActivity(Player player, TimeSystem timeSystem,  EventManager eventManager) { }

    @Override
    protected String getName() {
        return "Eating";
    }
}