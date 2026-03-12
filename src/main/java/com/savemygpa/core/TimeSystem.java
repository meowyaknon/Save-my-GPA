package com.savemygpa.core;

import com.savemygpa.config.GameConfig;

public class TimeSystem {
    private int currentDay;
    private int currentHour;

    public TimeSystem() {
        this.currentDay = 1;
        this.currentHour = GameConfig.START_HOUR;
    }

    public int getCurrentDay() {
        return currentDay;
    }

    public int  getCurrentHour() {
        return currentHour;
    }

    public void advanceTime(int hours) {
        currentHour += hours;
    }

    public boolean isDayOver() {
        return currentHour >= GameConfig.END_HOUR;
    }

    public void endDay() {
        currentDay++;
        currentHour = GameConfig.START_HOUR;
    }

    public boolean isEnoughTime(int hours) {
        return currentHour + hours <= GameConfig.END_HOUR;
    }

}
