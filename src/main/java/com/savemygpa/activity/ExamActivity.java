package com.savemygpa.activity;

import com.savemygpa.config.GameConfig;
import com.savemygpa.config.StatConfig;
import com.savemygpa.core.TimeSystem;
import com.savemygpa.event.EventManager;
import com.savemygpa.player.Player;
import com.savemygpa.player.StatType;

public class ExamActivity extends Activity {

    @Override
    public RequirementReason canPerform(Player player, TimeSystem timeSystem) {
        if (!timeSystem.isEnoughTime(getTimeCost())) {
            return RequirementReason.NOT_ENOUGH_TIME;
        }
        else if (!player.hasStat(StatType.MOOD, StatConfig.EXAM_MOOD_REQUIREMENT)) {
            return RequirementReason.NOT_ENOUGH_MOOD;
        }
        else if (!player.hasStat(StatType.ENERGY, StatConfig.EXAM_ENERGY_REQUIREMENT)) {
            return RequirementReason.NOT_ENOUGH_ENERGY;
        }
        return null;
    }

    @Override
    public String getFailMessage(RequirementReason reason) {
        return switch (reason) {
            case NOT_ENOUGH_TIME -> "Not enough time to take the exam";
            case NOT_ENOUGH_ENERGY -> "Too tired to take the Exam";
            case NOT_ENOUGH_MOOD -> "Not in the mood for exam";
        };
    }

    @Override
    protected void applyEffects(Player player, TimeSystem timeSystem) {
        player.changeStat(StatType.MOOD, -StatConfig.EXAM_MOOD_LOSS);
        player.changeStat(StatType.ENERGY, -StatConfig.EXAM_ENERGY_LOSS);
    }

    @Override
    protected String getName() {
        return "Exam";
    }

    @Override
    protected int getTimeCost() {
        return GameConfig.EXAM_TIME_COST;
    }

    @Override
    protected void afterActivity(Player player, TimeSystem timeSystem, EventManager eventManager) {

    }
}
