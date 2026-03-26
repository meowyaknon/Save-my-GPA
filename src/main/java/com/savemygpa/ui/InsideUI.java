package com.savemygpa.ui;

import com.savemygpa.core.TimeSystem;
import com.savemygpa.event.EventManager;
import com.savemygpa.player.Player;
import javafx.animation.FadeTransition;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.image.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.*;
import javafx.util.Duration;

/**
 * InsideUI — IT Building interior.
 * • Reuses OutsideUI's HUD (stats, clock, effects, character).
 * • ESC triggers pause — no visible cog button.
 * • Activity panel uses a transparent-black overlay style.
 */
public class InsideUI {

    private static final String IT_BG  = "/images/map/it_building_bg.jpg"; // optional
    private static final String IT_IMG = "/images/map/it_building.png";

    public interface Callbacks {
        void onClassroom();
        void onAuditorium();
        void onCoworking();
        void onProgExam();
        void onMathExam();
        void onBack();
        void onPause();
        boolean isProgExamDay();
        boolean isMathExamDay();
        Player       getPlayer();
        TimeSystem   getTimeSystem();
        EventManager getEventManager();
    }

    private final Callbacks cb;

    public InsideUI(Callbacks cb) {
        this.cb = cb;
    }

    public StackPane buildView() {
        StackPane root = new StackPane();

        // Background: image or gradient fallback
        var bgUrl = getClass().getResource(IT_BG);
        if (bgUrl != null) {
            ImageView bg = new ImageView(new Image(bgUrl.toExternalForm()));
            bg.setFitWidth(1920); bg.setFitHeight(1080);
            bg.setPreserveRatio(false); bg.setMouseTransparent(true);
            root.getChildren().add(bg);
        } else {
            root.setStyle("-fx-background-color: linear-gradient(to bottom, #0d1b2a 0%, #1b2a3b 60%, #16213e 100%);");
        }

        // HUD canvas
        Pane canvas = new Pane();
        canvas.setPrefSize(1920, 1080);
        canvas.setMouseTransparent(false);

        OutsideUI hud = new OutsideUI(cb.getPlayer(), cb.getTimeSystem(), cb.getEventManager(), null);
        hud.buildHudNodes();
        hud.addHudToCanvas(canvas);
        hud.refresh();

        root.getChildren().add(canvas);

        // Centre activity panel (fades in)
        VBox panel = buildPanel();
        panel.setOpacity(0);

        FadeTransition ft = new FadeTransition(Duration.millis(380), panel);
        ft.setToValue(1); ft.play();

        root.getChildren().add(panel);

        // ESC → pause
        root.setFocusTraversable(true);
        root.setOnKeyPressed(e -> { if (e.getCode() == KeyCode.ESCAPE) cb.onPause(); });
        root.requestFocus();

        return root;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Centre panel
    // ─────────────────────────────────────────────────────────────────────────

    private VBox buildPanel() {
        VBox panel = new VBox(14);

        FadeTransition ft = new FadeTransition(Duration.millis(380), panel);
        ft.setToValue(1); ft.play();

        panel.setAlignment(Pos.CENTER);
        panel.setMaxWidth(560);
        // Transparent-black panel — matches popup style in the rest of the game
        panel.setStyle("""
            -fx-background-color: rgba(0,0,0,0.62);
            -fx-background-radius: 24;
            -fx-padding: 32 44 32 44;
            -fx-effect: dropshadow(gaussian,rgba(0,0,0,0.75),32,0.55,0,6);
            -fx-border-color: rgba(255,255,255,0.07);
            -fx-border-radius: 24;
            -fx-border-width: 1;
        """);

        // Optional IT Building illustration
        var itUrl = getClass().getResource(IT_IMG);
        if (itUrl != null) {
            ImageView itImg = new ImageView(new Image(itUrl.toExternalForm()));
            itImg.setFitWidth(200); itImg.setPreserveRatio(true);
            panel.getChildren().add(itImg);
        }

        String dayTag = cb.isProgExamDay() ? "⚠️  วันสอบ Programming"
                : cb.isMathExamDay() ? "⚠️  วันสอบ Math"
                : "IT Building";
        Text header = new Text("🏫  " + dayTag);
        header.setFont(Font.font("Comic Sans MS", FontWeight.BOLD, 24));
        header.setFill(Color.web("#ffe082"));
        panel.getChildren().addAll(header, divider());

        if (cb.isProgExamDay()) {
            panel.getChildren().addAll(
                    btn("💻  เข้าห้องสอบ Programming", Style.DANGER,  cb::onProgExam),
                    btn("🎭  Auditorium",              Style.PURPLE,  cb::onAuditorium),
                    btn("💻  Coworking Space",         Style.TEAL,    cb::onCoworking)
            );
        } else if (cb.isMathExamDay()) {
            panel.getChildren().addAll(
                    btn("📐  เข้าห้องสอบ Math (มินิเกม)", Style.DANGER,  cb::onMathExam),
                    btn("🎭  Auditorium",                  Style.PURPLE,  cb::onAuditorium),
                    btn("💻  Coworking Space",             Style.TEAL,    cb::onCoworking)
            );
        } else {
            panel.getChildren().addAll(
                    btn("📖  Classroom",       Style.BLUE,   cb::onClassroom),
                    btn("🎭  Auditorium",      Style.PURPLE, cb::onAuditorium),
                    btn("💻  Coworking Space", Style.TEAL,   cb::onCoworking)
            );
        }

        panel.getChildren().addAll(divider(), btn("←  กลับ", Style.BACK, cb::onBack));
        return panel;
    }

    private enum Style {
        BLUE  ("#4fc3f7","#051020"),
        PURPLE("#ce93d8","#180a28"),
        TEAL  ("#80cbc4","#041a18"),
        DANGER("#ef9a9a","#280606"),
        BACK  ("rgba(255,255,255,0.12)","#ccccdd");

        final String bg, fg;
        Style(String bg, String fg){ this.bg=bg; this.fg=fg; }
    }

    private Button btn(String text, Style s, Runnable onClick) {
        Button b = new Button(text);
        b.setStyle("""
            -fx-font-family: 'Comic Sans MS';
            -fx-font-size: 17px;
            -fx-background-color: %s;
            -fx-text-fill: %s;
            -fx-font-weight: bold;
            -fx-background-radius: 14;
            -fx-padding: 11 36 11 36;
            -fx-cursor: hand;
            -fx-min-width: 320;
            -fx-effect: dropshadow(gaussian,rgba(0,0,0,0.40),8,0.3,0,2);
        """.formatted(s.bg, s.fg));
        b.setOnMouseEntered(e -> b.setOpacity(0.82));
        b.setOnMouseExited(e  -> b.setOpacity(1.00));
        b.setOnMousePressed(e  -> { b.setScaleX(0.97); b.setScaleY(0.97); });
        b.setOnMouseReleased(e -> { b.setScaleX(1.0);  b.setScaleY(1.0);  });
        b.setOnAction(e -> onClick.run());
        return b;
    }

    private Line divider() {
        Line l = new Line(0, 0, 460, 0);
        l.setStroke(Color.web("rgba(255,255,255,0.12)"));
        l.setStrokeWidth(1);
        return l;
    }
}