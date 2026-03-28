package com.savemygpa.core;

import com.savemygpa.config.GameConfig;

public class TimeSystem {
    private int currentDay;
    private int currentHour;

    public TimeSystem() {
        this.currentDay = 1;
        this.currentHour = GameConfig.START_HOUR;
    }

    public void setCurrentDay(int currentDay) {
        this.currentDay = currentDay;
    }

    public void setCurrentHour(int currentHour) {
        this.currentHour = currentHour;
    }

    public int getCurrentDay() {
        return currentDay;
    }

    public int getCurrentHour() {
        return currentHour;
    }

    public int getTimeLeft() {
        return GameConfig.END_HOUR - getCurrentHour();
    }

    public void advanceTime(int hours) {
        if(currentHour < GameConfig.END_HOUR){
            setCurrentHour(Math.min(getCurrentHour() + hours, GameConfig.END_HOUR));
        }
    }

    public void endDay() {
        currentDay++;
        currentHour = GameConfig.START_HOUR;
    }

    public boolean isEnoughTime(int hours) {
        return currentHour + hours <= GameConfig.END_HOUR;
    }

    public TimeOfDay getTimeOfDay() {
        if (currentHour >= 8 &&  currentHour < 12) {
            return TimeOfDay.MORNING;
        }
        else if (currentHour >= 12 &&  currentHour < 16) {
            return TimeOfDay.AFTERNOON;
        }
        else {
            return TimeOfDay.EVENING;
        }
    }

}
