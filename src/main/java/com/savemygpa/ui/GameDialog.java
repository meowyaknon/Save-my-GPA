package com.savemygpa.ui;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.geometry.Pos;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.paint.CycleMethod;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.*;
import javafx.util.Duration;

public class GameDialog {

    // ── Accent colours per dialog type ───────────────────────────────────────
    private static final String ACCENT_INFO    = "#4fc3f7"; // blue
    private static final String ACCENT_EVENT   = "#f0e68c"; // gold
    private static final String ACCENT_CONFIRM = "#ef9a9a"; // red
    private static final String ACCENT_SETTINGS= "#80cbc4"; // teal

    // ── Show a simple info dialog ─────────────────────────────────────────────
    public static void show(StackPane root, String title, String message, Runnable onClose) {
        StackPane overlay = overlay();
        HBox box = buildBox(ACCENT_INFO);

        VBox text = textCol(title, message);
        Button ok = btn("ตกลง  ✔", ACCENT_INFO);
        ok.setOnAction(e -> dismiss(root, overlay, onClose));

        overlay.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER || e.getCode() == KeyCode.SPACE)
                dismiss(root, overlay, onClose);
        });

        box.getChildren().addAll(text, spacer(), ok);
        HBox.setHgrow(text, Priority.ALWAYS);
        finish(root, overlay, box);
    }

    // ── Yes / No confirm ──────────────────────────────────────────────────────
    public static void confirm(StackPane root, String title, String message,
                               Runnable onYes, Runnable onNo) {
        StackPane overlay = overlay();
        HBox box = buildBox(ACCENT_CONFIRM);

        VBox text = textCol(title, message);
        Button yes = btn("✅  ใช่",   ACCENT_INFO);
        Button no  = btn("❌  ไม่",   ACCENT_CONFIRM);
        yes.setOnAction(e -> dismiss(root, overlay, onYes));
        no .setOnAction(e -> dismiss(root, overlay, onNo));

        VBox btns = new VBox(10, yes, no);
        btns.setAlignment(Pos.CENTER);

        box.getChildren().addAll(text, spacer(), btns);
        HBox.setHgrow(text, Priority.ALWAYS);
        finish(root, overlay, box);
    }

    // ── Event notification ────────────────────────────────────────────────────
    public static void event(StackPane root, String eventName, String description, Runnable onClose) {
        StackPane overlay = overlay();
        HBox box = buildBox(ACCENT_EVENT);

        Label badge = new Label("🎲 Event!");
        badge.setStyle("""
            -fx-font-family: 'Comic Sans MS';
            -fx-font-size: 13px;
            -fx-text-fill: #f0e68c;
            -fx-background-color: rgba(200,160,0,0.28);
            -fx-background-radius: 8;
            -fx-padding: 2 8 2 8;
        """);

        VBox text = new VBox(6, badge, titleLabel(eventName, ACCENT_EVENT), bodyLabel(description));
        Button ok = btn("รับทราบ  ✔", ACCENT_EVENT);
        ok.setOnAction(e -> dismiss(root, overlay, onClose));

        box.getChildren().addAll(text, spacer(), ok);
        HBox.setHgrow(text, Priority.ALWAYS);
        finish(root, overlay, box);
    }

    // ── Settings panel ────────────────────────────────────────────────────────
    public static StackPane[] openPanel(StackPane root, String title, String accent) {
        StackPane overlay = overlay();
        HBox box = buildBox(accent);

        Label t = titleLabel(title, accent);
        VBox inner = new VBox(14, t);
        inner.setAlignment(Pos.TOP_LEFT);
        inner.setPrefWidth(680);
        HBox.setHgrow(inner, Priority.ALWAYS);

        box.getChildren().add(inner);
        overlay.getChildren().add(box);
        root.getChildren().add(overlay);
        overlay.setFocusTraversable(true);
        overlay.requestFocus();
        animateIn(box);

        return new StackPane[]{ overlay };
    }

    // helper for SettingsUI / BusStopUI / CoworkingUI to call dismiss
    public static void dismissOverlay(StackPane root, StackPane overlay, Runnable onClose) {
        dismiss(root, overlay, onClose);
    }

    // ═════════════════════════════════════════════════════════════════════════
    // Shared builders
    // ═════════════════════════════════════════════════════════════════════════

    /** Wide horizontal box with left accent bar. */
    static HBox buildBox(String accentColor) {
        // Accent bar on the left edge
        Rectangle accentBar = new Rectangle(6, 120);
        accentBar.setFill(Color.web(accentColor));
        accentBar.setArcWidth(6);
        accentBar.setArcHeight(6);

        HBox box = new HBox(20);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setMaxWidth(820);
        box.setMinHeight(100);
        box.setStyle("""
            -fx-background-color: linear-gradient(to right, rgba(12,12,32,0.98), rgba(22,28,52,0.96));
            -fx-background-radius: 16;
            -fx-padding: 24 28 24 0;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.80), 28, 0.65, 0, 6);
            -fx-border-color: rgba(255,255,255,0.07);
            -fx-border-radius: 16;
            -fx-border-width: 1;
        """);
        box.getChildren().add(accentBar);
        return box;
    }

    static VBox textCol(String title, String message) {
        VBox v = new VBox(8, titleLabel(title, ACCENT_INFO), bodyLabel(message));
        v.setAlignment(Pos.CENTER_LEFT);
        return v;
    }

    static Label titleLabel(String text, String color) {
        Label l = new Label(text);
        l.setWrapText(true);
        l.setMaxWidth(480);
        l.setStyle("""
            -fx-font-family: 'Comic Sans MS';
            -fx-font-size: 20px;
            -fx-font-weight: bold;
            -fx-text-fill: %s;
        """.formatted(color));
        return l;
    }

    static Label bodyLabel(String text) {
        Label l = new Label(text);
        l.setWrapText(true);
        l.setMaxWidth(480);
        l.setStyle("""
            -fx-font-family: 'Comic Sans MS';
            -fx-font-size: 14px;
            -fx-text-fill: #d0d0d0;
            -fx-line-spacing: 3;
        """);
        return l;
    }

    static Button btn(String text, String accentColor) {
        Button b = new Button(text);
        b.setStyle("""
            -fx-font-family: 'Comic Sans MS';
            -fx-font-size: 14px;
            -fx-background-color: %s;
            -fx-text-fill: #0a0a1e;
            -fx-font-weight: bold;
            -fx-background-radius: 10;
            -fx-padding: 8 20 8 20;
            -fx-cursor: hand;
        """.formatted(accentColor));
        b.setOnMouseEntered(e -> b.setOpacity(0.80));
        b.setOnMouseExited(e  -> b.setOpacity(1.00));
        return b;
    }

    private static Region spacer() {
        Region r = new Region();
        HBox.setHgrow(r, Priority.SOMETIMES);
        r.setMinWidth(16);
        return r;
    }

    // ── Animation / lifecycle ─────────────────────────────────────────────────

    static StackPane overlay() {
        StackPane p = new StackPane();
        p.setStyle("-fx-background-color: rgba(0,0,0,0.55);");
        p.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        return p;
    }

    static void finish(StackPane root, StackPane overlay, HBox box) {
        overlay.getChildren().add(box);
        root.getChildren().add(overlay);
        overlay.setFocusTraversable(true);
        overlay.requestFocus();
        animateIn(box);
    }

    static void animateIn(javafx.scene.Node box) {
        box.setScaleX(0.90); box.setScaleY(0.90); box.setOpacity(0);
        ScaleTransition st = new ScaleTransition(Duration.millis(220), box);
        st.setToX(1); st.setToY(1);
        FadeTransition ft = new FadeTransition(Duration.millis(220), box);
        ft.setToValue(1);
        st.play(); ft.play();
    }

    static void dismiss(StackPane root, StackPane overlay, Runnable cb) {
        FadeTransition ft = new FadeTransition(Duration.millis(150), overlay);
        ft.setToValue(0);
        ft.setOnFinished(e -> {
            root.getChildren().remove(overlay);
            if (cb != null) cb.run();
        });
        ft.play();
    }
}