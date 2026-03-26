package com.savemygpa.launcher;

import com.savemygpa.audio.AudioManager;
import com.savemygpa.exam.CaptchaMiniGame;
import com.savemygpa.exam.CountingMiniGame;
import com.savemygpa.ui.*;
import javafx.application.Application;
import javafx.animation.*;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import com.savemygpa.player.*;
import com.savemygpa.player.effect.StatusEffect;
import com.savemygpa.player.effect.buff.SeniorNoteBuff;
import com.savemygpa.player.effect.debuff.WetFeetDebuff;
import com.savemygpa.player.effect.debuff.NoStackOverflowDebuff;
import com.savemygpa.core.*;
import com.savemygpa.activity.*;
import com.savemygpa.event.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;

public class GameLauncher extends Application {

    private Stage        stage;
    private Player       player;
    private TimeSystem   timeSystem;
    private EventManager eventManager;
    private final StackPane sceneRoot = new StackPane();
    private static final int BASE_W = 1920, BASE_H = 1080;
    private OutsideUI outsideUI;
    private StackPane pauseOverlay = null;
    private boolean pauseOpen    = false;
    private boolean actionLocked = false;
    private enum PauseOrigin { OUTSIDE, IT_BUILDING }
    private PauseOrigin pauseOrigin = PauseOrigin.OUTSIDE;
    private boolean hasSavedGame = false, agreedToTerms = false;
    private int progExam1Score = 0, mathExam1Score = 0, progExam2Score = 0, mathExam2Score = 0;

    private boolean progExamTakenToday = false;
    private boolean mathExamTakenToday = false;

    private static final int TOTAL_DAYS = 14;
    private static final String SAVE_VERSION = "1", SAVE_FILE_NAME = "save_my_gpa_save.properties";
    private long lastSaveMillis = 0;

    private void runOnce(Runnable action) {
        if (actionLocked) return;
        action.run();
        actionLocked = true;
        PauseTransition unlock = new PauseTransition(Duration.millis(600));
        unlock.setOnFinished(e -> actionLocked = false);
        unlock.play();
    }

    @Override
    public void start(Stage stage) {
        this.stage = stage;
        stage.setTitle("Save My GPA");
        stage.setScene(new Scene(sceneRoot, BASE_W, BASE_H));
        stage.setResizable(true);
        sceneRoot.setStyle("-fx-background-color: #000;");
        stage.show();
        loadFromDisk();
        AudioManager audio = AudioManager.getInstance();
        applyResolution(audio.getResolutionWidth(), audio.getResolutionHeight());
        if (agreedToTerms) showMainMenu(); else showIntroSequence();
    }

    private void setContent(javafx.scene.Node node) {
        if (!sceneRoot.getChildren().isEmpty()) sceneRoot.getChildren().set(0, node);
        else sceneRoot.getChildren().add(node);
    }

    private void applyResolution(int w, int h) {
        AudioManager.getInstance().setResolution(w, h);
        double sx = (double) w / BASE_W, sy = (double) h / BASE_H;
        stage.getScene().getWindow().setWidth(w);
        stage.getScene().getWindow().setHeight(h);
        sceneRoot.setScaleX(sx); sceneRoot.setScaleY(sy);
        sceneRoot.setTranslateX((w - BASE_W) / 2.0);
        sceneRoot.setTranslateY((h - BASE_H) / 2.0);
    }

    // ═════════════════════════════════════════════════════════════════════════
    // New Game
    // ═════════════════════════════════════════════════════════════════════════

    private void startNewGame() {
        player = new Player(6, 0, 60);
        timeSystem = new TimeSystem();
        eventManager = new EventManager();
        EventRegistry.registerAll(eventManager);
        hasSavedGame = true;
        progExam1Score = mathExam1Score = progExam2Score = mathExam2Score = 0;
        progExamTakenToday = false;
        mathExamTakenToday = false;
        persistSave(true);
        showNewGameIntro();
    }

    private void showNewGameIntro() {
        StackPane blackScreen = new StackPane();
        blackScreen.setStyle("-fx-background-color: #000000;");
        blackScreen.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        Label storyLabel = new Label("");
        storyLabel.setFont(Font.font("Comic Sans MS", FontWeight.BOLD, 30));
        storyLabel.setTextFill(Color.WHITE);
        storyLabel.setWrapText(true);
        storyLabel.setMaxWidth(900);
        storyLabel.setTextAlignment(TextAlignment.CENTER);
        storyLabel.setStyle("-fx-effect: dropshadow(gaussian,rgba(200,220,255,0.35),14,0.4,0,0);");
        storyLabel.setOpacity(0);
        blackScreen.getChildren().add(storyLabel);
        setContent(blackScreen);
        FadeTransition fadeIn = new FadeTransition(Duration.millis(600), blackScreen);
        fadeIn.setFromValue(0); fadeIn.setToValue(1);
        fadeIn.setOnFinished(e -> {
            storyLabel.setOpacity(1);
            String[] story = {
                    "ถึงเวลาเปิดเทอมแล้ว...",
                    "ได้เวลาเริ่มการศึกษา",
                    "สถาบันเทคโนโลยีพระจอมเกล้าเจ้าคุณทหารลาดกระบัง",
                    "คณะเทคโนโลยีสารสนเทศ  ปีที่ 1",
                    "ตื่นเต้น... กลัว... แต่ก็พร้อมสู้!",
                    "2 สัปดาห์ข้างหน้าจะเป็นยังไง?",
                    "ทุกอย่างขึ้นอยู่กับตัวเอง..."
            };
            typeSegments(storyLabel, story, () -> {
                FadeTransition fadeOut = new FadeTransition(Duration.millis(700), blackScreen);
                fadeOut.setToValue(0);
                fadeOut.setOnFinished(ev -> showGameplayWithFadeIn());
                fadeOut.play();
            });
        });
        fadeIn.play();
    }

    // ═════════════════════════════════════════════════════════════════════════
    // Gameplay with fade-in
    // ═════════════════════════════════════════════════════════════════════════

    private void showGameplayWithFadeIn() {
        if (isGameOver()) { showEnding(); return; }
        if (outsideUI != null) outsideUI.stopAnimations();
        outsideUI = new OutsideUI(player, timeSystem, eventManager, buildOutsideCallbacks());
        javafx.scene.Node gameView = outsideUI.buildView();
        gameView.setOpacity(0);
        setContent(gameView);
        eventManager.triggerVisit(player, timeSystem, Location.OUTSIDE);
        outsideUI.refresh();
        maybeSaveProgress(false);
        FadeTransition ft = new FadeTransition(Duration.millis(800), gameView);
        ft.setFromValue(0); ft.setToValue(1);
        ft.play();
    }

    private boolean isProgExamDay() { int d = timeSystem.getCurrentDay(); return d == 6 || d == 13; }
    private boolean isMathExamDay() { int d = timeSystem.getCurrentDay(); return d == 1 || d == 14; }
    private boolean isGameOver()    { return timeSystem.getCurrentDay() > TOTAL_DAYS; }

    private void onDayEnd() {
        int day = timeSystem.getCurrentDay();
        if (day == 6)  progExam1Score = player.getStat(StatType.INTELLIGENCE);
        if (day == 1)  mathExam1Score = player.getStat(StatType.INTELLIGENCE);
        if (day == 13) progExam2Score = player.getStat(StatType.INTELLIGENCE);
        if (day == 14) mathExam2Score = player.getStat(StatType.INTELLIGENCE);
        player.getEffect(SeniorNoteBuff.class).ifPresent(b -> b.tickDay(player));
        player.removeEffect(WetFeetDebuff.class);
        player.removeEffect(NoStackOverflowDebuff.class);
        progExamTakenToday = false;
        mathExamTakenToday = false;
        eventManager.newDayReset();
        timeSystem.endDay();
        if (timeSystem.getCurrentDay() == 8) {
            int cur = player.getStat(StatType.INTELLIGENCE);
            player.changeStat(StatType.INTELLIGENCE, -cur);
            showDialog("📚 รอบสอบแรกจบแล้ว!",
                    "💻 Programming: " + progExam1Score + "\n📐 Math: " + mathExam1Score +
                            "\n\nIntelligence รีเซ็ต — สู้ต่อไป!", this::showGameplay);
            return;
        }
        if (isGameOver()) { showEnding(); return; }
        showGameplay();
    }

    // ── Go Home (with cutscene) ───────────────────────────────────────────────
    private void doGoHome() {
        if (actionLocked) return;
        actionLocked = true;
        ActivityCutscene.play(sceneRoot,
                ActivityCutscene.lineFor("GoHomeActivity"),
                () -> {
                    int bonus = timeSystem.getTimeLeft();
                    player.changeStat(StatType.ENERGY, 3 + (player.getStat(StatType.MOOD) / 40) + bonus);
                    player.changeStat(StatType.MOOD, 10 + bonus);
                    actionLocked = false;
                    onDayEnd();
                });
    }

    // ── Activity helpers ──────────────────────────────────────────────────────

    private void performWithCutscene(Activity activity, Location location, Runnable onAfter) {
        if (actionLocked) return;
        RequirementReason reason = activity.canPerform(player, timeSystem);
        if (reason != null) {
            String msg = activity.getFailMessage(reason);
            if (msg != null) showDialog("ไม่สามารถทำได้", msg, null);
            if (outsideUI != null) outsideUI.sayFail();
            return;
        }
        actionLocked = true;
        String line = ActivityCutscene.lineFor(activity.getClass().getSimpleName());
        ActivityCutscene.play(sceneRoot, line, () -> {
            activity.performActivity(player, timeSystem, eventManager);
            if (location != null) eventManager.triggerAfterActivity(player, timeSystem, location);
            if (outsideUI != null) { outsideUI.refresh(); outsideUI.saySuccess(); }
            maybeSaveProgress(false);
            actionLocked = false;
            if (onAfter != null) onAfter.run();
        });
    }

    private boolean performSilent(Activity activity, Location location) {
        RequirementReason reason = activity.canPerform(player, timeSystem);
        if (reason != null) {
            String msg = activity.getFailMessage(reason);
            if (msg != null) showDialog("ไม่สามารถทำได้", msg, null);
            if (outsideUI != null) outsideUI.sayFail();
            return false;
        }
        activity.performActivity(player, timeSystem, eventManager);
        if (location != null) eventManager.triggerAfterActivity(player, timeSystem, location);
        if (outsideUI != null) { outsideUI.refresh(); outsideUI.saySuccess(); }
        maybeSaveProgress(false);
        return true;
    }

    private void showDialog(String title, String message, Runnable onClose) {
        GameDialog.show(sceneRoot, title, message, onClose);
    }

    // ── Pause ─────────────────────────────────────────────────────────────────

    private void showPause(PauseOrigin origin) {
        if (pauseOpen) return;
        pauseOpen = true;
        pauseOrigin = origin;
        PauseMenuUI pauseUI = new PauseMenuUI(new PauseMenuUI.Callbacks() {
            @Override public void onResume() {
                if (pauseOverlay != null) {
                    FadeTransition ft = new FadeTransition(Duration.millis(180), pauseOverlay);
                    ft.setToValue(0);
                    ft.setOnFinished(e -> {
                        sceneRoot.getChildren().remove(pauseOverlay);
                        pauseOverlay = null;
                        pauseOpen = false;
                    });
                    ft.play();
                }
            }
            @Override public void onSettings() {
                sceneRoot.getChildren().remove(pauseOverlay);
                pauseOverlay = null; pauseOpen = false;
                Runnable back = pauseOrigin == PauseOrigin.OUTSIDE
                        ? GameLauncher.this::showGameplay
                        : GameLauncher.this::showITBuilding;
                showSettings(back);
            }
            @Override public void onMainMenu() {
                sceneRoot.getChildren().remove(pauseOverlay);
                pauseOverlay = null; pauseOpen = false;
                showMainMenu();
            }
        });
        pauseOverlay = pauseUI.buildView();
        sceneRoot.getChildren().add(pauseOverlay);
    }

    // ═════════════════════════════════════════════════════════════════════════
    // Screens
    // ═════════════════════════════════════════════════════════════════════════

    private void showIntroSequence() {
        StackPane intro = new StackPane();
        intro.setStyle("-fx-background-color: #000;");
        VBox content = new VBox(18);
        content.setAlignment(Pos.CENTER);
        content.setStyle("-fx-padding:30;");

        Text t1 = new Text("Save My GPA");
        t1.setFill(Color.WHITE);
        t1.setFont(Font.font("Comic Sans MS", FontWeight.BOLD, 56));
        t1.setOpacity(0);

        Text t2 = new Text("ทีมผู้สร้าง SaveMyGPA Team");
        t2.setFill(Color.WHITE);
        t2.setFont(Font.font("Comic Sans MS", FontWeight.SEMI_BOLD, 32));
        t2.setOpacity(0);

        Label story = new Label();
        story.setTextFill(Color.WHITE);
        story.setFont(Font.font("Comic Sans MS", 24));
        story.setWrapText(true);
        story.setMaxWidth(900);
        story.setAlignment(Pos.CENTER);
        story.setOpacity(0);

        content.getChildren().addAll(t1, t2, story);
        intro.getChildren().add(content);
        setContent(intro);

        String[] segs = {
                "ในคืนที่นักศึกษาหญิงคนหนึ่งรอผลสอบ...",
                "หัวใจเต้นแรงเมื่อรู้ว่า \"เธอสอบติดแล้ว!\"",
                "คณะเทคโนโลยีสารสนเทศ KMITL",
                "แต่ก่อนจะก้าวไปสู่บทเรียนถัดไป...",
                "เธอต้องเลือกเส้นทางของตัวเอง"
        };

        FadeTransition i1 = fade(t1,0,1,1.0),
                o1=fade(t1,1,0,0.8),
                i2=fade(t2,0,1,0.9),
                o2=fade(t2,1,0,0.8),
                si=fade(story,0,1,0.8);

        si.setOnFinished(e -> typeSegments(story, segs, this::showAgreement));
        new SequentialTransition(i1, pause(0.6), o1, i2, pause(0.6), o2, si).play();
    }

    private void showAgreement() {
        AcceptanceUI ui = new AcceptanceUI(new AcceptanceUI.Callbacks() {
            @Override public void onAccept() {
                agreedToTerms = true;
                persistSave(true);
                showMainMenu();
            }
            @Override public void onRefuse() { showRefusalEnding(); }
        });
        setContent(ui.buildView());
    }

    private void showMainMenu() {
        actionLocked = false;
        pauseOpen    = false;
        MainMenuUI ui = new MainMenuUI(hasSavedGame, new MainMenuUI.Callbacks() {
            @Override public void onContinue()  { runOnce(GameLauncher.this::showGameplayWithFadeIn); }
            @Override public void onNewGame()   { runOnce(GameLauncher.this::startNewGame); }
            @Override public void onHowToPlay() { runOnce(GameLauncher.this::showHowToPlay); }
            @Override public void onSettings()  { runOnce(() -> showSettings(GameLauncher.this::showMainMenu)); }
            @Override public void onCredits()   { runOnce(GameLauncher.this::showCredits); }
            @Override public void onQuit()      { stage.close(); }
        });
        setContent(ui.buildView());
    }

    private void showSettings(Runnable onBack) {
        SettingsUI ui = new SettingsUI(AudioManager.getInstance(),
                () -> ActivityCutscene.transition(sceneRoot, onBack, null));
        ActivityCutscene.transition(sceneRoot, () -> setContent(ui.buildView()), null);
    }

    private void showHowToPlay() {
        ActivityCutscene.transition(sceneRoot, () -> {
            StackPane root = new StackPane();
            root.setStyle("-fx-background-color: linear-gradient(to bottom, #0d1b2a, #1b2a3b);");
            VBox content = new VBox(20); content.setAlignment(Pos.CENTER);
            content.setStyle("-fx-padding: 60;"); content.setMaxWidth(900);
            Text title = new Text("🕹️  วิธีเล่น Save My GPA");
            title.setFont(Font.font("Comic Sans MS", FontWeight.BOLD, 34));
            title.setFill(Color.web("#ffe082"));
            Label text = new Label(
                    "วัน 1-5   →  เรียนปกติ\n" +
                            "วัน 6      →  สอบ Programming รอบ 1\n" +
                            "วัน 7      →  สอบ Math รอบ 1 (มินิเกม)\n" +
                            "วัน 8      →  INT รีเซ็ต\n" +
                            "วัน 8-12 →  เรียนปกติ\n" +
                            "วัน 13    →  สอบ Programming รอบ 2\n" +
                            "วัน 14    →  สอบ Math รอบ 2 (มินิเกม)\n\n" +
                            "🏆  เกรด:  ≥80 → A  |  ≥70 → B  |  ≥60 → C  |  ≥50 → D  |  <50 → F");
            text.setStyle("-fx-font-family:'Comic Sans MS'; -fx-font-size:20px; -fx-text-fill:#d0e8ff; -fx-line-spacing:6;");
            text.setWrapText(true);
            Button back = navBtn("←  กลับ");
            back.setOnAction(e -> runOnce(() ->
                    ActivityCutscene.transition(sceneRoot, GameLauncher.this::showMainMenu, null)));
            content.getChildren().addAll(title, text, back);
            content.setOpacity(0);
            root.getChildren().add(content);
            setContent(root);
            FadeTransition ft = new FadeTransition(Duration.millis(400), content);
            ft.setToValue(1); ft.play();
        }, null);
    }

    private void showCredits() {
        ActivityCutscene.transition(sceneRoot, () -> {
            StackPane root = new StackPane();
            root.setStyle("-fx-background-color: linear-gradient(to bottom, #0d1b2a, #1b2a3b);");
            VBox content = new VBox(20); content.setAlignment(Pos.CENTER);
            content.setStyle("-fx-padding: 60;");
            Text title = new Text("👥  เครดิต");
            title.setFont(Font.font("Comic Sans MS", FontWeight.BOLD, 36));
            title.setFill(Color.web("#ce93d8"));
            Label text = new Label("🎮  Save My GPA\nCreated by the SaveMyGPA Team\n\nขอบคุณทุกคนที่เล่น! 🎉");
            text.setStyle("-fx-font-family:'Comic Sans MS'; -fx-font-size:22px; -fx-text-fill:#e0d0f0; -fx-line-spacing:6;");
            text.setTextAlignment(TextAlignment.CENTER);
            Button back = navBtn("←  กลับ");
            back.setOnAction(e -> runOnce(() ->
                    ActivityCutscene.transition(sceneRoot, GameLauncher.this::showMainMenu, null)));
            content.getChildren().addAll(title, text, back);
            content.setOpacity(0);
            root.getChildren().add(content);
            setContent(root);
            FadeTransition ft = new FadeTransition(Duration.millis(400), content);
            ft.setToValue(1); ft.play();
        }, null);
    }

    // ── Outside (instant swap – used after day-end transitions) ──────────────

    private void showGameplay() {
        if (isGameOver()) { showEnding(); return; }
        if (outsideUI != null) outsideUI.stopAnimations();
        outsideUI = new OutsideUI(player, timeSystem, eventManager, buildOutsideCallbacks());
        setContent(outsideUI.buildView());
        eventManager.triggerVisit(player, timeSystem, Location.OUTSIDE);
        outsideUI.refresh();
        maybeSaveProgress(false);
    }

    private OutsideUI.Callbacks buildOutsideCallbacks() {
        return new OutsideUI.Callbacks() {
            @Override public void onBusStop()    { runOnce(GameLauncher.this::showBusStop); }
            @Override public void onCanteen()    { runOnce(GameLauncher.this::doCanteen); }
            @Override public void onITBuilding() { runOnce(GameLauncher.this::showITBuilding); }
            @Override public void onGoHome()     { runOnce(GameLauncher.this::doGoHome); }
            @Override public void onPause()      { showPause(PauseOrigin.OUTSIDE); }
            @Override public void showMessage(String t, String m) { showDialog(t, m, null); }
            @Override public boolean doActivity(Activity a, Location l) { return performSilent(a, l); }
        };
    }

    // ── Canteen ───────────────────────────────────────────────────────────────

    private void doCanteen() {
        EatActivity eat = new EatActivity();
        RequirementReason r = eat.canPerform(player, timeSystem);
        if (r != null) {
            showDialog("ไม่สามารถทำได้", eat.getFailMessage(r), null);
            if (outsideUI != null) outsideUI.sayFail();
            return;
        }
        actionLocked = true;
        ActivityCutscene.play(sceneRoot, ActivityCutscene.lineFor("EatActivity"), () -> {
            eat.performActivity(player, timeSystem, eventManager);
            eventManager.triggerAfterActivity(player, timeSystem, Location.CANTEEN);
            if (outsideUI != null) { outsideUI.refresh(); outsideUI.saySuccess(); }
            maybeSaveProgress(false);
            actionLocked = false;
        });
    }

    // ── Bus Stop ──────────────────────────────────────────────────────────────

    private void showBusStop() {
        BusStopUI ui = new BusStopUI(sceneRoot, new BusStopUI.Callbacks() {
            @Override public void onKLLC()   { performWithCutscene(new KLLCActivity(), Location.BUS_STOP, GameLauncher.this::showGameplay); }
            @Override public void onGoHome() { doGoHome(); }
            @Override public void onBack()   { /* overlay already dismissed */ }
        });
        ui.show();
    }

    // ── IT Building ───────────────────────────────────────────────────────────
    // OutsideUI.doITTransition() fades OUT rootPane to opacity=0, then calls
    // cb.onITBuilding() → this method. Content is swapped while screen is dark,
    // then we fade the new InsideUI view from 0→1 for a smooth transition.

    private void showITBuilding() {
        eventManager.triggerVisit(player, timeSystem, Location.IT_BUILDING);
        if (outsideUI != null) outsideUI.refresh();
        actionLocked = false;

        InsideUI ui = new InsideUI(new InsideUI.Callbacks() {
            @Override public boolean isProgExamDay()    { return GameLauncher.this.isProgExamDay(); }
            @Override public boolean isMathExamDay()    { return GameLauncher.this.isMathExamDay(); }
            @Override public void onPause()             { showPause(PauseOrigin.IT_BUILDING); }
            @Override public Player       getPlayer()       { return player; }
            @Override public TimeSystem   getTimeSystem()   { return timeSystem; }
            @Override public EventManager getEventManager() { return eventManager; }

            @Override public void onClassroom() {
                performWithCutscene(new ClassroomActivity(), Location.CLASSROOM,
                        GameLauncher.this::showITBuilding);
            }
            @Override public void onAuditorium() {
                performWithCutscene(new AuditoriumActivity(), Location.AUDITORIUM,
                        GameLauncher.this::showITBuilding);
            }
            @Override public void onCoworking() {
                showCoworkingSpace();
            }
            @Override public void onProgExam() { doProgExam(); }
            @Override public void onMathExam() { doMathExam(); }
            @Override public void onBack() {
                if (actionLocked) return;
                actionLocked = true;
                ActivityCutscene.transition(sceneRoot, () -> {
                    actionLocked = false;
                    showGameplay();
                }, null);
            }
        });

        // Build the view, start it invisible, swap content (screen already dark
        // from OutsideUI's fade-out), then fade the new screen in.
        javafx.scene.Node itView = ui.buildView();
        itView.setOpacity(0);
        setContent(itView);
        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), itView);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();
    }

    // ── Coworking Space ───────────────────────────────────────────────────────

    private void showCoworkingSpace() {
        CoworkingUI ui = new CoworkingUI(sceneRoot, new CoworkingUI.Callbacks() {
            @Override public void onRelax() {
                performWithCutscene(new CoworkingRelaxActivity(), Location.COWORKING,
                        GameLauncher.this::showITBuilding);
            }
            @Override public void onStudy() {
                performWithCutscene(new CoworkingStudyActivity(), Location.COWORKING,
                        GameLauncher.this::showITBuilding);
            }
            @Override public void onBack() { /* overlay already dismissed */ }
        });
        ui.show();
    }

    // ── Exams ─────────────────────────────────────────────────────────────────

    private void doProgExam() {
        if (progExamTakenToday) {
            showDialog("สอบแล้ววันนี้",
                    "คุณสอบ Programming ไปแล้ววันนี้\nผลคะแนนถูกบันทึกเรียบร้อย ✅", null);
            return;
        }
        ExamActivity exam = new ExamActivity();
        RequirementReason r = exam.canPerform(player, timeSystem);
        if (r != null) { showDialog("ไม่สามารถสอบได้", exam.getFailMessage(r), null); return; }
        progExamTakenToday = true;
        actionLocked = true;
        CaptchaMiniGame[] ref = new CaptchaMiniGame[1];
        ref[0] = new CaptchaMiniGame(player, () -> {
            performSilent(exam, Location.CLASSROOM);
            String round = (timeSystem.getCurrentDay() <= 7) ? "รอบ 1" : "รอบ 2";
            actionLocked = false;
            showDialog("✅ สอบ Programming " + round + " เสร็จ!",
                    "🎯 คะแนน: " + ref[0].getTotalScore() + " / 50",
                    this::showITBuilding);
        });
        setContent(ref[0].getView());
        stage.setFullScreenExitHint(""); stage.setFullScreen(true);
    }

    private void doMathExam() {
        if (mathExamTakenToday) {
            showDialog("สอบแล้ววันนี้",
                    "คุณสอบ Math ไปแล้ววันนี้\nผลคะแนนถูกบันทึกเรียบร้อย ✅", null);
            return;
        }
        ExamActivity exam = new ExamActivity();
        RequirementReason r = exam.canPerform(player, timeSystem);
        if (r != null) { showDialog("ไม่สามารถสอบได้", exam.getFailMessage(r), null); return; }
        mathExamTakenToday = true;
        performSilent(exam, Location.CLASSROOM);
        actionLocked = true;
        CountingMiniGame[] ref = new CountingMiniGame[1];
        ref[0] = new CountingMiniGame(player, () -> {
            String round = (timeSystem.getCurrentDay() <= 7) ? "รอบ 1" : "รอบ 2";
            actionLocked = false;
            showDialog("✅ สอบ Math " + round + " เสร็จ!",
                    "🎮 คะแนน: " + ref[0].getTotalScore() + " / 50",
                    this::showITBuilding);
        });
        setContent(ref[0].getView());
        stage.setFullScreenExitHint(""); stage.setFullScreen(true);
    }

    // ═════════════════════════════════════════════════════════════════════════
    // Endings
    // ═════════════════════════════════════════════════════════════════════════

    private void showEnding() {
        int progAvg = (progExam1Score + progExam2Score) / 2;
        int mathAvg = (mathExam1Score + mathExam2Score) / 2;
        int overall = (progAvg + mathAvg) / 2;
        String grade;
        if      (overall >= 80) grade = "A";
        else if (overall >= 70) grade = "B";
        else if (overall >= 60) grade = "C";
        else if (overall >= 50) grade = "D";
        else                    grade = "F";

        StackPane blackScreen = new StackPane();
        blackScreen.setStyle("-fx-background-color: #000000;");
        blackScreen.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        Label storyLabel = new Label("");
        storyLabel.setFont(Font.font("Comic Sans MS", FontWeight.BOLD, 32));
        storyLabel.setTextFill(Color.WHITE);
        storyLabel.setWrapText(true);
        storyLabel.setMaxWidth(1000);
        storyLabel.setTextAlignment(TextAlignment.CENTER);
        storyLabel.setStyle("-fx-effect: dropshadow(gaussian,rgba(200,220,255,0.35),14,0.4,0,0);");
        storyLabel.setOpacity(0);
        blackScreen.getChildren().add(storyLabel);
        setContent(blackScreen);
        FadeTransition fadeIn = new FadeTransition(Duration.millis(900), blackScreen);
        fadeIn.setFromValue(0); fadeIn.setToValue(1);
        final String fg = grade; final int fP = progAvg, fM = mathAvg, fO = overall;
        fadeIn.setOnFinished(e -> {
            storyLabel.setOpacity(1);
            typeSegments(storyLabel, endingStory(fg), () -> showEndingResult(fg, fO, fP, fM));
        });
        fadeIn.play();
    }

    private void showEndingResult(String grade, int overall, int progAvg, int mathAvg) {
        String titleColor = endingColor(grade);
        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: #000000;");
        VBox content = new VBox(28); content.setAlignment(Pos.CENTER); content.setStyle("-fx-padding: 60;");
        Text gradeText = new Text(grade);
        gradeText.setFont(Font.font("Comic Sans MS", FontWeight.BOLD, 130));
        gradeText.setFill(Color.web(titleColor));
        gradeText.setStyle("-fx-effect: dropshadow(gaussian," + titleColor + ",44,0.65,0,0);");
        Text titleText = new Text(endingTitle(grade));
        titleText.setFont(Font.font("Comic Sans MS", FontWeight.BOLD, 30));
        titleText.setFill(Color.web(titleColor));
        Label scores = new Label(
                "📊 สรุปผลการสอบ\n\n" +
                        "💻 Programming:  " + progExam1Score + "  |  " + progExam2Score + "  →  เฉลี่ย " + progAvg + "\n" +
                        "📐 Math:              " + mathExam1Score + "  |  " + mathExam2Score + "  →  เฉลี่ย " + mathAvg + "\n\n" +
                        "📈 คะแนนเฉลี่ยรวม:  " + overall);
        scores.setStyle("-fx-font-family:'Comic Sans MS';-fx-font-size:20px;-fx-text-fill:#c8d8f0;-fx-line-spacing:6;");
        scores.setTextAlignment(TextAlignment.CENTER);
        Button again = navBtn("🔄  เล่นใหม่");
        Button quit  = navBtn("🚪  ออกจากเกม");
        again.setOnAction(e -> runOnce(this::startNewGame));
        quit .setOnAction(e -> stage.close());
        HBox btns = new HBox(28, again, quit); btns.setAlignment(Pos.CENTER);
        content.getChildren().addAll(gradeText, titleText, scores, btns);
        content.setOpacity(0);
        root.getChildren().add(content);
        setContent(root);
        FadeTransition ft = new FadeTransition(Duration.millis(600), content);
        ft.setToValue(1); ft.play();
    }

    private String[] endingStory(String grade) {
        return switch (grade) {
            case "A" -> new String[]{"ผลสอบออกมาแล้ว...","เกรด A — ทุกวิชาผ่านด้วยคะแนนสูง",
                    "ภาพย้อนกลับมา... คืนที่อ่านหนังสือข้ามวันข้ามคืน",
                    "ทุกหยดเหงื่อที่ทุ่มเทลงไป — มันคุ้มค่า",
                    "โทรหาเพื่อน — 'เฮ้! เราได้ A แล้วนะ!'",
                    "กลับบ้าน นอนลงบนที่นอนนุ่มๆ","แล้วก็ยิ้ม... 'ปี 2 ฉันจะเอา A อีกครั้ง'"};
            case "B" -> new String[]{"ผลสอบออกมาแล้ว...","เกรด B — ไม่ใช่สิ่งที่หวัง แต่ก็ภูมิใจ",
                    "MC นั่งคิด — 'ถ้าจัดเวลาให้ดีกว่านี้สักนิด...'",
                    "Nobody is perfect — แต่ทุกคนพัฒนาได้",
                    "MC พิมพ์โน้ตแผนการอ่านใหม่ พร้อมกว่าเดิม"};
            case "C" -> new String[]{"ผลสอบออกมาแล้ว...","เกรด C — รอดมาได้ แม้จะหนักแค่ไหน",
                    "MC หายใจลึกๆ — 'อย่างน้อยก็ผ่าน'",
                    "MC งีบหลับระหว่างทวน — ร่างกายต้องการพักบ้างแล้ว"};
            case "D" -> new String[]{"ผลสอบออกมาแล้ว...","เกรด D — ผ่านแบบ Probation ฉิวเฉียด",
                    "น้ำตาไหลออกมาโดยไม่รู้ตัว",
                    "'ฉันทำดีที่สุดแล้ว...' — MC พึมพำ",
                    "ลมหายใจค่อยๆ นิ่ง — ใจยังก้าวต่อไปได้"};
            default  -> new String[]{"ผลสอบออกมาแล้ว...","เกรด F — Retired จากคณะ",
                    "น้ำตาหยดลงบนหน้าจอ","'ฉันทำดีที่สุดที่ฉันทำได้แล้ว'",
                    "MC เปิดเว็บ TCAS ขึ้นมา เลื่อนดูคณะต่างๆ อย่างตั้งใจ",
                    "Hope dies last — ความหวังไม่มีวันตาย"};
        };
    }

    private String endingTitle(String grade) {
        return switch (grade) {
            case "A" -> "Ending A  —  All Stars Passed";
            case "B" -> "Ending B  —  Nobody Is Perfect";
            case "C" -> "Ending C  —  Struggling Success";
            case "D" -> "Ending D  —  Probation";
            default  -> "Ending F  —  Hope Dies Last";
        };
    }

    private String endingColor(String grade) {
        return switch (grade) {
            case "A" -> "#ffe082"; case "B" -> "#80cbc4";
            case "C" -> "#4fc3f7"; case "D" -> "#ce93d8";
            default  -> "#ef9a9a";
        };
    }

    private void showRefusalEnding() {
        agreedToTerms = false;
        StackPane intro = new StackPane();
        intro.setStyle("-fx-background-color:#000;");
        Label story = new Label();
        story.setTextFill(Color.WHITE);
        story.setFont(Font.font("Comic Sans MS", 24));
        story.setWrapText(true);
        story.setMaxWidth(900);
        story.setOpacity(0);
        VBox c = new VBox(story);
        c.setAlignment(Pos.CENTER);
        c.setStyle("-fx-padding:60;");
        intro.getChildren().add(c);
        setContent(intro);
        String[] segs = {
                "คุณปฏิเสธที่จะเล่นเกม",
                "บางครั้งการไม่ทำอะไรก็เป็นทางเลือก...",
                "แต่ GPA ก็ไม่รอดเหมือนกัน 😢"
        };
        FadeTransition ft = fade(story, 0, 1, 0.8);
        ft.setOnFinished(e -> typeSegments(story, segs, stage::close));
        ft.play();
    }

    // ── Save / Load ───────────────────────────────────────────────────────────

    private Path getSavePath() { return Paths.get(System.getProperty("user.home"), SAVE_FILE_NAME); }

    private void loadFromDisk() {
        agreedToTerms = false; hasSavedGame = false;
        Path path = getSavePath(); if (!Files.exists(path)) return;
        Properties props = new Properties();
        try (InputStream in = Files.newInputStream(path)) { props.load(in); } catch (Exception e) { return; }
        agreedToTerms = Boolean.parseBoolean(props.getProperty("termsAgreed", "false"));
        hasSavedGame  = Boolean.parseBoolean(props.getProperty("hasSavedGame", "false"));
        AudioManager audio = AudioManager.getInstance();
        try {
            audio.setGameVolume(Double.parseDouble(props.getProperty("vol.game", "1.0")));
            audio.setSfxVolume(Double.parseDouble(props.getProperty("vol.sfx", "0.8")));
            audio.setMusicVolume(Double.parseDouble(props.getProperty("vol.music", "0.6")));
            audio.setResolution(Integer.parseInt(props.getProperty("res.width", "1920")),
                    Integer.parseInt(props.getProperty("res.height", "1080")));
        } catch (NumberFormatException ignored) {}
        if (!hasSavedGame) return;
        try {
            player = new Player(Integer.parseInt(props.getProperty("energy", "6")),
                    Integer.parseInt(props.getProperty("intelligence", "0")),
                    Integer.parseInt(props.getProperty("mood", "60")));
            timeSystem = new TimeSystem();
            timeSystem.setCurrentDay(Integer.parseInt(props.getProperty("day", "1")));
            timeSystem.setCurrentHour(Integer.parseInt(props.getProperty("hour", "8")));
            eventManager = new EventManager(); EventRegistry.registerAll(eventManager);
            eventManager.setEventsToday(Integer.parseInt(props.getProperty("eventsToday", "0")));
            progExam1Score = Integer.parseInt(props.getProperty("progExam1Score", "0"));
            mathExam1Score = Integer.parseInt(props.getProperty("mathExam1Score", "0"));
            progExam2Score = Integer.parseInt(props.getProperty("progExam2Score", "0"));
            mathExam2Score = Integer.parseInt(props.getProperty("mathExam2Score", "0"));
            progExamTakenToday = Boolean.parseBoolean(props.getProperty("progExamTakenToday", "false"));
            mathExamTakenToday = Boolean.parseBoolean(props.getProperty("mathExamTakenToday", "false"));
            int count = Integer.parseInt(props.getProperty("effectsCount", "0"));
            for (int i = 0; i < count; i++) {
                String cn = props.getProperty("effect." + i + ".class"); if (cn == null) continue;
                int rem = Integer.parseInt(props.getProperty("effect." + i + ".remaining", "0"));
                StatusEffect eff = (StatusEffect) Class.forName(cn).getDeclaredConstructor().newInstance();
                if (eff instanceof SeniorNoteBuff snb) snb.setDaysRemaining(rem); else eff.setRemainingDuration(rem);
                player.addEffect(eff);
            }
        } catch (Exception ignored) {
            agreedToTerms = false; hasSavedGame = false;
            player = null; timeSystem = null; eventManager = null;
        }
    }

    private void persistSave(boolean force) {
        if (!force && System.currentTimeMillis() - lastSaveMillis < 2000) return;
        lastSaveMillis = System.currentTimeMillis();
        Properties props = new Properties();
        props.setProperty("version", SAVE_VERSION);
        props.setProperty("termsAgreed", String.valueOf(agreedToTerms));
        props.setProperty("hasSavedGame", String.valueOf(hasSavedGame));
        AudioManager audio = AudioManager.getInstance();
        props.setProperty("vol.game",  String.valueOf(audio.getGameVolume()));
        props.setProperty("vol.sfx",   String.valueOf(audio.getSfxVolume()));
        props.setProperty("vol.music", String.valueOf(audio.getMusicVolume()));
        props.setProperty("res.width",  String.valueOf(audio.getResolutionWidth()));
        props.setProperty("res.height", String.valueOf(audio.getResolutionHeight()));
        if (hasSavedGame && player != null && timeSystem != null && eventManager != null) {
            props.setProperty("day",   String.valueOf(timeSystem.getCurrentDay()));
            props.setProperty("hour",  String.valueOf(timeSystem.getCurrentHour()));
            props.setProperty("energy",        String.valueOf(player.getStat(StatType.ENERGY)));
            props.setProperty("intelligence",   String.valueOf(player.getStat(StatType.INTELLIGENCE)));
            props.setProperty("mood",           String.valueOf(player.getStat(StatType.MOOD)));
            props.setProperty("progExam1Score", String.valueOf(progExam1Score));
            props.setProperty("mathExam1Score", String.valueOf(mathExam1Score));
            props.setProperty("progExam2Score", String.valueOf(progExam2Score));
            props.setProperty("mathExam2Score", String.valueOf(mathExam2Score));
            props.setProperty("eventsToday",        String.valueOf(eventManager.getEventsToday()));
            props.setProperty("progExamTakenToday", String.valueOf(progExamTakenToday));
            props.setProperty("mathExamTakenToday", String.valueOf(mathExamTakenToday));
            List<StatusEffect> effects = player.getActiveEffects();
            props.setProperty("effectsCount", String.valueOf(effects.size()));
            for (int i = 0; i < effects.size(); i++) {
                StatusEffect eff = effects.get(i);
                props.setProperty("effect." + i + ".class",     eff.getClass().getName());
                props.setProperty("effect." + i + ".remaining", String.valueOf(eff.getRemainingDuration()));
            }
        }
        try {
            Path p = getSavePath();
            Path par = p.getParent();
            if (par != null) Files.createDirectories(par);
            try (OutputStream out = Files.newOutputStream(p)) { props.store(out, "Save My GPA"); }
        } catch (Exception ignored) {}
    }

    private void maybeSaveProgress(boolean force) {
        if (!agreedToTerms || !hasSavedGame || player == null || timeSystem == null || eventManager == null) return;
        persistSave(force);
    }

    // ── Animation helpers ─────────────────────────────────────────────────────

    private FadeTransition fade(javafx.scene.Node n, double from, double to, double secs) {
        FadeTransition ft = new FadeTransition(Duration.seconds(secs), n);
        ft.setFromValue(from); ft.setToValue(to); return ft;
    }
    private PauseTransition pause(double s) { return new PauseTransition(Duration.seconds(s)); }

    private void typeSegments(Label target, String[] segs, Runnable onDone) {
        target.setText("");
        SequentialTransition seq = new SequentialTransition();
        for (int idx = 0; idx < segs.length; idx++) {
            String seg = segs[idx];
            Timeline tl = new Timeline();
            for (int i = 0; i < seg.length(); i++) {
                final int next = i + 1;
                tl.getKeyFrames().add(new KeyFrame(Duration.millis(48 * i),
                        e -> target.setText(seg.substring(0, next))));
            }
            seq.getChildren().add(tl);
            seq.getChildren().add(pause(1.1));
            if (idx < segs.length - 1) {
                FadeTransition out = fade(target, 1, 0, 0.3);
                out.setOnFinished(e -> target.setText(""));
                seq.getChildren().addAll(out, fade(target, 0, 1, 0.2));
            }
        }
        if (onDone != null) seq.setOnFinished(e -> onDone.run());
        seq.play();
    }

    private Button navBtn(String text) {
        Button btn = new Button(text);
        btn.setStyle("""
            -fx-font-family: 'Comic Sans MS';
            -fx-font-size: 18px;
            -fx-background-color: rgba(79,195,247,0.85);
            -fx-text-fill: #0a1628;
            -fx-font-weight: bold;
            -fx-background-radius: 14;
            -fx-padding: 10 28 10 28;
            -fx-cursor: hand;
        """);
        btn.setOnMouseEntered(e -> btn.setOpacity(0.82));
        btn.setOnMouseExited(e  -> btn.setOpacity(1.00));
        return btn;
    }

    public static void main(String[] args) { launch(args); }
}