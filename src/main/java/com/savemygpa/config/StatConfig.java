package com.savemygpa.config;

public final class StatConfig {

    // KLLC
    public static final int KLLC_LOW_INTELLIGENCE_GAIN = 2;
    public static final int KLLC_MEDIUM_INTELLIGENCE_GAIN = 3;
    public static final int KLLC_HIGH_INTELLIGENCE_GAIN = 4;
    public static final int KLLC_MOOD_LOSS = 5;
    public static final int KLLC_ENERGY_LOSS = 2;

    public static final int KLLC_MOOD_REQUIREMENT = 5;
    public static final int KLLC_ENERGY_REQUIREMENT = 2;

    // Canteen
    public static final int CANTEEN_ENERGY_GAIN = 4;
    public static final int CANTEEN_MOOD_GAIN = 5;

    // Classroom
    public static final int CLASSROOM_LOW_INTELLIGENCE_GAIN = 3;
    public static final int CLASSROOM_MEDIUM_INTELLIGENCE_GAIN = 5;
    public static final int CLASSROOM_HIGH_INTELLIGENCE_GAIN = 6;
    public static final int CLASSROOM_MOOD_LOSS = 20;
    public static final int CLASSROOM_ENERGY_LOSS = 4;

    public static final int CLASSROOM_MOOD_REQUIREMENT = 20;
    public static final int CLASSROOM_ENERGY_REQUIREMENT = 4;

    // Auditorium
    public static final int AUDITORIUM_MOOD_GAIN = 25;
    public static final int AUDITORIUM_ENERGY_LOSS = 1;

    public static final int AUDITORIUM_ENERGY_REQUIREMENT = 2;

    // Co Working Space (Relax)
    public static final int RELAX_ENERGY_GAIN = 1;
    public static final int RELAX_MOOD_GAIN = 20;

    // Co Working Space (Review)
    public static final int REVIEW_ENERGY_LOSS = 1;
    public static final int REVIEW_MOOD_LOSS = 5;
    public static final int REVIEW_LOW_INTELLIGENCE_GAIN = 0;
    public static final int REVIEW_MEDIUM_INTELLIGENCE_GAIN = 1;
    public static final int REVIEW_HIGH_INTELLIGENCE_GAIN = 2;

    public static final int REVIEW_ENERGY_REQUIREMENT = 1;
    public static final int REVIEW_MOOD_REQUIREMENT = 5;

    private StatConfig() {}

}
