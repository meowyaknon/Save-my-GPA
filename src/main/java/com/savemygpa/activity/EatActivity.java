package com.savemygpa.activity;

import com.savemygpa.config.GameConfig;
import com.savemygpa.config.StatConfig;
import com.savemygpa.core.TimeSystem;
import com.savemygpa.player.Player;
import com.savemygpa.player.StatType;

public class EatActivity extends Activity {

    @Override
    protected boolean canPerform(Player player, TimeSystem timeSystem) {
        return timeSystem.isEnoughTime(GameConfig.CAFETERIA_TIME_COST);
    }

    @Override
    protected void applyEffects(Player player) {
        player.changeStat(StatType.ENERGY, StatConfig.CAFETERIA_ENERGY_GAIN);
    }

    @Override
    protected int getTimeCost() {
        return GameConfig.CAFETERIA_TIME_COST;
    }

    @Override
    protected String getName() {
        return "Eating";
    }
}
