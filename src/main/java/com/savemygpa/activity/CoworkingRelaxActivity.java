package com.savemygpa.activity;

import com.savemygpa.config.GameConfig;
import com.savemygpa.config.StatConfig;
import com.savemygpa.core.TimeSystem;
import com.savemygpa.player.Player;
import com.savemygpa.player.StatType;

public class CoworkingRelaxActivity extends Activity {

    @Override
    protected boolean canPerform(Player player, TimeSystem timeSystem) {
        return timeSystem.isEnoughTime(GameConfig.RELAX_TIME_COST);
    }

    @Override
    protected void applyEffects(Player player) {
        player.changeStat(StatType.MOOD, StatConfig.RELAX_MOOD_GAIN);
        player.changeStat(StatType.ENERGY, StatConfig.RELAX_ENERGY_GAIN);
    }

    @Override
    protected int getTimeCost() {
        return GameConfig.RELAX_TIME_COST;
    }

    @Override
    protected String getName() {
        return "Relaxing";
    }
}
