package com.savemygpa.event.classroom;

import com.savemygpa.core.TimeSystem;
import com.savemygpa.event.Event;
import com.savemygpa.event.EventContext;
import com.savemygpa.event.Location;
import com.savemygpa.player.Player;
import com.savemygpa.player.StatType;
import com.savemygpa.player.effect.debuff.NoStackOverflowDebuff;

public class InternetDownEvent extends Event {

    @Override public boolean isVisitTriggered() { return false; }

    @Override
    public boolean canOccur(Player player, TimeSystem timeSystem, EventContext context) {
        return context.getLocation() == Location.CLASSROOM
                && !player.hasEffect(NoStackOverflowDebuff.class);
    }

    @Override
    protected void applyEffects(Player player, TimeSystem timeSystem) {
        player.changeStat(StatType.MOOD, -25);
        player.addEffect(new NoStackOverflowDebuff());
    }

    @Override
    public String getName() { return "Internet ล่ม"; }
    @Override
    public String getDescription() { return "เน็ตล่ม กู้โค้ดไม่ได้ ค้นหาวิธีแก้ Error ไม่ได้ ทำให้การเรียนรู้วันนี้ติดขัดไปหมด\n\n" + 
                                    "ผลกระทบ: Mood -25\n\n" +
                                    "Debuff: StackOverflow Down ( int ที่จะได้จากการเรียนลดลง 5 หน่วย และ mood จะตันที่ 75 ในวันนั้นๆ )"; }
    @Override public double getChance() { return 0.15; }
}