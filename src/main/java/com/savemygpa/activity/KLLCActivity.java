package com.savemygpa.activity;

import com.savemygpa.config.GameConfig;
import com.savemygpa.config.StatConfig;
import com.savemygpa.core.TimeSystem;
import com.savemygpa.player.Player;
import com.savemygpa.player.StatType;

public class KLLCActivity extends Activity {

    @Override
    protected boolean canPerform(Player player, TimeSystem timeSystem) {
        return timeSystem.isEnoughTime(GameConfig.KLLC_TIME_COST)
                && player.hasStat(StatType.MOOD, StatConfig.KLLC_MOOD_REQUIREMENT)
                && player.hasStat(StatType.ENERGY, StatConfig.KLLC_ENERGY_REQUIREMENT);
    }

    @Override
    protected void applyEffects(Player player) {
        player.changeStat(StatType.INTELLIGENCE, StatConfig.KLLC_INTELLIGENCE_GAIN);
        player.changeStat(StatType.MOOD, StatConfig.KLLC_MOOD_GAIN);
    }

    @Override
    protected int getTimeCost() {
        return GameConfig.KLLC_TIME_COST;
    }

    @Override
    protected String getName() {
        return "Studying";
    }
}
