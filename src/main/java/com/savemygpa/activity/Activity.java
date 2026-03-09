package com.savemygpa.activity;

import com.savemygpa.core.TimeSystem;
import com.savemygpa.player.Player;

public interface Activity {

    void execute(Player player, TimeSystem timeSystem);

}
