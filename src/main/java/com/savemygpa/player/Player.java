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
            case ENERGY -> energy += amount;
            case INTELLIGENCE -> intelligence += amount;
            case MOOD -> mood += amount;
        }
    }
}
