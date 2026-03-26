package com.savemygpa.ui;

import javafx.animation.*;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.util.Duration;

/**
 * PauseMenuUI — full-screen pause overlay.
 * Background is a transparent-black blur-style overlay (no solid colour).
 * Triggered by ESC only — no visible button.
 */
public class PauseMenuUI {

    public interface Callbacks {
        void onResume();
        void onSettings();
        void onMainMenu();
    }

    private final Callbacks cb;

    public PauseMenuUI(Callbacks cb) {
        this.cb = cb;
    }

    public StackPane buildView() {
        StackPane root = new StackPane();
        root.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        // Transparent-black overlay — game is still visible behind it
        root.setStyle("-fx-background-color: rgba(0,0,0,0.58);");

        VBox box = new VBox(22);
        box.setAlignment(Pos.CENTER);
        box.setMaxWidth(420);
        box.setStyle("""
            -fx-background-color: rgba(6,10,28,0.88);
            -fx-background-radius: 22;
            -fx-padding: 42 52 42 52;
            -fx-effect: dropshadow(gaussian,rgba(0,0,0,0.85),36,0.65,0,6);
            -fx-border-color: rgba(255,255,255,0.08);
            -fx-border-radius: 22;
            -fx-border-width: 1;
        """);

        Text title = new Text("⏸  หยุดชั่วคราว");
        title.setFont(Font.font("Comic Sans MS", FontWeight.BOLD, 28));
        title.setFill(Color.web("#f0e68c"));

        Text hint = new Text("กด ESC เพื่อกลับไปเล่น");
        hint.setFont(Font.font("Comic Sans MS", 14));
        hint.setFill(Color.web("#888899"));

        Button resumeBtn   = makeBtn("▶  เล่นต่อ",        "#4fc3f7", "#051020");
        Button settingsBtn = makeBtn("⚙  ตั้งค่า",         "#80cbc4", "#041a18");
        Button menuBtn     = makeBtn("🏠  กลับเมนูหลัก",  "#ef9a9a", "#280606");

        resumeBtn  .setOnAction(e -> cb.onResume());
        settingsBtn.setOnAction(e -> cb.onSettings());
        menuBtn    .setOnAction(e -> cb.onMainMenu());

        // ESC inside the pause overlay also resumes
        root.setFocusTraversable(true);
        root.setOnKeyPressed(e -> {
            if (e.getCode() == javafx.scene.input.KeyCode.ESCAPE) cb.onResume();
        });

        box.getChildren().addAll(title, hint, resumeBtn, settingsBtn, menuBtn);
        root.getChildren().add(box);

        // Entrance animation
        box.setScaleX(0.88); box.setScaleY(0.88); box.setOpacity(0);
        ScaleTransition st = new ScaleTransition(Duration.millis(180), box);
        st.setToX(1); st.setToY(1);
        FadeTransition ft = new FadeTransition(Duration.millis(180), box);
        ft.setToValue(1);
        st.play(); ft.play();

        // Request focus so ESC is captured immediately
        root.requestFocus();

        return root;
    }

    private Button makeBtn(String text, String bg, String fg) {
        Button btn = new Button(text);
        btn.setStyle("""
            -fx-font-family: 'Comic Sans MS';
            -fx-font-size: 18px;
            -fx-background-color: %s;
            -fx-text-fill: %s;
            -fx-font-weight: bold;
            -fx-background-radius: 14;
            -fx-padding: 11 40 11 40;
            -fx-cursor: hand;
            -fx-min-width: 260;
            -fx-effect: dropshadow(gaussian,rgba(0,0,0,0.40),8,0.3,0,2);
        """.formatted(bg, fg));
        btn.setOnMouseEntered(e -> btn.setOpacity(0.82));
        btn.setOnMouseExited(e  -> btn.setOpacity(1.00));
        btn.setOnMousePressed(e  -> { btn.setScaleX(0.96); btn.setScaleY(0.96); });
        btn.setOnMouseReleased(e -> { btn.setScaleX(1.0);  btn.setScaleY(1.0);  });
        return btn;
    }
}