package com.savemygpa.launcher;

import com.savemygpa.exam.CaptchaMiniGame;
import javafx.application.Application;
import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import javafx.scene.text.*;
import javafx.stage.Stage;

import com.savemygpa.player.effect.StatusEffect;
import com.savemygpa.player.*;
import com.savemygpa.player.effect.buff.SeniorNoteBuff;
import com.savemygpa.player.effect.debuff.WetFeetDebuff;
import com.savemygpa.player.effect.debuff.NoStackOverflowDebuff;
import com.savemygpa.core.*;
import com.savemygpa.activity.*;
import com.savemygpa.event.*;
import com.savemygpa.exam.CountingMiniGame;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;

public class GameLauncher extends Application {

    // ── Core objects ─────────────────────────────────────────────────────────
    private Stage stage;
    private Player player;
    private TimeSystem timeSystem;
    private EventManager eventManager;

    // ── Game state ────────────────────────────────────────────────────────────
    private boolean hasSavedGame  = false;
    private boolean agreedToTerms = false;

    // ── Save / Load ────────────────────────────────────────────────────────────
    private static final String SAVE_VERSION = "1";
    private static final String SAVE_FILE_NAME = "save_my_gpa_save.properties";
    private long lastSaveMillis = 0;

    // Exam scores — captured at end of each exam day via onDayEnd()
    private int progExam1Score = 0;  // day 6
    private int mathExam1Score = 0;  // day 7
    private int progExam2Score = 0;  // day 13
    private int mathExam2Score = 0;  // day 14

    private static final int TOTAL_DAYS = 14;

    // ── Stat display labels ──────────────────────────────────────────────────
    private final Label energyLabel   = new Label();
    private final Label moodLabel     = new Label();
    private final Label intLabel      = new Label();
    private final Label timeLabel     = new Label();
    private final Label timeLeftLabel = new Label();
    private final Label effectsLabel  = new Label();

    // ═════════════════════════════════════════════════════════════════════════
    // JavaFX entry
    // ═════════════════════════════════════════════════════════════════════════

    @Override
    public void start(Stage stage) {
        this.stage = stage;
        stage.setTitle("Save My GPA");
        loadFromDisk();
        if (agreedToTerms) showMainMenu();
        else showIntroSequence();
        stage.show();
    }

    // ═════════════════════════════════════════════════════════════════════════
    // Game initialisation
    // ═════════════════════════════════════════════════════════════════════════

    private void startNewGame() {
        player       = new Player(6, 0, 60);
        timeSystem   = new TimeSystem();
        eventManager = new EventManager();
        EventRegistry.registerAll(eventManager);

        hasSavedGame   = true;
        progExam1Score = 0;
        mathExam1Score = 0;
        progExam2Score = 0;
        mathExam2Score = 0;

        persistSave(true); // create save file immediately
        showGameplay();
    }

    // ═════════════════════════════════════════════════════════════════════════
    // Day classification helpers
    // ═════════════════════════════════════════════════════════════════════════

    private boolean isProgExamDay() {
        int d = timeSystem.getCurrentDay();
        return d == 6 || d == 13;
    }

    private boolean isMathExamDay() {
        int d = timeSystem.getCurrentDay();
        return d == 7 || d == 14;
    }

    private boolean isExamDay() {
        return isProgExamDay() || isMathExamDay();
    }

    private boolean isGameOver() {
        return timeSystem.getCurrentDay() > TOTAL_DAYS;
    }

    private String getDayTypeLabel() {
        if (isProgExamDay()) return "⚠️ วันสอบ Programming!";
        if (isMathExamDay()) return "⚠️ วันสอบ Math!";
        return "วันเรียนปกติ";
    }

    // ═════════════════════════════════════════════════════════════════════════
    // Day-end logic
    // ═════════════════════════════════════════════════════════════════════════

    private void onDayEnd() {
        int day = timeSystem.getCurrentDay(); // the day ending NOW

        // Capture exam scores before advancing
        if (day == 6)  progExam1Score = player.getStat(StatType.INTELLIGENCE);
        if (day == 7)  mathExam1Score = player.getStat(StatType.INTELLIGENCE);
        if (day == 13) progExam2Score = player.getStat(StatType.INTELLIGENCE);
        if (day == 14) mathExam2Score = player.getStat(StatType.INTELLIGENCE);

        // Tick day-based buffs
        player.getEffect(SeniorNoteBuff.class).ifPresent(b -> b.tickDay(player));

        // Clear day-scoped debuffs (WetFeet, NoStackOverflow)
        clearDayEffects();

        // Reset daily event counter
        eventManager.newDayReset();

        // Advance to next day
        timeSystem.endDay();

        // INT reset at start of day 8
        if (timeSystem.getCurrentDay() == 8) {
            int current = player.getStat(StatType.INTELLIGENCE);
            player.changeStat(StatType.INTELLIGENCE, -current);
            showPopup(
                    "📚 รอบสอบแรกจบแล้ว!\n\n" +
                            "💻 Programming Score: " + progExam1Score + "\n" +
                            "📐 Math Score: "        + mathExam1Score + "\n\n" +
                            "Intelligence รีเซ็ตเป็น 0\n" +
                            "Mood และ Energy ยังคงเดิม — สู้ต่อไป!"
            );
        }

        if (isGameOver()) {
            showEnding();
        }
    }

    /**
     * Removes WetFeet and NoStackOverflow at end of day.
     * Replaces the missing player.clearDayEffects() — these debuffs use super(99)
     * so we remove them by class directly instead.
     */
    private void clearDayEffects() {
        player.removeEffect(WetFeetDebuff.class);
        player.removeEffect(NoStackOverflowDebuff.class);
    }

    // ═════════════════════════════════════════════════════════════════════════
    // Activity execution
    // ═════════════════════════════════════════════════════════════════════════

    private boolean perform(Activity activity, Location location) {
        RequirementReason reason = activity.canPerform(player, timeSystem);
        if (reason != null) {
            showPopup(activity.getFailMessage(reason));
            return false;
        }

        activity.performActivity(player, timeSystem, eventManager);

        // Only fire activity-triggered events after success
        if (location != null) {
            eventManager.triggerAfterActivity(player, timeSystem, location);
        }

        updateStats();
        return true;
    }

    private void doGoHome() {
        // Capture hour BEFORE endDay() resets it to START_HOUR
        int hourBonus = timeSystem.getTimeLeft();
        player.changeStat(StatType.ENERGY,  3 + (player.getStat(StatType.MOOD) / 40) + hourBonus);
        player.changeStat(StatType.MOOD,   10 + hourBonus);

        onDayEnd();

        if (!isGameOver()) {
            showGameplay();
            updateStats();
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // Stat panel
    // ═════════════════════════════════════════════════════════════════════════

    private void updateStats() {
        energyLabel.setText  ("⚡ Energy: "  + player.getStat(StatType.ENERGY));
        moodLabel.setText    ("😊 Mood: "    + player.getStat(StatType.MOOD));
        intLabel.setText     ("🧠 INT: "     + player.getStat(StatType.INTELLIGENCE));
        timeLabel.setText    ("📅 Day "      + timeSystem.getCurrentDay()
                + " | 🕐 "     + timeSystem.getCurrentHour() + ":00");
        timeLeftLabel.setText("⏳ เหลือ: "  + timeSystem.getTimeLeft() + " ชม.");

        if (player.getActiveEffects().isEmpty()) {
            effectsLabel.setText("✨ ไม่มี effect");
        } else {
            StringBuilder sb = new StringBuilder("🎭 Effects:\n");
            player.getActiveEffects().forEach(e ->
                    sb.append("  • ").append(e.getName()).append("\n"));
            effectsLabel.setText(sb.toString());
        }

        // Auto-save gameplay state (after the acceptance screen).
        maybeSaveProgress(false);
    }

    private VBox statsPanel() {
        effectsLabel.setWrapText(true);
        VBox box = new VBox(8,
                energyLabel, moodLabel, intLabel,
                new Separator(),
                timeLabel, timeLeftLabel,
                new Separator(),
                effectsLabel);
        box.setPrefWidth(185);
        box.setStyle("-fx-padding: 10; -fx-background-color: #f0f0f0;");
        updateStats();
        return box;
    }

    // ═════════════════════════════════════════════════════════════════════════
    // Save / Load
    // ═════════════════════════════════════════════════════════════════════════

    private Path getSavePath() {
        // Store in user home so it persists between runs.
        return Paths.get(System.getProperty("user.home"), SAVE_FILE_NAME);
    }

    private void loadFromDisk() {
        // Reset defaults first.
        agreedToTerms = false;
        hasSavedGame = false;

        Path path = getSavePath();
        if (!Files.exists(path)) return;

        Properties props = new Properties();
        try (InputStream in = Files.newInputStream(path)) {
            props.load(in);
        } catch (Exception ignored) {
            // If load fails, fall back to defaults.
            return;
        }

        agreedToTerms = Boolean.parseBoolean(props.getProperty("termsAgreed", "false"));
        hasSavedGame = Boolean.parseBoolean(props.getProperty("hasSavedGame", "false"));

        if (!hasSavedGame) return;

        try {
            int day = Integer.parseInt(props.getProperty("day", "1"));
            int hour = Integer.parseInt(props.getProperty("hour", "8"));
            int energy = Integer.parseInt(props.getProperty("energy", "6"));
            int intelligence = Integer.parseInt(props.getProperty("intelligence", "0"));
            int mood = Integer.parseInt(props.getProperty("mood", "60"));

            progExam1Score = Integer.parseInt(props.getProperty("progExam1Score", "0"));
            mathExam1Score = Integer.parseInt(props.getProperty("mathExam1Score", "0"));
            progExam2Score = Integer.parseInt(props.getProperty("progExam2Score", "0"));
            mathExam2Score = Integer.parseInt(props.getProperty("mathExam2Score", "0"));

            player = new Player(energy, intelligence, mood);
            timeSystem = new TimeSystem();
            timeSystem.setCurrentDay(day);
            timeSystem.setCurrentHour(hour);

            eventManager = new EventManager();
            EventRegistry.registerAll(eventManager);
            int eventsToday = Integer.parseInt(props.getProperty("eventsToday", "0"));
            eventManager.setEventsToday(eventsToday);

            int effectsCount = Integer.parseInt(props.getProperty("effectsCount", "0"));
            for (int i = 0; i < effectsCount; i++) {
                String className = props.getProperty("effect." + i + ".class");
                if (className == null || className.isBlank()) continue;

                int remaining = Integer.parseInt(props.getProperty("effect." + i + ".remaining", "0"));
                StatusEffect effect = (StatusEffect) Class.forName(className).getDeclaredConstructor().newInstance();

                // Restore effect state.
                if (effect instanceof SeniorNoteBuff snb) {
                    snb.setDaysRemaining(remaining);
                } else {
                    effect.setRemainingDuration(remaining);
                }

                player.addEffect(effect);
            }

        } catch (Exception ignored) {
            // If any part of loading fails, keep defaults and do not crash.
            agreedToTerms = false;
            hasSavedGame = false;
            player = null;
            timeSystem = null;
            eventManager = null;
        }
    }

    private void persistSave(boolean force) {
        if (!force && System.currentTimeMillis() - lastSaveMillis < 2000) return;
        lastSaveMillis = System.currentTimeMillis();

        Path path = getSavePath();
        Properties props = new Properties();
        props.setProperty("version", SAVE_VERSION);
        props.setProperty("termsAgreed", String.valueOf(agreedToTerms));
        props.setProperty("hasSavedGame", String.valueOf(hasSavedGame));

        if (hasSavedGame && player != null && timeSystem != null && eventManager != null) {
            props.setProperty("day", String.valueOf(timeSystem.getCurrentDay()));
            props.setProperty("hour", String.valueOf(timeSystem.getCurrentHour()));
            props.setProperty("energy", String.valueOf(player.getStat(StatType.ENERGY)));
            props.setProperty("intelligence", String.valueOf(player.getStat(StatType.INTELLIGENCE)));
            props.setProperty("mood", String.valueOf(player.getStat(StatType.MOOD)));

            props.setProperty("progExam1Score", String.valueOf(progExam1Score));
            props.setProperty("mathExam1Score", String.valueOf(mathExam1Score));
            props.setProperty("progExam2Score", String.valueOf(progExam2Score));
            props.setProperty("mathExam2Score", String.valueOf(mathExam2Score));

            props.setProperty("eventsToday", String.valueOf(eventManager.getEventsToday()));

            List<StatusEffect> effects = player.getActiveEffects();
            props.setProperty("effectsCount", String.valueOf(effects.size()));

            for (int i = 0; i < effects.size(); i++) {
                StatusEffect eff = effects.get(i);
                props.setProperty("effect." + i + ".class", eff.getClass().getName());
                props.setProperty("effect." + i + ".remaining", String.valueOf(eff.getRemainingDuration()));
            }
        }

        try {
            try {
                Path parent = path.getParent();
                if (parent != null) Files.createDirectories(parent);
            } catch (Exception ignored) {}

            try (OutputStream out = Files.newOutputStream(path)) {
                props.store(out, "Save My GPA");
            }
        } catch (Exception ignored) {
            // ignore save failure
        }
    }

    private void maybeSaveProgress(boolean force) {
        if (!agreedToTerms || !hasSavedGame) return;
        if (player == null || timeSystem == null || eventManager == null) return;
        persistSave(force);
    }

    // ═════════════════════════════════════════════════════════════════════════
    // Screens
    // ═════════════════════════════════════════════════════════════════════════

    private Image loadImage(String resourcePath) {
        // resourcePath should start with "/" (classpath absolute path)
        java.net.URL url = getClass().getResource(resourcePath);
        if (url == null) {
            throw new IllegalStateException("Missing resource on classpath: " + resourcePath);
        }
        return new Image(url.toExternalForm());
    }

    private void typeText(Label target, String fullText, Duration perChar, Runnable onDone) {
        target.setText("");
        Timeline timeline = new Timeline();

        // Reveals text char-by-char for a "typewriter" feel.
        for (int i = 0; i < fullText.length(); i++) {
            final int next = i + 1;
            timeline.getKeyFrames().add(
                    new javafx.animation.KeyFrame(
                            perChar.multiply(i),
                            e -> target.setText(fullText.substring(0, next))
                    )
            );
        }

        timeline.setOnFinished(e -> {
            if (onDone != null) onDone.run();
        });
        timeline.play();
    }

    private Font cuteFont(double size, FontWeight weight) {
        // Use a "cute" font family; JVM will fall back if not available.
        return Font.font("Comic Sans MS", weight, size);
    }

    private Font cuteFont(double size) {
        return Font.font("Comic Sans MS", size);
    }

    private Timeline createTypeTimeline(Label target, String text, Duration perChar) {
        Timeline timeline = new Timeline();
        for (int i = 0; i < text.length(); i++) {
            final int next = i + 1;
            timeline.getKeyFrames().add(
                    new javafx.animation.KeyFrame(
                            perChar.multiply(i),
                            e -> target.setText(text.substring(0, next))
                    )
            );
        }
        return timeline;
    }

    private void typeTextSegments(
            Label target,
            String[] segments,
            Duration perChar,
            Duration pauseAfterTyped,
            Duration fadeOutDuration,
            Duration fadeInDuration,
            Runnable onDone
    ) {
        if (segments == null || segments.length == 0) {
            if (onDone != null) onDone.run();
            return;
        }

        target.setText("");

        SequentialTransition seq = new SequentialTransition();
        for (int idx = 0; idx < segments.length; idx++) {
            String seg = segments[idx];
            Timeline typing = createTypeTimeline(target, seg, perChar);
            seq.getChildren().add(typing);
            seq.getChildren().add(new PauseTransition(pauseAfterTyped));

            boolean isLast = (idx == segments.length - 1);
            if (!isLast) {
                FadeTransition fadeOut = new FadeTransition(fadeOutDuration, target);
                fadeOut.setFromValue(1);
                fadeOut.setToValue(0);
                fadeOut.setOnFinished(e -> target.setText(""));

                FadeTransition fadeIn = new FadeTransition(fadeInDuration, target);
                fadeIn.setFromValue(0);
                fadeIn.setToValue(1);

                seq.getChildren().add(fadeOut);
                seq.getChildren().add(fadeIn);
            }
        }

        if (onDone != null) seq.setOnFinished(e -> onDone.run());
        seq.play();
    }

    private Button createAnimatedImageButton(String imageResourcePath, double fitWidth, Runnable onClick) {
        Button btn = new Button();
        btn.setStyle("-fx-background-color: transparent; -fx-padding: 0; -fx-border-width: 0;");
        btn.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);

        ImageView iv = new ImageView(loadImage(imageResourcePath));
        iv.setPreserveRatio(true);
        iv.setFitWidth(fitWidth);
        btn.setGraphic(iv);

        // Idle "bob" animation
        TranslateTransition bob = new TranslateTransition(Duration.millis(650), btn);
        bob.setFromY(0);
        bob.setToY(-8);
        bob.setAutoReverse(true);
        bob.setCycleCount(Animation.INDEFINITE);
        bob.play();

        btn.setOnMouseEntered(e -> {
            bob.pause(); // stop bobbing on hover
            btn.setScaleX(1.08);
            btn.setScaleY(1.08);
        });
        btn.setOnMouseExited(e -> {
            btn.setScaleX(1.0);
            btn.setScaleY(1.0);
            bob.play(); // resume bobbing
        });

        btn.setOnAction(e -> onClick.run());
        return btn;
    }

    private void showIntroSequence() {
        // Black intro screen: game title -> creator team -> typed story.
        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: #000000;");

        VBox content = new VBox(18);
        content.setAlignment(Pos.CENTER);
        content.setStyle("-fx-padding: 30;");

        Text title = new Text("Save My GPA");
        title.setFill(Color.WHITE);
        title.setFont(cuteFont(46, FontWeight.BOLD));
        title.setOpacity(0);

        Text creators = new Text("ทีมผู้สร้าง SaveMyGPA Team");
        creators.setFill(Color.WHITE);
        creators.setFont(cuteFont(26, FontWeight.SEMI_BOLD));
        creators.setOpacity(0);

        Label story = new Label();
        story.setTextFill(Color.WHITE);
        story.setFont(cuteFont(20));
        story.setWrapText(true);
        story.setMaxWidth(760);
        story.setAlignment(Pos.CENTER);
        story.setOpacity(0);

        content.getChildren().addAll(title, creators, story);
        root.getChildren().add(content);

        stage.setScene(new Scene(root, 960, 540));

        String[] introSegments = new String[]{
                "ในคืนที่นักศึกษาหญิงคนหนึ่งรอผลสอบ...",
                "หัวใจเต้นแรงเมื่อรู้ว่า “เธอสอบติดแล้ว!”",
                "คณะเทคโนโลยีสารสนเทศ",
                "มหาวิทยาลัยพระจอมเกล้าเจ้าคุณทหารลาดกระบัง",
                "แต่ก่อนจะก้าวไปสู่บทเรียนถัดไป...",
                "เธอต้องยอมรับข้อตกลงบางอย่าง",
                "และเลือกเส้นทางของตัวเอง"
        };

        FadeTransition titleIn = new FadeTransition(Duration.seconds(1.0), title);
        titleIn.setFromValue(0);
        titleIn.setToValue(1);

        PauseTransition pause1 = new PauseTransition(Duration.seconds(0.6));

        FadeTransition titleOut = new FadeTransition(Duration.seconds(0.8), title);
        titleOut.setFromValue(1);
        titleOut.setToValue(0);

        FadeTransition creatorsIn = new FadeTransition(Duration.seconds(0.9), creators);
        creatorsIn.setFromValue(0);
        creatorsIn.setToValue(1);

        PauseTransition pause2 = new PauseTransition(Duration.seconds(0.6));

        FadeTransition creatorsOut = new FadeTransition(Duration.seconds(0.8), creators);
        creatorsOut.setFromValue(1);
        creatorsOut.setToValue(0);

        FadeTransition storyIn = new FadeTransition(Duration.seconds(0.8), story);
        storyIn.setFromValue(0);
        storyIn.setToValue(1);
        storyIn.setOnFinished(e -> typeTextSegments(
                story,
                introSegments,
                Duration.millis(38),     // even slower typing (cute pacing)
                Duration.seconds(0.85),  // pause after each segment
                Duration.millis(260),    // fade out before next segment
                Duration.millis(180),
                this::showAgreement
        ));

        new SequentialTransition(
                titleIn,
                pause1,
                titleOut,
                creatorsIn,
                pause2,
                creatorsOut,
                storyIn
        ).play();
    }

    private void showAgreement() {
        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: #000000;");

        ImageView bg = new ImageView(loadImage("/images/acceptance/acceptance_page.jpg"));
        bg.setPreserveRatio(true);
        root.getChildren().add(bg);

        Button accept = createAnimatedImageButton(
                "/images/acceptance/accept_button.png",
                300,
                () -> {
                    agreedToTerms = true;
                    persistSave(true); // persist terms so next boot skips intro + agreement
                    showMainMenu();
                }
        );

        Button refuse = createAnimatedImageButton(
                "/images/acceptance/refuse_button.png",
                300,
                this::showRefusalEnding
        );

        HBox buttons = new HBox(60, accept, refuse);
        buttons.setAlignment(Pos.CENTER);
        StackPane.setAlignment(buttons, Pos.CENTER);

        root.getChildren().add(buttons);

        Scene scene = new Scene(root, 960, 540);
        stage.setScene(scene);

        bg.fitWidthProperty().bind(scene.widthProperty());
        bg.fitHeightProperty().bind(scene.heightProperty());
    }

    private void showMainMenu() {
        Text title = new Text("🎓 Save My GPA");
        title.setStyle("-fx-font-size: 24; -fx-font-weight: bold;");

        VBox root = new VBox(15);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-padding: 40;");
        root.getChildren().add(title);

        if (hasSavedGame) {
            Button cont = new Button("▶ Continue");
            cont.setPrefWidth(210);
            cont.setOnAction(e -> showGameplay());
            root.getChildren().add(cont);
        }

        Button start   = new Button("🆕 Start New Game");
        Button how     = new Button("📖 How To Play");
        Button credits = new Button("👥 Credits");
        Button quit    = new Button("🚪 Quit");
        for (Button b : new Button[]{start, how, credits, quit}) b.setPrefWidth(210);

        start.setOnAction(e -> startNewGame());
        how.setOnAction(e -> showHowToPlay());
        credits.setOnAction(e -> showCredits());
        quit.setOnAction(e -> stage.close());

        root.getChildren().addAll(start, how, credits, quit);
        stage.setScene(new Scene(root, 600, 450));
    }

    private void showHowToPlay() {
        Label text = new Label(
                "🕹️ วิธีเล่น Save My GPA\n\n" +
                        "📅 ตารางวัน:\n" +
                        "  วัน 1-5  → เรียนปกติ\n" +
                        "  วัน 6    → สอบ Programming รอบ 1\n" +
                        "  วัน 7    → สอบ Math รอบ 1 (มีมินิเกม!)\n" +
                        "  วัน 8    → INT รีเซ็ตเป็น 0 (Mood/Energy คงเดิม)\n" +
                        "  วัน 8-12 → เรียนปกติ\n" +
                        "  วัน 13   → สอบ Programming รอบ 2\n" +
                        "  วัน 14   → สอบ Math รอบ 2 (มีมินิเกม!)\n\n" +
                        "📊 Stats:\n" +
                        "  🧠 INT (max 100)   — ยิ่งสูง คะแนนสอบยิ่งดี\n" +
                        "  😊 Mood (max 100)  — ส่งผลต่อ INT ที่ได้จากการเรียน\n" +
                        "  ⚡ Energy (max 10) — ต้องใช้ทำกิจกรรม\n\n" +
                        "🎲 Event สุ่ม: สูงสุด 3 ครั้ง/วัน\n\n" +
                        "🏆 เกรด: INT เฉลี่ย ≥70 → A | ≥40 → C | <40 → F"
        );
        text.setWrapText(true);
        text.setStyle("-fx-font-size: 13;");

        Button back = new Button("← กลับ");
        back.setOnAction(e -> showMainMenu());

        VBox root = new VBox(20, text, back);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-padding: 30;");
        stage.setScene(new Scene(root, 650, 530));
    }

    private void showCredits() {
        Label text = new Label("🎮 Save My GPA\n\nCreated by the SaveMyGPA Team\n\nขอบคุณทุกคนที่เล่น!");
        text.setTextAlignment(TextAlignment.CENTER);
        Button back = new Button("← กลับ");
        back.setOnAction(e -> showMainMenu());
        VBox root = new VBox(20, text, back);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-padding: 40;");
        stage.setScene(new Scene(root, 500, 280));
    }

    // ── Gameplay ─────────────────────────────────────────────────────────────

    private void showGameplay() {
        if (isGameOver()) { showEnding(); return; }

        // Fire visit-triggered events on arriving at main map (outside)
        eventManager.triggerVisit(player, timeSystem, Location.OUTSIDE);
        updateStats();

        int day = timeSystem.getCurrentDay();
        Label header = new Label("📅 Day " + day + " — " + getDayTypeLabel());
        header.setStyle("-fx-font-size: 15; -fx-font-weight: bold;");

        Button busStop    = new Button("🚌 Bus Stop");
        Button canteen    = new Button("🍽️ Canteen");
        Button itBuilding = new Button("🏫 IT Building");
        Button goHome     = new Button("🏠 Go Home");
        Button mainMenu   = new Button("⏸ Main Menu");
        for (Button b : new Button[]{busStop, canteen, itBuilding, goHome, mainMenu}) b.setPrefWidth(210);

        busStop.setOnAction(e -> showBusStop());
        canteen.setOnAction(e -> { perform(new EatActivity(), Location.CANTEEN); showGameplay(); });
        itBuilding.setOnAction(e -> showITBuilding());
        goHome.setOnAction(e -> doGoHome());
        mainMenu.setOnAction(e -> showMainMenu());

        VBox actions = new VBox(12, header, new Separator(),
                busStop, canteen, itBuilding, goHome, mainMenu);
        actions.setAlignment(Pos.CENTER);
        actions.setStyle("-fx-padding: 20;");

        BorderPane root = new BorderPane();
        root.setCenter(actions);
        root.setRight(statsPanel());
        stage.setScene(new Scene(root, 850, 530));
        updateStats();
    }

    private void showBusStop() {
        updateStats();

        Label header = new Label("🚌 ป้ายรถเมล์");
        header.setStyle("-fx-font-size: 15; -fx-font-weight: bold;");

        Button kllc   = new Button("📚 ไป KLLC (เรียน)");
        Button goHome = new Button("🏠 Go Home");
        Button cancel = new Button("← กลับ");
        for (Button b : new Button[]{kllc, goHome, cancel}) b.setPrefWidth(210);

        kllc.setOnAction(e -> { perform(new KLLCActivity(), Location.BUS_STOP); showGameplay(); });
        goHome.setOnAction(e -> doGoHome());
        cancel.setOnAction(e -> showGameplay());

        VBox center = new VBox(15, header, kllc, goHome, cancel);
        center.setAlignment(Pos.CENTER);

        BorderPane root = new BorderPane();
        root.setCenter(center);
        root.setRight(statsPanel());
        stage.setScene(new Scene(root, 850, 530));
    }

    private void showITBuilding() {
        // Fire visit-triggered events on arriving at IT building
        eventManager.triggerVisit(player, timeSystem, Location.IT_BUILDING);
        updateStats();

        Label header = new Label("🏫 IT Building — " + getDayTypeLabel());
        header.setStyle("-fx-font-size: 15; -fx-font-weight: bold;");

        VBox center = new VBox(12);
        center.setAlignment(Pos.CENTER);
        center.setStyle("-fx-padding: 20;");
        center.getChildren().addAll(header, new Separator());

        if (isProgExamDay()) {
            Button examBtn    = new Button("💻 เข้าห้องสอบ Programming");
            Button auditorium = new Button("🎭 Auditorium");
            Button cowork     = new Button("💻 Coworking Space");
            for (Button b : new Button[]{examBtn, auditorium, cowork}) b.setPrefWidth(210);

            examBtn.setOnAction(e -> doProgExam());
            auditorium.setOnAction(e -> { perform(new AuditoriumActivity(), Location.AUDITORIUM); showITBuilding(); });
            cowork.setOnAction(e -> showCoworkingSpace());
            center.getChildren().addAll(examBtn, auditorium, cowork);

        } else if (isMathExamDay()) {
            Button examBtn    = new Button("📐 เข้าห้องสอบ Math (มินิเกม)");
            Button auditorium = new Button("🎭 Auditorium");
            Button cowork     = new Button("💻 Coworking Space");
            for (Button b : new Button[]{examBtn, auditorium, cowork}) b.setPrefWidth(220);

            examBtn.setOnAction(e -> doMathExam());
            auditorium.setOnAction(e -> { perform(new AuditoriumActivity(), Location.AUDITORIUM); showITBuilding(); });
            cowork.setOnAction(e -> showCoworkingSpace());
            center.getChildren().addAll(examBtn, auditorium, cowork);

        } else {
            Button classroom  = new Button("📖 Classroom");
            Button auditorium = new Button("🎭 Auditorium");
            Button cowork     = new Button("💻 Coworking Space");
            for (Button b : new Button[]{classroom, auditorium, cowork}) b.setPrefWidth(210);

            classroom.setOnAction(e -> { perform(new ClassroomActivity(), Location.CLASSROOM); showITBuilding(); });
            auditorium.setOnAction(e -> { perform(new AuditoriumActivity(), Location.AUDITORIUM); showITBuilding(); });
            cowork.setOnAction(e -> showCoworkingSpace());
            center.getChildren().addAll(classroom, auditorium, cowork);
        }

        Button back = new Button("← กลับ");
        back.setPrefWidth(210);
        back.setOnAction(e -> showGameplay());
        center.getChildren().addAll(new Separator(), back);

        BorderPane root = new BorderPane();
        root.setCenter(center);
        root.setRight(statsPanel());
        stage.setScene(new Scene(root, 850, 530));
    }

    private void showCoworkingSpace() {
        updateStats();

        Label header = new Label("💻 Coworking Space");
        header.setStyle("-fx-font-size: 15; -fx-font-weight: bold;");

        Button relax  = new Button("😴 Relax (+Energy, +Mood)");
        Button study  = new Button("📝 Study (+INT)");
        Button cancel = new Button("← กลับ");
        for (Button b : new Button[]{relax, study, cancel}) b.setPrefWidth(210);

        relax.setOnAction(e -> { perform(new CoworkingRelaxActivity(), Location.COWORKING); showITBuilding(); });
        study.setOnAction(e -> { perform(new CoworkingStudyActivity(), Location.COWORKING); showITBuilding(); });
        cancel.setOnAction(e -> showITBuilding());

        VBox center = new VBox(12, header, relax, study, cancel);
        center.setAlignment(Pos.CENTER);

        BorderPane root = new BorderPane();
        root.setCenter(center);
        root.setRight(statsPanel());
        stage.setScene(new Scene(root, 850, 530));
    }

    // ═════════════════════════════════════════════════════════════════════════
    // Exam execution
    // ═════════════════════════════════════════════════════════════════════════

    /**
     * Programming exam (day 6 & 13) — uses ExamActivity directly.
     * Stats cost applied, ForgetID event may fire, score captured at day-end.
     */
    private void doProgExam() {
        ExamActivity exam = new ExamActivity();
        RequirementReason reason = exam.canPerform(player, timeSystem);
        if (reason != null) {
            showPopup(exam.getFailMessage(reason));
            return;
        }

        // เปิด CaptchaMiniGame ก่อน แล้วค่อย perform หลังจบ
        CaptchaMiniGame[] gameRef = new CaptchaMiniGame[1];
        gameRef[0] = new CaptchaMiniGame(player, () -> {
            int score = gameRef[0].getTotalScore();

            // perform หลังเล่นจบ
            perform(exam, Location.CLASSROOM);

            int day = timeSystem.getCurrentDay();
            String round = (day <= 7) ? "รอบ 1" : "รอบ 2";
            showPopup(
                    "✅ สอบ Programming " + round + " เสร็จแล้ว!\n\n" +
                            "🎯 คะแนนมินิเกม: " + score + " / 50\n\n" +
                            "(คะแนนจะถูกบันทึกเมื่อคุณกลับบ้านสิ้นวันนี้)"
            );
            showITBuilding();
        });

        stage.setScene(new Scene(gameRef[0].getView(), 700, 600));
        stage.setFullScreenExitHint("");
        stage.setFullScreen(true);
    }

    /**
     * Math exam (day 7 & 14) — launches CountingMiniGame.
     * The minigame score is converted to INT bonus after completion.
     * ExamActivity stat costs (mood/energy) still apply.
     */
    private void doMathExam() {
        ExamActivity exam = new ExamActivity();
        RequirementReason reason = exam.canPerform(player, timeSystem);
        if (reason != null) {
            showPopup(exam.getFailMessage(reason));
            return;
        }

        // Apply exam stat costs and ForgetID event BEFORE minigame
        perform(exam, Location.CLASSROOM);

        CountingMiniGame[] gameRef = new CountingMiniGame[1];
        gameRef[0] = new CountingMiniGame(player, () -> {
            int score = gameRef[0].getTotalScore(); // 0-50

            int day = timeSystem.getCurrentDay();
            String round = (day <= 7) ? "รอบ 1" : "รอบ 2";
            showPopup(
                    "✅ สอบ Math " + round + " เสร็จแล้ว!\n\n" +
                            "🎮 คะแนนมินิเกม: " + score + " / 50\n" +
                            "(คะแนนจะถูกบันทึกเมื่อคุณกลับบ้านสิ้นวันนี้)"
            );
            updateStats();
            showITBuilding();
        });

        stage.setScene(new Scene(gameRef[0].getView(), 650, 520));
        stage.setFullScreenExitHint("");
        stage.setFullScreen(true);
    }

    // ═════════════════════════════════════════════════════════════════════════
    // Endings
    // ═════════════════════════════════════════════════════════════════════════

    private void showEnding() {
        if (!agreedToTerms) { showRefusalEnding(); return; }

        int progAvg    = (progExam1Score + progExam2Score) / 2;
        int mathAvg    = (mathExam1Score + mathExam2Score) / 2;
        int overallAvg = (progAvg + mathAvg) / 2;

        String grade, emoji, message;
        if (overallAvg >= 70) {
            grade = "A"; emoji = "🏆";
            message = "คุณทำได้ยอดเยี่ยม!\nการบริหารเวลาและการเรียนของคุณดีมาก\nGPA Saved! 🎉";
        } else if (overallAvg >= 40) {
            grade = "C"; emoji = "😅";
            message = "คุณผ่านไปได้... แต่หวุดหวิด\nครั้งหน้าต้องพยายามมากกว่านี้!";
        } else {
            grade = "F"; emoji = "💀";
            message = "GPA ไม่รอด...\nลองใหม่อีกครั้ง — คุณทำได้!";
        }

        Text title = new Text(emoji + " ผลการเรียน " + emoji);
        title.setStyle("-fx-font-size: 22; -fx-font-weight: bold;");

        Label scores = new Label(
                "📊 สรุปผลการสอบ\n\n" +
                        "💻 Programming:\n" +
                        "   รอบ 1: " + progExam1Score +
                        "  |  รอบ 2: " + progExam2Score +
                        "  →  เฉลี่ย: " + progAvg + "\n\n" +
                        "📐 Math:\n" +
                        "   รอบ 1: " + mathExam1Score +
                        "  |  รอบ 2: " + mathExam2Score +
                        "  →  เฉลี่ย: " + mathAvg + "\n\n" +
                        "📈 คะแนนรวมเฉลี่ย: " + overallAvg +
                        "  |  🎓 เกรด: " + grade + "\n\n" +
                        message
        );
        scores.setWrapText(true);
        scores.setTextAlignment(TextAlignment.CENTER);
        scores.setStyle("-fx-font-size: 13;");

        Button playAgain = new Button("🔄 เล่นใหม่");
        Button quit      = new Button("🚪 ออกจากเกม");
        playAgain.setPrefWidth(150);
        quit.setPrefWidth(150);

        playAgain.setOnAction(e -> startNewGame());
        quit.setOnAction(e -> stage.close());

        HBox buttons = new HBox(20, playAgain, quit);
        buttons.setAlignment(Pos.CENTER);

        VBox root = new VBox(20, title, scores, buttons);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-padding: 40;");
        stage.setScene(new Scene(root, 650, 520));
    }

    private void showRefusalEnding() {
        agreedToTerms = false;

        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: #000000;");

        VBox content = new VBox(18);
        content.setAlignment(Pos.CENTER);
        content.setStyle("-fx-padding: 30;");

        Text title = new Text("Secret Ending");
        title.setFill(Color.WHITE);
        title.setFont(cuteFont(36, FontWeight.BOLD));
        title.setOpacity(0);

        Label story = new Label();
        story.setTextFill(Color.WHITE);
        story.setFont(cuteFont(20));
        story.setWrapText(true);
        story.setMaxWidth(780);
        story.setAlignment(Pos.CENTER);
        story.setOpacity(0);

        Button back = new Button("← กลับไปหน้าเริ่มเกม");
        back.setOpacity(0);
        back.setOnAction(e -> showIntroSequence());
        // For refusal, we auto-exit after the typing sequence ends.
        back.setVisible(false);
        back.setManaged(false);

        content.getChildren().addAll(title, story, back);
        root.getChildren().add(content);

        stage.setScene(new Scene(root, 960, 540));

        String[] secretSegments = new String[]{
                "นักศึกษาหญิงคนนั้นเลือกที่จะไม่เดินต่อบนเส้นทาง GPA",
                "ในคืนที่ทุกอย่างเริ่มจะชัดเจน...",
                "เธอกลับตัดสินใจฟังหัวใจตัวเองมากกว่าเหตุผล",
                "บางครั้ง “การสอบติด” อาจไม่ใช่สิ่งที่ทำให้เราเติบโตที่สุด",
                "เธอจึงไปทำอย่างอื่นที่อาจจะดีสำหรับตัวเธอมากกว่า",
                "และพบความหมายของชีวิตในแบบที่แตกต่าง"
        };

        FadeTransition titleIn = new FadeTransition(Duration.seconds(0.8), title);
        titleIn.setFromValue(0);
        titleIn.setToValue(1);

        FadeTransition storyIn = new FadeTransition(Duration.seconds(0.8), story);
        storyIn.setFromValue(0);
        storyIn.setToValue(1);
        storyIn.setOnFinished(e -> typeTextSegments(
                story,
                secretSegments,
                Duration.millis(38),
                Duration.seconds(0.85),
                Duration.millis(260),
                Duration.millis(220),
                () -> {
                    stage.close();
                    javafx.application.Platform.exit();
                }
        ));

        new SequentialTransition(
                titleIn,
                new PauseTransition(Duration.seconds(0.4)),
                storyIn
        ).play();
    }

    // ═════════════════════════════════════════════════════════════════════════
    // Utility
    // ═════════════════════════════════════════════════════════════════════════

    private void showPopup(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}