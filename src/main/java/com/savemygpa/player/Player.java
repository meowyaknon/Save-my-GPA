package com.savemygpa.player;

public class Player {

    private int energy;
    private int intelligence;
    private int mood;

    private static final int MAX_ENERGY = 10;
    private static final int MAX_INTELLIGENCE = 100;
    private static final int MAX_MOOD = 100;

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
            case ENERGY -> energy = clamp(energy + amount, 0, MAX_ENERGY);
            case INTELLIGENCE -> intelligence = clamp(intelligence + amount, 0, MAX_INTELLIGENCE);
            case MOOD -> mood = clamp(mood + amount, 0, MAX_MOOD);
        }
    }

    public MoodTier getMoodTier(int amount) {
        if  (amount >= 0 && amount <= 30) {
            return MoodTier.LOW;
        }
        else if (amount >= 31 && amount <= 69) {
            return MoodTier.MEDIUM;
        }
        else if (amount >= 70 && amount <= 100) {
            return MoodTier.HIGH;
        }
        return null;
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    public boolean hasStat(StatType type, int require) {
        return getStat(type) >= require;
    }
}