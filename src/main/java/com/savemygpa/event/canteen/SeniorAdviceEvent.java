package com.savemygpa.event.canteen;

import com.savemygpa.core.TimeSystem;
import com.savemygpa.event.Event;
import com.savemygpa.event.EventContext;
import com.savemygpa.event.Location;
import com.savemygpa.player.Player;
import com.savemygpa.player.StatType;

public class SeniorAdviceEvent extends Event {

    @Override
    public boolean canOccur(Player player, TimeSystem timeSystem, EventContext context) {
        return context.getLocation() == Location.CANTEEN;
    }

    @Override
    protected void applyEffects(Player player, TimeSystem timeSystem) {
        player.changeStat(StatType.INTELLIGENCE, 5);
        System.out.println("[Event] รุ่นพี่ชวนคุย! ระหว่างนั่งพัก มีรุ่นพี่มาทักและให้คำปรึกษาเรื่องโปรเจค ได้ทริคดีๆ กลับไปเพียบ!");
    }

    @Override
    protected String getName() { return "รุ่นพี่ชวนคุยเรื่องโปรเจค"; }

    @Override
    protected String getDescription() {
        return "ระหว่างนั่งพัก มีรุ่นพี่มาทักและให้คำปรึกษาเรื่องโปรเจค ได้ทริคดีๆ กลับไปเพียบ!";
    }

    @Override
    public double getChance() { return 0.35; }
}