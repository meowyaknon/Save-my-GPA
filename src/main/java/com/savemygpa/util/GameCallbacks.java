package com.savemygpa.util;

import com.savemygpa.core.TimeSystem;
import com.savemygpa.event.EventManager;
import com.savemygpa.player.Player;
import com.savemygpa.ui.OutsideUI;

public interface GameCallbacks {

    // ── OutsideUI ─────────────────────────────────────────────────────────────
    void onBusStop();
    void onCanteen();
    void onITBuilding();

    // ── InsideUI ──────────────────────────────────────────────────────────────
    void onClassroom();
    void onAuditorium();
    void onCoworking();
    void onProgExam();
    void onMathExam();
    boolean isProgExamDay();
    boolean isMathExamDay();
    Player getPlayer();
    TimeSystem getTimeSystem();
    EventManager getEventManager();
    OutsideUI getOutsideUI();

    // ── BusStopUI ─────────────────────────────────────────────────────────────
    void onKLLC();
    void onGoHome();

    // ── CoworkingUI ───────────────────────────────────────────────────────────
    void onRelax();
    void onStudy();

    // ── AcceptanceUI ──────────────────────────────────────────────────────────
    void onAccept();
    void onRefuse();

    // ── MainMenuUI ────────────────────────────────────────────────────────────
    void onContinue();
    void onNewGame();
    void onHowToPlay();
    void onSettings();
    void onCredits();
    void onQuit();

    // ── PauseMenuUI ───────────────────────────────────────────────────────────
    void onResume();
    void onMainMenu();

    // ── Shared by multiple UIs ────────────────────────────────────────────────
    void onBack();
    void onPause();
}