package com.savemygpa.activity;

import com.savemygpa.core.TimeSystem;
import com.savemygpa.event.Event;
import com.savemygpa.player.Player;

public class ExamActivity extends Event {

    @Override
    public RequirementReason canPerform(Player player, TimeSystem timeSystem) {
        if (!timeSystem.isEnoughTime())
    }

}
