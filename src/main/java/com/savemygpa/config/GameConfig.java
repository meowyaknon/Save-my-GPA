package com.savemygpa.config;

public final class GameConfig {

    // Game Loop
    public static final int TARGET_FPS = 60;

    // Time System
    public static final int START_HOUR = 8;
    public static final int END_HOUR = 18;

    public static final int STUDY_DAYS = 5;
    public static final int EXAM_DAYS = 2;

    // Time cost of actions
    public static final int CLASSROOM_TIME_COST = 1;
    public static final int KLLC_TIME_COST = 1;
    public static final int CAFETERIA_TIME_COST = 1;
    public static final int AUDITORIUM_TIME_COST = 1;
    public static final int RELAX_TIME_COST = 1;
    public static final int REVIEW_TIME_COST = 1;
    public static final int GOHOME_TIME_COST = 11;

    private GameConfig() {}
}
