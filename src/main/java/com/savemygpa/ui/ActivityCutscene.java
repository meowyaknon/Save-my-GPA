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

public class ActivityCutscene {

    private static final int TYPING_MS = 80;

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

    // ── Full cutscene ───────────────────

    public static void play(StackPane sceneRoot, String text, Runnable onDone) {
        AudioManager.getInstance().playMusic(AudioManager.Music.CUTSCENE);

        StackPane overlay = buildOverlay(sceneRoot);

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
        sceneRoot.getChildren().add(overlay);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(420), overlay);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.setOnFinished(e -> {
            typeLabel.setOpacity(1);
            typeText(typeLabel, text, () -> {
                PauseTransition pause = new PauseTransition(Duration.millis(1600));
                pause.setOnFinished(pe -> {
                    FadeTransition lblOut = new FadeTransition(Duration.millis(300), typeLabel);
                    lblOut.setToValue(0);
                    lblOut.setOnFinished(le -> {
                        FadeTransition fadeOut = new FadeTransition(Duration.millis(420), overlay);
                        fadeOut.setFromValue(1);
                        fadeOut.setToValue(0);
                        fadeOut.setOnFinished(fe -> {
                            dismissOverlay(sceneRoot, overlay);
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

    public static void transition(StackPane sceneRoot, Runnable onSwap, Runnable onDone) {
        StackPane overlay = buildOverlay(sceneRoot);
        sceneRoot.getChildren().add(overlay);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), overlay);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.setOnFinished(e -> {
            if (onSwap != null) onSwap.run();
            FadeTransition fadeOut = new FadeTransition(Duration.millis(300), overlay);
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);
            fadeOut.setOnFinished(fe -> {
                dismissOverlay(sceneRoot, overlay);
                if (onDone != null) onDone.run();
            });
            fadeOut.play();
        });
        fadeIn.play();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static StackPane buildOverlay(StackPane sceneRoot) {
        StackPane overlay = new StackPane();
        overlay.setStyle("-fx-background-color: #000000;");
        overlay.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        overlay.prefWidthProperty().bind(sceneRoot.widthProperty());
        overlay.prefHeightProperty().bind(sceneRoot.heightProperty());
        overlay.setOpacity(0);
        return overlay;
    }

    private static void dismissOverlay(StackPane sceneRoot, StackPane overlay) {
        overlay.prefWidthProperty().unbind();
        overlay.prefHeightProperty().unbind();
        sceneRoot.getChildren().remove(overlay);
    }

    // ── Typewriter ────────────────────────────────────────────────────────────

    private static void typeText(Label label, String text, Runnable onDone) {
        label.setText("");
        Timeline tl = new Timeline();
        AudioManager audio = AudioManager.getInstance();

        for (int i = 0; i < text.length(); i++) {
            final int idx = i + 1;
            final char ch = text.charAt(i);
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