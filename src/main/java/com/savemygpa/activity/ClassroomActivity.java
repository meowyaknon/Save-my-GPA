package com.savemygpa.activity;

import com.savemygpa.config.GameConfig;
import com.savemygpa.config.StatConfig;
import com.savemygpa.core.TimeSystem;
import com.savemygpa.player.Player;
import com.savemygpa.player.StatType;

public class ClassroomActivity extends Activity {

    @Override
    protected boolean canPerform(Player player, TimeSystem timeSystem) {
        return timeSystem.isEnoughTime(GameConfig.CLASSROOM_TIME_COST)
                && player.hasStat(StatType.ENERGY, StatConfig.CLASSROOM_ENERGY_REQUIREMENT)
                && player.hasStat(StatType.MOOD, StatConfig.CLASSROOM_MOOD_REQUIREMENT);
    }

    @Override
    protected void applyEffects(Player player) {
        player.changeStat(StatType.INTELLIGENCE, StatConfig.CLASSROOM_INTELLIGENCE_GAIN);
        player.changeStat(StatType.MOOD, StatConfig.CLASSROOM_MOOD_LOSS);
        player.changeStat(StatType.ENERGY, StatConfig.CLASSROOM_ENERGY_LOSS);
    }

    @Override
    protected int getTimeCost() {
        return GameConfig.CLASSROOM_TIME_COST;
    }

    @Override
    protected String getName() {
        return "Classroom Study";
    }
}
