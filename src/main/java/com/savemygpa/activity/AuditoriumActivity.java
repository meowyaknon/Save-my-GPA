package com.savemygpa.activity;

import com.savemygpa.config.GameConfig;
import com.savemygpa.config.StatConfig;
import com.savemygpa.core.TimeSystem;
import com.savemygpa.player.Player;
import com.savemygpa.player.StatType;

public class AuditoriumActivity extends Activity {

    @Override
    protected boolean canPerform(Player player, TimeSystem timeSystem) {
        return timeSystem.isEnoughTime(GameConfig.AUDITORIUM_TIME_COST)
                && player.hasStat(StatType.MOOD, StatConfig.AUDITORIUM_MOOD_REQUIREMENT)
                && player.hasStat(StatType.ENERGY, StatConfig.AUDITORIUM_ENERGY_REQUIREMENT);
    }

    @Override
    protected void applyEffects(Player player) {
        player.changeStat(StatType.MOOD, StatConfig.AUDITORIUM_MOOD_GAIN);
        player.changeStat(StatType.ENERGY, -StatConfig.AUDITORIUM_ENERGY_LOSS);
    }

    @Override
    protected int getTimeCost() {
        return GameConfig.AUDITORIUM_TIME_COST;
    }

    @Override
    protected String getName() {
        return "Auditorium Relax";
    }

}
