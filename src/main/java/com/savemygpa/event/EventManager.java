package com.savemygpa.event;

import java.util.*;
import com.savemygpa.core.TimeSystem;
import com.savemygpa.player.Player;

public class EventManager {

    public interface EventListener {
        void onEvent(String name, String description);
    }

    private List<Event> events = new ArrayList<>();
    private Random random = new Random();

    private int eventsToday = 0;
    private static final int MAX_EVENTS_PER_DAY = 3;

    private EventListener eventListener;

    public void setEventListener(EventListener listener) {
        this.eventListener = listener;
    }

    public int getEventsToday() {
        return eventsToday;
    }

    public void setEventsToday(int eventsToday) {
        this.eventsToday = Math.max(0, eventsToday);
    }

    public void newDayReset() {
        eventsToday = 0;
    }

    public void register(Event event) {
        events.add(event);
    }

    public void triggerVisit(Player player, TimeSystem timeSystem, Location location) {
        trigger(player, timeSystem, new EventContext(location, false), true);
    }

    public void triggerAfterActivity(Player player, TimeSystem timeSystem, Location location) {
        trigger(player, timeSystem, new EventContext(location, true), false);
    }

    private void trigger(Player player, TimeSystem timeSystem, EventContext context, boolean visitOnly) {

        if (visitOnly) {
            player.updateEffectsOnTransition();
        }

        if (eventsToday >= MAX_EVENTS_PER_DAY) return;

        List<Event> possibleEvents = new ArrayList<>();
        for (Event e : events) {
            if (e.isVisitTriggered() == visitOnly && e.canOccur(player, timeSystem, context)) {
                possibleEvents.add(e);
            }
        }

        if (possibleEvents.isEmpty()) return;

        Event chosen = possibleEvents.get(random.nextInt(possibleEvents.size()));

        double effectiveChance = Math.min(chosen.getChance() * player.getEventChanceMultiplier(), 1.0);

        if (random.nextDouble() <= effectiveChance) {
            chosen.occur(player, timeSystem);
            eventsToday++;
            // Notify the UI
            if (eventListener != null) {
                eventListener.onEvent(chosen.getName(), chosen.getDescription());
            }
        }
    }
}