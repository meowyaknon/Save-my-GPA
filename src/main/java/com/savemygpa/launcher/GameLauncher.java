package com.savemygpa.launcher;

import com.savemygpa.audio.AudioManager;
import com.savemygpa.exam.CaptchaMiniGame;
import com.savemygpa.exam.CountingMiniGame;
import com.savemygpa.ui.*;
import com.savemygpa.util.GameCallbacks;
import javafx.application.Application;
import javafx.animation.*;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import com.savemygpa.player.*;
import com.savemygpa.player.effect.StatusEffect;
import com.savemygpa.player.effect.buff.SeniorNoteBuff;
import com.savemygpa.player.effect.debuff.WetFeetDebuff;
import com.savemygpa.player.effect.debuff.StackOverflowDownDebuff;
import com.savemygpa.core.*;
import com.savemygpa.activity.*;
import com.savemygpa.event.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;

public class GameLauncher extends Application implements GameCallbacks {

    private Stage        stage;
    private Player       player;
    private TimeSystem   timeSystem;
    private EventManager eventManager;

    private final StackPane root      = new StackPane();
    private final StackPane gameLayer = new StackPane();

    private static final int BASE_W = 1920, BASE_H = 1080;

    private OutsideUI outsideUI;
    private StackPane pauseOverlay = null;
    private boolean   pauseOpen    = false;
    private boolean   actionLocked = false;

    private enum ScreenOrigin { MAIN_MENU, OUTSIDE, IT_BUILDING }
    private ScreenOrigin screenOrigin = ScreenOrigin.MAIN_MENU;

    private boolean hasSavedGame = false, agreedToTerms = false;
    private int progExam1Score = 0, mathExam1Score = 0, progExam2Score = 0, mathExam2Score = 0;
    private boolean progExamTakenToday   = false;
    private boolean mathExamTakenToday   = false;
    private boolean examNoticeShownToday = false;

    private static final int    TOTAL_DAYS     = 14;
    private static final String SAVE_VERSION   = "1";
    private static final String SAVE_FILE_NAME = "save_my_gpa_save.properties";
    private long lastSaveMillis = 0;

    private static final String LOGO_PATH = "/images/menu/logo.png";
    private static final int    TYPING_MS = 80;

    static final String TEXT_COLOR = "#3b1a1a";

    private static final String[] ENDING_A_SCENES = {
            "/images/endings/A/1.jpg", "/images/endings/A/2.jpg",
            "/images/endings/A/3.jpg", "/images/endings/A/4.jpg",
            "/images/endings/A/5.jpg", "/images/endings/A/6.jpg",
            "/images/endings/A/7.jpg", "/images/endings/A/8.jpg",
    };
    private static final String[] ENDING_B_SCENES = {
            "/images/endings/B/1.jpg", "/images/endings/B/2.jpg",
            "/images/endings/B/3.jpg", "/images/endings/B/4.jpg",
            "/images/endings/B/5.jpg", "/images/endings/B/6.jpg",
    };
    private static final String[] ENDING_C_SCENES = {
            "/images/endings/C/1.JPG", "/images/endings/C/2.JPG",
            "/images/endings/C/3.JPG", "/images/endings/C/4.JPG",
            "/images/endings/C/5.JPG", "/images/endings/C/6.JPG",
    };
    private static final String[] ENDING_D_SCENES = {
            "/images/endings/D/1.JPG", "/images/endings/D/2.JPG",
            "/images/endings/D/3.JPG", "/images/endings/D/4.JPG",
            "/images/endings/D/5.JPG", "/images/endings/D/6.JPG",
    };
    private static final String[] ENDING_F_SCENES = {
            "/images/endings/F/1.JPG", "/images/endings/F/2.JPG",
            "/images/endings/F/3.JPG", "/images/endings/F/4.JPG",
            "/images/endings/F/5.JPG", "/images/endings/F/6.JPG",
    };

    // =========================================================================
    // start()
    // =========================================================================
    @Override
    public void start(Stage stage) {
        Font.loadFont(getClass().getResourceAsStream("/fonts/IBMPlexSansThai-Medium.ttf"), 14);
        this.stage = stage;
        stage.setTitle("Save My GPA");
        Scene scene = new Scene(root);
        stage.setScene(scene);
        root.getChildren().add(gameLayer);
        root.setStyle("-fx-background-color: black;");
        stage.setWidth(1280);
        stage.setHeight(720);
        stage.centerOnScreen();
        stage.setResizable(true);
        applyScale();
        scene.widthProperty() .addListener((o, ov, nv) -> applyScale());
        scene.heightProperty().addListener((o, ov, nv) -> applyScale());
        stage.show();
        loadFromDisk();
        AudioManager.getInstance().playMusic(AudioManager.Music.INTRO);
        if (agreedToTerms) showMainMenuWithFade(); else showIntroSequence();
    }

    private void applyScale() {
        double w = stage.getScene().getWidth();
        double h = stage.getScene().getHeight();
        if (w <= 0 || h <= 0) return;
        double scale = Math.min(w / BASE_W, h / BASE_H);
        gameLayer.setScaleX(scale); gameLayer.setScaleY(scale);
        gameLayer.setTranslateX((w - BASE_W * scale) / 2.0);
        gameLayer.setTranslateY((h - BASE_H * scale) / 2.0);
    }

    private void setContent(javafx.scene.Node node) {
        gameLayer.getChildren().clear();
        gameLayer.getChildren().add(node);
    }

    // =========================================================================
    // GameCallbacks — OutsideUI
    // =========================================================================
    @Override public void onBusStop()    { runOnce(this::showBusStop); }
    @Override public void onCanteen()    { runOnce(this::doCanteen); }
    @Override public void onITBuilding() { runOnce(this::showITBuilding); }

    // =========================================================================
    // GameCallbacks — InsideUI
    // =========================================================================
    @Override public boolean      isProgExamDay()   { return isProgExamDayInternal(); }
    @Override public boolean      isMathExamDay()   { return isMathExamDayInternal(); }
    @Override public Player       getPlayer()       { return player; }
    @Override public TimeSystem   getTimeSystem()   { return timeSystem; }
    @Override public EventManager getEventManager() { return eventManager; }
    @Override public OutsideUI    getOutsideUI()    { return outsideUI; }

    @Override public void onClassroom()  { AudioManager.getInstance().playAccept(); performWithCutscene(new ClassroomActivity(),  Location.CLASSROOM,  () -> showITBuilding(false)); }
    @Override public void onAuditorium() { AudioManager.getInstance().playAccept(); performWithCutscene(new AuditoriumActivity(), Location.AUDITORIUM, () -> showITBuilding(false)); }
    @Override public void onCoworking()  { AudioManager.getInstance().playAccept(); showCoworkingSpace(); }
    @Override public void onProgExam()   { AudioManager.getInstance().playAccept(); doProgExam(); }
    @Override public void onMathExam()   { AudioManager.getInstance().playAccept(); doMathExam(); }

    // =========================================================================
    // GameCallbacks — BusStopUI
    // =========================================================================
    @Override public void onKLLC()   { AudioManager.getInstance().playAccept(); performWithCutscene(new KLLCActivity(), Location.BUS_STOP, this::showGameplay); }
    @Override public void onGoHome() { AudioManager.getInstance().playAccept(); doGoHome(); }

    // =========================================================================
    // GameCallbacks — CoworkingUI
    // =========================================================================
    @Override public void onRelax() { AudioManager.getInstance().playAccept(); performWithCutscene(new CoworkingRelaxActivity(), Location.COWORKING, () -> showITBuilding(false)); }
    @Override public void onStudy() { AudioManager.getInstance().playAccept(); performWithCutscene(new CoworkingStudyActivity(), Location.COWORKING, () -> showITBuilding(false)); }

    // =========================================================================
    // GameCallbacks — AcceptanceUI
    // =========================================================================
    @Override public void onAccept() { AudioManager.getInstance().playAccept(); agreedToTerms = true; persistSave(true); showMainMenuWithFade(); }
    @Override public void onRefuse() { AudioManager.getInstance().playRefuse(); showSecretEnding(); }

    // =========================================================================
    // GameCallbacks — MainMenuUI
    // =========================================================================
    @Override public void onContinue()  { AudioManager.getInstance().playAccept(); runOnce(this::showGameplayWithFadeIn); }
    @Override public void onNewGame()   { AudioManager.getInstance().playAccept(); runOnce(this::startNewGame); }
    @Override public void onHowToPlay() { AudioManager.getInstance().playAccept(); runOnce(this::showHowToPlay); }
    @Override public void onCredits()   { AudioManager.getInstance().playAccept(); runOnce(this::showCredits); }
    @Override public void onQuit()      { AudioManager.getInstance().playRefuse(); stage.close(); }

    // =========================================================================
    // GameCallbacks — PauseMenuUI
    // =========================================================================
    @Override public void onResume() {
        if (pauseOverlay != null)
            PauseMenuUI.dismiss(root, pauseOverlay, () -> { pauseOverlay = null; pauseOpen = false; });
    }
    @Override public void onMainMenu() {
        if (pauseOverlay != null)
            PauseMenuUI.dismiss(root, pauseOverlay, () -> { pauseOverlay = null; pauseOpen = false; showMainMenuWithFade(); });
    }

    // =========================================================================
    // GameCallbacks — Shared
    // =========================================================================
    @Override public void onBack() {
        if (actionLocked) return;
        AudioManager.getInstance().playRefuse();
        actionLocked = true;
        ActivityCutscene.transition(root, () -> { actionLocked = false; showGameplay(); }, null);
    }

    @Override public void onSettings() {
        if (pauseOverlay != null) {
            PauseMenuUI.dismiss(root, pauseOverlay, () -> {
                pauseOverlay = null;
                pauseOpen    = false;
                showSettings();
            });
        } else {
            AudioManager.getInstance().playAccept();
            runOnce(this::showSettings);
        }
    }

    @Override public void onPause() { showPause(); }

    // =========================================================================
    // Helpers
    // =========================================================================
    private void runOnce(Runnable action) {
        if (actionLocked) return;
        action.run();
        actionLocked = true;
        PauseTransition pt = new PauseTransition(Duration.millis(600));
        pt.setOnFinished(e -> actionLocked = false);
        pt.play();
    }

    private void speakFail(String message) {
        if (outsideUI != null) {
            if (message != null) outsideUI.sayFail(message);
            else                 outsideUI.sayFail();
        }
    }

    private void speakEvent(String name) {
        if (outsideUI != null) outsideUI.sayEvent(name);
    }

    // =========================================================================
    // Event listener
    // =========================================================================
    private void wireEventListener() {
        if (eventManager == null) return;
        eventManager.setEventListener((name, description) -> {
            speakEvent("🎲 " + name);
            GameDialog.event(root, name, description, null);
        });
    }

    // =========================================================================
    // New Game
    // =========================================================================
    private void startNewGame() {
        player = new Player(6, 0, 60);
        timeSystem = new TimeSystem();
        eventManager = new EventManager();
        EventRegistry.registerAll(eventManager);
        wireEventListener();
        hasSavedGame = true;
        progExam1Score = mathExam1Score = progExam2Score = mathExam2Score = 0;
        progExamTakenToday = mathExamTakenToday = examNoticeShownToday = false;
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
        outsideUI = new OutsideUI(this);
        javafx.scene.Node gv = outsideUI.buildView();
        gv.setOpacity(0);
        setContent(gv);
        eventManager.triggerVisit(player, timeSystem, Location.OUTSIDE);
        outsideUI.refresh();
        maybeSaveProgress(false);
        FadeTransition ft = new FadeTransition(Duration.millis(800), gv);
        ft.setFromValue(0); ft.setToValue(1); ft.play();
    }

    private boolean isProgExamDayInternal() { int d = timeSystem.getCurrentDay(); return d == 6 || d == 13; }
    private boolean isMathExamDayInternal() { int d = timeSystem.getCurrentDay(); return d == 7 || d == 14; }
    private boolean isGameOver()            { return timeSystem.getCurrentDay() > TOTAL_DAYS; }

    private void onDayEnd() {
        player.getEffect(SeniorNoteBuff.class).ifPresent(b -> b.tickDay(player));
        player.removeEffect(WetFeetDebuff.class);
        player.removeEffect(StackOverflowDownDebuff.class);
        progExamTakenToday = mathExamTakenToday = false;
        examNoticeShownToday = false;
        eventManager.newDayReset();
        timeSystem.endDay();
        if (timeSystem.getCurrentDay() == 8) {
            int cur = player.getStat(StatType.INTELLIGENCE);
            player.changeStat(StatType.INTELLIGENCE, -cur);
            showDay8ScorePanel();
            return;
        }
        if (isGameOver()) { showEnding(); return; }
        showGameplay();
    }

    // ── Day-8 score panel ─────────────────────────────────────────────────────
    private void showDay8ScorePanel() {
        StackPane overlay = new StackPane();
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.62);");
        overlay.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        overlay.prefWidthProperty().bind(root.widthProperty());
        overlay.prefHeightProperty().bind(root.heightProperty());

        StackPane card = new StackPane();
        card.setMaxWidth(460);
        var bgUrl = getClass().getResource("/images/popup/big_block_black_V.png");
        if (bgUrl != null) {
            ImageView bgIv = new ImageView(new Image(bgUrl.toExternalForm()));
            bgIv.setFitWidth(460); bgIv.setPreserveRatio(true);
            card.getChildren().add(bgIv);
        } else {
            card.setStyle("-fx-background-color:rgba(8,8,24,0.97);-fx-background-radius:24;" +
                    "-fx-min-width:460;-fx-min-height:540;" +
                    "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.85),28,0.65,0,5);");
        }

        VBox body = new VBox(16);
        body.setAlignment(Pos.CENTER);
        body.setStyle("-fx-padding: 56 36 44 36;");

        Text heading = new Text("📚 จบเทอมแรกแล้ว!");
        heading.setFont(Font.font("Comic Sans MS", FontWeight.BOLD, 28));
        heading.setFill(Color.web(TEXT_COLOR));

        javafx.scene.shape.Line sep = new javafx.scene.shape.Line(0, 0, 340, 0);
        sep.setStroke(Color.web(TEXT_COLOR, 0.35)); sep.setStrokeWidth(1.5);

        Label scores = new Label(
                "💻 Programming\n" + progExam1Score + " คะแนน\n\n" +
                        "📐 Math\n" + mathExam1Score + " คะแนน\n\n" +
                        "Intelligence รีเซ็ต\nสู้ต่อไป! 💪");
        scores.setStyle("-fx-font-family:'Comic Sans MS';-fx-font-size:22px;" +
                "-fx-text-fill:" + TEXT_COLOR + ";-fx-line-spacing:4;");
        scores.setTextAlignment(TextAlignment.CENTER);
        scores.setAlignment(Pos.CENTER);

        var btnUrl = getClass().getResource("/images/menu/menu_continue.png");
        javafx.scene.Node closeNode;
        if (btnUrl != null) {
            ImageView iv = new ImageView(new Image(btnUrl.toExternalForm()));
            iv.setFitWidth(280); iv.setPreserveRatio(true);
            iv.setCursor(javafx.scene.Cursor.HAND);
            iv.setOnMouseEntered(e -> { iv.setScaleX(1.06); iv.setScaleY(1.06); iv.setOpacity(0.88); });
            iv.setOnMouseExited (e -> { iv.setScaleX(1.0);  iv.setScaleY(1.0);  iv.setOpacity(1.0);  });
            iv.setOnMousePressed(e -> { iv.setScaleX(0.94); iv.setScaleY(0.94); e.consume(); });
            iv.setOnMouseReleased(e -> { iv.setScaleX(1.0); iv.setScaleY(1.0); });
            iv.setOnMouseClicked(e -> {
                e.consume();
                FadeTransition fo = new FadeTransition(Duration.millis(150), overlay);
                fo.setToValue(0);
                fo.setOnFinished(ev -> {
                    overlay.prefWidthProperty().unbind();
                    overlay.prefHeightProperty().unbind();
                    root.getChildren().remove(overlay);
                    showGameplay();
                });
                fo.play();
            });
            closeNode = iv;
        } else {
            Button fb = new Button("ต่อไป ▶");
            fb.setStyle("-fx-font-family:'Comic Sans MS';-fx-font-size:18px;" +
                    "-fx-background-color:#4fc3f7;-fx-text-fill:" + TEXT_COLOR + ";" +
                    "-fx-font-weight:bold;-fx-background-radius:14;-fx-padding:10 28 10 28;-fx-cursor:hand;");
            fb.setOnAction(e -> {
                FadeTransition fo = new FadeTransition(Duration.millis(150), overlay);
                fo.setToValue(0);
                fo.setOnFinished(ev -> {
                    overlay.prefWidthProperty().unbind();
                    overlay.prefHeightProperty().unbind();
                    root.getChildren().remove(overlay);
                    showGameplay();
                });
                fo.play();
            });
            closeNode = fb;
        }

        body.getChildren().addAll(heading, sep, scores, closeNode);
        StackPane.setAlignment(body, Pos.CENTER);
        card.getChildren().add(body);

        javafx.scene.transform.Scale cs = new javafx.scene.transform.Scale(1, 1);
        cs.pivotXProperty().bind(javafx.beans.binding.Bindings.divide(card.widthProperty(), 2));
        cs.pivotYProperty().bind(javafx.beans.binding.Bindings.divide(card.heightProperty(), 2));
        cs.xProperty().bind(javafx.beans.binding.Bindings.createDoubleBinding(
                () -> Math.min(root.getWidth() / 1920.0, root.getHeight() / 1080.0),
                root.widthProperty(), root.heightProperty()));
        cs.yProperty().bind(cs.xProperty());
        card.getTransforms().add(cs);

        overlay.getChildren().add(card);
        root.getChildren().add(overlay);

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
        ActivityCutscene.play(root, ActivityCutscene.lineFor("GoHomeActivity"), () -> {
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
        if (reason != null) { speakFail(activity.getFailMessage(reason)); return; }
        actionLocked = true;
        ActivityCutscene.play(root, ActivityCutscene.lineFor(activity.getClass().getSimpleName()), () -> {
            activity.performActivity(player, timeSystem);
            if (location != null) eventManager.triggerAfterActivity(player, timeSystem, location);
            if (outsideUI != null) { outsideUI.refresh(); outsideUI.saySuccess(); }
            maybeSaveProgress(false);
            actionLocked = false;
            if (onAfter != null) onAfter.run();
        });
    }

    private boolean performSilent(Activity activity, Location location) {
        RequirementReason reason = activity.canPerform(player, timeSystem);
        if (reason != null) { speakFail(activity.getFailMessage(reason)); return false; }
        activity.performActivity(player, timeSystem);
        if (location != null) eventManager.triggerAfterActivity(player, timeSystem, location);
        if (outsideUI != null) { outsideUI.refresh(); outsideUI.saySuccess(); }
        maybeSaveProgress(false);
        return true;
    }

    private void showPause() {
        if (pauseOpen) return;
        pauseOpen = true;
        PauseMenuUI pauseUI = new PauseMenuUI(root, this);
        pauseOverlay = pauseUI.buildView();
        root.getChildren().add(pauseOverlay);
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
        AcceptanceUI ui = new AcceptanceUI(this);
        javafx.scene.Node av = ui.buildView();
        av.setOpacity(0); setContent(av);
        FadeTransition ft = new FadeTransition(Duration.millis(500), av);
        ft.setFromValue(0); ft.setToValue(1); ft.play();
    }

    private void showMainMenuWithFade() {
        AudioManager.getInstance().playMusic(AudioManager.Music.MAIN_MENU);
        actionLocked = false;
        pauseOpen    = false;
        screenOrigin = ScreenOrigin.MAIN_MENU;
        MainMenuUI ui = new MainMenuUI(hasSavedGame, this);
        javafx.scene.Node mv = ui.buildView();
        mv.setOpacity(0); setContent(mv);
        FadeTransition ft = new FadeTransition(Duration.millis(500), mv);
        ft.setFromValue(0); ft.setToValue(1); ft.play();
    }

    private void showSettings() {
        ScreenOrigin returnTo = screenOrigin;
        screenOrigin = ScreenOrigin.MAIN_MENU;
        SettingsUI ui = new SettingsUI(AudioManager.getInstance(),  () -> {
            switch (returnTo) {
                case MAIN_MENU   -> ActivityCutscene.transition(root, this::showMainMenuWithFade, null);
                case OUTSIDE     -> ActivityCutscene.transition(root, this::showGameplay, null);
                case IT_BUILDING -> ActivityCutscene.transition(root, this::showITBuilding, null);
            }
        });
        ActivityCutscene.transition(root, () -> setContent(ui.buildView()), null);
    }

    private void showHowToPlay() {
        ActivityCutscene.transition(root, () -> {
            HowToPlayUI ui = new HowToPlayUI(() -> runOnce(() -> {
                AudioManager.getInstance().playRefuse();
                ActivityCutscene.transition(root, this::showMainMenuWithFade, null);
            }));
            setContent(ui.buildView());
        }, null);
    }

    private void showCredits() {
        ActivityCutscene.transition(root, () -> {
            CreditsUI ui = new CreditsUI(() -> runOnce(() -> {
                AudioManager.getInstance().playRefuse();
                ActivityCutscene.transition(root, this::showMainMenuWithFade, null);
            }));
            setContent(ui.buildView());
        }, null);
    }

    private void showGameplay() {
        if (isGameOver()) { showEnding(); return; }
        screenOrigin = ScreenOrigin.OUTSIDE;
        AudioManager.getInstance().playMusic(AudioManager.Music.OUTSIDE);
        outsideUI = new OutsideUI(this);
        setContent(outsideUI.buildView());
        eventManager.triggerVisit(player, timeSystem, Location.OUTSIDE);
        outsideUI.refresh();
        maybeSaveProgress(false);
        if (!examNoticeShownToday) {
            examNoticeShownToday = true;
            showExamDayNotice(null);
        }
    }

    private void doCanteen() {
        EatActivity eat = new EatActivity();
        RequirementReason r = eat.canPerform(player, timeSystem);
        if (r != null) { speakFail(eat.getFailMessage(r)); return; }
        actionLocked = true;
        ActivityCutscene.play(root, ActivityCutscene.lineFor("EatActivity"), () -> {
            eat.performActivity(player, timeSystem);
            eventManager.triggerAfterActivity(player, timeSystem, Location.CANTEEN);
            if (outsideUI != null) { outsideUI.refresh(); outsideUI.saySuccess(); }
            maybeSaveProgress(false);
            actionLocked = false;
            AudioManager.getInstance().playMusic(AudioManager.Music.OUTSIDE);
        });
    }

    private void showBusStop() {
        new BusStopUI(root, this).show();
    }

    private void showITBuilding() {
        showITBuilding(true);
    }

    private void showITBuilding(boolean triggerVisitEvent) {
        screenOrigin = ScreenOrigin.IT_BUILDING;
        AudioManager.getInstance().playMusic(AudioManager.Music.INSIDE);
        if (triggerVisitEvent) {
            eventManager.triggerVisit(player, timeSystem, Location.IT_BUILDING);
        }
        if (outsideUI == null) outsideUI = new OutsideUI(this);
        outsideUI.refresh();
        actionLocked = false;
        InsideUI ui = new InsideUI(this);
        javafx.scene.Node iv = ui.buildView();
        iv.setOpacity(0); setContent(iv);
        FadeTransition fi = new FadeTransition(Duration.millis(300), iv);
        fi.setFromValue(0); fi.setToValue(1); fi.play();
    }

    private void showCoworkingSpace() {
        new CoworkingUI(root, this).show();
    }

    // ── Exams ─────────────────────────────────────────────────────────────────
    private void doProgExam() {
        if (progExamTakenToday) { speakFail("สอบ Programming ไปแล้ววันนี้!"); return; }
        ExamActivity exam = new ExamActivity();
        RequirementReason r = exam.canPerform(player, timeSystem);
        if (r != null) { speakFail(exam.getFailMessage(r)); return; }
        progExamTakenToday = true;
        actionLocked = true;
        CaptchaMiniGame[] ref = new CaptchaMiniGame[1];
        ref[0] = new CaptchaMiniGame(player, () -> {
            performSilent(exam, Location.CLASSROOM);
            if (timeSystem.getCurrentDay() <= 6) progExam1Score = ref[0].getTotalScore();
            else                                  progExam2Score = ref[0].getTotalScore();
            actionLocked = false;
            showITBuilding();
        });
        setContent(ref[0].getView());
        stage.setFullScreenExitHint(""); stage.setFullScreen(true);
    }

    private void doMathExam() {
        if (mathExamTakenToday) { speakFail("สอบ Math ไปแล้ววันนี้!"); return; }
        ExamActivity exam = new ExamActivity();
        RequirementReason r = exam.canPerform(player, timeSystem);
        if (r != null) { speakFail(exam.getFailMessage(r)); return; }
        mathExamTakenToday = true;
        performSilent(exam, Location.CLASSROOM);
        actionLocked = true;
        CountingMiniGame[] ref = new CountingMiniGame[1];
        ref[0] = new CountingMiniGame(player, () -> {
            if (timeSystem.getCurrentDay() <= 7) mathExam1Score = ref[0].getTotalScore();
            else                                  mathExam2Score = ref[0].getTotalScore();
            actionLocked = false;
            showITBuilding();
        });
        setContent(ref[0].getView());
        stage.setFullScreenExitHint(""); stage.setFullScreen(true);
    }

    private void showExamDayNotice(Runnable onDone) {
        String subject, detail;
        if (isProgExamDayInternal()) {
            subject = "💻 วันสอบ Programming!";
            detail  = "วันนี้มีสอบ Programming\nเตรียมตัวให้พร้อมและไปที่ห้องสอบได้เลย!\n\n🧠 Intelligence ยิ่งสูง คะแนนยิ่งดี";
        } else if (isMathExamDayInternal()) {
            subject = "📐 วันสอบ Math!";
            detail  = "วันนี้มีสอบ Math\nไปที่อาคาร IT เพื่อเข้าห้องสอบ!\n\n🧠 Intelligence ยิ่งสูง คะแนนยิ่งดี";
        } else {
            if (onDone != null) onDone.run();
            return;
        }
        GameDialog.event(root, subject, detail, onDone);
    }

    // =========================================================================
    // Endings
    // =========================================================================
    private void showEnding() {
        if (player != null && player.getStat(StatType.MOOD) <= 10) { showSecretEnding2(); return; }

        int overall = (progExam1Score + progExam2Score + mathExam1Score + mathExam2Score) / 2;
        String grade = overall >= 80 ? "A" : overall >= 70 ? "B" : overall >= 60 ? "C" : overall >= 50 ? "D" : "F";

        String endingMusic = switch (grade) {
            case "A"           -> AudioManager.Music.ENDING_GREAT;
            case "B"           -> AudioManager.Music.ENDING_MID;
            case "C", "D"      -> AudioManager.Music.ENDING_BAD2;
            default            -> AudioManager.Music.ENDING_BAD;
        };
        AudioManager.getInstance().playMusic(endingMusic);

        final String fg = grade;
        final int fO = overall;

        String[] scenes = switch (grade) {
            case "A" -> ENDING_A_SCENES;
            case "B" -> ENDING_B_SCENES;
            case "C" -> ENDING_C_SCENES;
            case "D" -> ENDING_D_SCENES;
            default  -> ENDING_F_SCENES;
        };

        showEndingCinematic(scenes, endingStory(grade), () -> showEndingResult(fg, fO));
    }

    private void showEndingCinematic(String[] scenes, String[] lines, Runnable onDone) {
        StackPane cinPane = new StackPane();
        cinPane.setStyle("-fx-background-color:#000;");
        cinPane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        ImageView bgImg = new ImageView();
        bgImg.setFitWidth(BASE_W); bgImg.setFitHeight(BASE_H);
        bgImg.setPreserveRatio(false);
        bgImg.setOpacity(0);

        StackPane subtitleBar = new StackPane();
        subtitleBar.setStyle("-fx-background-color: rgba(0,0,0,0.62);-fx-padding: 0;");
        subtitleBar.setMaxWidth(Double.MAX_VALUE);
        subtitleBar.setPrefHeight(130);
        subtitleBar.setMaxHeight(130);
        StackPane.setAlignment(subtitleBar, Pos.BOTTOM_CENTER);

        Label subtitle = new Label("");
        subtitle.setFont(Font.font("Comic Sans MS", FontWeight.BOLD, 38));
        subtitle.setTextFill(Color.WHITE);
        subtitle.setWrapText(true);
        subtitle.setMaxWidth(1600);
        subtitle.setTextAlignment(TextAlignment.CENTER);
        subtitle.setAlignment(Pos.CENTER);
        subtitle.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.95), 12, 0.6, 0, 2);-fx-padding: 18 40 18 40;");
        subtitleBar.getChildren().add(subtitle);

        cinPane.getChildren().addAll(bgImg, subtitleBar);
        setContent(cinPane);
        playEndingScene(cinPane, bgImg, subtitle, scenes, lines, 0, onDone);
    }

    private void playEndingScene(StackPane cinPane, ImageView bgImg, Label subtitle,
                                 String[] scenes, String[] lines, int index, Runnable onDone) {
        if (index >= lines.length) {
            FadeTransition fo = new FadeTransition(Duration.millis(800), cinPane);
            fo.setFromValue(1); fo.setToValue(0);
            fo.setOnFinished(e -> onDone.run());
            fo.play();
            return;
        }
        String imgPath = scenes[Math.min(index, scenes.length - 1)];
        var imgUrl = getClass().getResource(imgPath);
        if (imgUrl != null) bgImg.setImage(new Image(imgUrl.toExternalForm()));
        FadeTransition bgIn = new FadeTransition(Duration.millis(700), bgImg);
        bgIn.setToValue(1);
        bgIn.setOnFinished(e -> {
            subtitle.setText("");
            typeTextOnLabel(subtitle, lines[index], () -> {
                PauseTransition hold = new PauseTransition(Duration.millis(1600));
                hold.setOnFinished(pe -> {
                    FadeTransition subOut = new FadeTransition(Duration.millis(400), subtitle);
                    subOut.setToValue(0);
                    subOut.setOnFinished(se -> {
                        subtitle.setOpacity(1);
                        FadeTransition bgOut = new FadeTransition(Duration.millis(600), bgImg);
                        bgOut.setToValue(0);
                        bgOut.setOnFinished(be ->
                                playEndingScene(cinPane, bgImg, subtitle, scenes, lines, index + 1, onDone));
                        bgOut.play();
                    });
                    subOut.play();
                });
                hold.play();
            });
        });
        bgIn.play();
    }

    private void typeTextOnLabel(Label target, String text, Runnable onDone) {
        target.setText("");
        AudioManager audio = AudioManager.getInstance();
        Timeline tl = new Timeline();
        for (int i = 0; i < text.length(); i++) {
            final int next = i + 1;
            final char ch  = text.charAt(i);
            tl.getKeyFrames().add(new KeyFrame(Duration.millis((long) TYPING_MS * i), e -> {
                target.setText(text.substring(0, next));
                if (ch != ' ' && ch != '\n') audio.playTyping();
            }));
        }
        tl.setOnFinished(e -> { if (onDone != null) onDone.run(); });
        tl.play();
    }

    // =========================================================================
    // Ending result screens
    // =========================================================================
    private void showEndingResult(String grade, int overall) {
        String c = endingColor(grade);
        StackPane r = new StackPane();
        r.setStyle("-fx-background-color:#000;");
        r.setPrefSize(BASE_W, BASE_H); r.setMinSize(BASE_W, BASE_H); r.setMaxSize(BASE_W, BASE_H);
        r.setAlignment(Pos.CENTER);

        VBox content = new VBox(28);
        content.setAlignment(Pos.CENTER); content.setFillWidth(false); content.setMaxWidth(1000);
        content.setStyle("-fx-padding:60 0 60 0; -fx-background-color:transparent;");

        Text gt = new Text(grade);
        gt.setFont(Font.font("Comic Sans MS", FontWeight.BOLD, 130));
        gt.setFill(Color.web(c));
        gt.setStyle("-fx-effect: dropshadow(gaussian," + c + ",44,0.65,0,0);");
        gt.setTextAlignment(TextAlignment.CENTER);

        Text tt = new Text(endingTitle(grade));
        tt.setFont(Font.font("Comic Sans MS", FontWeight.BOLD, 30));
        tt.setFill(Color.web(c)); tt.setTextAlignment(TextAlignment.CENTER);

        Label sc = new Label(
                "📊 สรุปผลการสอบ\n\n💻 Programming:  " + progExam1Score + "  |  " + progExam2Score
                        + "\n📐 Math:              " + mathExam1Score + "  |  " + mathExam2Score
                        + "\n\n📈 คะแนนเฉลี่ยรวม:  " + overall);
        sc.setStyle("-fx-font-family:'Comic Sans MS';-fx-font-size:20px;-fx-text-fill:#ffffff;-fx-line-spacing:6;");
        sc.setTextAlignment(TextAlignment.CENTER); sc.setAlignment(Pos.CENTER);
        sc.setWrapText(true); sc.setMaxWidth(900);

        ImageView againImg = makeEndingImgBtn("/images/menu/menu_start2.png");
        ImageView quitImg  = makeEndingImgBtn("/images/menu/back_to_menu.png");
        againImg.setOnMouseClicked(e -> { AudioManager.getInstance().playAccept(); runOnce(this::startNewGame); });
        quitImg .setOnMouseClicked(e -> { AudioManager.getInstance().playRefuse(); runOnce(this::showMainMenuWithFade); });

        HBox btns = new HBox(28, againImg, quitImg); btns.setAlignment(Pos.CENTER);
        content.getChildren().addAll(gt, tt, sc, btns);
        StackPane.setAlignment(content, Pos.CENTER); r.getChildren().add(content);
        content.setOpacity(0); setContent(r);
        FadeTransition ft = new FadeTransition(Duration.millis(600), content); ft.setToValue(1); ft.play();
    }

    private String[] endingStory(String g) {
        return switch (g) {
            case "A" -> new String[]{
                    "ผลสอบออกมาแล้ว...",
                    "เกรด A — ทุกวิชาผ่านด้วยคะแนนสูงสุด",
                    "ภาพความทรงจำผ่านมาในหัว... คืนที่นั่งอ่านหนังสือจนฟ้าสาง",
                    "ติวกับเพื่อนๆ ทำ Quiz ผิดบ้างถูกบ้าง แต่ไม่เคยหยุดพยายาม",
                    "โทรหาเพื่อนทันทีที่เห็นผล — เสียงหัวเราะดังไปทั่วสาย",
                    "นัดกินข้าวเลี้ยงฉลอง แล้วก็ไปเดิน Shopping ด้วยกัน",
                    "ช่วงปิดเทอมนี้ออกไปเที่ยวให้สุด — พักผ่อนให้เต็มที่",
                    "'ปี 2... ฉันจะเอา A อีกครั้ง' — แล้วก็หลับตาลงด้วยรอยยิ้ม"
            };
            case "B" -> new String[]{
                    "ผลสอบออกมาแล้ว...",
                    "เกรด B — ไม่ใช่สิ่งที่หวัง แต่ทุกคะแนนมันมีความหมาย",
                    "เธอนั่งมองผลอยู่สักครู่ แล้วก็ยิ้มอย่างเข้าใจตัวเอง",
                    "ครั้งหน้าต้องวางแผนให้ดีกว่านี้ เริ่มจากจัดตารางให้ชัด",
                    "เปิดโน้ตขึ้นมาจดแผนการอ่านของเทอมหน้า ทีละวิชา",
                    "Nobody is perfect — แต่ทุกคนสามารถพัฒนาได้ และเธอจะทำ"
            };
            case "C" -> new String[]{
                    "ผลสอบออกมาแล้ว...",
                    "เกรด C — รอดมาได้ แม้จะหนักแค่ไหน",
                    "โล่งใจ... แต่ก็รู้ว่ามันไม่ใช่ที่สุดของตัวเอง",
                    "กลับบ้านมาหยิบหนังสือขึ้นมาทวนของเก่าทันที",
                    "บรรทัดต่อบรรทัด... ตาค่อยๆ หนักขึ้น",
                    "แล้วก็หลับไปคาหน้าหนังสือ — พรุ่งนี้ค่อยสู้ต่อ"
            };
            case "D" -> new String[]{
                    "ผลสอบออกมาแล้ว...",
                    "เกรด D — ผ่านแบบ Probation ฉิวเฉียด",
                    "มือสั่นเล็กน้อยตอนคลิกดูผล — ใจหนักอยู่ในอก",
                    "น้ำตาไหลออกมาโดยไม่รู้ตัว เงียบไปสักพัก",
                    "หยิบยาแก้ปวดหัวขึ้นมากิน แล้วนั่งมองเพดาน",
                    "'ไม่... ฉันต้องทำให้ได้ดีกว่านี้' — เธอพูดกับตัวเองอย่างเด็ดเดี่ยว"
            };
            default  -> new String[]{
                    "ผลสอบออกมาแล้ว...",
                    "เกรด F — Retired จากคณะ",
                    "เธออ่านผลซ้ำแล้วซ้ำเล่า หวังว่ามันจะเปลี่ยน แต่มันไม่เปลี่ยน",
                    "น้ำตาไหล แต่ก็เช็ดออก — ยังมีทางต่อไปเสมอ",
                    "'ฉันทำดีที่สุดแล้วในตอนนั้น' — เธอกอดตัวเองไว้",
                    "เปิดหน้า TCAS ขึ้นมา... เริ่มหาคณะใหม่ ลองใหม่อีกครั้ง"
            };
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
        AudioManager.getInstance().playMusic(AudioManager.Music.ENDING_GREAT);
        StackPane black = new StackPane();
        black.setStyle("-fx-background-color:#000;");
        black.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
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
        StackPane r = new StackPane();
        r.setStyle("-fx-background-color:#000;");
        r.setPrefSize(BASE_W, BASE_H); r.setMinSize(BASE_W, BASE_H); r.setMaxSize(BASE_W, BASE_H);
        r.setAlignment(Pos.CENTER);
        VBox content = new VBox(32);
        content.setAlignment(Pos.CENTER); content.setFillWidth(false); content.setMaxWidth(1000);
        content.setStyle("-fx-padding:60 0 60 0;-fx-background-color:transparent;");
        Text gt = new Text("✦");
        gt.setFont(Font.font("Comic Sans MS", FontWeight.BOLD, 120)); gt.setFill(Color.web(ac)); gt.setTextAlignment(TextAlignment.CENTER);
        Text tt = new Text("Secret Ending 1 —  New Career Path");
        tt.setFont(Font.font("Comic Sans MS", FontWeight.BOLD, 30)); tt.setFill(Color.web(ac)); tt.setTextAlignment(TextAlignment.CENTER);
        Label desc = new Label("🎨  เส้นทางที่ไม่มีใครคาดถึง\n\nความกล้าที่จะเลือกเส้นทางของตัวเอง\n\n[ Unlocked: Secret Ending 🌟 ]");
        desc.setStyle("-fx-font-family:'Comic Sans MS';-fx-font-size:22px;-fx-text-fill:#ffffff;-fx-line-spacing:6;");
        desc.setTextAlignment(TextAlignment.CENTER); desc.setAlignment(Pos.CENTER); desc.setWrapText(true); desc.setMaxWidth(900);
        ImageView tryAgainImg = makeEndingImgBtn("/images/menu/menu_start2.png");
        ImageView quitImg     = makeEndingImgBtn("/images/menu/menu_quit.png");
        tryAgainImg.setOnMouseClicked(e -> { AudioManager.getInstance().playAccept(); runOnce(this::showIntroSequence); });
        quitImg    .setOnMouseClicked(e -> { AudioManager.getInstance().playRefuse(); stage.close(); });
        HBox btns = new HBox(32, tryAgainImg, quitImg); btns.setAlignment(Pos.CENTER);
        content.getChildren().addAll(gt, tt, desc, btns);
        StackPane.setAlignment(content, Pos.CENTER); r.getChildren().add(content);
        content.setOpacity(0); setContent(r);
        FadeTransition ft = new FadeTransition(Duration.millis(600), content); ft.setToValue(1); ft.play();
    }

    private void showSecretEnding2() {
        AudioManager.getInstance().playMusic(AudioManager.Music.ENDING_BAD);
        StackPane black = new StackPane();
        black.setStyle("-fx-background-color:#000;");
        black.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        Label lbl = makeCutsceneLabel(); lbl.setOpacity(0);
        black.getChildren().add(lbl); setContent(black);
        FadeTransition fi = new FadeTransition(Duration.millis(800), black);
        fi.setFromValue(0); fi.setToValue(1);
        fi.setOnFinished(e -> { lbl.setOpacity(1); typeSegments(lbl, new String[]{
                "ผลสอบออกมาแล้ว...","แต่ใจมันว่างเปล่าเกินกว่าจะดีใจ",
                "เธอเปิดผลคะแนน — ตัวเลขมันดี","แต่มันไม่รู้สึกอะไรเลย...",
                "ตลอด 2 สัปดาห์ที่ผ่านมา เธอเอาแต่วิ่งตามเกรด",
                "จนลืมไปว่าตัวเองชอบอะไร รู้สึกอะไร",
                "เกรดดีแต่ไม่มีความสุข — มันคุ้มค่าจริงหรือ?"
        }, this::showSecretEnding2Result); });
        fi.play();
    }

    private void showSecretEnding2Result() {
        final String ac = "#78909c";
        StackPane r = new StackPane();
        r.setStyle("-fx-background-color:#000;");
        r.setPrefSize(BASE_W, BASE_H); r.setMinSize(BASE_W, BASE_H); r.setMaxSize(BASE_W, BASE_H);
        r.setAlignment(Pos.CENTER);
        VBox content = new VBox(32);
        content.setAlignment(Pos.CENTER); content.setFillWidth(false); content.setMaxWidth(1000);
        content.setStyle("-fx-padding:60 0 60 0;-fx-background-color:transparent;");
        Text gt = new Text("💔");
        gt.setFont(Font.font("Comic Sans MS", FontWeight.BOLD, 110)); gt.setFill(Color.web(ac)); gt.setTextAlignment(TextAlignment.CENTER);
        Text tt = new Text("Secret Ending 2  —  Hollow Victory");
        tt.setFont(Font.font("Comic Sans MS", FontWeight.BOLD, 30)); tt.setFill(Color.web(ac)); tt.setTextAlignment(TextAlignment.CENTER);
        Label desc = new Label("📊 ผลการสอบดี — แต่ใจมันพัง\n\nไม่ว่าเกรดจะสวยแค่ไหน\nถ้าหัวใจไม่มีความสุข\nความพยายามทั้งหมดก็ไร้ความหมาย\n\nเกรดคือเครื่องมือ — ไม่ใช่ชีวิตทั้งหมด\n\n[ Unlocked: Secret Ending 2 🌑 ]");
        desc.setStyle("-fx-font-family:'Comic Sans MS';-fx-font-size:22px;-fx-text-fill:#ffffff;-fx-line-spacing:6;");
        desc.setTextAlignment(TextAlignment.CENTER); desc.setAlignment(Pos.CENTER); desc.setWrapText(true); desc.setMaxWidth(900);
        ImageView againImg = makeEndingImgBtn("/images/menu/menu_start2.png");
        ImageView quitImg  = makeEndingImgBtn("/images/menu/back_to_menu.png");
        againImg.setOnMouseClicked(e -> { AudioManager.getInstance().playAccept(); runOnce(this::startNewGame); });
        quitImg .setOnMouseClicked(e -> { AudioManager.getInstance().playRefuse(); runOnce(this::showMainMenuWithFade); });
        HBox btns = new HBox(32, againImg, quitImg); btns.setAlignment(Pos.CENTER);
        content.getChildren().addAll(gt, tt, desc, btns);
        StackPane.setAlignment(content, Pos.CENTER); r.getChildren().add(content);
        content.setOpacity(0); setContent(r);
        FadeTransition ft = new FadeTransition(Duration.millis(600), content); ft.setToValue(1); ft.play();
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
            wireEventListener();
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
        } catch (Exception ignored) {
            agreedToTerms = hasSavedGame = false;
            player = null; timeSystem = null; eventManager = null;
        }
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
            p.setProperty("energy",        String.valueOf(player.getStat(StatType.ENERGY)));
            p.setProperty("intelligence",   String.valueOf(player.getStat(StatType.INTELLIGENCE)));
            p.setProperty("mood",           String.valueOf(player.getStat(StatType.MOOD)));
            p.setProperty("progExam1Score", String.valueOf(progExam1Score));
            p.setProperty("mathExam1Score", String.valueOf(mathExam1Score));
            p.setProperty("progExam2Score", String.valueOf(progExam2Score));
            p.setProperty("mathExam2Score", String.valueOf(mathExam2Score));
            p.setProperty("eventsToday",         String.valueOf(eventManager.getEventsToday()));
            p.setProperty("progExamTakenToday",  String.valueOf(progExamTakenToday));
            p.setProperty("mathExamTakenToday",  String.valueOf(mathExamTakenToday));
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

    private ImageView makeEndingImgBtn(String resourcePath) {
        var url = getClass().getResource(resourcePath);
        ImageView iv = url != null ? new ImageView(new Image(url.toExternalForm())) : new ImageView();
        iv.setFitWidth(260); iv.setPreserveRatio(true); iv.setCursor(javafx.scene.Cursor.HAND);
        iv.setOnMouseEntered(e -> { iv.setScaleX(1.07); iv.setScaleY(1.07); iv.setOpacity(0.88); });
        iv.setOnMouseExited (e -> { iv.setScaleX(1.0);  iv.setScaleY(1.0);  iv.setOpacity(1.0);  });
        iv.setOnMousePressed(e -> { iv.setScaleX(0.93); iv.setScaleY(0.93); e.consume(); });
        iv.setOnMouseReleased(e -> { iv.setScaleX(1.0); iv.setScaleY(1.0); });
        return iv;
    }

    private Label makeCutsceneLabel() {
        Label l = new Label("");
        l.setFont(Font.font("Comic Sans MS", FontWeight.BOLD, 42));
        l.setTextFill(Color.WHITE);
        l.setWrapText(true); l.setMaxWidth(1400);
        l.setTextAlignment(TextAlignment.CENTER); l.setAlignment(Pos.CENTER);
        l.setStyle("-fx-effect:dropshadow(gaussian,rgba(180,220,255,0.40),16,0.45,0,0);");
        StackPane.setAlignment(l, Pos.CENTER); return l;
    }

    private FadeTransition fade(javafx.scene.Node n, double from, double to, double secs) {
        FadeTransition ft = new FadeTransition(Duration.seconds(secs), n);
        ft.setFromValue(from); ft.setToValue(to); return ft;
    }

    private PauseTransition pause(double s) { return new PauseTransition(Duration.seconds(s)); }

    private void typeSegments(Label target, String[] segs, Runnable onDone) {
        target.setText("");
        AudioManager audio = AudioManager.getInstance();
        SequentialTransition seq = new SequentialTransition();
        for (int idx = 0; idx < segs.length; idx++) {
            String seg = segs[idx];
            Timeline tl = new Timeline();
            for (int i = 0; i < seg.length(); i++) {
                final int next = i + 1; final char ch = seg.charAt(i);
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

    public static void main(String[] args) { launch(args); }
}