package com.savemygpa.event;

import java.util.*;

import com.savemygpa.core.TimeSystem;
import com.savemygpa.player.Player;

public class EventManager {

    private List<Event> events = new ArrayList<>();
    private Random random = new Random();

    private int eventsToday = 0;
    private static final int MAX_EVENTS_PER_DAY = 3;

    public void newDayReset() {
        eventsToday = 0;
    }

    public void register(Event event) {
        events.add(event);
    }

    public void trigger(Player player, TimeSystem timeSystem, EventContext context) {

        player.tickEffectsOnTransition();

        if (eventsToday >= MAX_EVENTS_PER_DAY) return;

        List<Event> possibleEvents = new ArrayList<>();
        for (Event e : events) {
            if (e.canOccur(player, timeSystem, context)) {
                possibleEvents.add(e);
            }
        }

        if (possibleEvents.isEmpty()) return;

        Event chosen = possibleEvents.get(random.nextInt(possibleEvents.size()));

        double effectiveChance = chosen.getChance() * player.getEventChanceMultiplier();
        effectiveChance = Math.min(effectiveChance, 1.0);

        if (random.nextDouble() <= effectiveChance) {
            chosen.occur(player, timeSystem);
            eventsToday++;
        }
    }
}