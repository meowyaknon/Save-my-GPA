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
        return null;
    }

    @Override
    public String getFailMessage(RequirementReason reason) {
        return "ฉันเหลือเวลาไม่พอที่จะทำข้อสอบ";
    }

    @Override
    protected void applyEffects(Player player, TimeSystem timeSystem) {
    }

    @Override
    protected String getName() {
        return "Exam";
    }

    @Override
    protected int getTimeCost() {
        return GameConfig.EXAM_TIME_COST;
    }

}
