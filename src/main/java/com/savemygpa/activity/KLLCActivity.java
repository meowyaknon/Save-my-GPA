package com.savemygpa.activity;

import com.savemygpa.config.GameConfig;
import com.savemygpa.config.StatConfig;
import com.savemygpa.core.TimeSystem;
import com.savemygpa.event.EventManager;
import com.savemygpa.player.Player;
import com.savemygpa.player.StatType;

public class KLLCActivity extends Activity {

    @Override
    public RequirementReason canPerform(Player player, TimeSystem timeSystem) {
        if (!timeSystem.isEnoughTime(getTimeCost())) {
            return RequirementReason.NOT_ENOUGH_TIME;
        }
        else if (!player.hasStat(StatType.MOOD, StatConfig.KLLC_MOOD_REQUIREMENT)) {
            return RequirementReason.NOT_ENOUGH_MOOD;
        }
        else if (!player.hasStat(StatType.ENERGY, StatConfig.KLLC_ENERGY_REQUIREMENT)) {
            return RequirementReason.NOT_ENOUGH_ENERGY;
        }
        return null;
    }

    @Override
    public String getFailMessage(RequirementReason reason) {
        return switch (reason) {
            case NOT_ENOUGH_MOOD -> "ฉันไม่มีอารมณ์ที่จะไป KLLC";
            case NOT_ENOUGH_ENERGY -> "ฉันไม่มีแรงพอที่จะไป KLLC";
            case NOT_ENOUGH_TIME -> "ฉันเหลือเวลาไม่พอที่จะไป KLLC";
        };
    }

    @Override
    protected void applyEffects(Player player, TimeSystem timeSystem) {

        int intelligenceGain  = switch (player.getStatTier(player.getStat(StatType.MOOD))) {
            case HIGH -> StatConfig.KLLC_HIGH_INTELLIGENCE_GAIN;
            case MEDIUM ->  StatConfig.KLLC_MEDIUM_INTELLIGENCE_GAIN;
            case LOW -> StatConfig.KLLC_LOW_INTELLIGENCE_GAIN;
        };

        player.changeStat(StatType.INTELLIGENCE, intelligenceGain);
        player.changeStat(StatType.MOOD, -StatConfig.KLLC_MOOD_LOSS);
        player.changeStat(StatType.ENERGY, -StatConfig.KLLC_ENERGY_LOSS);
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
