package com.savemygpa.activity;

import com.savemygpa.config.GameConfig;
import com.savemygpa.config.StatConfig;
import com.savemygpa.core.TimeSystem;
import com.savemygpa.player.MoodTier;
import com.savemygpa.player.Player;
import com.savemygpa.player.StatType;

public class AuditoriumActivity extends Activity {

    @Override
    public RequirementReason canPerform(Player player, TimeSystem timeSystem) {
        if (!timeSystem.isEnoughTime(getTimeCost())) {
            return  RequirementReason.NOT_ENOUGH_TIME;
        }
        else if (!player.hasStat(StatType.MOOD, StatConfig.AUDITORIUM_MOOD_REQUIREMENT)) {
            return  RequirementReason.NOT_ENOUGH_MOOD;
        }
        else if (!player.hasStat(StatType.ENERGY, StatConfig.AUDITORIUM_ENERGY_REQUIREMENT)) {
            return  RequirementReason.NOT_ENOUGH_ENERGY;
        }
        return null;
    }

    @Override
    public String getFailMessage(RequirementReason reason) {
        return switch (reason) {
            case NOT_ENOUGH_MOOD -> "No mood for auditorium";
            case NOT_ENOUGH_ENERGY -> "No energy for auditorium";
            case NOT_ENOUGH_TIME -> "No time for auditorium";
        };
    }

    @Override
    protected void applyEffects(Player player,  TimeSystem timeSystem) {
        player.changeStat(StatType.MOOD, StatConfig.AUDITORIUM_MOOD_GAIN);
        player.changeStat(StatType.ENERGY, -StatConfig.AUDITORIUM_ENERGY_LOSS);
    }

    @Override
    protected int getTimeCost() {
        return GameConfig.AUDITORIUM_TIME_COST;
    }

    @Override
    protected void afterActivity(Player player, TimeSystem timeSystem) { }

    @Override
    protected String getName() {
        return "Auditorium Relax";
    }

}
