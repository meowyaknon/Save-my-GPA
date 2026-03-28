package com.savemygpa.event.canteen;

import com.savemygpa.core.TimeSystem;
import com.savemygpa.event.Event;
import com.savemygpa.event.EventContext;
import com.savemygpa.event.Location;
import com.savemygpa.player.Player;
import com.savemygpa.player.StatType;
import com.savemygpa.player.effect.buff.SeniorNoteBuff;

public class FreeMealEvent extends Event {

    @Override public boolean isVisitTriggered() { return false; }

    @Override
    public boolean canOccur(Player player, TimeSystem timeSystem, EventContext context) {
        return context.getLocation() == Location.CANTEEN
                && !player.hasEffect(SeniorNoteBuff.class);
    }

    @Override
    protected void applyEffects(Player player, TimeSystem timeSystem) {
        player.changeStat(StatType.ENERGY, 10);
        player.addEffect(new SeniorNoteBuff());
    }

    @Override
    public String getName() { return "เจอ \"พี่รหัส\" สายซัพพอร์ต"; }
    @Override
    public String getDescription() { return "บังเอิญเจอพี่รหัส พี่เลยเลี้ยงข้าวชุดใหญ่พร้อมแนะแนวทางทำ Assignment ให้ง่ายขึ้น\n\n" + 
                                    "ผลกระทบ: Energy +10\n\n" +
                                    "Buff: Senior's Note (ได้ int เพิ่ม 2 หน่วย เป็นเวลา 2 วันหลังจากได้บัฟ)"; }
    @Override public double getChance() { return 0.15; }
}