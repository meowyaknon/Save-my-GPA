package com.savemygpa.ui;

import javafx.animation.*;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

public class ActivityCutscene {

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

    public static void play(StackPane root, String text, Runnable onDone) {
        StackPane overlay = new StackPane();
        overlay.setStyle("-fx-background-color: #000000;");
        overlay.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        overlay.setOpacity(0);

        Label typeLabel = new Label("");
        typeLabel.setFont(Font.font("Comic Sans MS", FontWeight.BOLD, 36));
        typeLabel.setTextFill(Color.WHITE);
        typeLabel.setWrapText(true);
        typeLabel.setMaxWidth(900);
        typeLabel.setStyle("-fx-effect: dropshadow(gaussian, rgba(100,200,255,0.4), 12, 0.4, 0, 0);");
        typeLabel.setOpacity(0);
        overlay.getChildren().add(typeLabel);
        root.getChildren().add(overlay);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(420), overlay);
        fadeIn.setFromValue(0); fadeIn.setToValue(1);
        fadeIn.setOnFinished(e -> {
            typeLabel.setOpacity(1);
            typeText(typeLabel, text, () -> {
                PauseTransition pause = new PauseTransition(Duration.millis(900));
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

    private static void typeText(Label label, String text, Runnable onDone) {
        label.setText("");
        Timeline tl = new Timeline();
        for (int i = 0; i < text.length(); i++) {
            final int idx = i + 1;
            tl.getKeyFrames().add(new KeyFrame(Duration.millis(48 * i),
                    e -> label.setText(text.substring(0, idx))));
        }
        tl.setOnFinished(e -> { if (onDone != null) onDone.run(); });
        tl.play();
    }
}