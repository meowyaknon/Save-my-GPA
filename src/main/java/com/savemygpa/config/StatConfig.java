package com.savemygpa.config;

public class StatConfig {

    // Home
    public static final int HOME_ENERGY_GAIN = 3;
    public static final int HOME_MOOD_GAIN = 1;

    // KLLC
    public static final int KLLC_INTELLIGENCE_GAIN = 1;
    public static final int KLLC_MOOD_GAIN = 1;
    public static final int KLLC_ENERGY_LOSS = 1;

    public static final int KLLC_MOOD_REQUIREMENT = 1;
    public static final int KLLC_ENERGY_REQUIREMENT = 2;

    // Cafeteria
    public static final int CAFETERIA_ENERGY_GAIN = 1;

    // Classroom
    public static final int CLASSROOM_INTELLIGENCE_GAIN = 1;
    public static final int CLASSROOM_MOOD_LOSS = 1;
    public static final int CLASSROOM_ENERGY_LOSS = 1;

    public static final int CLASSROOM_MOOD_REQUIREMENT = 2;
    public static final int CLASSROOM_ENERGY_REQUIREMENT = 2;

    // Auditorium
    public static final int AUDITORIUM_MOOD_GAIN = 1;
    public static final int AUDITORIUM_ENERGY_LOSS = 1;

    public static final int AUDITORIUM_MOOD_REQUIREMENT = 2;
    public static final int AUDITORIUM_ENERGY_REQUIREMENT = 2;

    // Co Working Space (Relax)
    public static final int RELAX_ENERGY_GAIN = 1;
    public static final int RELAX_MOOD_GAIN = 1;

    // Co Working Space (Review)
    public static final int REVIEW_ENERGY_LOSS = 1;
    public static final int REVIEW_MOOD_LOSS = 1;
    public static final int REVIEW_INTELLIGENCE_GAIN = 1;

    public static final int REVIEW_ENERGY_REQUIREMENT = 2;
    public static final int REVIEW_MOOD_REQUIREMENT = 2;

    // Random Events

    private StatConfig() {}

}
