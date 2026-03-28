package com.savemygpa.event.outside;

import com.savemygpa.core.TimeOfDay;
import com.savemygpa.core.TimeSystem;
import com.savemygpa.event.Event;
import com.savemygpa.event.EventContext;
import com.savemygpa.event.Location;
import com.savemygpa.player.Player;
import com.savemygpa.player.StatType;
import com.savemygpa.player.effect.buff.AuraOfLuckBuff;

public class LuckyDragonEvent extends Event {

    @Override public boolean isVisitTriggered() { return true; }

    @Override
    public boolean canOccur(Player player, TimeSystem timeSystem, EventContext context) {
        return context.getLocation() == Location.OUTSIDE
                && timeSystem.getTimeOfDay() == TimeOfDay.AFTERNOON;
    }

    @Override
    protected void applyEffects(Player player, TimeSystem timeSystem) {
        player.changeStat(StatType.MOOD, 20);
        player.addEffect(new AuraOfLuckBuff());
    }

    @Override
    public String getName() { return "พรจาก \"น้องเงินทอง\""; }
    @Override
    public String getDescription() { return "ดินผ่านน้องที่กำลังนอนอาบแดดอย่างสบายใจ แค่เห็นก็รู้สึกว่าวันนี้ต้องเป็นวันที่ดี!\n\n" + 
                                    "ผลกระทบ: Mood +20\n\n" +
                                    "Buff: Aura of Luck (เพิ่มโอกาสสุ่มเจออีเวนต์ดีๆ ใน 2 ครั้งถัดไป)"; }
    @Override public double getChance() { return 0.4; }
}