package com.savemygpa.ui;

import com.savemygpa.audio.AudioManager;
import javafx.animation.*;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

/**
 * ActivityCutscene — black-overlay typewriter cutscene for activity actions.
 *
 * TYPING SPEED CHANGE:
 *   Location: typeText() → Duration.millis(TYPING_MS * i)
 *   Was: 70 ms per character
 *   Now: 110 ms per character  (TYPING_MS constant below)
 *
 * This affects every activity cutscene popup (classroom, auditorium,
 * coworking, eat, KLLC, exam, go-home, and the generic "กำลังดำเนินการ").
 * GameLauncher story/ending cutscenes use their own TYPING_MS constant (80ms).
 */
public class ActivityCutscene {

    // CHANGE: slowed from 70 ms to 110 ms per character
    // LINE TO CHANGE: Duration.millis(TYPING_MS * i)  inside typeText()
    private static final int TYPING_MS = 110;

    public static String lineFor(String activityKey) {
        return switch (activityKey) {
            case "ClassroomActivity"       -> "กำลังเรียนวิชา OOP อยู่...";
            case "AuditoriumActivity"      -> "นั่งฟังดนตรีในหอประชุม...";
            case "CoworkingRelaxActivity"  -> "พักผ่อนที่ Coworking Space...";
            case "CoworkingStudyActivity"  -> "กำลังทบทวนเนื้อหา...";
            case "EatActivity"             -> "กำลังกินข้าวที่โรงอาหาร...";
            case "KLLCActivity"            -> "กำลังเรียนที่ KLLC...";
            case "ExamActivity"            -> "กำลังเข้าห้องสอบ...";
            case "GoHomeActivity"          -> "กำลังกลับบ้านไปนอน เพื่อเริ่มต้นวันใหม่...";
            default                        -> "กำลังดำเนินการ...";
        };
    }

    // ── Full cutscene (black overlay + typewriter + audio) ───────────────────

    public static void play(StackPane root, String text, Runnable onDone) {
        AudioManager.getInstance().playMusic(AudioManager.Music.CUTSCENE);

        StackPane overlay = new StackPane();
        overlay.setStyle("-fx-background-color: #000000;");
        overlay.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        overlay.setOpacity(0);

        // Centred label — wider max-width so long Thai sentences fit on one line
        Label typeLabel = new Label("");
        typeLabel.setFont(Font.font("Comic Sans MS", FontWeight.BOLD, 40));
        typeLabel.setTextFill(Color.WHITE);
        typeLabel.setWrapText(true);
        typeLabel.setMaxWidth(1300);
        typeLabel.setAlignment(Pos.CENTER);
        typeLabel.setStyle("""
            -fx-text-alignment: center;
            -fx-effect: dropshadow(gaussian, rgba(100,200,255,0.4), 14, 0.4, 0, 0);
        """);
        typeLabel.setOpacity(0);
        overlay.getChildren().add(typeLabel);
        root.getChildren().add(overlay);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(420), overlay);
        fadeIn.setFromValue(0); fadeIn.setToValue(1);
        fadeIn.setOnFinished(e -> {
            typeLabel.setOpacity(1);
            typeText(typeLabel, text, () -> {
                PauseTransition pause = new PauseTransition(Duration.millis(1600));
                pause.setOnFinished(pe -> {
                    FadeTransition lblOut = new FadeTransition(Duration.millis(300), typeLabel);
                    lblOut.setToValue(0);
                    lblOut.setOnFinished(le -> {
                        FadeTransition fadeOut = new FadeTransition(Duration.millis(420), overlay);
                        fadeOut.setFromValue(1); fadeOut.setToValue(0);
                        fadeOut.setOnFinished(fe -> {
                            root.getChildren().remove(overlay);
                            if (onDone != null) onDone.run();
                        });
                        fadeOut.play();
                    });
                    lblOut.play();
                });
                pause.play();
            });
        });
        fadeIn.play();
    }

    // ── Silent transition (screen swap only, no audio change) ────────────────

    public static void transition(StackPane root, Runnable onSwap, Runnable onDone) {
        StackPane overlay = new StackPane();
        overlay.setStyle("-fx-background-color: #000000;");
        overlay.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        overlay.setOpacity(0);
        root.getChildren().add(overlay);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), overlay);
        fadeIn.setFromValue(0); fadeIn.setToValue(1);
        fadeIn.setOnFinished(e -> {
            if (onSwap != null) onSwap.run();
            FadeTransition fadeOut = new FadeTransition(Duration.millis(300), overlay);
            fadeOut.setFromValue(1); fadeOut.setToValue(0);
            fadeOut.setOnFinished(fe -> {
                root.getChildren().remove(overlay);
                if (onDone != null) onDone.run();
            });
            fadeOut.play();
        });
        fadeIn.play();
    }

    // ── Typewriter with per-character typing SFX ──────────────────────────────

    /**
     * TYPING SPEED CHANGE — this is the method to edit.
     * Duration.millis(TYPING_MS * i) controls speed.
     * TYPING_MS = 110 ms/char (was 70).
     */
    private static void typeText(Label label, String text, Runnable onDone) {
        label.setText("");
        Timeline tl = new Timeline();
        AudioManager audio = AudioManager.getInstance();

        for (int i = 0; i < text.length(); i++) {
            final int idx = i + 1;
            final char ch = text.charAt(i);
            // CHANGE: 110 ms per character (was 70)
            tl.getKeyFrames().add(new KeyFrame(Duration.millis(TYPING_MS * i), e -> {
                label.setText(text.substring(0, idx));
                if (ch != ' ' && ch != '\n') {
                    audio.playTyping();
                }
            }));
        }
        tl.setOnFinished(e -> { if (onDone != null) onDone.run(); });
        tl.play();
    }
}