package com.savemygpa.ui;

import com.savemygpa.audio.AudioManager;
import javafx.animation.FadeTransition;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.util.Duration;

/**
 * SettingsUI — standalone full-page settings screen.
 * Opened like HowToPlay / Credits (screen swap, not popup).
 * Controls: Game Volume, SFX Volume, Music Volume.
 */
public class SettingsUI {

    public interface Callbacks {
        /** Called when player presses Back. */
        void onBack();
    }

    private final Callbacks    cb;
    private final AudioManager audio;

    public SettingsUI(AudioManager audio, Callbacks cb) {
        this.audio = audio;
        this.cb    = cb;
    }

    public StackPane buildView() {
        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #0a0a1e, #1a1a3e);");

        VBox content = new VBox(28);
        content.setAlignment(Pos.CENTER);
        content.setStyle("-fx-padding: 60;");

        Text title = new Text("⚙  ตั้งค่า");
        title.setFont(Font.font("Comic Sans MS", FontWeight.BOLD, 40));
        title.setFill(Color.web("#80cbc4"));
        title.setStyle("-fx-effect: dropshadow(gaussian,rgba(128,203,196,0.5),14,0.4,0,0);");

        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: rgba(128,203,196,0.25);");

        VBox sliders = new VBox(22,
                volRow("🔊  Game Volume  (Master)", audio.getGameVolume(),  v -> audio.setGameVolume(v)),
                volRow("🎵  Sound Volume  (SFX)",   audio.getSfxVolume(),   v -> audio.setSfxVolume(v)),
                volRow("🎶  Music Volume",           audio.getMusicVolume(), v -> audio.setMusicVolume(v))
        );
        sliders.setAlignment(Pos.CENTER);
        sliders.setMaxWidth(600);

        Button back = makeBtn("← กลับ", "#4fc3f7", "#0a0a1e");
        back.setOnAction(e -> cb.onBack());

        content.getChildren().addAll(title, sep, sliders, back);

        // Fade-in entrance (same as main menu)
        content.setOpacity(0);
        FadeTransition ft = new FadeTransition(Duration.seconds(0.5), content);
        ft.setToValue(1); ft.play();

        root.getChildren().add(content);
        return root;
    }

    // ── One slider row ─────────────────────────────────────────────────────────
    private static HBox volRow(String name, double initial,
                               java.util.function.DoubleConsumer onChange) {
        Label lbl = new Label(name);
        lbl.setMinWidth(260);
        lbl.setStyle("""
            -fx-font-family: 'Comic Sans MS';
            -fx-font-size: 18px;
            -fx-text-fill: #e0e0e0;
        """);

        Slider s = new Slider(0, 1, initial);
        s.setPrefWidth(240);
        s.setStyle("-fx-accent: #80cbc4;");

        Label pct = new Label(pctStr(initial));
        pct.setMinWidth(50);
        pct.setStyle("""
            -fx-font-family: 'Comic Sans MS';
            -fx-font-size: 16px;
            -fx-text-fill: #aaaaaa;
        """);

        s.valueProperty().addListener((obs, o, n) -> {
            onChange.accept(n.doubleValue());
            pct.setText(pctStr(n.doubleValue()));
        });

        HBox row = new HBox(18, lbl, s, pct);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private static String pctStr(double v) { return (int) Math.round(v * 100) + "%"; }

    private static Button makeBtn(String text, String bg, String fg) {
        Button btn = new Button(text);
        btn.setStyle("""
            -fx-font-family: 'Comic Sans MS';
            -fx-font-size: 17px;
            -fx-background-color: %s;
            -fx-text-fill: %s;
            -fx-font-weight: bold;
            -fx-background-radius: 12;
            -fx-padding: 10 36 10 36;
            -fx-cursor: hand;
            -fx-min-width: 200;
        """.formatted(bg, fg));
        btn.setOnMouseEntered(e -> btn.setOpacity(0.82));
        btn.setOnMouseExited(e  -> btn.setOpacity(1.00));
        return btn;
    }
}