package com.savemygpa.event;

import com.savemygpa.event.auditorium.FreeFoodEvent;
import com.savemygpa.event.busstop.GoodSeatEvent;
import com.savemygpa.event.busstop.SlowBusEvent;
import com.savemygpa.event.canteen.FreeMealEvent;
import com.savemygpa.event.canteen.NoSeatEvent;
import com.savemygpa.event.canteen.SeniorAdviceEvent;
import com.savemygpa.event.classroom.ForgetIDEvent;
import com.savemygpa.event.classroom.InternetDownEvent;
import com.savemygpa.event.classroom.PerfectCompileEvent;
import com.savemygpa.event.itbuilding.BrokenDoorEvent;
import com.savemygpa.event.itbuilding.DeanTreatsEvent;
import com.savemygpa.event.itbuilding.HurtHeadEvent;
import com.savemygpa.event.itbuilding.SourceCodeEvent;
import com.savemygpa.event.outside.DuckEvent;
import com.savemygpa.event.outside.LuckyDragonEvent;
import com.savemygpa.event.outside.RainEvent;

public class EventRegistry {

    public static void registerAll(EventManager manager) {
        // Outside
        manager.register(new LuckyDragonEvent());
        manager.register(new DuckEvent());
        manager.register(new RainEvent());

        // Bus Stop
        manager.register(new SlowBusEvent());
        manager.register(new GoodSeatEvent());

        // IT Building (shared / entering)
        manager.register(new BrokenDoorEvent());
        manager.register(new DeanTreatsEvent());
        manager.register(new SourceCodeEvent());

        // IT Building (common room)
        manager.register(new HurtHeadEvent());

        // Auditorium
        manager.register(new FreeFoodEvent());

        // Classroom
        manager.register(new InternetDownEvent());
        manager.register(new PerfectCompileEvent());
        manager.register(new ForgetIDEvent());

        // Canteen
        manager.register(new FreeMealEvent());
        manager.register(new SeniorAdviceEvent());
        manager.register(new NoSeatEvent());
    }
}