package com.savemygpa.event.coworkingspace;

import com.savemygpa.core.TimeSystem;
import com.savemygpa.event.Event;
import com.savemygpa.event.EventContext;
import com.savemygpa.event.Location;
import com.savemygpa.player.Player;
import com.savemygpa.player.StatType;
import com.savemygpa.player.effect.debuff.WhyDizzyDebuff;

public class HurtHeadEvent extends Event {

    @Override public boolean isVisitTriggered() { return false; }

    @Override
    public boolean canOccur(Player player, TimeSystem timeSystem, EventContext context) {
        return context.getLocation() == Location.COWORKING
                && !player.hasEffect(WhyDizzyDebuff.class);
    }

    @Override
    protected void applyEffects(Player player, TimeSystem timeSystem) {
        player.changeStat(StatType.INTELLIGENCE, -5);
        player.addEffect(new WhyDizzyDebuff());
    }

    @Override
    public String getName() { return "เดินไม่ดูทาง! โป๊กกระไดเข้าให้"; }
    @Override
    public String getDescription() { return "มันเกิดขึ้นไวมาก คุณก้าวเข้ามาในห้องอันคุ้นเคย ไม่ทันได้ตั้งตัวหัวก็โขกเข้าบรรไดเจ้ากรรมเข้าให้!\n\n" + 
                                    "ผลกระทบ: Int -5\n\n" +
                                    "Debuff: Why dizzy? (สมองกระทบกระเทือน ทำให้การเรียนครั้งถัดไปได้รับค่า int ลดลง 2 หน่วย)"; }
    @Override public double getChance() { return 0.25; }
}