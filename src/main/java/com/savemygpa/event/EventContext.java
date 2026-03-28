package com.savemygpa.event;

public class EventContext {

    private Location location;
    private boolean afterActivity;

    public EventContext(Location location, boolean afterActivity) {
        this.location = location;
        this.afterActivity = afterActivity;
    }

    public Location getLocation() {
        return location;
    }

}