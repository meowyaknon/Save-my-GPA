package com.savemygpa.event.itbuilding;

import com.savemygpa.core.TimeSystem;
import com.savemygpa.event.Event;
import com.savemygpa.event.EventContext;
import com.savemygpa.event.Location;
import com.savemygpa.player.Player;
import com.savemygpa.player.StatType;
import com.savemygpa.player.effect.debuff.WhyDizzyDebuff;

public class HurtHeadEvent extends Event {

    @Override
    public boolean canOccur(Player player, TimeSystem timeSystem, EventContext context) {
        return context.getLocation() == Location.COWORKING
                && !player.hasEffect(WhyDizzyDebuff.class);
    }

    @Override
    protected void applyEffects(Player player, TimeSystem timeSystem) {
        player.changeStat(StatType.INTELLIGENCE, -5);
        player.addEffect(new WhyDizzyDebuff());
        System.out.println("[Event] โป๊กกระได! มันเกิดขึ้นไวมาก คุณก้าวเข้ามาในห้องอันคุ้นเคย ไม่ทันได้ตั้งตัวก็โขกเข้าบรรไดเจ้ากรรมเข้าให้ ! มึนตึบเลย");
    }

    @Override
    protected String getName() { return "โป๊กกระได"; }

    @Override
    protected String getDescription() {
        return "มันเกิดขึ้นไวมาก คุณก้าวเข้ามาในห้องอันคุ้นเคย ไม่ทันได้ตั้งตัวก็โขกเข้าบรรไดเจ้ากรรมเข้าให้ ! มึนตึบเลย";
    }

    @Override
    public double getChance() { return 0.25; }
}