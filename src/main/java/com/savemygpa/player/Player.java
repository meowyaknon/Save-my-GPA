package com.savemygpa.player;

public class Player {
    private int energy;
    private int intelligence;
    private int mood;

    public Player(int energy, int intelligence, int mood) {
        this.energy = energy;
        this.intelligence = intelligence;
        this.mood = mood;
    }

    public int getStat(StatType type) {
        return switch (type) {
            case ENERGY -> energy;
            case INTELLIGENCE -> intelligence;
            case MOOD -> mood;
        };
    }

    public void changeStat(StatType type, int amount) {
        switch (type) {
            case ENERGY -> energy = Math.max(0, energy + amount);
            case INTELLIGENCE -> intelligence =  Math.max(0, intelligence + amount);
            case MOOD -> mood = Math.max(0, mood + amount);
        }
    }

    public boolean hasStat(StatType type, int require) {
        return getStat(type) >= require;
    }
}
