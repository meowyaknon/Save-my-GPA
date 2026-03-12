package com.savemygpa.activity;

import com.savemygpa.config.GameConfig;
import com.savemygpa.config.StatConfig;
import com.savemygpa.core.TimeSystem;
import com.savemygpa.player.Player;
import com.savemygpa.player.StatType;

public class CoworkingStudyActivity extends Activity {

    @Override
    protected boolean canPerform(Player player, TimeSystem timeSystem) {
        return timeSystem.isEnoughTime(GameConfig.REVIEW_TIME_COST)
                && player.hasStat(StatType.MOOD, StatConfig.REVIEW_MOOD_REQUIREMENT)
                && player.hasStat(StatType.ENERGY, StatConfig.REVIEW_ENERGY_REQUIREMENT);
    }

    @Override
    protected void applyEffects(Player player) {
        player.changeStat(StatType.INTELLIGENCE, StatConfig.REVIEW_INTELLIGENCE_GAIN);
        player.changeStat(StatType.MOOD, StatConfig.REVIEW_MOOD_LOSS);
        player.changeStat(StatType.ENERGY, StatConfig.REVIEW_ENERGY_LOSS);
    }

    @Override
    protected int getTimeCost() {
        return GameConfig.REVIEW_TIME_COST;
    }

    @Override
    protected String getName() {
        return "Reviewing";
    }
}
