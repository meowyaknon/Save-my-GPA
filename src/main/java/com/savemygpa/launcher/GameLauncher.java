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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.*;
import javafx.stage.Screen;
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
    private final StackPane root = new StackPane();
    private final StackPane gameLayer = new StackPane();
    private final StackPane overlayLayer = new StackPane();

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

    private static final int    TOTAL_DAYS    = 14;
    private static final String SAVE_VERSION  = "1";
    private static final String SAVE_FILE_NAME= "save_my_gpa_save.properties";
    private long lastSaveMillis = 0;

    private static final String LOGO_PATH = "/images/menu/logo.png";

    // ── TYPING SPEED (ms per character) ──────────────────────────────────────
    private static final int TYPING_MS = 80;

    private void runOnce(Runnable action) {
        if (actionLocked) return;
        action.run();
        actionLocked = true;
        PauseTransition pt = new PauseTransition(Duration.millis(600));
        pt.setOnFinished(e -> actionLocked = false);
        pt.play();
    }

    // =========================================================================
    // start() — auto-scale to any physical screen
    // =========================================================================
    @Override
    public void start(Stage stage) {
        this.stage = stage;
        stage.setTitle("Save My GPA");

        Scene scene = new Scene(root);
        stage.setScene(scene);

        overlayLayer.setMouseTransparent(true);
        root.getChildren().addAll(gameLayer, overlayLayer);
        root.setStyle("-fx-background-color: black;");

        // Start at exact 16:9 window — no black bars in windowed mode
        stage.setWidth(1280);
        stage.setHeight(720);
        stage.centerOnScreen();
        stage.setResizable(true);
        stage.show();

        // Enforce 16:9 aspect ratio — width drives height
        stage.widthProperty().addListener((o, ov, nv) -> {
            if (!stage.isFullScreen()) {
                double h = nv.doubleValue() / (BASE_W / (double) BASE_H);
                if (Math.abs(stage.getHeight() - h) > 1) stage.setHeight(h);
            }
        });
        stage.heightProperty().addListener((o, ov, nv) -> {
            if (!stage.isFullScreen()) {
                double w = nv.doubleValue() * (BASE_W / (double) BASE_H);
                if (Math.abs(stage.getWidth() - w) > 1) stage.setWidth(w);
            }
        });

        applyScale();
        scene.widthProperty() .addListener((o, ov, nv) -> applyScale());
        scene.heightProperty().addListener((o, ov, nv) -> applyScale());

        loadFromDisk();
        AudioManager.getInstance().playMusic(AudioManager.Music.INTRO);
        if (agreedToTerms) showMainMenuWithFade(); else showIntroSequence();
    }

    private void applyScale() {
        double w = stage.getScene().getWidth();
        double h = stage.getScene().getHeight();
        if (w <= 0 || h <= 0) return;

        double scale = Math.min(w / BASE_W, h / BASE_H);

        // Scale both layers identically so overlays (popups, pause) match game coords
        for (javafx.scene.Node layer : new javafx.scene.Node[]{gameLayer, overlayLayer}) {
            ((javafx.scene.layout.StackPane) layer).setScaleX(scale);
            ((javafx.scene.layout.StackPane) layer).setScaleY(scale);
            ((javafx.scene.layout.StackPane) layer).setTranslateX((w - BASE_W * scale) / 2.0);
            ((javafx.scene.layout.StackPane) layer).setTranslateY((h - BASE_H * scale) / 2.0);
        }
    }

    private void setContent(javafx.scene.Node node) {
        gameLayer.getChildren().clear();
        gameLayer.getChildren().add(node);
    }

    // =========================================================================
    // New Game
    // =========================================================================
    private void startNewGame() {
        player = new Player(6, 0, 60);
        timeSystem = new TimeSystem();
        eventManager = new EventManager();
        EventRegistry.registerAll(eventManager);
        hasSavedGame = true;
        progExam1Score = mathExam1Score = progExam2Score = mathExam2Score = 0;
        progExamTakenToday = mathExamTakenToday = false;
        persistSave(true);
        showNewGameIntro();
    }

    private void showNewGameIntro() {
        AudioManager.getInstance().playMusic(AudioManager.Music.CUTSCENE);
        StackPane black = new StackPane();
        black.setStyle("-fx-background-color:#000;");
        black.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        Label lbl = makeCutsceneLabel();
        lbl.setOpacity(0);
        black.getChildren().add(lbl);
        setContent(black);
        FadeTransition fi = new FadeTransition(Duration.millis(600), black);
        fi.setFromValue(0); fi.setToValue(1);
        fi.setOnFinished(e -> {
            lbl.setOpacity(1);
            typeSegments(lbl, new String[]{
                    "ถึงเวลาเปิดเทอมแล้ว...",
                    "ได้เวลาเริ่มการศึกษา",
                    "สถาบันเทคโนโลยีพระจอมเกล้าเจ้าคุณทหารลาดกระบัง",
                    "คณะเทคโนโลยีสารสนเทศ  ปีที่ 1",
                    "ตื่นเต้น... กลัว... แต่ก็พร้อมสู้!",
                    "2 สัปดาห์ข้างหน้าจะเป็นยังไง?",
                    "ทุกอย่างขึ้นอยู่กับตัวเอง..."
            }, () -> {
                FadeTransition fo = new FadeTransition(Duration.millis(700), black);
                fo.setToValue(0);
                fo.setOnFinished(ev -> showGameplayWithFadeIn());
                fo.play();
            });
        });
        fi.play();
    }

    // =========================================================================
    // Gameplay
    // =========================================================================
    private void showGameplayWithFadeIn() {
        if (isGameOver()) { showEnding(); return; }
        AudioManager.getInstance().playMusic(AudioManager.Music.OUTSIDE);
        if (outsideUI != null) outsideUI.stopAnimations();
        outsideUI = new OutsideUI(player, timeSystem, eventManager, buildOutsideCallbacks());
        javafx.scene.Node gv = outsideUI.buildView();
        gv.setOpacity(0);
        setContent(gv);
        eventManager.triggerVisit(player, timeSystem, Location.OUTSIDE);
        outsideUI.refresh();
        maybeSaveProgress(false);
        FadeTransition ft = new FadeTransition(Duration.millis(800), gv);
        ft.setFromValue(0); ft.setToValue(1); ft.play();
    }

    private boolean isProgExamDay() { int d = timeSystem.getCurrentDay(); return d == 6 || d == 13; }
    private boolean isMathExamDay() { int d = timeSystem.getCurrentDay(); return d == 7 || d == 14; }
    private boolean isGameOver()    { return timeSystem.getCurrentDay() > TOTAL_DAYS; }

    private void onDayEnd() {
        player.getEffect(SeniorNoteBuff.class).ifPresent(b -> b.tickDay(player));
        player.removeEffect(WetFeetDebuff.class);
        player.removeEffect(NoStackOverflowDebuff.class);
        progExamTakenToday = mathExamTakenToday = false;
        eventManager.newDayReset();
        timeSystem.endDay();
        if (timeSystem.getCurrentDay() == 8) {
            int cur = player.getStat(StatType.INTELLIGENCE);
            player.changeStat(StatType.INTELLIGENCE, -cur);
            // CHANGE: replaced showDialog with image-based vertical card popup
            showDay8ScorePanel();
            return;
        }
        if (isGameOver()) { showEnding(); return; }
        showGameplay();
    }

    private void showDay8ScorePanel() {
        StackPane overlay = new StackPane();
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.62);");
        overlay.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        // Vertical card — image background
        StackPane card = new StackPane();
        card.setMaxWidth(460);

        var bgUrl = getClass().getResource("/images/popup/big_block_black_V.png");
        if (bgUrl != null) {
            ImageView bgIv = new ImageView(new Image(bgUrl.toExternalForm()));
            bgIv.setFitWidth(460);
            bgIv.setPreserveRatio(true);
            card.getChildren().add(bgIv);
        } else {
            // Plain fallback
            card.setStyle("""
                -fx-background-color: rgba(8,8,24,0.97);
                -fx-background-radius: 24;
                -fx-min-width: 460; -fx-min-height: 540;
                -fx-effect: dropshadow(gaussian,rgba(0,0,0,0.85),28,0.65,0,5);
            """);
        }

        // ── Text content ──────────────────────────────────────────────────────
        VBox body = new VBox(16);
        body.setAlignment(Pos.CENTER);
        body.setStyle("-fx-padding: 56 36 44 36;");

        Text heading = new Text("📚 รอบสอบแรกจบแล้ว!");
        heading.setFont(Font.font("Comic Sans MS", FontWeight.BOLD, 28));
        heading.setFill(Color.web("#ffe082"));
        heading.setStyle("-fx-effect: dropshadow(gaussian,rgba(255,224,130,0.5),12,0.4,0,0);");

        javafx.scene.shape.Line sep = new javafx.scene.shape.Line(0, 0, 340, 0);
        sep.setStroke(Color.web("#ffe082", 0.35));
        sep.setStrokeWidth(1.5);

        Label scores = new Label(
                "💻 Programming\n" + progExam1Score + " คะแนน\n\n" +
                        "📐 Math\n" + mathExam1Score + " คะแนน\n\n" +
                        "Intelligence รีเซ็ต\nสู้ต่อไป! 💪"
        );
        scores.setStyle("""
            -fx-font-family: 'Comic Sans MS';
            -fx-font-size: 22px;
            -fx-text-fill: #d8eeff;
            -fx-line-spacing: 4;
        """);
        scores.setTextAlignment(TextAlignment.CENTER);
        scores.setAlignment(Pos.CENTER);

        body.getChildren().addAll(heading, sep, scores);
        StackPane.setAlignment(body, Pos.CENTER);
        card.getChildren().add(body);

        // ── Red circle ✕ close button (top-right of card) ────────────────────
        StackPane closeBtn = new StackPane();
        closeBtn.setPrefSize(42, 42);
        closeBtn.setMaxSize(42, 42);
        closeBtn.setStyle("""
            -fx-background-color: #e53935;
            -fx-background-radius: 21;
            -fx-cursor: hand;
            -fx-effect: dropshadow(gaussian,rgba(0,0,0,0.55),7,0.5,0,2);
        """);
        Text xMark = new Text("✕");
        xMark.setFill(Color.WHITE);
        xMark.setFont(Font.font("Comic Sans MS", FontWeight.BOLD, 18));
        closeBtn.getChildren().add(xMark);
        closeBtn.setOnMouseEntered(e -> closeBtn.setOpacity(0.80));
        closeBtn.setOnMouseExited(e  -> closeBtn.setOpacity(1.00));
        closeBtn.setOnMouseClicked(e -> {
            FadeTransition fo = new FadeTransition(Duration.millis(150), overlay);
            fo.setToValue(0);
            fo.setOnFinished(ev -> { overlayLayer.getChildren().remove(overlay);
                overlayLayer.setMouseTransparent(true);
                showGameplay();
            });
            fo.play();
        });
        StackPane.setAlignment(closeBtn, Pos.TOP_RIGHT);
        closeBtn.setTranslateX(-10);
        closeBtn.setTranslateY(10);
        card.getChildren().add(closeBtn);

        overlay.getChildren().add(card);
        overlayLayer.setMouseTransparent(false);
        overlayLayer.getChildren().add(overlay);

        // Entrance
        card.setScaleX(0.88); card.setScaleY(0.88); card.setOpacity(0);
        ScaleTransition st = new ScaleTransition(Duration.millis(220), card);
        st.setToX(1); st.setToY(1);
        FadeTransition ft = new FadeTransition(Duration.millis(220), card);
        ft.setToValue(1);
        st.play(); ft.play();
    }

    private void doGoHome() {
        if (actionLocked) return;
        actionLocked = true;
        ActivityCutscene.play(overlayLayer, ActivityCutscene.lineFor("GoHomeActivity"), () -> {
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
            if (outsideUI != null) { if (msg != null) outsideUI.sayFail(msg); else outsideUI.sayFail(); }
            return;
        }
        actionLocked = true;
        ActivityCutscene.play(overlayLayer, ActivityCutscene.lineFor(activity.getClass().getSimpleName()), () -> {
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
            if (outsideUI != null) { if (msg != null) outsideUI.sayFail(msg); else outsideUI.sayFail(); }
            return false;
        }
        activity.performActivity(player, timeSystem, eventManager);
        if (location != null) eventManager.triggerAfterActivity(player, timeSystem, location);
        if (outsideUI != null) { outsideUI.refresh(); outsideUI.saySuccess(); }
        maybeSaveProgress(false);
        return true;
    }

    private void showDialog(String title, String msg, Runnable onClose) {
        GameDialog.show(overlayLayer, title, msg, onClose);
    }

    // ── Pause ─────────────────────────────────────────────────────────────────
    private void showPause(PauseOrigin origin) {
        if (pauseOpen) return;
        pauseOpen = true; pauseOrigin = origin;
        PauseMenuUI pauseUI = new PauseMenuUI(new PauseMenuUI.Callbacks() {
            @Override public void onResume() {
                if (pauseOverlay != null) {
                    FadeTransition ft = new FadeTransition(Duration.millis(180), pauseOverlay);
                    ft.setToValue(0);
                    ft.setOnFinished(e -> { overlayLayer.getChildren().remove(pauseOverlay);
                        overlayLayer.setMouseTransparent(true);
                        pauseOverlay = null;
                        pauseOpen = false;
                    });
                    ft.play();
                }
            }
            @Override public void onSettings() {
                overlayLayer.getChildren().remove(pauseOverlay);
                overlayLayer.setMouseTransparent(true);
                pauseOverlay = null;
                pauseOpen = false;
                showSettings(pauseOrigin == PauseOrigin.OUTSIDE ? GameLauncher.this::showGameplay : GameLauncher.this::showITBuilding);
            }
            @Override public void onMainMenu() {
                overlayLayer.getChildren().remove(pauseOverlay);
                overlayLayer.setMouseTransparent(true);
                pauseOverlay = null;
                pauseOpen = false;
                showMainMenuWithFade();
            }
        });
        pauseOverlay = pauseUI.buildView();
        overlayLayer.setMouseTransparent(false);
        overlayLayer.getChildren().add(pauseOverlay);
    }

    // =========================================================================
    // Screens
    // =========================================================================
    private void showIntroSequence() {
        StackPane intro = new StackPane();
        intro.setStyle("-fx-background-color:#000;");
        Text t2 = new Text("ทีมผู้สร้าง SaveMyGPA Team");
        t2.setFill(Color.WHITE);
        t2.setFont(Font.font("Comic Sans MS", FontWeight.SEMI_BOLD, 32));
        t2.setOpacity(0);
        Label story = makeCutsceneLabel();
        story.setOpacity(0);
        ImageView logoView = buildLogoView(700);
        logoView.setOpacity(0);
        VBox content = new VBox(28);
        content.setAlignment(Pos.CENTER);
        content.setMaxWidth(BASE_W); content.setMaxHeight(BASE_H);
        content.getChildren().addAll(logoView, t2, story);
        intro.getChildren().add(content);
        setContent(intro);

        FadeTransition logoIn  = fade(logoView, 0, 1, 1.0);
        FadeTransition logoOut = fade(logoView, 1, 0, 0.8);
        FadeTransition t2In    = fade(t2,    0, 1, 0.9);
        FadeTransition t2Out   = fade(t2,    1, 0, 0.8);
        FadeTransition storyIn = fade(story, 0, 1, 0.8);
        storyIn.setOnFinished(e -> {
            AudioManager.getInstance().playMusic(AudioManager.Music.CUTSCENE);
            typeSegments(story, new String[]{
                    "ในคืนที่นักศึกษาหญิงคนหนึ่งรอผลสอบ...",
                    "หัวใจเต้นแรงเมื่อรู้ว่า \"เธอสอบติดแล้ว!\"",
                    "คณะเทคโนโลยีสารสนเทศ KMITL",
                    "แต่ก่อนจะก้าวไปสู่บทเรียนถัดไป...",
                    "เธอต้องเลือกเส้นทางของตัวเอง"
            }, this::showAgreement);
        });
        new SequentialTransition(logoIn, pause(0.8), logoOut, t2In, pause(0.6), t2Out, storyIn).play();
    }

    private void showAgreement() {
        AudioManager.getInstance().playMusic(AudioManager.Music.ACCEPTANCE);
        AcceptanceUI ui = new AcceptanceUI(new AcceptanceUI.Callbacks() {
            @Override public void onAccept() {
                AudioManager.getInstance().playAccept();
                agreedToTerms = true; persistSave(true);
                fadeOutThenRun(v -> showMainMenuWithFade());
            }
            @Override public void onRefuse() {
                AudioManager.getInstance().playRefuse();
                showSecretEnding();
            }
        });
        javafx.scene.Node av = ui.buildView();
        av.setOpacity(0); setContent(av);
        FadeTransition ft = new FadeTransition(Duration.millis(500), av);
        ft.setFromValue(0); ft.setToValue(1); ft.play();
    }

    private void fadeOutThenRun(java.util.function.Consumer<Void> then) {
        if (overlayLayer.getChildren().isEmpty()) { then.accept(null); return; }
        javafx.scene.Node cur = overlayLayer.getChildren().get(0);
        FadeTransition out = new FadeTransition(Duration.millis(450), cur);
        out.setFromValue(cur.getOpacity()); out.setToValue(0);
        out.setOnFinished(e -> then.accept(null)); out.play();
    }

    private void showMainMenuWithFade() {
        AudioManager.getInstance().playMusic(AudioManager.Music.MAIN_MENU);
        actionLocked = false; pauseOpen = false;
        MainMenuUI ui = new MainMenuUI(hasSavedGame, new MainMenuUI.Callbacks() {
            @Override public void onContinue()  { AudioManager.getInstance().playAccept(); runOnce(GameLauncher.this::showGameplayWithFadeIn); }
            @Override public void onNewGame()   { AudioManager.getInstance().playAccept(); runOnce(GameLauncher.this::startNewGame); }
            @Override public void onHowToPlay() { AudioManager.getInstance().playAccept(); runOnce(GameLauncher.this::showHowToPlay); }
            @Override public void onSettings()  { AudioManager.getInstance().playAccept(); runOnce(() -> showSettings(GameLauncher.this::showMainMenuWithFade)); }
            @Override public void onCredits()   { AudioManager.getInstance().playAccept(); runOnce(GameLauncher.this::showCredits); }
            @Override public void onQuit()      { AudioManager.getInstance().playRefuse(); stage.close(); }
        });
        javafx.scene.Node mv = ui.buildView();
        mv.setOpacity(0); setContent(mv);
        FadeTransition ft = new FadeTransition(Duration.millis(500), mv);
        ft.setFromValue(0); ft.setToValue(1); ft.play();
    }

    private void showSettings(Runnable onBack) {
        SettingsUI ui = new SettingsUI(AudioManager.getInstance(),
                () -> ActivityCutscene.transition(overlayLayer, onBack, null));
        ActivityCutscene.transition(overlayLayer, () -> setContent(ui.buildView()), null);
    }

    private void showHowToPlay() {
        ActivityCutscene.transition(overlayLayer, () -> {
            HowToPlayUI ui = new HowToPlayUI(() -> runOnce(() -> {
                AudioManager.getInstance().playRefuse();
                ActivityCutscene.transition(overlayLayer, GameLauncher.this::showMainMenuWithFade, null);
            }));
            setContent(ui.buildView());
        }, null);
    }

    private void showCredits() {
        ActivityCutscene.transition(overlayLayer, () -> {
            CreditsUI ui = new CreditsUI(() -> runOnce(() -> {
                AudioManager.getInstance().playRefuse();
                ActivityCutscene.transition(overlayLayer, GameLauncher.this::showMainMenuWithFade, null);
            }));
            setContent(ui.buildView());
        }, null);
    }

    private void showGameplay() {
        if (isGameOver()) { showEnding(); return; }
        AudioManager.getInstance().playMusic(AudioManager.Music.OUTSIDE);
        if (outsideUI != null) outsideUI.stopAnimations();
        outsideUI = new OutsideUI(player, timeSystem, eventManager, buildOutsideCallbacks());
        setContent(outsideUI.buildView());
        eventManager.triggerVisit(player, timeSystem, Location.OUTSIDE);
        outsideUI.refresh();
        maybeSaveProgress(false);
    }

    private OutsideUI.Callbacks buildOutsideCallbacks() {
        return new OutsideUI.Callbacks() {
            @Override public void onBusStop()    { AudioManager.getInstance().playAccept(); runOnce(GameLauncher.this::showBusStop); }
            @Override public void onCanteen()    { AudioManager.getInstance().playAccept(); runOnce(GameLauncher.this::doCanteen); }
            @Override public void onITBuilding() { AudioManager.getInstance().playAccept(); runOnce(GameLauncher.this::showITBuilding); }
            @Override public void onGoHome()     { AudioManager.getInstance().playAccept(); runOnce(GameLauncher.this::doGoHome); }
            @Override public void onPause()      { showPause(PauseOrigin.OUTSIDE); }
            @Override public void showMessage(String t, String m) { showDialog(t, m, null); }
            @Override public boolean doActivity(Activity a, Location l) { return performSilent(a, l); }
        };
    }

    private void doCanteen() {
        EatActivity eat = new EatActivity();
        RequirementReason r = eat.canPerform(player, timeSystem);
        if (r != null) { String m = eat.getFailMessage(r); if (outsideUI != null) { if (m != null) outsideUI.sayFail(m); else outsideUI.sayFail(); } return; }
        actionLocked = true;
        ActivityCutscene.play(overlayLayer, ActivityCutscene.lineFor("EatActivity"), () -> {
            eat.performActivity(player, timeSystem, eventManager);
            eventManager.triggerAfterActivity(player, timeSystem, Location.CANTEEN);
            if (outsideUI != null) { outsideUI.refresh(); outsideUI.saySuccess(); }
            maybeSaveProgress(false);
            actionLocked = false;
            AudioManager.getInstance().playMusic(AudioManager.Music.OUTSIDE);
        });
    }

    private void showBusStop() {
        new BusStopUI(root, new BusStopUI.Callbacks() {   // ← was overlayLayer
            @Override public void onKLLC()   { AudioManager.getInstance().playAccept(); performWithCutscene(new KLLCActivity(), Location.BUS_STOP, GameLauncher.this::showGameplay); }
            @Override public void onGoHome() { AudioManager.getInstance().playAccept(); doGoHome(); }
            @Override public void onBack()   { AudioManager.getInstance().playRefuse(); }
        }).show();
    }

    private void showITBuilding() {
        AudioManager.getInstance().playMusic(AudioManager.Music.INSIDE);
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
            @Override public void onClassroom()  { AudioManager.getInstance().playAccept(); performWithCutscene(new ClassroomActivity(), Location.CLASSROOM, GameLauncher.this::showITBuilding); }
            @Override public void onAuditorium() { AudioManager.getInstance().playAccept(); performWithCutscene(new AuditoriumActivity(), Location.AUDITORIUM, GameLauncher.this::showITBuilding); }
            @Override public void onCoworking()  { AudioManager.getInstance().playAccept(); showCoworkingSpace(); }
            @Override public void onProgExam()   { AudioManager.getInstance().playAccept(); doProgExam(); }
            @Override public void onMathExam()   { AudioManager.getInstance().playAccept(); doMathExam(); }
            @Override public void onBack() {
                if (actionLocked) return;
                AudioManager.getInstance().playRefuse();
                actionLocked = true;
                ActivityCutscene.transition(overlayLayer, () -> { actionLocked = false; showGameplay(); }, null);
            }
        });
        javafx.scene.Node iv = ui.buildView();
        iv.setOpacity(0); setContent(iv);
        FadeTransition fi = new FadeTransition(Duration.millis(300), iv);
        fi.setFromValue(0); fi.setToValue(1); fi.play();
    }

    private void showCoworkingSpace() {
        new CoworkingUI(root, new CoworkingUI.Callbacks() {   // ← was overlayLayer
            @Override public void onRelax() { AudioManager.getInstance().playAccept(); performWithCutscene(new CoworkingRelaxActivity(), Location.COWORKING, GameLauncher.this::showITBuilding); }
            @Override public void onStudy() { AudioManager.getInstance().playAccept(); performWithCutscene(new CoworkingStudyActivity(), Location.COWORKING, GameLauncher.this::showITBuilding); }
            @Override public void onBack()  { AudioManager.getInstance().playRefuse(); }
        }).show();
    }

    // ── Exams — CHANGE: removed all popups (completion + already-taken) ────────
    private void doProgExam() {
        // CHANGE: silently no-op if already taken (was: showDialog)
        if (progExamTakenToday) return;
        ExamActivity exam = new ExamActivity();
        RequirementReason r = exam.canPerform(player, timeSystem);
        if (r != null) { if (outsideUI != null) outsideUI.sayFail(exam.getFailMessage(r)); return; }
        progExamTakenToday = true;
        actionLocked = true;
        CaptchaMiniGame[] ref = new CaptchaMiniGame[1];
        ref[0] = new CaptchaMiniGame(player, () -> {
            performSilent(exam, Location.CLASSROOM);
            // CHANGE: save score silently, no dialog shown
            if (timeSystem.getCurrentDay() <= 7) progExam1Score = ref[0].getTotalScore();
            else                                  progExam2Score = ref[0].getTotalScore();
            actionLocked = false;
            showITBuilding();
        });
        setContent(ref[0].getView());
        stage.setFullScreenExitHint(""); stage.setFullScreen(true);
    }

    private void doMathExam() {
        // CHANGE: silently no-op if already taken (was: showDialog)
        if (mathExamTakenToday) return;
        ExamActivity exam = new ExamActivity();
        RequirementReason r = exam.canPerform(player, timeSystem);
        if (r != null) { if (outsideUI != null) outsideUI.sayFail(exam.getFailMessage(r)); return; }
        mathExamTakenToday = true;
        performSilent(exam, Location.CLASSROOM);
        actionLocked = true;
        CountingMiniGame[] ref = new CountingMiniGame[1];
        ref[0] = new CountingMiniGame(player, () -> {
            // CHANGE: save score silently, no dialog shown
            if (timeSystem.getCurrentDay() <= 7) mathExam1Score = ref[0].getTotalScore();
            else                                  mathExam2Score = ref[0].getTotalScore();
            actionLocked = false;
            showITBuilding();
        });
        setContent(ref[0].getView());
        stage.setFullScreenExitHint(""); stage.setFullScreen(true);
    }

    // =========================================================================
    // Endings
    // =========================================================================
    private void showEnding() {
        AudioManager.getInstance().playMusic(AudioManager.Music.CUTSCENE);
        int progAvg = (progExam1Score + progExam2Score) / 2;
        int mathAvg = (mathExam1Score + mathExam2Score) / 2;
        int overall = (progAvg + mathAvg) / 2;
        String grade = overall >= 80 ? "A" : overall >= 70 ? "B" : overall >= 60 ? "C" : overall >= 50 ? "D" : "F";
        StackPane black = new StackPane();
        black.setStyle("-fx-background-color:#000;"); black.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        Label lbl = makeCutsceneLabel(); lbl.setOpacity(0);
        black.getChildren().add(lbl); setContent(black);
        FadeTransition fi = new FadeTransition(Duration.millis(900), black);
        fi.setFromValue(0); fi.setToValue(1);
        final String fg = grade; final int fP = progAvg, fM = mathAvg, fO = overall;
        fi.setOnFinished(e -> { lbl.setOpacity(1); typeSegments(lbl, endingStory(fg), () -> showEndingResult(fg, fO, fP, fM)); });
        fi.play();
    }

    private void showEndingResult(String grade, int overall, int progAvg, int mathAvg) {
        String c = endingColor(grade);
        StackPane root = new StackPane(); root.setStyle("-fx-background-color:#000;");
        VBox content = new VBox(28); content.setAlignment(Pos.CENTER); content.setStyle("-fx-padding:60;");
        Text gt = new Text(grade);
        gt.setFont(Font.font("Comic Sans MS", FontWeight.BOLD, 130)); gt.setFill(Color.web(c));
        gt.setStyle("-fx-effect: dropshadow(gaussian,"+c+",44,0.65,0,0);");
        Text tt = new Text(endingTitle(grade));
        tt.setFont(Font.font("Comic Sans MS", FontWeight.BOLD, 30)); tt.setFill(Color.web(c));
        Label sc = new Label("📊 สรุปผลการสอบ\n\n💻 Programming:  "+progExam1Score+"  |  "+progExam2Score+"  →  เฉลี่ย "+progAvg+"\n📐 Math:              "+mathExam1Score+"  |  "+mathExam2Score+"  →  เฉลี่ย "+mathAvg+"\n\n📈 คะแนนเฉลี่ยรวม:  "+overall);
        sc.setStyle("-fx-font-family:'Comic Sans MS';-fx-font-size:20px;-fx-text-fill:#c8d8f0;-fx-line-spacing:6;");
        sc.setTextAlignment(TextAlignment.CENTER);
        Button again = navBtn("🔄  เล่นใหม่");
        Button quit  = navBtn("🚪  ออกจากเกม");
        again.setOnAction(e -> { AudioManager.getInstance().playAccept(); runOnce(this::startNewGame); });
        quit .setOnAction(e -> { AudioManager.getInstance().playRefuse(); stage.close(); });
        HBox btns = new HBox(28, again, quit); btns.setAlignment(Pos.CENTER);
        content.getChildren().addAll(gt, tt, sc, btns);
        content.setOpacity(0); root.getChildren().add(content); setContent(root);
        FadeTransition ft = new FadeTransition(Duration.millis(600), content);
        ft.setToValue(1);
        ft.play();
    }

    private String[] endingStory(String g) {
        return switch (g) {
            case "A" -> new String[]{"ผลสอบออกมาแล้ว...","เกรด A — ทุกวิชาผ่านด้วยคะแนนสูง","ทุกหยดเหงื่อที่ทุ่มเทลงไป — มันคุ้มค่า","แล้วก็ยิ้ม... 'ปี 2 ฉันจะเอา A อีกครั้ง'"};
            case "B" -> new String[]{"ผลสอบออกมาแล้ว...","เกรด B — ไม่ใช่สิ่งที่หวัง แต่ก็ภูมิใจ","Nobody is perfect — แต่ทุกคนพัฒนาได้"};
            case "C" -> new String[]{"ผลสอบออกมาแล้ว...","เกรด C — รอดมาได้ แม้จะหนักแค่ไหน","อย่างน้อยก็ผ่าน — สู้ต่อไปนะ!"};
            case "D" -> new String[]{"ผลสอบออกมาแล้ว...","เกรด D — ผ่านแบบ Probation ฉิวเฉียด","ลมหายใจค่อยๆ นิ่ง — ใจยังก้าวต่อไปได้"};
            default  -> new String[]{"ผลสอบออกมาแล้ว...","เกรด F — Retired จากคณะ","Hope dies last — ความหวังไม่มีวันตาย"};
        };
    }

    private String endingTitle(String g) {
        return switch (g) {
            case "A" -> "Ending A  —  All Stars Passed";
            case "B" -> "Ending B  —  Nobody Is Perfect";
            case "C" -> "Ending C  —  Struggling Success";
            case "D" -> "Ending D  —  Probation";
            default  -> "Ending F  —  Hope Dies Last";
        };
    }

    private String endingColor(String g) {
        return switch (g) {
            case "A" -> "#ffe082"; case "B" -> "#80cbc4";
            case "C" -> "#4fc3f7"; case "D" -> "#ce93d8";
            default  -> "#ef9a9a";
        };
    }

    private void showSecretEnding() {
        agreedToTerms = false;
        AudioManager.getInstance().playMusic(AudioManager.Music.CUTSCENE);
        StackPane black = new StackPane(); black.setStyle("-fx-background-color:#000;"); black.setMaxSize(Double.MAX_VALUE,Double.MAX_VALUE);
        Label lbl = makeCutsceneLabel(); lbl.setOpacity(0);
        black.getChildren().add(lbl); setContent(black);
        FadeTransition fi = new FadeTransition(Duration.millis(800), black);
        fi.setFromValue(0); fi.setToValue(1);
        fi.setOnFinished(e -> { lbl.setOpacity(1); typeSegments(lbl, new String[]{
                "คุณปฏิเสธที่จะเข้าเรียน...",
                "แต่บางทีเส้นทางชีวิตก็ไม่ได้มีแค่ทางเดียว",
                "เธอวางกระเป๋านักศึกษาลง...",
                "แล้วหยิบสมุดสเก็ตช์ขึ้นมาแทน",
                "6 เดือนต่อมา — แกลเลอรีศิลปะแห่งแรกของเธอเปิดแล้ว",
                "คือจุดเริ่มต้นที่ยิ่งใหญ่ที่สุดก็ได้"
        }, this::showSecretEndingResult); });
        fi.play();
    }

    private void showSecretEndingResult() {
        final String ac = "#f48fb1";
        StackPane root = new StackPane(); root.setStyle("-fx-background-color:#000;");
        VBox content = new VBox(32); content.setAlignment(Pos.CENTER); content.setStyle("-fx-padding:60;");
        Text gt = new Text("✦"); gt.setFont(Font.font("Comic Sans MS", FontWeight.BOLD, 120)); gt.setFill(Color.web(ac));
        gt.setStyle("-fx-effect:dropshadow(gaussian,"+ac+",50,0.70,0,0);");
        Text tt = new Text("Secret Ending  —  New Career Path");
        tt.setFont(Font.font("Comic Sans MS", FontWeight.BOLD, 30)); tt.setFill(Color.web(ac));
        Label desc = new Label("🎨  เส้นทางที่ไม่มีใครคาดถึง\n\nความกล้าที่จะเลือกเส้นทางของตัวเอง\n\n[ Unlocked: Secret Ending 🌟 ]");
        desc.setStyle("-fx-font-family:'Comic Sans MS';-fx-font-size:22px;-fx-text-fill:#f8d7e8;-fx-line-spacing:6;");
        desc.setTextAlignment(TextAlignment.CENTER);
        Button tryAgain = navBtnColored("🔄  ลองอีกครั้ง", ac, "#1a0010");
        Button quit = navBtn("🚪  ออกจากเกม");
        tryAgain.setOnAction(e -> { AudioManager.getInstance().playAccept(); runOnce(this::showIntroSequence); });
        quit    .setOnAction(e -> { AudioManager.getInstance().playRefuse(); stage.close(); });
        HBox btns = new HBox(32, tryAgain, quit); btns.setAlignment(Pos.CENTER);
        content.getChildren().addAll(gt, tt, desc, btns);
        content.setOpacity(0); root.getChildren().add(content); setContent(root);
        FadeTransition ft = new FadeTransition(Duration.millis(600), content);
        ft.setToValue(1);
        ft.play();
    }

    // =========================================================================
    // Save / Load
    // =========================================================================
    private Path getSavePath() { return Paths.get(System.getProperty("user.home"), SAVE_FILE_NAME); }

    private void loadFromDisk() {
        agreedToTerms = hasSavedGame = false;
        Path path = getSavePath(); if (!Files.exists(path)) return;
        Properties p = new Properties();
        try (InputStream in = Files.newInputStream(path)) { p.load(in); } catch (Exception e) { return; }
        agreedToTerms = Boolean.parseBoolean(p.getProperty("termsAgreed","false"));
        hasSavedGame  = Boolean.parseBoolean(p.getProperty("hasSavedGame","false"));
        AudioManager a = AudioManager.getInstance();
        try {
            a.setGameVolume(Double.parseDouble(p.getProperty("vol.game","1.0")));
            a.setSfxVolume(Double.parseDouble(p.getProperty("vol.sfx","0.8")));
            a.setMusicVolume(Double.parseDouble(p.getProperty("vol.music","0.6")));
        } catch (NumberFormatException ignored) {}
        if (!hasSavedGame) return;
        try {
            player = new Player(Integer.parseInt(p.getProperty("energy","6")),
                    Integer.parseInt(p.getProperty("intelligence","0")),
                    Integer.parseInt(p.getProperty("mood","60")));
            timeSystem = new TimeSystem();
            timeSystem.setCurrentDay(Integer.parseInt(p.getProperty("day","1")));
            timeSystem.setCurrentHour(Integer.parseInt(p.getProperty("hour","8")));
            eventManager = new EventManager(); EventRegistry.registerAll(eventManager);
            eventManager.setEventsToday(Integer.parseInt(p.getProperty("eventsToday","0")));
            progExam1Score = Integer.parseInt(p.getProperty("progExam1Score","0"));
            mathExam1Score = Integer.parseInt(p.getProperty("mathExam1Score","0"));
            progExam2Score = Integer.parseInt(p.getProperty("progExam2Score","0"));
            mathExam2Score = Integer.parseInt(p.getProperty("mathExam2Score","0"));
            progExamTakenToday = Boolean.parseBoolean(p.getProperty("progExamTakenToday","false"));
            mathExamTakenToday = Boolean.parseBoolean(p.getProperty("mathExamTakenToday","false"));
            int count = Integer.parseInt(p.getProperty("effectsCount","0"));
            for (int i = 0; i < count; i++) {
                String cn = p.getProperty("effect."+i+".class"); if (cn == null) continue;
                int rem = Integer.parseInt(p.getProperty("effect."+i+".remaining","0"));
                StatusEffect eff = (StatusEffect) Class.forName(cn).getDeclaredConstructor().newInstance();
                if (eff instanceof SeniorNoteBuff snb) snb.setDaysRemaining(rem); else eff.setRemainingDuration(rem);
                player.addEffect(eff);
            }
        } catch (Exception ignored) { agreedToTerms = hasSavedGame = false; player = null; timeSystem = null; eventManager = null; }
    }

    private void persistSave(boolean force) {
        if (!force && System.currentTimeMillis() - lastSaveMillis < 2000) return;
        lastSaveMillis = System.currentTimeMillis();
        Properties p = new Properties();
        p.setProperty("version", SAVE_VERSION);
        p.setProperty("termsAgreed", String.valueOf(agreedToTerms));
        p.setProperty("hasSavedGame", String.valueOf(hasSavedGame));
        AudioManager a = AudioManager.getInstance();
        p.setProperty("vol.game",  String.valueOf(a.getGameVolume()));
        p.setProperty("vol.sfx",   String.valueOf(a.getSfxVolume()));
        p.setProperty("vol.music", String.valueOf(a.getMusicVolume()));
        if (hasSavedGame && player != null && timeSystem != null && eventManager != null) {
            p.setProperty("day",  String.valueOf(timeSystem.getCurrentDay()));
            p.setProperty("hour", String.valueOf(timeSystem.getCurrentHour()));
            p.setProperty("energy",       String.valueOf(player.getStat(StatType.ENERGY)));
            p.setProperty("intelligence",  String.valueOf(player.getStat(StatType.INTELLIGENCE)));
            p.setProperty("mood",          String.valueOf(player.getStat(StatType.MOOD)));
            p.setProperty("progExam1Score",String.valueOf(progExam1Score));
            p.setProperty("mathExam1Score",String.valueOf(mathExam1Score));
            p.setProperty("progExam2Score",String.valueOf(progExam2Score));
            p.setProperty("mathExam2Score",String.valueOf(mathExam2Score));
            p.setProperty("eventsToday",        String.valueOf(eventManager.getEventsToday()));
            p.setProperty("progExamTakenToday", String.valueOf(progExamTakenToday));
            p.setProperty("mathExamTakenToday", String.valueOf(mathExamTakenToday));
            List<StatusEffect> effs = player.getActiveEffects();
            p.setProperty("effectsCount", String.valueOf(effs.size()));
            for (int i = 0; i < effs.size(); i++) {
                StatusEffect eff = effs.get(i);
                p.setProperty("effect."+i+".class",     eff.getClass().getName());
                p.setProperty("effect."+i+".remaining", String.valueOf(eff.getRemainingDuration()));
            }
        }
        try {
            Path sp = getSavePath(); Path par = sp.getParent();
            if (par != null) Files.createDirectories(par);
            try (OutputStream out = Files.newOutputStream(sp)) { p.store(out, "Save My GPA"); }
        } catch (Exception ignored) {}
    }

    private void maybeSaveProgress(boolean force) {
        if (!agreedToTerms || !hasSavedGame || player == null || timeSystem == null || eventManager == null) return;
        persistSave(force);
    }

    // =========================================================================
    // Helpers
    // =========================================================================
    private ImageView buildLogoView(double fitWidth) {
        var url = getClass().getResource(LOGO_PATH);
        if (url == null) { ImageView e = new ImageView(); e.setFitWidth(fitWidth); e.setFitHeight(120); return e; }
        ImageView iv = new ImageView(new Image(url.toExternalForm()));
        iv.setFitWidth(fitWidth); iv.setPreserveRatio(true); return iv;
    }

    private Label makeCutsceneLabel() {
        Label l = new Label("");
        l.setFont(Font.font("Comic Sans MS", FontWeight.BOLD, 42));
        l.setTextFill(Color.WHITE); l.setWrapText(true); l.setMaxWidth(1400);
        l.setTextAlignment(TextAlignment.CENTER); l.setAlignment(Pos.CENTER);
        l.setStyle("-fx-effect:dropshadow(gaussian,rgba(180,220,255,0.40),16,0.45,0,0);");
        StackPane.setAlignment(l, Pos.CENTER); return l;
    }

    private FadeTransition fade(javafx.scene.Node n, double from, double to, double secs) {
        FadeTransition ft = new FadeTransition(Duration.seconds(secs), n);
        ft.setFromValue(from); ft.setToValue(to); return ft;
    }

    private PauseTransition pause(double s) { return new PauseTransition(Duration.seconds(s)); }

    /**
     * Typewriter for story/ending cutscenes in GameLauncher.
     * TYPING SPEED CHANGE — line: Duration.millis(TYPING_MS * i)
     * Was 48 ms/char, now 80 ms/char.
     */
    private void typeSegments(Label target, String[] segs, Runnable onDone) {
        target.setText("");
        AudioManager audio = AudioManager.getInstance();
        SequentialTransition seq = new SequentialTransition();
        for (int idx = 0; idx < segs.length; idx++) {
            String seg = segs[idx];
            Timeline tl = new Timeline();
            for (int i = 0; i < seg.length(); i++) {
                final int next = i + 1; final char ch = seg.charAt(i);
                // CHANGE: 80 ms per char (was 48)
                tl.getKeyFrames().add(new KeyFrame(Duration.millis(TYPING_MS * i), e -> {
                    target.setText(seg.substring(0, next));
                    if (ch != ' ' && ch != '\n') audio.playTyping();
                }));
            }
            seq.getChildren().add(tl);
            seq.getChildren().add(pause(1.4));
            if (idx < segs.length - 1) {
                FadeTransition out = fade(target, 1, 0, 0.3);
                out.setOnFinished(e -> target.setText(""));
                seq.getChildren().addAll(out, fade(target, 0, 1, 0.2));
            }
        }
        if (onDone != null) seq.setOnFinished(e -> onDone.run());
        seq.play();
    }

    private Button navBtn(String text) { return navBtnColored(text,"rgba(79,195,247,0.85)","#0a1628"); }

    private Button navBtnColored(String text, String bg, String fg) {
        Button btn = new Button(text);
        btn.setStyle("""
            -fx-font-family:'Comic Sans MS';-fx-font-size:18px;
            -fx-background-color:%s;-fx-text-fill:%s;-fx-font-weight:bold;
            -fx-background-radius:14;-fx-padding:10 28 10 28;-fx-cursor:hand;
        """.formatted(bg, fg));
        btn.setOnMouseEntered(e -> btn.setOpacity(0.82));
        btn.setOnMouseExited(e  -> btn.setOpacity(1.00));
        return btn;
    }

    public static void main(String[] args) { launch(args); }
}