package com.savemygpa.activity;

import com.savemygpa.config.GameConfig;
import com.savemygpa.config.StatConfig;
import com.savemygpa.core.TimeSystem;
import com.savemygpa.event.EventManager;
import com.savemygpa.player.Player;
import com.savemygpa.player.StatType;
import com.savemygpa.player.effect.debuff.WhyDizzyDebuff;

public class ClassroomActivity extends Activity {

    @Override
    public RequirementReason canPerform(Player player, TimeSystem timeSystem) {
        if (!timeSystem.isEnoughTime(getTimeCost())) return RequirementReason.NOT_ENOUGH_TIME;
        if (!player.hasStat(StatType.MOOD,   StatConfig.CLASSROOM_MOOD_REQUIREMENT))   return RequirementReason.NOT_ENOUGH_MOOD;
        if (!player.hasStat(StatType.ENERGY, StatConfig.CLASSROOM_ENERGY_REQUIREMENT)) return RequirementReason.NOT_ENOUGH_ENERGY;
        return null;
    }

    @Override
    public String getFailMessage(RequirementReason reason) {
        return switch (reason) {
            case NOT_ENOUGH_MOOD   -> "No mood for classroom";
            case NOT_ENOUGH_ENERGY -> "No energy for classroom";
            case NOT_ENOUGH_TIME   -> "No time for classroom";
        };
    }

    @Override
    protected void applyEffects(Player player, TimeSystem timeSystem) {
        int intelligenceGain = switch (player.getStatTier(player.getStat(StatType.MOOD))) {
            case HIGH   -> StatConfig.CLASSROOM_HIGH_INTELLIGENCE_GAIN;
            case MEDIUM -> StatConfig.CLASSROOM_MEDIUM_INTELLIGENCE_GAIN;
            case LOW    -> StatConfig.CLASSROOM_LOW_INTELLIGENCE_GAIN;
        };

        player.changeIntelligenceFromEffect(intelligenceGain);
        player.changeStat(StatType.MOOD,   -StatConfig.CLASSROOM_MOOD_LOSS);
        player.changeStat(StatType.ENERGY, -StatConfig.CLASSROOM_ENERGY_LOSS);

        player.removeEffect(WhyDizzyDebuff.class);
    }

    @Override
    protected int getTimeCost() { return GameConfig.CLASSROOM_TIME_COST; }

    @Override
    protected String getName() { return "Classroom Study"; }
}